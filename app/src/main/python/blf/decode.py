# -*- coding: UTF-8 -*-
import json
from base64 import b64decode
import re


class Decode():

    def __init__(self) -> None:
        pass

    def df(self, infile):
        with open(infile, "r", encoding="utf-8") as f1:
            content = f1.readlines()

        key = ''.join(format(ord(x), '08b') for x in bytes.decode(b64decode('MTk4MjAyMTY=')))
        bin_result = content[0]

        # key decode
        string = ""
        length = 64
        result_length = len(bin_result)
        last_length = result_length % 64
        key_temp = key[:last_length]
        key_str_list = []
        while length < len(bin_result):
            key_str_list.append(key)
            length = length + 64

        key_str = ''.join(key_str_list)
        key_str = key_str + key_temp
        b_int = int(key_str.encode("utf-8"), 2)
        bin_int = int(bin_result.encode("utf-8"), 2)
        result_int = bin_int ^ b_int
        result = f'{result_int:08b}'
        result = result.zfill(len(bin_result))
        string = result

        # bin decode
        length = 0
        result = ""
        bin_list = re.findall(r'.{8}', string)

        result_list = []
        for object in bin_list:
            result_temp = chr(int(object.encode('utf-8'), 2))
            result_list.append(result_temp)
        result = "".join(result_list)

        # base 64 decode
        result_base64_decode = b64decode(result.encode("utf-8"))
        result_base64_decode = result_base64_decode.decode(encoding='utf-8')
        result_json = json.loads(result_base64_decode)
        return result_json
