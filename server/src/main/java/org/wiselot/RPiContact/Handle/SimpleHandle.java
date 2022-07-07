package org.wiselot.RPiContact.Handle;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class SimpleHandle {

    protected static ExecutorService threadPool;
    protected int defaultPort = -1;

    public SimpleHandle(ThreadPoolExecutor pool){
        threadPool = pool;
    }
    public SimpleHandle(ThreadPoolExecutor pool,int defaultPort){
        threadPool = pool;
        defaultPort = defaultPort;
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