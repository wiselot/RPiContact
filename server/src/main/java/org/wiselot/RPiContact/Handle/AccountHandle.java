package org.wiselot.RPiContact.Handle;

import org.apache.log4j.Logger;
import org.wiselot.RPiContact.DataPool.AccountDataBase;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

public class AccountHandle extends SimpleHandle implements Runnable{
    private static AccountDataBase accountDataBase;

    private Map<Socket, AccountDataBase.Account> connections;

    public AccountHandle(AccountDataBase db,ThreadPoolExecutor pool,int port){
        super(pool,port);
        threadPool = pool;
        accountDataBase = db;
        logger = Logger.getLogger(AccountHandle.class);
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            Socket socket;
            DataInterpreter dataInterpreter;
            while(true) {
                socket = serverSocket.accept();
                logger.info(socket.getRemoteSocketAddress() + " connect to the handle !");
                dataInterpreter = new DataInterpreter(socket, this);
                dataInterpreter.start();
                threadPool.execute(dataInterpreter);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class DataInterpreter extends SimpleHandle.RunnableNextHandle{

        private int flag = 0;

        private AccountHandle accountHandle;

        public DataInterpreter(Socket socket,AccountHandle ah)
        {
            super(socket);
            this.accountHandle = ah;
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
                    logger.info("Get command \"" + msg + "\" from " + socket.getRemoteSocketAddress());
                    if (msg.matches("login\s+in\s+\\w+\s+.+")) {
                        get = msg.split(" ");
                        name = get[2];
                        passwd = get[3];
                        al = accountDataBase.getAccount(name);
                        for (AccountDataBase.Account account : al) {
                            if (account.getPasswd().equals(passwd) &&
                                    (!connections.containsKey(socket))&&(!connections.containsValue(account))) {
                                outputStream.write("true".getBytes(StandardCharsets.UTF_8));
                                logger.info(socket.getRemoteSocketAddress() + " login in as account " +
                                        name + ",UUID=\"" + account.getUuid());
                                flag = 1;
                                //accountHandle.connections.add(new AccountConnection(account,socket));
                                connections.put(socket,account);
                                break;
                            }
                        }
                        if(flag!=1) {
                            outputStream.write("false".getBytes(StandardCharsets.UTF_8));
                            logger.fatal(socket.getRemoteSocketAddress() + " failed to login in as account " +
                                    name);
                        }
                    }
                    else if(msg.matches("login\s+out")){
                        if(connections.containsKey(socket)) {
                            logger.info(socket.getRemoteSocketAddress() + " login out !");
                        }
                        else{
                            logger.fatal(socket.getRemoteSocketAddress() + " not login in!");
                        }
                        flag = -1;
                    }
                    else if(msg.contains("message")&&flag==1){
                        // 已登录准备发送信息

                    }
                    else{
                        logger.fatal("Can't understand command from " + socket.getRemoteSocketAddress());
                    }
                }
            } catch (SQLException | IOException ex) {
                throw new RuntimeException(ex);
            }
            logger.info("End Connection with " + socket.getRemoteSocketAddress());
            this.interrupt();
        }
    }

}