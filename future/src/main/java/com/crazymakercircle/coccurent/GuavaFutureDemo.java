package com.crazymakercircle.coccurent;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuavaFutureDemo {
    public static final int SLEEP_GAP = 500;
    public static String getCurThreadName() {
        return Thread.currentThread().getName();
    }

    // 业务逻辑: 烧水
    static class HotWaterJob implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            try {
                System.out.println("洗好水壶");
                System.out.println("灌上凉水");
                System.out.println("放在火上");
                Thread.sleep(SLEEP_GAP);
                System.out.println("水开了");

            } catch (InterruptedException e) {
                System.out.println("烧水发生异常");
                return false;
            }
            System.out.println("烧水结束！！");
            return true;
        }
    }

    // 业务逻辑: 清洗
    static class WashJob implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            try {
                System.out.println("洗茶壶");
                System.out.println("洗茶杯");
                System.out.println("拿茶叶");
                Thread.sleep(SLEEP_GAP);
                System.out.println("洗完了");
            } catch (InterruptedException e) {
                System.out.println("清洗时发生异常");
                return false;
            }
            System.out.println("清洗结束！！");
            return true;
        }
    }

    static class MainJob implements Runnable {
        boolean waterOk = false;
        boolean cupOk = false;
        int gap = SLEEP_GAP / 10;

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(gap);
                    System.out.println("读书中~~~");
                } catch (InterruptedException e) {
                    System.out.println(getCurThreadName() + "发生异常被中断");
                }
                if (waterOk && cupOk) {
                    drinkTea(waterOk, cupOk);
                    break;
                }
            }
        }

        public static void drinkTea(boolean waterOk, boolean cupOk) {
            if (waterOk && cupOk) {
                System.out.println("准备事项做完了，可以泡茶喝！");
            } else if (!waterOk) {
                System.out.println("烧水出现问题, 无法泡茶！");
            } else if (!cupOk){
                System.out.println("清洗杯子出现问题, 无法泡茶！");
            }
        }
    }

    public static void main(String[] args) {
        // 创建一个新的线程实例，作为泡茶主线程
        MainJob mainJob = new MainJob();
        Thread mainThread = new Thread(mainJob);
        mainThread.setName("主线程");
        mainThread.start();

        // 烧水的业务逻辑实例
        Callable<Boolean> hotJob = new HotWaterJob();
        // 清洗的业务逻辑实例
        Callable<Boolean> washJob = new WashJob();

        // 创建java线程池
        ExecutorService jPool = Executors.newFixedThreadPool(10);
        ListeningExecutorService gPool = MoreExecutors.listeningDecorator(jPool);

        // 提交烧水的业务逻辑实例，到Guava线程池获取异步任务
        ListenableFuture<Boolean> hotFuture = gPool.submit(hotJob);
        // 绑定异步回调，烧水完成后，把喝水任务的waterOK标志设置为true
        Futures.addCallback(hotFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable Boolean r) {
                if (r) {
                    mainJob.waterOk = true;
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("烧水失败，没有茶喝了");
            }
        }, gPool);

        ListenableFuture<Boolean> washFuture = gPool.submit(washJob);
        Futures.addCallback(washFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable Boolean r) {
                if (r) {
                    mainJob.cupOk = true;
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("杯子洗不了，没有茶喝了");
            }
        }, gPool);
    }
}
