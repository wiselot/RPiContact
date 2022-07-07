package org.wiselot.RPiContact.Handle;

import org.wiselot.RPiContact.DataPool.AccountDataBase;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

public class AccountHandle extends SimpleHandle{
    private static AccountDataBase accountDataBase;

    public AccountHandle(AccountDataBase db,ThreadPoolExecutor pool){
        super(pool);
        threadPool = pool;
        accountDataBase = db;
    }
    public AccountHandle(AccountDataBase db,ThreadPoolExecutor pool,int port){
        super(pool,port);
        threadPool = pool;
        accountDataBase = db;
    }

    public void startPieceHandle(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = serverSocket.accept();
        System.out.println(socket.getRemoteSocketAddress());
        DataInterpreter dataInterpreter = new DataInterpreter(socket);
        dataInterpreter.start();
        threadPool.execute(dataInterpreter);

    }

    public void startPieceHandle() throws IOException {
        startPieceHandle(defaultPort);
    }

    public static class DataInterpreter extends SimpleHandle.RunnableNextHandle{

        private int flag = 0;
        AccountDataBase.Account loginAccount;

        public DataInterpreter(Socket socket) {
            super(socket);
        }
        @Override
        public void run(){
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String msg;
                String[] get;
                String name;
                String passwd;
                ArrayList<AccountDataBase.Account> al;
                while(flag>=0){
                    msg = bufferedReader.readLine();
                    System.out.println("GET COMMAND : " + msg);
                    if (msg.matches("login\s+in\s+\\w+\s+.+")) {
                        get = msg.split(" ");
                        name = get[2];
                        passwd = get[3];
                        al = accountDataBase.getAccount(name);
                        for (AccountDataBase.Account account : al) {
                            if (account.getPasswd().equals(passwd)) {
                                outputStream.write("true".getBytes(StandardCharsets.UTF_8));
                                System.out.println(socket.getRemoteSocketAddress() +
                                        "以" + name + "成功登录!");
                                flag = 1;
                                loginAccount = account;
                                break;
                            }
                        }
                        if(flag!=1) {
                            outputStream.write("false".getBytes(StandardCharsets.UTF_8));
                            System.out.println(socket.getRemoteSocketAddress() +
                                    "试图以" + name + "登录但失败了");
                        }
                    }
                    else if(msg.matches("login\s+out")){
                        System.out.println(socket.getRemoteSocketAddress() +
                                "成功登出!");
                        flag = -1;
                    }
                    else if(msg.contains("message")&&flag==1){
                        // 已登录准备发送信息

                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("与" + socket.getInetAddress() + "结束会话");
            this.interrupt();
        }
    }

}