from typing import Optional, Dict
from canmatrix import CanMatrix, formats
import json



# path = r"D:\Python_worksapce\for_andriod\parsedbc\MS11\E4\MS11_ADASCANFD_220504.dbc"

path1 = "/storage/emulated/0/Download/Lark/MS11_ChassisFusionCANFD_240903_1.dbc"






def can_matrix_to_dict(can_matrix: CanMatrix) -> dict:
    # 将 CanMatrix 转换为字典
    result = {
        "frames": [
            {
                "id": frame.arbitration_id.id,
                "name": frame.name,
                "signals": [
                    {
                        "name": signal.name,
                        "start_bit": signal.start_bit,
                        "size": signal.size,
                        "is_little_endian": signal.is_little_endian,
                    }
                    for signal in frame.signals
                ],
                "comment": frame.comment,
                "is_fd": frame.is_fd,
            }
            for frame in can_matrix.frames
        ]
    }
    return result


def serialize_can_matrices(can_matrix_dict: Optional[Dict[str, CanMatrix]]) -> Optional[str]:
    if can_matrix_dict is None:
        return None

    serialized_dict = {}
    for key, can_matrix in can_matrix_dict.items():
        serialized_dict[key] = can_matrix_to_dict(can_matrix)

    # 将字典转换为 JSON 字符串
    return json.dumps(serialized_dict)


def parse_can_matrix_data(can_matrix_dict: Optional[Dict[str, CanMatrix]]) -> None:
    if can_matrix_dict is None:
        print("No data to parse.")
        return

    for key, can_matrix in can_matrix_dict.items():
        print(f"Parsing CAN Matrix with key: {key}")
        # 这里可以添加解析每个 CanMatrix 的逻辑
        for message in can_matrix.frames:
            print(f"  - Message ID: {message.arbitration_id.id}, Name: {message.name}, "
                  f"Signals: {message.signals}, Comment: {message.comment}, Is FD: {message.is_fd}")


def Python_say_Hello(path):
    # 序列化数据
    print ("Hello Python")
    db = formats.loadp(path)
    serialized_data = serialize_can_matrices(db)
    # 打印序列化后的 JSON 字符串
    print(serialized_data)
    # 解析数据
    parse_can_matrix_data(db)
    print ("Hello Python")# def try(path):
#     db = formats.loadp(path)