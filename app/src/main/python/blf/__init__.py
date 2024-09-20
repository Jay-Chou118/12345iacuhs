import subprocess,os,re
from flask import Blueprint,request
from .utils import ReturnStatus,getExecuteContent,g_exeLog,stopExecute
from .blfReader import BlfReader
from .analysisSignalbin import TraceData
from .decode import Decode

blf=Blueprint('blf', __name__)
blf_data = {}
dbc_data = {}

@blf.route("/blf/checkAll",methods=['POST'])
def blfCheckAll():
    """
    @api {post} /blf/checkAll 全量检查
    @apiVersion 1.0.0
    @apiName blfCheckAll
    @apiGroup blf 通讯质量
    @apiBody {String} path 文件路径
    @apiBody {String="E3","E4"} sdb SDB版本
    @apiBody {Number} tolerance 容差
    @apiSampleRequest /blf/checkAll
    """
    data = request.json
    dir=os.path.join(os.path.dirname(__file__),  'assets')#os.getcwd()
    command = "cd \"{0}\" && .\\blfcheck.exe -c channel_check -i \"{1}\" && .\\blfcheck.exe -c ecu_check -i \"{1}\" -b \".\\bin\\{2}\\msg.bin\" && .\\blfcheck.exe -c msg_check -i \"{1}\" -b \".\\bin\\{2}\\msg.bin\" && .\\blfcheck.exe -c cyclic_check -i \"{1}\" -b \".\\bin\\{2}\\msg.bin\" -t {3} && .\\blfcheck.exe -c overload_check -i \"{1}\" && .\\blfcheck.exe -c synctime_check -i \"{1}\" -b \".\\bin\\{2}\\signal.bin\" -a \".\\analyz.exe\"".format(dir,data.get("path"),data.get("sdb"),data.get("tolerance"))
    ret=getExecuteContent(command)
    res = ReturnStatus(0, {"dir":dir}, "执行成功")
    if ret.returncode:
        res.code = 1001
        res.msg = ""
    return res.get_json_str()



@blf.route("/blf/stopAll",methods=['POST'])
def blfStopAll():
    """
    @api {post} /blf/stopAll 中断检查
    @apiVersion 1.0.0
    @apiName blfStopAll
    @apiGroup blf 通讯质量
    @apiSampleRequest /blf/stopAll
    """
    stopExecute()
    return ''


@blf.route("/blf/getExeLog",methods=['POST'])
def blfGetExeLog():
    """
    @api {post} /blf/getExeLog 轮询日志
    @apiVersion 1.0.0
    @apiName blfGetExeLog
    @apiGroup blf 通讯质量
    @apiSampleRequest /blf/getExeLog
    """
    res = ReturnStatus(0, [], "执行成功")
    ret_list = []
    q_size = g_exeLog.qsize()
    if not g_exeLog.empty():
        for i in range(q_size):
            ret_list.append(g_exeLog.get())
        res.data = ret_list

    return res.get_json_str()

@blf.route("/blf/exportExelog",methods=['POST'])
def blfExportExeLog():
    """
    @api {post} /blf/exportExelog 导出日志
    @apiVersion 1.0.0
    @apiName blfExportExeLog
    @apiGroup blf 通讯质量
    @apiBody {string} content 文本
    @apiBody {string} directory 文件路径
    @apiSampleRequest /blf/exportExelog
    """
    data=request.json
    try:
        fo= open(data.get("directory"),"w")
        fo.write(data.get('content'))
        fo.close()
    except:
        return '1'
    else:
        return '0'

    

@blf.route("/blf/getAnalysis",methods=['POST'])
def blfGetAnalysis():
    """
    @api {post} /blf/getAnalysis 全量解析
    @apiVersion 1.0.0
    @apiName blfGetAnalysis
    @apiGroup blf 通讯质量
    @apiBody {string} msg 消息ID
    @apiBody {string} channel 通道ID
    @apiBody {string} sdb SDB版本
    @apiSampleRequest /blf/getAnalysis
    """
    data=request.json
    print(data)
    dir=os.path.join(os.path.dirname(__file__),  'assets')#os.getcwd()
    command = "cd \"{0}\" && .\\analyz.exe -c traceDataALL -l \"{3}\" -b \"{0}\\bin\\{4}\\signal.bin\"".format(dir,data.get("channel"),data.get("msg"),data.get("directory"),data.get('sdb'))
    print(command)
    t = subprocess.run(command,shell=True,text=True,stdin=subprocess.PIPE,stdout=subprocess.PIPE)
    # print(t.stdout)
    str= re.sub(r"\d+:",lambda match:"\""+match.group()[:-1]+"\":",re.sub(r"\'","\"",t.stdout.split("\n")[7]))
    return str

@blf.route("/blf/getAnalysisByParams",methods=['POST'])
def blfGetAnalysisByParams():
    """
    @api {post} /blf/getAnalysisByParams 指定信号解析
    @apiVersion 1.0.0
    @apiName blfGetAnalysisByParams
    @apiGroup blf 通讯质量
    @apiBody {array} params [[msg 消息ID,channel 通道ID,signal 信号名]]
    @apiSampleRequest /blf/getAnalysisByParams
    """
    data=request.json
    print(data)
    abc =  TraceData()
    params = data.get("params")
    global blf_data 
    global dbc_data
    result = abc.getDataALLbyparam(blf_data,dbc_data,params)
    # print(result)
    return result

@blf.route("/blf/getDBC",methods=['POST'])
def blfGetDBC():
    """
    @api {post} /blf/getDBC 获取dbc信息
    @apiVersion 1.0.0
    @apiName blfGetDBC
    @apiGroup blf 通讯质量
    @apiBody {string} sdb SDB版本
    @apiSampleRequest /blf/getDBC
    """
    data=request.json
    print(data)
    bin_file = data.get("sdb")
    decode_file = Decode()
    global dbc_data 
    dir=os.path.join(os.path.dirname(__file__),  'assets')#os.getcwd()
    signal_dic = decode_file.df(dir+"/bin/"+bin_file+"/signal.bin")
    for x in signal_dic['data']:
        if x['channel'] not in dbc_data:
            dbc_data[x['channel']] = {}
            dbc_data[x['channel']][x['msg_id']] = {}
            dbc_data[x['channel']][x['msg_id']][x['signal_name']] = {"startbit":x['start_bit'],"length":x['bit_length'],"x":x['factor'],"y":x['offset_value'],"channel":x['channel']}
        else:
            if x['msg_id'] not in dbc_data[x['channel']]:
                dbc_data[x['channel']][x['msg_id']] = {}
                dbc_data[x['channel']][x['msg_id']][x['signal_name']] = {"startbit":x['start_bit'],"length":x['bit_length'],"x":x['factor'],"y":x['offset_value'],"channel":x['channel']}
            else:
                dbc_data[x['channel']][x['msg_id']][x['signal_name']] = {"startbit":x['start_bit'],"length":x['bit_length'],"x":x['factor'],"y":x['offset_value'],"channel":x['channel']}
    return "success"

@blf.route("/blf/getBLFdata",methods=['POST'])
def blfGetBLFdata():
    """
    @api {post} /blf/getBLFdata 读取blf文件
    @apiVersion 1.0.0
    @apiName blfGetBLFdata
    @apiGroup blf 通讯质量
    @apiBody {string} blfFile blf文件路径
    @apiSampleRequest /blf/getBLFdata
    """
    data=request.json
    print(data)
    abc =  BlfReader()
    blfPath = data.get("blfFile")
    global blf_data
    blf_data = abc.readfasttemp(blfPath)
    return "success"
