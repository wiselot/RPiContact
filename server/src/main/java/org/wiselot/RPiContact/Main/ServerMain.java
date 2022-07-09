package org.wiselot.RPiContact.Main;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.wiselot.RPiContact.DataPool.AccountDataBase;
import org.wiselot.RPiContact.DataPool.DataBase;
import org.wiselot.RPiContact.DataPool.MessageDataBase;
import org.wiselot.RPiContact.Handle.AccountHandle;
import org.wiselot.RPiContact.Handle.MessageHandle;
import org.wiselot.RPiContact.Handle.SimpleHandle;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerMain {

    private static String rootDir = ".";
    private static String setDir = rootDir + "/settings" ;
    private static int corePoolSize = Runtime.getRuntime().availableProcessors();
    private static int maximumPoolSize = 5;
    private static int keepAliveTime = 6;

    private static final Set<DataBase> dataBaseSet = new HashSet<>();
    private static final Set<SimpleHandle> handleSet = new HashSet<>();
    private static Logger logger = Logger.getLogger(ServerMain.class);

    @Contract(pure = true)
    public static void main(String @NotNull [] args){
        for(String arg : args){
            /*
            usage :
            -rootDir=[path]         指定工作目录
            -setDir=[path]          指定设置目录
            -corePoolSize=[num]     默认核心线程池大小
            -maximumPoolSize=[num]  默认最大线程池大小
            -keepAliveTime=[num]    默认等待时间(ms)
             */
            int i = 0;
            try {
                i = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
            }catch (NumberFormatException ignored){

            }
            if(arg.matches("-rootDir(\s+)?=(\s+)?.+")){
                rootDir = arg.substring(arg.indexOf('=')+1);
            }
            else if(arg.matches("-setDir(\s+)?=(\s+)?.+")){
                setDir = rootDir + arg.substring(arg.indexOf('=')+1);
            }
            else if(arg.matches("-corePoolSize(\s+)?=(\s+)?\\d+")){
                corePoolSize = i;
            }
            else if(arg.matches("-maximumPoolSize(\s+)?=(\s+)?\\d+")){
                maximumPoolSize = i;
            }
            else if(arg.matches("-keepAliveTime(\s+)?=(\s+)?\\d+")){
                keepAliveTime = i / 1000;
            }
            else{
                logger.fatal("Invalid argument " + arg);
                System.exit(1);
            }
        }
        File settingsDir = new File(setDir);
        if(!settingsDir.exists()){
            settingsDir.mkdirs();
        }
        Properties sets = new Properties();
        File settings = new File(setDir + "/global.properties");
        System.out.println(settings.getPath());
        if(settings.exists()){
            try {
                InputStream inputStream = new BufferedInputStream(new FileInputStream(settings.getPath()));
                sets.load(inputStream);
                corePoolSize = Integer.parseInt(sets.getProperty("corePoolSize"));
                maximumPoolSize = Integer.parseInt(sets.getProperty("maximumPoolSize"));
                keepAliveTime = Integer.parseInt(sets.getProperty("keepAliveTime"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            try {
                settings.createNewFile();
                sets.put("corePoolSize",String.valueOf(corePoolSize));
                sets.put("maximumPoolSize",String.valueOf(maximumPoolSize));
                sets.put("keepAliveTime",String.valueOf(keepAliveTime));

                /*
                这样写太麻烦,不如用反射
                 */

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(settings.getPath()));
                sets.store(bos,"Setting from arguments");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Properties adSets = new Properties();
        Properties mdSets = new Properties();
        InputStream adis,mdis;
        AccountDataBase ad;
        MessageDataBase md;
        try {
            adis = new BufferedInputStream(new FileInputStream(setDir + "/db/Account.properties"));
            mdis = new BufferedInputStream(new FileInputStream(setDir + "/db/Message.properties"));
            adSets.load(adis);
            mdSets.load(mdis);
             ad = (AccountDataBase) new AccountDataBase(adSets.getProperty("driver"),
                    adSets.getProperty("table")).connect(adSets.getProperty("host"),
                    adSets.getProperty("user"),
                    adSets.getProperty("passwd"));
             md = (MessageDataBase) new MessageDataBase(mdSets.getProperty("driver"),
                    mdSets.getProperty("table")).connect(adSets.getProperty("host"),
                    adSets.getProperty("user"),
                    adSets.getProperty("passwd"));
            dataBaseSet.add(ad);
            dataBaseSet.add(md);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("Load " + dataBaseSet.size() + " DataBases Done!");
        AccountHandle accountHandle =  new AccountHandle(ad,
                new ThreadPoolExecutor(corePoolSize,
                        maximumPoolSize,
                        keepAliveTime,
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<Runnable>(2),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy()),0);
        MessageHandle messageHandle = new MessageHandle(md,
                new ThreadPoolExecutor(corePoolSize,
                        maximumPoolSize,
                        keepAliveTime,
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<Runnable>(2),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy()),0);
        accountHandle.start();
        messageHandle.start();
        logger.info("Load 2 Handles down!");
    }
}
