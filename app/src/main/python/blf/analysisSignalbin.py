import json

class TraceData():
    def __init__(self) -> None:
        pass
    
    def getDataALLbyparam(self,blf_channel_data,signal_list,params):
        print("step 1 start.")
        print(len(blf_channel_data))
        print(len(signal_list))
        # get data
        traceList = {}
        traceList["error"] = []
        for param in params:
            channel = param[0]
            arbitration_id = param[1]
            signal_name = param[2]
            if channel not in traceList:
                traceList[channel] = {}
            if channel not in blf_channel_data:
                traceList["error"].append("there is no channel {0} in blf.".format(channel))
                continue
            if arbitration_id not in blf_channel_data[channel]:
                traceList["error"].append("there is no msg {0} in channel {1} in blf.".format(arbitration_id,channel))
                continue
            for x in blf_channel_data[channel][arbitration_id]:
                bytes_data = ''
                if arbitration_id not in traceList[channel]:
                    traceList[channel][arbitration_id] = {}
                for value in x[1]:
                    outvalue = '{:08b}'.format(value)
                    bytes_data = outvalue + bytes_data
                if channel not in signal_list:
                    traceList["error"].append("there is no channel {0} in dbc.".format(channel))
                else:
                    if arbitration_id not in signal_list[channel]:
                        traceList["error"].append("there is no msg {0} in channel {1} in dbc.".format(arbitration_id,channel))
                    else:
                        if signal_name not in signal_list[channel][arbitration_id]:
                            traceList["error"].append("there is no signal {0} no msg {1} in channel {2} in dbc.".format(signal_name,arbitration_id,channel))
                        else:
                            signal = signal_name
                            target_signal = signal_list[channel][arbitration_id][signal]
                            over_line = target_signal['length'] / 8
                            data = ""
                            if over_line > 1:
                                startbit_line_length = target_signal['startbit'] % 8 + 1
                                startbit = (target_signal['startbit'] + 1) * -1
                                endbit = startbit + startbit_line_length
                                if endbit == 0:
                                    data = bytes_data[startbit:]
                                else:
                                    data = bytes_data[startbit:endbit]
                                    
                                s = int(target_signal['startbit'] / 8) + 1
                                count = 1
                                rest_length = target_signal['length'] - startbit_line_length
                                while count <= over_line:
                                    startbit_temp = (s + count) * 8 - 1
                                    startbit = (startbit_temp + 1) * -1
                                    if rest_length > 8:
                                        endbit = startbit + 8
                                        rest_length -= 8
                                    else:
                                        endbit = startbit + rest_length
                                    data = data + bytes_data[startbit:endbit]
                                    count += 1
                                value = int(data,2)
                            else:
                                startbit = (target_signal['startbit'] + 1) * -1
                                endbit = startbit + target_signal['length']
                                if endbit == 0:
                                    value = int(bytes_data[startbit:],2)
                                else:
                                    value = int(bytes_data[startbit:endbit],2)
                            value = value * float(target_signal['x']) + float(target_signal['y'])
                            if signal not in traceList[channel][arbitration_id]:
                                traceList[channel][arbitration_id][signal] = {}
                                traceList[channel][arbitration_id][signal]["timestamp"] = []
                                traceList[channel][arbitration_id][signal]["value"] = []
                                traceList[channel][arbitration_id][signal]["timestamp"].append(x[0])
                                traceList[channel][arbitration_id][signal]["value"].append(value)
                            else:
                                traceList[channel][arbitration_id][signal]["timestamp"].append(x[0])
                                traceList[channel][arbitration_id][signal]["value"].append(value)
        print("step 3 start.")
        return json.dumps(traceList)

        
