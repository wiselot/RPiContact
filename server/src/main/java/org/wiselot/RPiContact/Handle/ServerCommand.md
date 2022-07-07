# 传输至服务器的指令语法说明
#### usage的表达式较简,正则表达式参见code
### 1.登录验证
**usage** : login in [name] [passwd]    \
**return** : true / false (成功/失败)   \
**explain** : 服务器与客户端建立联系,线程阻塞

### 2.退出登录
**usage** : login out   \
**return** : bye    \
**explain** : 线程结束

### 3.添加联系人
**usage** : friend add [name]/[uuid] ([backupName]) \
**return** : done,[name],[uuid] / air,[uuid0],... / failed \
**explain** : 返回状态: done: 添加成功,air: 用户不唯一,返回多个uuid选择,failed:无法找到联系人 \
**talk** : 最好再加个UID,不然输入varchar 36太烦了

### 4.展示联系人(见Client)
### 5.发送消息
**usage** : msg send [uuid0],... [text] \
**return** : true,[msgUUID] / false

### 6.撤回消息
**usage** : msg ret [msgUUID0],... \
**return** : [true0 / false0],...

### 7.接收消息
**usage** : msg pull sendTo=[uuid0],... \
**return** : [msgUUID0,sendUUID0,getUUID0,Text,sendTime],... \
**explain** : 同步服务器和本地消息