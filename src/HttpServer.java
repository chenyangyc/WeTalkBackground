
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    ServerSocket server;               //本服务器
    ServerSocket infoServer;           // 处理消息轮询
    Socket client;                     //发请求的客户端
    Socket infoClient;

    String commonMSG = "HTTP/1.1 %code %msg\r\n" +
						"Content-Type: application/json;charset=utf-8\r\n" +
                        "Connection: keep-alive\r\n" +
                        "Content-Length: %type_body\r\n\r\n";
    //构造函数
    HttpServer(){
        try {
            this.server = new ServerSocket(12000);
            this.infoServer = new ServerSocket(12333);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //main 函数
    public static void main(String[] args){
        HttpServer myServer = new HttpServer();
//        myServer.initDatabase();
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
                new Thread(new ServerThread(client, infoClient)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


