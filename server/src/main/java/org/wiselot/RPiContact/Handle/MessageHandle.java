package org.wiselot.RPiContact.Handle;

import org.apache.log4j.Logger;
import org.wiselot.RPiContact.DataPool.MessageDataBase;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

public class MessageHandle extends SimpleHandle implements Runnable{

    private static MessageDataBase messageDataBase;

    public MessageHandle(MessageDataBase md,ThreadPoolExecutor pool, int port) {
        super(pool,port);
        messageDataBase = md;
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
                System.out.println(socket.getRemoteSocketAddress());
                dataInterpreter = new DataInterpreter(socket, this);
                dataInterpreter.start();
                threadPool.execute(dataInterpreter);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class DataInterpreter extends SimpleHandle.RunnableNextHandle{

        private MessageHandle messageHandle;
        private int flag = 0;

        public DataInterpreter(Socket socket,MessageHandle messageHandle) {
            super(socket);
            this.messageHandle = messageHandle;
            logger = Logger.getLogger(MessageHandle.class);
        }

        @Override
        public void run() {
            InputStream inputStream = null;
            try {
                inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String msg;
                while(flag>=0){
                    msg = bufferedReader.readLine();
                    logger.info("Get command \"" + msg + "\" from " + socket.getRemoteSocketAddress());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
