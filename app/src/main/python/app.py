import copy
import json
import os
import struct
import unittest
from urllib import request

from flask import Flask, request, jsonify

from shared_pkg.myBlf import myBLFReader

app = Flask(__name__)

blf_file_state = {"file_size": 0, "file_path": ""}
valueDict = {}
frameDict_zero={}
dbc={}

@app.route('/')
def hello_world():  # put application's code here
    return 'Hello World!'

@app.route("/blft/getDBC",methods=['POST'])
def blfGetDBC():
    """
    @api {post} /blft/getDBC 获取dbc信息
    @apiVersion 1.0.0
    @apiName blfGetDBC
    @apiGroup blf 通讯质量
    @apiBody {string} sdb SDB版本
    @apiSampleRequest /blft/getDBC
    """
    # return json.dumps((os.getcwd(),os.path.join(os.path.dirname(__file__),  'assets')))
    global dbc
    global valueDict
    global frameDict
    global signalMap
    global signalMapNew
    global frameDict_zero
    global channelComment
    global frameName
    data_reset()

    data = request.json
    print(data)
    cartype = data.get('carType')

    if cartype != custom:
        is_custom = False
        sdb = data.get('sdb')
        # if sdb not in ['E3', 'E3U1']:
        #     return 'sdb版本错误！'
        # dir=os.path.join(os.path.dirname(__file__),  'assets')#os.getcwd()
        dir = os.path.dirname(__file__)
        dir_parent = os.path.join(Path(dir).parent, 'shared_pkg')
        key_ini = b'jiamiini'
        # print(dir)
        # dir=os.path.join(os.path.abspath('.') ,".\\backend\\apps\\blft\\assets")
        # f_h = open(dir+r".\dbc\dbConf.ini")
        # f_h = open(dir + r"\bdaces\cfignsf\Extp2DceSpicProtIo\Lib\nxqlzxhtq.dll",'rb')
        #
        # DBCconfingJson1aW5p.dll = f_h.read()
        # DBCconfingJson = lzss.decompress(DBCconfingJson1).decode()
        f_h = open(dir_parent + f"{os.sep}bdaces{os.sep}cfignsf{os.sep}Extp2DceSpicProtIo{os.sep}Lib{os.sep}aW5p.dll", 'rb')
        DBCconfingJson1 = f_h.read()
        des = DES.new(key_ini, DES.MODE_ECB)
        DBCconfingJson = des.decrypt(DBCconfingJson1).decode().rstrip(' ')
        DBCconfing = json.loads(DBCconfingJson)
        f_h.close()
        dbcFiles = DBCconfing[cartype][sdb]
        signalMap = {}
        signalMapNew = {}
        key_dbc = b'jiamidbc'
        des_dbc = DES.new(key_dbc, DES.MODE_ECB)
    else:
        is_custom = True
        files = data.get('files')
        comments = data.get('comments')
        dbcFiles = get_dbcConf(files,comments)
        signalMap = {}
        signalMapNew = {}

    for dbcFile in dbcFiles:
        dbc[dbcFile[1]] = {}
        valueDict[dbcFile[1]] = {}
        frameDict[dbcFile[1]] = {}
        signalMap[dbcFile[1]] = {}
        signalMapNew[dbcFile[1]] = {}
        channelComment[dbcFile[1]] = dbcFile[2]
        frameName[dbcFile[1]] = {}
        try:
            if not is_custom:
                dbc_str = open(dir_parent + dbcFile[0][1:], 'rb').read()
                # dbc_info = lzss.decompress(dbc_str).decode()
                dbc_info = des_dbc.decrypt(dbc_str).decode().rstrip(' ')
            else:
                try:
                    dbc_info = open(dbcFile[0], 'r', encoding="gbk").read()
                except:
                    dbc_info = open(dbcFile[0], 'r', encoding="utf-8").read()

            try:
                channel = canmatrix.formats.loads_flat(dbc_info, import_type='dbc', encoding='gbk', dbcImportEncoding='gbk')
            except:
                channel = canmatrix.formats.loads_flat(dbc_info, import_type='dbc', encoding='utf-8', dbcImportEncoding='utf-8')

        except:
            print("DBC ERROR！")
            return {}
        # channel=loadp_flat(dir+dbcFile[0],import_type='dbc')
        for each in channel.frames:
            frameID = each.arbitration_id.id
            # frameID = hex(int(frameID))
            dbc[dbcFile[1]][frameID] = each
            valueDict[dbcFile[1]][frameID] = {}
            frameDict[dbcFile[1]][frameID] = []
            signalMap[dbcFile[1]][frameID] = []
            signalMapNew[dbcFile[1]][each.name+"("+str(hex(frameID))+")"] = []
            frameName[dbcFile[1]][frameID] = each.name
            for signal in each.signals:
                signalMap[dbcFile[1]][frameID].append((signal.name, signal.comment))
                valueDict[dbcFile[1]][frameID][signal.name] = (signal.values, each.cycle_time)
                receivers = ''
                for rev in each.receivers:
                    receivers = receivers + rev + "/"
                receivers = receivers[0:len(receivers) - 1]
                transmitters = ''
                for trm in each.transmitters:
                    transmitters = transmitters + trm + "/"
                transmitters = transmitters[0:len(transmitters) - 1]
                finalname = transmitters + "::" + receivers + "::" + each.name + "::" + signal.name + "::" + ("CANFD signal" if each.is_fd else "CAN signal")
                signalMapNew[dbcFile[1]][each.name+"("+str(hex(frameID))+")"].append((signal.name, signal.comment, finalname))
    frameDict_zero = copy.deepcopy(frameDict)
    return json.dumps(signalMapNew)


@app.route("/blft/getBLFdata", methods=['POST'])
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
    file_size_G = file_size / (1024 ** 3)
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

@app.route("/blft/getAnalysisByParams", methods=['POST'])
def blfGetAnalysisByParams():
    """
    @api {post} /blft/getAnalysisByParams 指定信号解析
    @apiVersion 1.0.0
    @apiName blfGetAnalysisByParams
    @apiGroup blf 通讯质量
    @apiBody {array} params [[msg 消息ID,channel 通道ID,signal 信号名]]
    @apiSampleRequest /blft/getAnalysisByParams
    """
    data = request.json
    global dbc
    global frameDict
    global blf_file_state
    global isBigfile

    def trimSignalList(inputlist):
        temp = {}
        for each in inputlist:
            channelID = each[0]
            frameID = each[1]
            signalName = each[2]
            try:
                checkSignalValid = valueDict[channelID][frameID][signalName]
            except:
                continue
            if channelID not in temp.keys():
                temp[channelID] = {}
            if frameID not in temp[channelID].keys():
                temp[channelID][frameID] = []
            temp[channelID][frameID].append(signalName)
        ret = []
        for (key1, value1) in temp.items():
            for (key2, value2) in value1.items():
                ret.append((key1, key2, value2))
        return ret
    overLenSig = []
    signalList = data.get('params')
    signalListTrimed = trimSignalList(signalList)
    retSignalValue = []
    if isBigfile:
        channelIds = []
        frameIds = []
        for i in signalListTrimed:
            channelIds.append(i[0])
            frameIds.append(i[1])
        log = myBLFReader(blf_file_state["file_path"])
        for each in log:
            can_data = each[3]
            channelID = each[4]
            frameID = each[1]
            timestampe = each[0]
            if dbc.get(channelID) and channelID in list(channelIds):
                if frameID in dbc[channelID].keys() and frameID in frameIds:
                    # frameDict[channelID][frameID].append((timestampe,can_data))
                    frameDict[channelID][frameID].append(struct.pack('<d', timestampe) + can_data)

    for each in signalListTrimed:
        signalValue = {}
        channelID = each[0]
        frameID = each[1]
        signalsName = each[2]
        for sigName in signalsName:
            signalValue[sigName] = []
            signals = dbc[channelID][frameID].signals
            for signal in signals:
                if signal.size >= 32:
                    overLenSig.append(signal.name)
        if frameID in dbc[channelID].keys():
            framedatas = frameDict[channelID][frameID]
            for frameData in framedatas:
                timestampe = struct.unpack("<d", frameData[0:8])
                try:
                    decoded = dbc[channelID][frameID].decode(frameData[8:])
                except:
                    # print(f"无法解析的信号data: {channelID} {frameID}")
                    continue
                for sigName in signalsName:
                    if sigName in decoded.keys():
                        if sigName in overLenSig:
                            signalValue[sigName].append((timestampe, int(decoded[sigName].phys_value)))
                        else:
                            signalValue[sigName].append((timestampe, decoded[sigName].phys_value))
        for sigName in signalsName:
            if sigName in valueDict[channelID][frameID].keys():
                if sigName in overLenSig:
                    bigData = 1
                else:
                    bigData = 0
                signalEmum = valueDict[channelID][frameID][sigName][0]
                signalPeriod = valueDict[channelID][frameID][sigName][1]
                x = (channelID, frameID, sigName, signalValue[sigName], signalEmum, signalPeriod,bigData)
                retSignalValue.append(x)
    return json.dumps(retSignalValue)



def run():
    app.run(host='0.0.0.0', port=8080, debug=False)

if __name__ == '__main__':
    app.run()