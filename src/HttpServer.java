import DataBeans.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HttpServer {
    Map<Integer, ServerSocket> userMap;
    Connection connection;
    Statement statement;
    ServerSocket server;               //本服务器
    Socket client;                     //发请求的客户端
    String commonMSG = "HTTP/1.1 %code %msg\r\n" +
						"Content-Type: application/json;charset=utf-8\r\n" +
                        "Connection: keep-alive\r\n" +
                        "Content-Length: %type_body\r\n\r\n";
    //构造函数
    HttpServer(){
        try {
            this.server = new ServerSocket(12000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //main 函数
    public static void main(String[] args){
        HttpServer myServer = new HttpServer();
        myServer.initDatabase();
        myServer.begin();
    }



    //在此接受客户端的请求，并作响应
    private void begin() {
        String requestType;
        String requestPath;
        String requestBody = "";
        while(true){
            try {
                //开始监听
                client = this.server.accept();
                System.out.println("One client has connected to this server port: " + client.getLocalPort());
                StringBuffer header = new StringBuffer();
                StringBuffer body = new StringBuffer();

                Scanner scanner = new Scanner(System.in);
                String httpHeader, httpRequest;
                while(!(httpHeader = scanner.nextLine()).isEmpty()) {
                    header.append(httpHeader);
                }
                while(!(httpRequest = scanner.nextLine()).isEmpty()) {
                    body.append(httpRequest);
                }
                httpHeader = header.toString();
                httpRequest = body.toString();

                requestType = httpHeader.split(" ")[0];
                requestPath = httpHeader.split(" ")[1].substring(1);
                requestBody = httpRequest;
                //打印报文，便于检查
                System.out.println(requestType);
                System.out.println(requestPath);
                System.out.println(requestBody);
                if(requestType.equals("POST"))
                    post(server, requestPath, requestBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void post(ServerSocket server, String path, String body){
        if(path.equals("login")) {
            int id = 0;
            LoginRequestBean loginRequestBean = new LoginRequestBean();
            LoginResponseBean responseBean = new LoginResponseBean();
            loginRequestBean.convertFromJson(body);
            int c = login(loginRequestBean.getUserName(), loginRequestBean.getPwd(), responseBean.token, id);

            responseBean.setCode(c);
            responseBean.setType("login");
            if(c == 0) {
                responseBean.setMsg("OK");
                addOnlineUser(id, server);  //添加这个socket
            }
            else if(c == 1) responseBean.setMsg("密码错误");
            else if(c == -1) responseBean.setMsg("此用户尚未注册");
            else if(c == 2) {
                responseBean.setMsg("该用户已经在线");
//                gankIDFromTree(id);//去除之前的socket
//                insOnlineUser(id, socket);
            }
            loginRequestBean.convertFromJson(body);
            System.out.println(responseBean.convertFromObject());
            response(responseBean.convertFromObject());
        }
        else if(path.equals("register")) {
            RegisterRequestBean registerRequestBean = new RegisterRequestBean();
            CommonResponseBean commonResponseBean = new CommonResponseBean();
            registerRequestBean.convertFromJson(body);
            int c = regist(registerRequestBean.getUserName(), registerRequestBean.getPwd());
            commonResponseBean.setCode(c);
            commonResponseBean.setType("regist");
            if(c == -1) commonResponseBean.setMsg("重复的用户名");
            else commonResponseBean.setMsg("注册成功");
            System.out.println(commonResponseBean.convertFromObject());
            response(commonResponseBean.convertFromObject());
        }
        else if(path.equals("logout")) {
            CommonRequestBean  logoutRequest = new CommonRequestBean();
            CommonResponseBean logoutResponse = new CommonResponseBean();
            logoutRequest.convertFromJson(body);
            int c = logout(logoutRequest.getUserName(), logoutRequest.getToken());
            logoutResponse.setCode(c);
            logoutResponse.setType("logout");
            if(c == 1) logoutResponse.setMsg("已经离线");
            else if(c == 2) logoutResponse.setMsg("token无效");
            else if(c == -1)    logoutResponse.setMsg("用户不存在");
            response(logoutResponse.convertFromObject());
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(path.equals("friends")) {
            CommonRequestBean friendRequest = new CommonRequestBean();
            FriendsResponseBean friendsResponse = new FriendsResponseBean();
            friendRequest.convertFromJson(body);
            int c = getFriends(friendRequest.getUserName(), friendRequest.getToken(), friendsResponse.friendsList);
            if(c == 0) friendsResponse.setMsg("OK");
            else if(c == -1) friendsResponse.setMsg("用户不存在");
            else if(c == 1) friendsResponse.setMsg("token无效");
            response(friendsResponse.convertFromObject());
        }

        else if (path.equals("makefriends")) {
            MakeFriendsRequestBean makeFriendsRequest = new MakeFriendsRequestBean();
            CommonResponseBean makeFriendsResponse = new CommonResponseBean();
            makeFriendsRequest.convertFromJson(body);
            int c = makeFriends(makeFriendsRequest.getUserName(), makeFriendsRequest.getNewFriend(),makeFriendsRequest.getToken());
            makeFriendsResponse.setCode(c);
            makeFriendsResponse.setType("makefriend");
            if(c == 0) makeFriendsResponse.setMsg("发送请求成功");
            else if(c == -1) makeFriendsResponse.setMsg("用户不存在");
            else if(c == 1) makeFriendsResponse.setMsg("token无效");
            else if(c == 2) makeFriendsResponse.setMsg("已经是好友了");
            else if(c == 3) makeFriendsResponse.setMsg("目标用户不存在");
            else if(c == -2) makeFriendsResponse.setMsg("目标用户不在线");
            response(makeFriendsResponse.convertFromObject());
        }

        else if(path.equals("result")) {
            ResultRequestBean resultRequest = new ResultRequestBean();
            CommonResponseBean commonResponse = new CommonResponseBean();
            resultRequest.convertFromJson(body);
            int c = makeFriendsCheck(resultRequest.getUserName(), resultRequest.getTo(), resultRequest.getToken(), resultRequest.getStatus());
            commonResponse.setCode(c);
            commonResponse.setType("result");
            if(c == 0) {
                commonResponse.setMsg("发送结果成功");
                if(resultRequest.getStatus() != 0) addFriend(resultRequest.getFrom(), resultRequest.getTo());
            }
            else if(c == -1) commonResponse.setMsg("用户不存在");
            else if(c == 1) commonResponse.setMsg("token无效");
            else if(c == 2) commonResponse.setMsg("已经是好友了");
            else if(c == 3) commonResponse.setMsg("目标用户不存在");
            else if(c == -2) commonResponse.setMsg("目标用户不在线");
            response(commonResponse.convertFromObject());
        }

        else if(path.equals("sendmsg")) {
            SendMsgRequestBean request = new SendMsgRequestBean();
            CommonResponseBean response = new CommonResponseBean();
            request.convertFromJson(body);
            int c = sendToFriend(request.getUserName(), request.getTo(), request.getToken(), request.getMsg());
            response.setCode(c);
            response.setType("sendmsgres");
            if(c == 0) response.setMsg("发送成功");
            else if(c == -1) response.setMsg("没有这个用户");
            else if(c == 1) response.setMsg("token无效");
            else if(c == 2) response.setMsg("没有这个好友");
            else if(c == -2) response.setMsg("当前用户不在线");
            response(response.convertFromObject());
        }
    }

    private void response(String json) {
        try {
            client = this.server.accept();
            String result = commonMSG.replaceAll("%code", "200").replaceAll("%msg", "OK")
                    .replaceAll("%type_body", json) + json;
            OutputStream out = client.getOutputStream();
            out.write(result.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int regist(String userName, String pwd) {
        String sql = "SELECT * FROM UserData WHERE userdata.username = '"+userName+"'";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next())    return -1;  // 已注册，冲突
            else {
                resultSet = statement.executeQuery("SELECT max(ID) FROM UserData");
                resultSet.first();
                int id = resultSet.getInt(1) + 1;
                statement.executeUpdate("INSERT INTO UserData VALUES('"+userName+"', '"+id+"', '"+pwd+"',0,0)");
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //login返回0表示ok，返回-1表示无用户，返回1表示密码错误，2表示已经上线了
    private int login(String userName, String pwd, int token, int id) {
        char[] name = userName.toCharArray();
        String sql = "SELECT * FROM UserData WHERE userdata.username = '"+userName+"'";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()) {
                if(!resultSet.getString(3).equals(pwd)) return 1;
                else if(resultSet.getInt(4) == 1) {
                    id = resultSet.getInt(2);
                    return 2;
                } else {
                    id = resultSet.getInt(2);
                    token = (int)(Math.random() * 100);
                    String loginSql = "UPDATE UserData SET" +
                                        "isOnline = 1, token = '"+token+"'" +
                                        "WHERE userName = '"+userName+"')";
                    return 0;
                }
            } else  return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int logout(String userName, int token) {
        int id;
        String sql = "SELECT * FROM UserData WHERE userdata.username = '"+userName+"'";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                if(resultSet.getInt(4) == 0)    return 1;   //已经离线
                else if(resultSet.getInt(5) != token)   return 2;   //token conflicts
                id = resultSet.getInt(2);
            } else return -1;   //查无此人
            statement.executeUpdate("UPDATE UserData SET isOnline = 0 WHERE userName = '"+userName+"'");
            System.out.println("User" + id + "offline");
            userMap.remove(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getFriends(String userName, int token, List<String> friendsList) {
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM UserData WHERE userdata.username = '"+userName+"'");
            if(resultSet.next()) {
                if(resultSet.getInt(5) != token)    return 1;  //wrong token
                ResultSet resultSet1 = statement.executeQuery("SELECT friendName FROM UserFriends WHERE userName = '"+userName+"'");
                while (resultSet1.next()) {
                    friendsList.add(resultSet1.getNString(1));
                }
                return 0;
            } else return -1;   //no such user
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int makeFriends(String userName, String newFriend, int token){
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM UserData WHERE userName = '"+userName+"'");
            if(resultSet.next()) {
                if(resultSet.getInt(5) != token)    return 1;  //wrong token
                ResultSet resultSet1 = statement.executeQuery("SELECT * FROM UserFriends WHERE userName = '"+userName+"' AND friendName = '"+newFriend+"'");
                if(resultSet1.next())   return 2;   //already friends
                ResultSet resultSet2 = statement.executeQuery("SELECT ID FROM UserData WHERE userName = '"+newFriend+"'");
                if(resultSet2.next()) {
                    ServerSocket ss = userMap.get(resultSet2.getInt(2));
                    if(ss == null)  return -2;  //offline user
                    sendFriendResquest(ss, userName);
                    return 0;
                }else return 3; //already friends
            }else return -1;    //no such user
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void sendFriendResquest(ServerSocket s, String userName) {
        MakeFriendsResponseBean makeFriendsResponse = new MakeFriendsResponseBean();
        makeFriendsResponse.setCode(0);
        makeFriendsResponse.setMsg("好友请求");
        makeFriendsResponse.setType("friendrequest");
        makeFriendsResponse.from = userName;
        response(makeFriendsResponse.convertFromObject());
    }

    private int makeFriendsCheck(String userName, String newFriend, int token, int status) {
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM UserData WHERE userName = '"+userName+"'");
            if(resultSet.next()) {
                if(resultSet.getInt(5) != token)    return 1;  //wrong token
                ResultSet resultSet1 = statement.executeQuery("SELECT * FROM UserFriends WHERE userName = '"+userName+"' AND friendName = '"+newFriend+"'");
                if(resultSet1.next())   return 2;
                ResultSet resultSet2 = statement.executeQuery("SELECT ID FROM UserData WHERE userName = '"+newFriend+"'");
                if(resultSet2.next()) {
                    ServerSocket ss = userMap.get(resultSet2.getInt(2));
                    if(ss == null)  return -2;  //offline user
                    sendResquestRes(ss, userName, status);
                    return 0;
                }else return 3;
            }else return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void sendResquestRes(ServerSocket serverSocket, String from, int status) {
        CommonResponseBean commonResponse = new CommonResponseBean();
        commonResponse.setCode(status);
        commonResponse.setMsg(from);
        commonResponse.setType("makefriendsres");
        response(commonResponse.convertFromObject());

    }

    private void addFriend(String from, String to) {
        try {
            ResultSet resultSet = statement.executeQuery("SELECT MAX(ID) FROM UserFriends");
            resultSet.first();
            int id = resultSet.getInt(2);
            statement.executeUpdate("INSERT INTO UserFriends VALUES('"+from+"','"+to+"','"+id + 1+"')");
            statement.executeUpdate("INSERT INTO UserFriends VALUES('"+to+"','"+from+"','"+id + 2 +"')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int sendToFriend(String userName, String to, int token, String msg) {
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM UserData WHERE userName = '"+userName+"'");
            if(resultSet.next()) {
                if(resultSet.getInt(5) != token)    return 1;  //wrong token

                ResultSet resultSet1 = statement.executeQuery("SELECT * FROM UserFriends WHERE userName = '"+userName+"' AND friendName = '"+to+"'");
                if(resultSet1.next()) {
                    ResultSet resultSet2 = statement.executeQuery("SELECT ID FROM UserData WHERE userName = '"+to+"'");
                    resultSet2.first();
                    ServerSocket ss = userMap.get(resultSet2.getInt(2));
                    if(ss == null)  return -2;  //offline user
                    sendTo(ss, userName, msg);
                    return 0;
                }else return 2;
            }else return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void sendTo(ServerSocket serverSocket, String from, String msg) {
        MakeFriendsResponseBean response = new MakeFriendsResponseBean();
        response.setCode(0);
        response.setMsg(msg);
        response.setType("msg");
        response.from = from;
        response(response.convertFromObject());
    }

    private void addOnlineUser(int id, ServerSocket serverSocket) {
        System.out.println("新登入用户id：" + id);
        System.out.println("当前用户数量: " + userMap.size() + 1);
        userMap.put(id, serverSocket);
    }












    private void initDatabase(){
        try{
            String url="jdbc:postgresql://127.0.0.1:5432/postgres";
            String user="postgres";
            String password = "yc010106";
            Class.forName("org.postgresql.Driver");
            connection= DriverManager.getConnection(url, user, password);
            System.out.println("是否成功连接pg数据库" + connection);

            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            String sqlUser = "CREATE TABLE IF NOT EXISTS UserData" + //用户信息表
                                "(userName CHAR(20) PRIMARY KEY," +
                                "ID INTEGER," +
                                "passWord CHAR(20)," +
                                "isOnline INTEGER," +
                                "token INTEGER)";
            String sqlRlsp = "CREATE TABLE IF NOT EXISTS UserFriends" +//好友关系表
                                "(userName CHAR(20)," +
                                "friendName CHAR(20)," +
                                "ID INTEGER PRIMARY KEY)";

            statement.executeUpdate(sqlUser);
            statement.executeUpdate(sqlRlsp);

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}


