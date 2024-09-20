# -*- coding: UTF-8 -*-
from can.io.blf import BLFReader
from .fastblf import BLFReader as mblf

class BlfReader():
    
    def __init__(self) -> None:
        self.abc ={}
        pass
    
    def readForAll(self,file):
        blf = BLFReader(file)
        msg = {}
        msg_result = {}
        msg_list = []
        for value in blf:
            datalist = []
            for x in value.data:
                datalist.append(hex(x))
            msg = {
                    "arbitration_id":value.arbitration_id,
                    "timestamp":value.timestamp,
                    "bitrate_switch":value.bitrate_switch,
                    "channel":value.channel,
                    "dlc":value.dlc,
                    "error_state_indicator":value.error_state_indicator,
                    "is_error_frame":value.is_error_frame,
                    "is_extended_id":value.is_extended_id,
                    "is_fd":value.is_fd,
                    "is_remote_frame":value.is_remote_frame,
                    "is_rx":value.is_rx,
                    "data":datalist
                }
            msg_list.append(msg)
            if value.channel not in msg_result:
                msg_result[value.channel] = []
                msg_result[value.channel].append(msg)
            else:
                msg_result[value.channel].append(msg)
        header = {"fileName":blf.file.name,"fileSize":blf.file_size,"object_count":blf.object_count,"startTimestamp":blf.start_timestamp,"stopTimestamp":blf.stop_timestamp}
        return msg_result,msg_list,header
    
    def readBLFData(self,file):
        blf = BLFReader(file)
        msg = {}
        msg_result = {}
        for value in blf:
            datalist = []
            for x in value.data:
                datalist.append(hex(x))
            msg = {
                    "arbitration_id":value.arbitration_id,
                    "timestamp":value.timestamp,
                    "bitrate_switch":value.bitrate_switch,
                    "channel":value.channel,
                    "dlc":value.dlc,
                    "error_state_indicator":value.error_state_indicator,
                    "is_error_frame":value.is_error_frame,
                    "is_extended_id":value.is_extended_id,
                    "is_fd":value.is_fd,
                    "is_remote_frame":value.is_remote_frame,
                    "is_rx":value.is_rx,
                    "data":datalist
                }
            if value.channel not in msg_result:
                msg_result[value.channel] = []
                msg_result[value.channel].append(msg)
            else:
                msg_result[value.channel].append(msg)
        return msg_result
    
    def readBLFDataEasy(self,file):
        blf = BLFReader(file)
        msg = {}
        msg_result = {}
        for value in blf:
            msg = {
                    "arbitration_id":value.arbitration_id,
                    "timestamp":value.timestamp,
                    "channel":value.channel,
                    "data":value.data
                }
            if value.channel not in msg_result:
                msg_result[value.channel] = []
                msg_result[value.channel].append(msg)
            else:
                msg_result[value.channel].append(msg)
        return msg_result
    
    def readChannel(self,file):
        blf = BLFReader(file)
        channel_id = {}
        for value in blf:
            if value.channel not in channel_id:
                channel_id[value.channel] = 1
        return channel_id
    
    def readForChannelMsgID(self,file):
        blf = BLFReader(file)
        msg_result = {}
        for value in blf:
            if value.channel not in msg_result:
                msg_result[value.channel] = []
                msg_result[value.channel].append(value.arbitration_id)
            else:
                msg_result[value.channel].append(value.arbitration_id)
        return msg_result
    
    def readForTimestamp(self,file):
        blf = BLFReader(file)
        msg = {}
        msg_result = {}
        for value in blf:
            msg = {
                    "arbitration_id":value.arbitration_id,
                    "timestamp":value.timestamp,
                    "channel":value.channel,
                }
            if value.channel not in msg_result:
                msg_result[value.channel] = {}
                msg_result[value.channel][value.arbitration_id] = []
                msg_result[value.channel][value.arbitration_id].append(value.timestamp)
            else:
                if value.arbitration_id not in msg_result[value.channel]:
                    msg_result[value.channel][value.arbitration_id] = []
                    msg_result[value.channel][value.arbitration_id].append(value.timestamp)
                else:
                    msg_result[value.channel][value.arbitration_id].append(value.timestamp)
        return msg_result
    
    def readBLFDataForTimestamp(self,file):
        blf = BLFReader(file)
        msg = {}
        msg_result = {}
        for value in blf:
            if value.channel == 1 and value.arbitration_id == 263 :
                datalist = []
                for x in value.data:
                    datalist.append(hex(x))
                msg = {
                        "arbitration_id":value.arbitration_id,
                        "timestamp":value.timestamp,
                        "bitrate_switch":value.bitrate_switch,
                        "channel":value.channel,
                        "dlc":value.dlc,
                        "error_state_indicator":value.error_state_indicator,
                        "is_error_frame":value.is_error_frame,
                        "is_extended_id":value.is_extended_id,
                        "is_fd":value.is_fd,
                        "is_remote_frame":value.is_remote_frame,
                        "is_rx":value.is_rx,
                        "data":datalist
                    }
                if value.channel not in msg_result:
                    msg_result[value.channel] = []
                    msg_result[value.channel].append(msg)
                else:
                    msg_result[value.channel].append(msg)
                break
        return msg_result
    
    def readfast(self,file):
        blf = mblf(file)
        msg = []
        msg_result = {}
        for value in blf:
            msg = [value.arbitration_id,value.timestamp,value.data]
            if value.channel not in msg_result:
                msg_result[value.channel] = []
                msg_result[value.channel].append(msg)
            else:
                msg_result[value.channel].append(msg)
        return msg_result
    
    def readfasttemp(self,file):
        blf = mblf(file)
        msg = []
        msg_result = {}
        for value in blf:
            msg = [value.timestamp,value.data]
            if value.channel not in msg_result:
                msg_result[value.channel] = {}
                msg_result[value.channel][value.arbitration_id] = []
                msg_result[value.channel][value.arbitration_id].append(msg)
            else:
                if value.arbitration_id not in msg_result[value.channel]:
                    msg_result[value.channel][value.arbitration_id] = []
                    msg_result[value.channel][value.arbitration_id].append(msg)
                else:
                    msg_result[value.channel][value.arbitration_id].append(msg)
        return msg_result