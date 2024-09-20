"""
Implements support for BLF (Binary Logging Format) which is a proprietary
CAN log format from Vector Informatik GmbH (Germany).

No official specification of the binary logging format is available.
This implementation is based on Toby Lorenz' C++ library "Vector BLF" which is
licensed under GPLv3. https://bitbucket.org/tobylorenz/vector_blf.

The file starts with a header. The rest is one or more "log containers"
which consists of a header and some zlib compressed data, usually up to 128 kB
of uncompressed data each. This data contains the actual CAN messages and other
objects types.
"""

import struct
import zlib
import datetime
import time
import logging
from typing import List, BinaryIO, Generator, Union
from can.message import Message
from can.util import len2dlc, channel2int
from can.typechecking import StringPathLike
from can.io.generic import FileIOMessageWriter, BaseIOHandler


class BLFParseError(Exception):
    """BLF file could not be parsed correctly."""


LOG = logging.getLogger(__name__)

# signature ("LOGG"), header size,
# application ID, application major, application minor, application build,
# bin log major, bin log minor, bin log build, bin log patch,
# file size, uncompressed size, count of objects, count of objects read,
# time start (SYSTEMTIME), time stop (SYSTEMTIME)
FILE_HEADER_STRUCT = struct.Struct("<4sLBBBBBBBBQQLL8H8H")

# Pad file header to this size
FILE_HEADER_SIZE = 144

# signature ("LOBJ"), header size, header version, object size, object type
OBJ_HEADER_BASE_STRUCT = struct.Struct("<4sHHLL")

# flags, client index, object version, timestamp
OBJ_HEADER_V1_STRUCT = struct.Struct("<LHHQ")

# flags, timestamp status, object version, timestamp, (original timestamp)
OBJ_HEADER_V2_STRUCT = struct.Struct("<LBxHQ8x")

# compression method, size uncompressed
LOG_CONTAINER_STRUCT = struct.Struct("<H6xL4x")

# channel, flags, dlc, arbitration id, data
CAN_MSG_STRUCT = struct.Struct("<HBBL8s")

# channel, flags, dlc, arbitration id, frame length, bit count, FD flags,
# valid data bytes, data
CAN_FD_MSG_STRUCT = struct.Struct("<HBBLLBBB5x64s")

# channel, dlc, valid payload length of data, tx count, arbitration id,
# frame length, flags, bit rate used in arbitration phase,
# bit rate used in data phase, time offset of brs field,
# time offset of crc delimiter field, bit count, direction,
# offset if extDataOffset is used, crc
CAN_FD_MSG_64_STRUCT = struct.Struct("<BBBBLLLLLLLHBBL")

# channel, dlc, valid payload length of data, ECC, Flags,
# ErrorCodeExt,ExtFlags,ExtDataOffset,reserved1,arbitration id,
# frame length, BtrCfgArb, BtrCfgData,TimeOffsetBrsNs,TimeOffsetCRCDelNs,
# CRC,ErrorPosition,Reserved2
CAN_FD_MSG_64_ERROR_STRUCT = struct.Struct("<BBBBHHHBBLLLLLLLHH")

# channel, length, flags, ecc, position, dlc, frame length, id, flags ext, data
CAN_ERROR_EXT_STRUCT = struct.Struct("<HHLBBBxLLH2x8s")

# commented event type, foreground color, background color, relocatable,
# group name length, marker name length, description length
GLOBAL_MARKER_STRUCT = struct.Struct("<LLL3xBLLL12x")


CAN_MESSAGE = 1
LOG_CONTAINER = 10
CAN_ERROR_EXT = 73
CAN_MESSAGE2 = 86
GLOBAL_MARKER = 96
CAN_FD_MESSAGE = 100
CAN_FD_MESSAGE_64 = 101
CAN_FD_MESSAGE_ERROR_64 = 104

NO_COMPRESSION = 0
ZLIB_DEFLATE = 2

CAN_MSG_EXT = 0x80000000
REMOTE_FLAG = 0x80
EDL = 0x1
BRS = 0x2
ESI = 0x4
DIR = 0x1

TIME_TEN_MICS = 0x00000001
TIME_ONE_NANS = 0x00000002


def timestamp_to_systemtime(timestamp):
    if timestamp is None or timestamp < 631152000:
        # Probably not a Unix timestamp
        return (0, 0, 0, 0, 0, 0, 0, 0)
    t = datetime.datetime.fromtimestamp(timestamp)
    return (
        t.year,
        t.month,
        t.isoweekday() % 7,
        t.day,
        t.hour,
        t.minute,
        t.second,
        int(round(t.microsecond / 1000.0)),
    )


def systemtime_to_timestamp(systemtime):
    try:
        t = datetime.datetime(
            systemtime[0],
            systemtime[1],
            systemtime[3],
            systemtime[4],
            systemtime[5],
            systemtime[6],
            systemtime[7] * 1000,
        )
        return time.mktime(t.timetuple()) + systemtime[7] / 1000.0
    except ValueError:
        return 0


class BLFReader(BaseIOHandler):
    """
    Iterator of CAN messages from a Binary Logging File.

    Only CAN messages and error frames are supported. Other object types are
    silently ignored.
    """

    file: BinaryIO

    def __init__(self, file: Union[StringPathLike, BinaryIO]) -> None:
        """
        :param file: a path-like object or as file-like object to read from
                     If this is a file-like object, is has to opened in binary
                     read mode, not text read mode.
        """
        super().__init__(file, mode="rb")
        data = self.file.read(FILE_HEADER_STRUCT.size)
        header = FILE_HEADER_STRUCT.unpack(data)
        if header[0] != b"LOGG":
            raise BLFParseError("Unexpected file format")
        self.file_size = header[10]
        self.uncompressed_size = header[11]
        self.object_count = header[12]
        self.start_timestamp = systemtime_to_timestamp(header[14:22])
        self.stop_timestamp = systemtime_to_timestamp(header[22:30])
        # Read rest of header
        self.file.read(header[1] - FILE_HEADER_STRUCT.size)
        self._tail = b""
        self._pos = 0

    def __iter__(self) -> Generator[Message, None, None]:
        while True:
            data = self.file.read(OBJ_HEADER_BASE_STRUCT.size)
            if not data:
                # EOF
                break

            signature, _, _, obj_size, obj_type = OBJ_HEADER_BASE_STRUCT.unpack(data)
            if signature != b"LOBJ":
                raise BLFParseError()
            obj_data = self.file.read(obj_size - OBJ_HEADER_BASE_STRUCT.size)
            # Read padding bytes
            self.file.read(obj_size % 4)

            if obj_type == LOG_CONTAINER:
                method, uncompressed_size = LOG_CONTAINER_STRUCT.unpack_from(obj_data)
                container_data = obj_data[LOG_CONTAINER_STRUCT.size :]
                if method == NO_COMPRESSION:
                    data = container_data
                elif method == ZLIB_DEFLATE:
                    data = zlib.decompress(container_data, 15, uncompressed_size)
                else:
                    # Unknown compression method
                    LOG.warning("Unknown compression method (%d)", method)
                    continue
                yield from self._parse_container(data)
        self.stop()

    def _parse_container(self, data):
        if self._tail:
            data = b"".join((self._tail, data))
        try:
            yield from self._parse_data(data)
        except struct.error:
            # There was not enough data in the container to unpack a struct
            pass
        # Save the remaining data that could not be processed
        self._tail = data[self._pos :]

    def _parse_data(self, data):
        """Optimized inner loop by making local copies of global variables
        and class members and hardcoding some values."""
        unpack_obj_header_base = OBJ_HEADER_BASE_STRUCT.unpack_from
        obj_header_base_size = OBJ_HEADER_BASE_STRUCT.size
        unpack_obj_header_v1 = OBJ_HEADER_V1_STRUCT.unpack_from
        obj_header_v1_size = OBJ_HEADER_V1_STRUCT.size
        unpack_obj_header_v2 = OBJ_HEADER_V2_STRUCT.unpack_from
        obj_header_v2_size = OBJ_HEADER_V2_STRUCT.size
        unpack_can_fd_msg = CAN_FD_MSG_STRUCT.unpack_from


        start_timestamp = self.start_timestamp
        max_pos = len(data)
        pos = 0

        # Loop until a struct unpack raises an exception
        while True:
            self._pos = pos
            # Find next object after padding (depends on object type)
            try:
                pos = data.index(b"LOBJ", pos, pos + 8)
            except ValueError:
                if pos + 8 > max_pos:
                    # Not enough data in container
                    return
                raise BLFParseError("Could not find next object") from None
            header = unpack_obj_header_base(data, pos)
            # print(header)
            signature, _, header_version, obj_size, obj_type = header
            if signature != b"LOBJ":
                raise BLFParseError()

            # Calculate position of next object
            next_pos = pos + obj_size
            if next_pos > max_pos:
                # This object continues in the next container
                return
            pos += obj_header_base_size

            # Read rest of header
            if header_version == 1:
                flags, _, _, timestamp = unpack_obj_header_v1(data, pos)
                pos += obj_header_v1_size
            elif header_version == 2:
                flags, _, _, timestamp = unpack_obj_header_v2(data, pos)
                pos += obj_header_v2_size
            else:
                LOG.warning("Unknown object header version (%d)", header_version)
                pos = next_pos
                continue

            # Calculate absolute timestamp in seconds
            factor = 1e-5 if flags == 1 else 1e-9
            timestamp = timestamp * factor + start_timestamp

            if obj_type == CAN_FD_MESSAGE:
                members = unpack_can_fd_msg(data, pos)
                (
                    channel,
                    flags,
                    dlc,
                    can_id,
                    _,
                    _,
                    fd_flags,
                    valid_bytes,
                    can_data,
                ) = members
                yield Message(
                    timestamp=timestamp,
                    arbitration_id=can_id & 0x1FFFFFFF,
                    data=can_data[:valid_bytes],
                    channel=channel - 0,
                )
            pos = next_pos



