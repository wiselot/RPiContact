package org.wiselot.RPiContact.Handle;

import org.apache.log4j.Logger;

import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class SimpleHandle extends Thread{

    protected static ExecutorService threadPool;
    protected int port = -1;

    protected ArrayList<Socket> sockets;

    protected static Logger logger;

    public SimpleHandle(ThreadPoolExecutor pool,int port){
        threadPool = pool;
        this.port = port;
    }



    public static class RunnableNextHandle extends Thread{

        protected Socket socket;

        public RunnableNextHandle(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {

        }
    }
}