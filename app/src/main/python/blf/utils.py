import json
import subprocess
import queue

g_exeLog = queue.Queue(1024)
global res

class ReturnStatus:
    def __init__(self, code, data, msg):
        self.code = code
        self.data = data
        self.msg = msg

    def get_json_str(self):
        content = {
            "code": self.code,
            "msg": self.msg,
            "data": self.data
        }
        return json.dumps(content,ensure_ascii=False)


def getExecuteContent(cmd):
    cmd = "echo =====start===== && {} && echo =====end=====".format(cmd)
    print(cmd)
    global res
    res = subprocess.Popen(cmd,
                           shell=True,
                           text=True,  # 缓存内容为文本，避免后续编码显示问题
                           stdout=subprocess.PIPE,
                           stderr=subprocess.STDOUT)  # 执行shell语句并定义输出格式
    start_flag = False
    while res.poll() is None:
        line = res.stdout.readline().strip("\n")
        if line.startswith("=====end====="):
            start_flag = False
        if start_flag and line:
            if not line.startswith("debug"):
                print(line)
                g_exeLog.put(line)
        if line.startswith("=====start====="):
            start_flag = True
        res.stdout.flush()  # 刷新缓存，防止缓存过多造成卡死
    return res

def stopExecute():
    global res
    if res:
        res.kill()



if __name__ == "__main__":
    pass
