import json
from typing import List, Dict, Optional
from canmatrix import CanMatrix, formats


def can_matrix_to_list(can_matrix: CanMatrix) -> List[Dict]:
    # 将 CanMatrix 中的所有信号转换为列表
    result = [
        {
            "id": frame.arbitration_id.id,
            "name": frame.name,
            "cycle_time" : frame.cycle_time,
            "senders": "|".join(frame.transmitters),
            "receivers": "|".join(frame.receivers),
            "signals": [
                {
                    "name": signal.name,
                    "start_bit": signal.start_bit,
                    "size": signal.size,
                    "is_little_endian": signal.is_little_endian,
                    "is_signed": signal.is_signed,
                    "factor": float(signal.factor),
                    "offset": float(signal.offset),
                    "min": float(signal.min),
                    "max": float(signal.max),
                    "comment": signal.comment,
                    "initial_value": float(signal.initial_value),
                    "attributes": signal.attributes
                }
                for signal in frame.signals
            ],
            "comment": frame.comment,
            "is_fd": frame.is_fd,
        }
        for frame in can_matrix.frames
    ]
    return result


def serialize_can_matrices(can_matrix_dict: Optional[Dict[str, CanMatrix]]) -> Optional[str]:
    if can_matrix_dict is None:
        return None

    serialized_list = []
    for key, can_matrix in can_matrix_dict.items():
        serialized_list.extend(can_matrix_to_list(can_matrix))

    # 将列表转换为 JSON 字符串
    return json.dumps(serialized_list)


def parse_can_matrix_data(can_matrix_dict: Optional[Dict[str, CanMatrix]]) -> None:
    if can_matrix_dict is None:
        print("No data to parse.")
        return

    for key, can_matrix in can_matrix_dict.items():
        # print(f"Parsing CAN Matrix with key: {key}")
        # 这里可以添加解析每个 CanMatrix 的逻辑

        for message in can_matrix.frames:
            pass
#             print("==============={}".format(message.keys());
#             print(f"  - Message ID: {message.arbitration_id.id}, Name: {message.name}, "
#                   f"Signals: {message.signals}, Comment: {message.comment}, Is FD: {message.is_fd}")
            # print(f"  - Signals: {message.signals}")





def parse_dbc_to_msg(path):
    db = formats.loadp(path)
    serialized_data = serialize_can_matrices(db)
    # 打印序列化后的 JSON 字符串
#     print(serialized_data)
    return serialized_data
    # return serialized_data
    # 解析数据
    # parse_can_matrix_data(db)

def msg_to_signal(path):
    db = formats.loadp(path)
    serialized_data = serialize_can_matrix_data(db)



path1 = "/storage/emulated/0/Download/Lark/MS11_ChassisFusionCANFD_240903_1.dbc"
# parse_dbc_to_msg(path1)