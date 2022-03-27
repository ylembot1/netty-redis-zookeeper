package com.crazymakercircle.coccurent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JavaFutureDemo {

    public static final int SLEEP_GAP = 500;
    public static String getCurThreadName() {
        return Thread.currentThread().getName();
    }

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

    static class WashJob implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            try {
                System.out.println("洗茶壶");
                System.out.println("洗茶杯");
                System.out.println("拿茶叶");
                Thread.sleep(SLEEP_GAP + 30000);
                System.out.println("洗完了");
            } catch (InterruptedException e) {
                System.out.println("清洗时发生异常");
                return false;
            }
            System.out.println("清洗结束！！");
            return true;
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

    public static void main(String[] args) {
        Callable<Boolean> hJob = new HotWaterJob();
        FutureTask<Boolean> hTask = new FutureTask<>(hJob);
        Thread hThread = new Thread(hTask, "** 烧水-Thread");

        Callable<Boolean> wJob = new WashJob();
        FutureTask<Boolean> wTask = new FutureTask<>(wJob);
        Thread wThread = new Thread(wTask, "$$ 清洗-Thread");

        hThread.start();
        wThread.start();

        Thread.currentThread().setName("主线程");
        try {
            boolean waterOk = hTask.get(10, TimeUnit.SECONDS);
            System.out.println("waterOk?" + waterOk);
            boolean cupOk = wTask.get(10, TimeUnit.SECONDS);
            System.out.println("cupOk?" + cupOk);

            drinkTea(waterOk, cupOk);
        } catch (InterruptedException e) {
            System.out.println(getCurThreadName() + "发生异常中断");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("准备过程太久了，不喝了！");
            e.printStackTrace();
        }
        System.out.println(getCurThreadName() + " 运行结束");
    }
}
