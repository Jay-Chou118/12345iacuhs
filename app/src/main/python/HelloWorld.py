import json
from typing import List, Dict, Optional
from canmatrix import CanMatrix, formats


def can_matrix_to_list(can_matrix: CanMatrix) -> List[Dict]:
    result = [
        {
            "id": frame.arbitration_id.id,
            "name": frame.name,
            "cycle_time": frame.cycle_time,
            "senders": "|".join(frame.transmitters),
            "receivers": "|".join(frame.receivers),
            "signals": [
                {
                    "name": signal.name,
                    "start_bit": signal.start_bit - (signal.start_bit % 8) + ( 7 - (signal.start_bit % 8)),
                    "size": signal.size,
                    "is_little_endian": signal.is_little_endian,
                    "is_signed": signal.is_signed,
                    "factor": float(signal.factor),
                    "offset": float(signal.offset),
                    "min": float(signal.min),
                    "max": float(signal.max),
                    "comment":(signal.comment.encode('iso-8859-1').decode('gb2312') if hasattr(signal, 'comment') and isinstance(signal.comment, str) else ""),
                    # 'comment':' ',
                    "initial_value": float(signal.initial_value),
                    "choices": json.dumps(signal.values or {})
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

    return json.dumps(serialized_list,  ensure_ascii=False,indent=4)


def parse_dbc_file(path: str) -> Optional[str]:
    try:
        db = formats.loadp(path)
        serialized_data = serialize_can_matrices(db)
        print("length : " ,len(serialized_data))
        return serialized_data
    except Exception as e:
        print(f"Error loading DBC file from path '{path}': {e}")
        return None


# Example usage
# path1 = "/storage/emulated/0/Download/Lark/MS11_ChassisFusionCANFD_240903_1.dbc"
# result = parse_dbc_file(path1)
# if result:
#     print(result)