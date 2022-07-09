package org.wiselot.RPiContact.Main;

import org.jetbrains.annotations.NotNull;
import org.wiselot.RPiContact.DataPool.AccountDataBase;
import org.wiselot.RPiContact.DataPool.MessageDataBase;
import org.wiselot.RPiContact.Handle.AccountHandle;
import org.wiselot.RPiContact.Handle.MessageHandle;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class test {
    /*
    * ThreadPoolExecutor包含了7个核心参数，参数含义：

    corePoolSize：核心线程池的大小
    maximumPoolSize：最大线程池的大小
    keepAliveTime：当线程池中线程数大于corePoolSize，并且没有可执行任务时大于corePoolSize那部分线程的存活时间
    unit：keepAliveTime的时间单位
    workQueue：用来暂时保存任务的工作队列
    threadFactory：线程工厂提供线程的创建方式，默认使用Executors.defaultThreadFactory()
    handler：当线程池所处理的任务数超过其承载容量或关闭后继续有任务提交时，所调用的拒绝策略
    * 具体参考 https://blog.csdn.net/A5865459/article/details/124276571
    * */
    public static void main(String[] args)
    {

        AccountDataBase accountDataBase = new AccountDataBase("com.mysql.cj.jdbc.Driver","RPiAccount");
        accountDataBase.connect("jdbc:mysql://116.62.35.30:3306/Account?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                ,"remote_op","k7omain%");
        AccountHandle accountHandle =  new AccountHandle(accountDataBase,
                new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                        5,
                        6,
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<Runnable>(2),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy()),9990);
        accountHandle.start();

        MessageDataBase md = new MessageDataBase("com.mysql.cj.jdbc.Driver","RPiMessage");
        md.connect("jdbc:mysql://116.62.35.30:3306/Message?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                ,"remote_op","k7omain%");
        MessageHandle messageHandle = new MessageHandle(md,
                new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                        5,
                        6,
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<Runnable>(2),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy()),9991);
        messageHandle.start();
        /*
        AccountDataBase ad = new AccountDataBase("com.mysql.cj.jdbc.Driver","RPiAccount");
        ad.connect("jdbc:mysql://116.62.35.30:3306/Account?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                ,"remote_op","k7omain%");
        try {
            MessageDataBase.Message message = md.getMessage(UUID.fromString("6403de13-fdb5-11ec-998e-00163e193075"));
            displayMsg(message,ad);
            ArrayList<MessageDataBase.Message> msg = md.getMessage(ad.getAccount(UUID.fromString("2fcc06c9-f8db-11ec-a582-00163e193075")),false);
            for(MessageDataBase.Message m : msg){
                displayMsg(m,ad);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

         */
    }

    public static void displayMsg(MessageDataBase.@NotNull Message message, @NotNull AccountDataBase ad) throws SQLException {
        System.out.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(message.getSendDate().getTime()) + "]" +
                ad.getAccount(message.getSendUUID()).getName() + " to " +
                ad.getAccount(message.getGetUUID()).getName() + " :" +
                message.getText());
    }
}
