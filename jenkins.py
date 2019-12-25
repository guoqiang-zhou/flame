import urllib
import http.cookiejar
import requests
import json
import argparse
import sys,re
import time
from urllib import request
from bs4 import BeautifulSoup

'''引入外部参数'''
if __name__ == "__main__":
    # 创建命令行解析器句柄，并自定义描述信息
    parser = argparse.ArgumentParser(description="test the argparse package")
    # 定义必选参数 positionArg
    # parser.add_argument("project_name")
    # 定义可选参数module
    parser.add_argument("--module", "-v1", help="模块")
    # 定义可选参数case_suit_name
    parser.add_argument("--case_suit_name", "-v2", help="模块名称")
    # 定义可选参数execute_type
    parser.add_argument("--execute_type", "-v3", help="CI/Normal")
    # 定义可选参数execute_stage
    parser.add_argument("--execute_stage", "-v4", help="FeatureTest / RegressionTest / PreReleaseTest / ReleaseTest")
    args = parser.parse_args()  # 返回一个命名空间
    print(args)
    params = vars(args)  # 返回 args 的属性和属性值的字典
    parameter=[]
    for k, v in params.items():
        parameter.append(v)
        # print(v)
    print(parameter[0])
    print(parameter[1])
    print(parameter[2])
    print(parameter[3])

'''获取报告返回数值'''
path = '/opt/htdocs/jenkins/reports/soa/html/'+parameter[0]+'/'+parameter[1]+'_Report.html'

with open(path, 'r',encoding='utf-8') as f:
    Soup = BeautifulSoup(f.read(), 'lxml')
    titles = Soup.select('html > body > table > .Failure > td')
lists = []
for title in titles:
     lists.append(title.text)
print("api_total:",lists[0],",","api_pass_total:",lists[1],",","api_pass_rate:",lists[3],",","excute_time:",lists[7])

'''转换时间'''
ms = lists[7]
listss =re.compile(r'\d+').findall(ms)
ss = int(listss[0])/1000  #毫秒转为秒
print(r'将毫秒转换为秒：',ms,'-->',ss,'s')

'''执行接口调用方法'''
test_time = time.strftime ("%Y-%m-%d %H:%M:%S", time.localtime ()) #获取当前时间
url = " http://godeye.hqygou.com/api/receiveAutoTestResultTest"
# postdata =urllib.parse.urlencode({
header = {"Content-Type":"application/json"}
raw={
     "api_key": "2b0e740f99f20906a54d04ebe9816d9b",
     "api_sign": "sTest123!@#",
     "project_id": 10007,
     "project_name": "SOA",
     "platform": "OTHER",
     "level": "Core",
     "case_total": 0,
     "case_pass_total": 0,
     "case_pass_rate": 0,
     "api_total": lists[0],
     "api_pass_total": lists[1],
     "api_pass_rate": lists[3],
     "execute_detail": [{
            "case_suit_id": "1",
            "case_suit_name": parameter[1],
            "module": parameter[0],
            "case_total": "0",
            "case_pass_total": "0",
            "case_pass_rate": "0",
            "api_total": lists[0],
            "api_pass_total": lists[1],
            "api_pass_rate": lists[3]
        }],
     "execute_stage": parameter[3],
     "execute_scene": "BaselineRelease",
     "execute_type": parameter[2],
     "autotest_type": "Interface",
     "autotest_source": "Jmeter",
     "excute_start_time": test_time,
     "excute_end_time": test_time,
     "excute_time": ss,
     "report_url": "test",
     "test_result": 1,
     "execute_result": 1,
     "desc": ""
}
data = json.dumps(raw)
data1 = bytes(data,"utf8")
print(data1.decode('unicode_escape'))
req = urllib.request.Request(url,data1,header)
# print(urllib.request.urlopen(req).read().decode('utf-8'))

# #自动记住cookie
cj = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
r = opener.open(req)
print(r.read().decode('unicode_escape'))