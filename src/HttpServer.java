import DataBeans.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Map;

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
        String httpRequest;
        String requestType;
        String requestPath;
        String requestBody = "";
        while(true){
            try {
                //开始监听
                client = this.server.accept();
                System.out.println("One client has connected to this server port: " + client.getLocalPort());
                BufferedReader bf = new BufferedReader(new InputStreamReader(client.getInputStream()));
                httpRequest = bf.readLine();
                System.out.println(httpRequest);    // 打印报文，便于检查
                //获取到请求类型及Path地址,要将Path第一位的 ‘/’ 去掉
                String[] data = httpRequest.split("/r/n");
                requestType = httpRequest.split(" ")[0];
                requestPath = httpRequest.split(" ")[1].substring(1);
                int i;
                for(i = 0; i < data.length; i++) if(data[i] == "") break;
                for(i = i + 1; i < data.length; i++) requestBody += data[i]; //剥离content
                System.out.println(requestPath); // 打印请求的Path， 便于检查
                if(requestType == "post")    post(server, requestPath, requestBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void post(ServerSocket server, String path, String body){
        if(path == "login") {
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
            response(responseBean.convertFromObject());
        } else if(path == "register") {
            RegisterRequestBean registerRequestBean = new RegisterRequestBean();
            CommonResponseBean commonResponseBean = new CommonResponseBean();
            registerRequestBean.convertFromJson(body);
            int c = regist(registerRequestBean.getUserName(), registerRequestBean.getPwd());
            commonResponseBean.setCode(c);
            commonResponseBean.setType("regist");
            if(c == -1) commonResponseBean.setMsg("重复的用户名");
            else commonResponseBean.setMsg("注册成功");
            response(commonResponseBean.convertFromObject());
        } else if(path == "logout") {
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
                statement.executeQuery("INSERT INTO UserData VALUES('"+userName+"', '"+id+"', '"+pwd+"',0,0)");
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
//            System.out.println(resultSet);
            if(resultSet.next()) {
                if(resultSet.getString(3) != pwd) return 1;
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
            statement.executeQuery("UPDATE UserData SET isOnline = 0 WHERE userName = '"+userName+"'");
            System.out.println("User" + id + "offline");
            userMap.remove(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void addOnlineUser(int id, ServerSocket serverSocket) {
        System.out.println("新登入用户id：" + id);
        System.out.println("当前用户数量: " + userMap.size() + 1);
        userMap.put(id, serverSocket);
    }












    private void initDatabase(){
//        Connection connection=null;
//        Statement statement =null;
        try{
            String url="jdbc:postgresql://127.0.0.1:5432/postgres";
            String user="postgres";
            String password = "yc010106";
            Class.forName("org.postgresql.Driver");
            connection= DriverManager.getConnection(url, user, password);
            System.out.println("是否成功连接pg数据库" + connection);

            statement = connection.createStatement();
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

//            statement.close();
//            connection.close();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}


