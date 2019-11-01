# 简易微信开发白皮书

[TOC]



## 1. 软件概况展示

### 1.1 功能展示（截图附于文末）

- #### 基础功能

1） 注册、登陆、登出(包含是否已注册、重名判断等)

2） 添加、删除好友

3） 好友列表展示

4） 实时的新消息提醒和收发消息（包含对是否好友，好友在线状态的判断）

5） 支持对文件，图片的发送和接收

- #### 拓展功能

6） **保存聊天记录**

7） **实时弹窗提醒**用户收到新消息，并**展示发送方和消息预览**

8） 缓存聊天内容，可以**保存未读的聊天记录**

9） 服务器主动向客户端推送信息（TCP长连接，**模拟实现双工通信**）

10） 使用**`token`进行用户身份验证**，除密码验证外提供多一层的安全保障

11） UI美化，**消息气泡大小动态变化**

12） 展示用户信息，如用户名，头像等

### 1.2 健壮性及异常情况处理展示

1） 检测并阻止重复注册或登陆

2） 检测用户的在线状态

3） 检查用户的好友列表及好友关系



## 2. 软件整体系统架构分析

### 2.1 整体：前后端分离

- 简易微信整体**前后端分离**
- 服务器的开发语言为 java，开发工具为 intelliJ Idea；
- 客户端的开发语言为 kotlin， 开发工具为 Android Studio。 
- 服务器部署在本机，与各客户端采用socket连接，C/S模式。UML图如下：

### 2.2 服务器端：JDBC, Socket, Thread, PostgreSql

- 采用`JDBC`管理底层数据库，底层采用`postgreSql`数据库存储必要的信息
- 使用 java结合sql语句操作数据库，实现对信息的增删查改。
- 采用`Socket`连接实现与客户端的连接与通信，从而成为沟通客户端的中枢，典型的C/S。
- 采用多线程开启socket通信，使用统一的Server和Map进行管理
- 重写java的Tuple类，以实现服务器的多返回值

### 2.3 客户端：MVP、Fragment、Json

- 使用 kotlin开发，MVP技术架构。数据层(model)、数据处理层(presenter)、用户交互层(view)高度解耦，使得数据处理和用户交互更加优雅
- 主界面采用ViewPager+Fragment，可以轻松在各界面间滑动
- 每项操作都提供反馈，实时接收服务器消息
- 自定数据、协议格式，借鉴目前常用的Json格式
- 快速、稳定的图片传输

## 3. 自定义协议分析

### 3.1 传输协议及格式

简易微信server端采用TCP作为传输层协议，http作为应用层协议，并对http的协议格式和报文格式进行自定义修改，报文采用 json 格式数据文档实现server与client间的通信。

### 3.2 网络连接

- 服务器搭建在本地，监听12000端口，本机访问服务器使用 ip为127.0.0.1，其余客户端访问服务器时地址采用本机 ip。服务器程序会一直监听发起请求的客户端，与之保持连接状态，直到客户
  端主动断开连接。在此期间，服务器与客户端间的输入输出流将保持。需要注意的是，每次服务器重启会强制登出所有用户。 
- 服务器可以接受并处理post请求，以json格式的数据返回请求结果及信息

### 3.3 部分自定义协议的接口文档（完整版太多了，附于文档末尾）

#### 1. 返回报文的body组成

每次服务器返回的报文body都会包含以下信息：

```json
{  “code”:code,  “msg”:”msg”,  “type”:”type” }  
```

其中code表示请求的结果码，msg表示返回的具体信息，type表示请求的种类

#### 2. 用户注册

接口路径为 `ip/register`

请求报文的body格式：

```json
{  “username”:”name”,  “pwd”:”password” }  
```

返回报文的body格式：

```
{  “code”:0,  “msg”:”msg”,  “type”:”register” } 
```

返回的code及msg：

| code |     msg      |
| :--: | :----------: |
|  0   |   注册成功   |
|  -1  | 重复的用户名 |

#### 3. 用户登陆

接口路径为`ip/login`

请求报文的body格式：

```json
{  “username”:”name”,  “pwd”:”password” }  
```

返回的body格式：

```
{  “code”:0,  “msg”:”msg”,  “type”:”login”,  “token”:12345 }  
```

code及msg：

| code |      msg       |
| :--: | :------------: |
|  0   |       OK       |
|  1   |    密码错误    |
|  2   |   已经在线了   |
|  -1  | 该用户尚未注册 |

#### 4. 用户登出

路径`ip/logout`

请求报文：

```
{  “userName”:”name”,  “token”:12345 }  
```

返回报文：

```
{  “code”:0,  “msg”:”msg”,  “type”:” offline” }  
```

code及msg：

| code |    msg     |
| :--: | :--------: |
|  0   |     OK     |
|  1   | 已经离线了 |
|  2   | token无效  |
|  -1  | 用户不存在 |

#### 5. 获取好友列表

路径`ip/friends`

请求报文：

```
{  “username”:”name”,  “token”:12345 }  
```

返回报文：

```
{  “code”:0,  “msg”:”msg”,  “type”:” friends”,  “friends”:[“friend1”,”friend2”] } 
-- friends是一个jsonArray的数组
```

code及msg：

| code |     msg      |
| :--: | :----------: |
|  0   |      OK      |
|  1   |  token无效   |
|  -1  | 用户尚未注册 |

#### 6. 完整版自定义协议的接口文档附于本文档末尾

## 3. 各部分实现技术细节分析

### 3.1 服务器技术细节

#### 3.1.1 **`DataBeans`** 包

- 该包下存放登录、注册等各种不同请求的数据类，统一命名为**`XxxRequestBean`**,**`XxxResponseBean`**,如（`CommonRequestBean`, `CommonResponseBean`等）
- **`XxxRequestBean`**用于存储从请求报文中按格式解析出的信息，用于本地对数据库的操作
- **`XxxResponseBean`**用于存储本地对数据库增删查改后的结果，用于向发起请求的客户端返回信息以及向需要转发的客户端转发消息
- 值得一提的是在该包内抽象出了**`JsonToObject`**和**`ObjectToJson`**两个接口，实现通信报文中json格式数据与服务器中java对象类型数据的项目转化，提高了代码复用性和可读性。

#### 3.1.2 **`HttpServer.java`**

- 该类持有server服务器的对象，并持有`userMap`等数据结构保存用户的`id`和对应的`socket`通信，从而实现**服务器向特定用户主动推送消息**，实现**好友申请和收到消息的即时提醒**
- 在此类使用多线程处理每个独立的socket通信，避免相互之间的干扰，便于管理，也能实现多用户登录
- 此类中的**`initDataBase`**方法使用JDBC连接数据库，若是数据库不存在时则主动建立数据库，具有良好的健壮性

#### 3.1.3 **`ServerThread.java`**

- 该类定义每个独立的线程中socket通信的动作，其中包括：
- - 重载**`Thread`**的**`run`**方法**解析自定义的请求报文**，得到请求种类（如`GET`）和请求的`url`以及请求报文`body`，传递给**`post`**函数
  - **`post`**函数得到报文，根据`header`中的url对各个功能函数进行调用，进行相应处理后得到应返回的数据。并通过各`Bean`类的格式化函数将信息格式化为合法的报文进行返回
  - 使用**`response`返回发出请求的客户端**，使用**`sendInfos`向被请求的客户端主动推送消息**
  - `regist`， `login`等功能函数从报文的`body`中得到用户名，身份验证`token`等信息，用于实现简易微信的各项功能，如：
  - - `registe` 从报文中获得用户名和密码，在数据库中进行查询，进行注册或返回异常（如重复用户名）等操作
    - `login` ，`logout`时对身份`token`进行验证后，更新数据库并返回用户的请求状态，如“已经注册”“注册成功”“已经在线”“密码错误”“登出成功”等信息
    - `getFriends`时，根据用户名查询数据库，进行数据更新并返回好友列表
    - 发送消息时，客户端请求`sendMsg`，向服务器发出消息及接收方和发送时间；服务器接到该报文后解析出被发送方，然后通过**`sendToFriend`**查询数据库得到被发送方的信息是否合法，该用户是否在线等，并通过`Map`得到该用户对应的socket连接。若是均符合条件，调用**`sendTo`**对消息进行转发，主动发给消息接收方
    - 发送图片和文件类型的消息流程基本一致，函数分别为`sendPicsToFriend`, `sendPics`和`sendFileToFriends`, `sendFile`
    - 添加好友与发送消息时类似，用户将请求发送给服务器，然后由服务器通过被请求方对应的socket连接进行转发。其中`makeFriends`进行数据库查询判断信息是否合法，并得到被请求方对应的socket连接，然后使用`sendFriendRequest`由服务器主动向被请求方转发好友请求
    - 发送好友申请的结果流程与申请添加好友一致，函数为`makeFriendsCheck`和`sendResquestRes`
    - 删除好友`deleteFriend`时，用户向服务器发出请求，服务器通过`deleteFriend`处理数据库中的好友关系表，实现对数据的更新，并返回给用户操作状态
    - 发送图片和文件时，使用Socket的文件输入输出流`FileInputStream`和`FileOutputStream`，进行字节流传输，以二进制流的方式进行传输

#### 3.1.4 **`Tuple.java`**

- 重写java的Tuple类，以实现服务器某些功能函数要求的多返回值情况

### 3.2 客户端技术细节

#### 3.2.1**`NetService.kt`**

- 该包下存放登录、注册等各种不同请求方法，和存放**`socket`**
- 该文件下存放所有网络请求相关的方法，其中包括
  - **`loginService`**，进行登录请求同时接收服务器的回复，如“已经注册”“注册成功”“已经在线”“密码错误”“登出成功”等信息
  - **`logoutService`**登出请求同时接收服务器的回复
  - **`regiService`**注册请求同时接收服务器的回复
  - **`friendsService`**接收好友列表同时接收服务器的回复
  - **`deleteService`**删除好友同时接收服务器的回复
  - **`makefriendsService`**添加好友同时接收服务器的回复
  - **`confirmRequestService`**确认好友信息同时接收服务器的回复
  - **`sendPictureService`**发送照片同时接收服务器的回复
  - **`sendMessageService`**发送消息同时接收服务器的回答

#### 3.2.2 `LoginBean.kt`

- 在该包下保存用于保存服务器请求的数据类
- 命名统一为`XXBean`

#### 3.2.3`Items.kt`

- 在该包下保存`RecyclerView`解耦之后的各种`item`
- 使用`RecyclerDSL`

#### 3.2.4`UISupport.kt`

- 用于制作**沉浸式状态栏**

#### 3.2.5`TabFragment`

- 提供了管理三个页面的三个方法：
  - 管理聊天界面：`setTalkList`
  - 管理好友界面：`setFriendList`
  - 管理设置：`setMineList`

#### 3.2.6`ItemAdapter`

- 用于管理聊天界面的`RecyclerView`的消息展示
- 通过`addItem`方法直接添加子项

#### 3.2.7`LoginActivity`

- 登陆界面，监听两个`EditText`和一个`LoginButton`

#### 3.2.8`MainActivity`

- 应用的总界面，有三个`Fragment`，分别为消息列表，好友列表和个人信息
- 使用`Handler`进行回调，进行线程间通信
- 外层是`Viewpager`，可以轻松实现页面间滑动

#### 3.2.9`TalkActivity`

- 聊天界面
- 点九图实现气泡
- 使用`Handler`进行回调
- 可以发送消息、图片、文件
- 增加文件选择器，可从本地选择文件

#### 3.2.10 `MVP架构`

- 对数据层（Model）、处理层（Presenter）和交互层（View）进行解耦
- 便于维护和根据特定需求进行修改

### 3.3 数据库技术细节

数据库中采用sql语句建立两个表，分别存储用户信息及用户好友关系。

```sql
CREATE TABLE IF NOT EXISTS UserData(
    userName VARCHAR PRIMARY KEY,	--用户名，作为主键
    ID INTEGER,						--用户id
    passWord VARCHAR,				--密码
    isOnline INTEGER,				--用户在线状态
    token INTEGER);					--用于身份验证的token

CREATE TABLE IF NOT EXISTS UserFriends(
    userName VARCHAR,				--用户名
    friendName VARCHAR,				--用户的好友名
    ID INTEGER PRIMARY KEY);		--用户id
    
-- 主键的意义在于快速检索每个用户对应的socket
```

其中userName存储用户名， ID作为与每个用户对应的Socket通信的标识，isOnline存储用户的在线离线状态，便于服务器在收到消息和好友申请时的判断和处理。



## 4. 难点重点回顾(以下代码均为示意，并非源代码)

### 4.1 服务器端

- 最早时未使用统一的`Server`和 `clientMap`管理Socket连接，导致各个用户的连接之间出现数据相互影响的情况，后通过引入多线程处理消息和Map统一管理来解决这一问题

```java
public static Map<Integer, Socket> userMap = new HashMap<>();
try{
    client = this.server.accept();
    new Thread(new ServerThread(client)).start();
} catch (IOException e) {
    e.printStackTrace();
}
```

- 由于TCP短连接的问题，导致连接不稳定，经常意外中断，后通过持续监听的心跳机制实现TCP长连接解决了该问题

```java
while(true){
	assert server != null;
    client = this.server.accept();
}
```

- 转发消息时经常误判需要被发送的socket并出现`wirterror`，后来通过封装为函数，通过传参的方式确定socket和报文格式解决

```java
String commonMSG = "HTTP/1.1 %code %msg\r\n" +
						"Content-Type: application/json;charset=utf-8\r\n" +
                        "Connection: keep-alive\r\n" +
                        "Content-Length: %type_body\r\n\r\n";
                        
private void sendInfos(Socket infoClient, String json) {
	try {
		String res = json + "\n";
        OutputStream out = client.getOutputStream();
        out.write(res.getBytes());
        out.flush();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

## 5. 完整版接口文档

#### 1. 返回报文的body组成

每次服务器返回的报文body都会包含以下信息：

```json
{  “code”:code,  “msg”:”msg”,  “type”:”type” }  
```

其中code表示请求的结果码，msg表示返回的具体信息，type表示请求的种类

#### 2. 用户注册

接口路径为 `ip/register`

请求报文的body格式：

```json
{  “username”:”name”,  “pwd”:”password” }  
```

返回报文的body格式：

```
{  “code”:0,  “msg”:”msg”,  “type”:”register” } 
```

返回的code及msg：

| code |     msg      |
| :--: | :----------: |
|  0   |   注册成功   |
|  -1  | 重复的用户名 |

#### 3. 用户登陆

接口路径为`ip/login`

请求报文的body格式：

```json
{  “username”:”name”,  “pwd”:”password” }  
```

返回的body格式：

```
{  “code”:0,  “msg”:”msg”,  “type”:”login”,  “token”:12345 }  
```

code及msg：

| code |      msg       |
| :--: | :------------: |
|  0   |       OK       |
|  1   |    密码错误    |
|  2   |   已经在线了   |
|  -1  | 该用户尚未注册 |

#### 4. 用户登出

路径`ip/logout`

请求报文：

```
{  “userName”:”name”,  “token”:12345 }  
```

返回报文：

```
{  “code”:0,  “msg”:”msg”,  “type”:” offline” }  
```

code及msg：

| code |    msg     |
| :--: | :--------: |
|  0   |     OK     |
|  1   | 已经离线了 |
|  2   | token无效  |
|  -1  | 用户不存在 |

#### 5. 获取好友列表

路径`ip/friends`

请求报文：

```
{  “username”:”name”,  “token”:12345 }  
```

返回报文：

```
{  “code”:0,  “msg”:”msg”,  “type”:” friends”,  “friends”:[“friend1”,”friend2”] } 
-- friends是一个jsonArray的数组
```

code及msg：

| code |     msg      |
| :--: | :----------: |
|  0   |      OK      |
|  1   |  token无效   |
|  -1  | 用户尚未注册 |

#### 6. 发送加好友请求

路径`ip/makefriends`

请求报文：

```json
{  “username”:”name”,  “token”:12345,  “newfriend”:”newfriend” }  
```

返回报文：

```json
{  “code”:0,  “msg”:”msg”,  “type”:” makefriend”, }  
```

code及msg：

| code |      msg       |
| :--: | :------------: |
|  0   |  请求发送成功  |
|  1   |   token 无效   |
|  2   |  已经是好友了  |
|  3   | 目标用户不存在 |
|  -1  |  用户尚未注册  |
|  -2  | 目标用户不在线 |

请求只有在对方在线时在允许发送

#### 7. 回复好友请求

路径`ip/result`

请求报文：

```json
{  “from”:”A”,  “to”:”B”,  “token”:12345,  “status”:1 }
-- from 代表当前用户的用户名
-- to 代表接受方的用户名
-- status 代表回复的结果
-- token用于身份验证，保证安全性
```

返回报文：

```
{ “code”:0,  “msg”:”msg”,  “type”:” result” }  
```

code及msg;

| code |      msg       |
| :--: | :------------: |
|  0   |      成功      |
|  1   |   token无效    |
|  2   |  已经是好友了  |
|  3   | 目标用户不存在 |
|  -1  |  用户尚未注册  |
|  -2  | 目标用户不在线 |

#### 8. 服务器端转发好友请求

报文格式：

```
{  “code”:0,  “msg”:”msg”,  “type”:” friendrequest”, “from”:”from” } 
```

服务器收到用户发出的好友申请时，会主动向被申请方发出该消息

#### 9. 服务器端转发好友请求的回复结果

报文格式：

```
{  “code”:0,  “msg”:”msg”,  “type”:” makefriendres” }  
```

服务器收到被申请方的回复时，会主动向申请方发出该消息

#### 10. 发送消息

路径`ip/sendmsg`

报文格式：

```json
{  “username”:”name”,  “token”:12345, “to”:”to”, “msg”:”data” , "time":"time"}
```

返回格式：

```
{  “code”:0,  “msg”:”msg”,  “type”:” sendmsgres” }
```

code及msg：

| code |      msg       |
| :--: | :------------: |
|  0   |    发送成功    |
|  1   |   token无效    |
|  2   |  没有这个好友  |
|  -1  | 该用户尚未注册 |
|  -2  | 当前用户不在线 |

#### 11. 服务器转发消息

报文格式：

```json
{  “code”:0,  “msg”:”msg”,  “type”:” msg”, “from”:”from” , "time":"time"}  
```

当服务器收到客户端发送的消息时，该 json 由服务器主动发出。其中 code 总是 0，from 表示
发送方用户名，msg 为消息本体。 

#### 12. 发送文件

路径`ip/sendFile`

报文格式

```json
{  “code”:0,  “msg”:”msg”,  “type”:” msg”, “from”:”from” , "to": "to", "time":"time"， "filename": "filename"}  
- msg 发送文件转码的字节流
- filename 发送文件名
```

返回格式

```json
{  “code”:0,  “msg”:”msg”,  “type”:” sendfile” }  
```

| code |      msg       |
| :--: | :------------: |
|  0   |    发送成功    |
|  1   |   token无效    |
|  2   |  没有这个好友  |
|  -1  | 该用户尚未注册 |
|  -2  | 当前用户不在线 |

#### 13. 服务器主动转发文件

发送报文

```json
{  “code”:0,  “msg”:”msg”,  “type”:” msg”, “from”:”from” , "time":"time", "filename": "filename"}  
- msg 发送文件转码的字节流
- filename 发送文件名
```

#### 14. 删除好友

路径`ip/delete`

报文格式

```sql
{  “username”:”name”,  “token”:12345,  “friendname”:”friendname” }
```

返回格式

```sql
{  “code”:0,  “msg”:”msg”,  “type”:” delete” }
```

| code |    msg    |
| :--: | :-------: |
|  0   | 删除成功  |
|  1   | token无效 |
