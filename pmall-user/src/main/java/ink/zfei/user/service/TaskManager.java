package ink.zfei.user.service;

import java.util.Timer;
import java.util.TimerTask;

public class TaskManager {

    private static Timer timer;


    public static void main(String[] args) throws InterruptedException {
        timer = new Timer("任务名称", true);

        timer.schedule(new MyTask(), 0, 1000 * 1); // 10秒钟执行一次

        Thread.sleep(10000000);
    }


    public static class MyTask extends TimerTask {

        @Override

        public void run() {

            System.out.println("执行任务......");

            // 这里写你要定期执行的任务

        }

    }
}
