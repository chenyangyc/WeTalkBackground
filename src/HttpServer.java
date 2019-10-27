import DataBeans.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HttpServer {

    ServerSocket server;               //本服务器
    ServerSocket infoServer;           // 处理消息轮询
    ServerSocket msgServer;             // 处理消息监听
    Socket client;                     //发请求的客户端
    Socket infoClient;
    Socket msgClient;
    public static Connection connection;
    public static Statement statement;
    public static Map<Integer, Socket> userMap = new HashMap<>();
    public static Map<Integer, Socket> infoClientMap = new HashMap<>();
    public static Map<Integer, Socket> msgClientMap = new HashMap<>();

    String commonMSG = "HTTP/1.1 %code %msg\r\n" +
						"Content-Type: application/json;charset=utf-8\r\n" +
                        "Connection: keep-alive\r\n" +
                        "Content-Length: %type_body\r\n\r\n";
    //构造函数
    HttpServer(){
        try {
            this.server = new ServerSocket(12000);
            this.infoServer = new ServerSocket(12333);
            this.msgServer = new ServerSocket(13000);
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
        while(true){
            try {
                assert server != null;
                assert infoServer != null;
                client = this.server.accept();
                infoClient = this.infoServer.accept();
                msgClient = this.msgServer.accept();
                new Thread(new ServerThread(client, infoClient, msgClient)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initDatabase(){
        try{
            String url="jdbc:postgresql://127.0.0.1:5432/postgres";
            String user="postgres";
            String password = "your code";
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
            statement.executeUpdate("UPDATE UserData SET isOnline = 0");
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}


