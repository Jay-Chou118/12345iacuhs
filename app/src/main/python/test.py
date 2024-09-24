import copy
import json
import os
import struct
import unittest
from urllib import request

from flask import Flask, jsonify

from shared_pkg.myBlf import myBLFReader

test = Flask(__name__)


@test.route('/')
def hello_world():  # put application's code here
    return 'Hello World!'

@test.route("/blft/getBLFdata", methods=['POST'])
def blfGetBLFdata():
    """
    @api {post} /blft/getBLFdata 读取blf文件
    @apiVersion 1.0.0
    @apiName blfGetBLFdata
    @apiGroup blf 通讯质量
    @apiBody {string} blfFile blf文件路径
    @apiSampleRequest /blft/getBLFdata
    """
    data = request.json
    global log
    global dbc
    global frameDict
    global frameDict_zero
    global blf_file_state
    global isBigfile
    frameDict = copy.deepcopy(frameDict_zero)
    file = data.get('blfFile')
    stats = os.stat(file)
    # 以字节为单位
    file_size = stats.st_size
    file_size_G = file_size/(1024 ** 3)
    blf_file_state["file_size"] = file_size_G
    blf_file_state["file_path"] = file
    log = myBLFReader(data.get('blfFile'))

    if data.get("mode") == "bigFile":
        isBigfile = True
        ret = {}
        ret["startTime"] = log.start_timestamp
        return json.dumps(ret)
    else:
        isBigfile = False

    counter = 0
    for each in log:
        can_data = each[3]
        channelID = each[4]
        frameID = each[1]
        timestampe = each[0]
        if dbc.get(channelID):
            if frameID in dbc[channelID].keys():
                # frameDict[channelID][frameID].append((timestampe,can_data))
                frameDict[channelID][frameID].append(struct.pack('<d', timestampe) + can_data)

    ret = {}
    ret["startTime"] = log.start_timestamp
    return json.dumps(ret)

@test.route("/blft/getParsedData", methods=['GET'])
def getParsedData():
    """
    @api {get} /blft/getParsedData 获取解析后的数据
    @apiVersion 1.0.0
    @apiName getParsedData
    @apiGroup blf 通讯质量
    @apiQuery {string} blfFile blf文件路径
    @apiSuccessExample {json} Success-Response:
       {
           "startTime": "开始时间戳",
           "frames": {
               "通道ID": {
                   "帧ID": [
                       "时间戳_CAN数据"
                   ]
               }
           }
       }
    """
    blfFile = request.args.get('blfFile')
    if not blfFile:
        return jsonify({"error": "Missing blfFile parameter"}), 400

    # 模拟数据
    parsed_data = {
        "startTime": log.start_timestamp,
        "frames": frameDict
    }

    return jsonify(parsed_data)


def run():
    test.run(host='0.0.0.0', port=8080, debug=False)


if __name__ == '__main__':
    run()
