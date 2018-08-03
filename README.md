# 项目说明 
 用于提供单对单IM轻量化消息服务  
  去mq 去db 数据加密   
 自定义通信协议 
## snow
 IM 通讯项目core
## Serv 业务启动   
  spring boot方式启动     
## Center中心服务 
 提供中心数据  
 thrift和serv 通信（serv调用 ）并提供http api接口返回信息
 
 thrift-0.11.0发现不能生成文件 所以换0.10.0
  
### 开源项目引入
  https://github.com/somewhereMrli/albedo-thrift  
 由于没有中央仓库 所以直接copy过来 
