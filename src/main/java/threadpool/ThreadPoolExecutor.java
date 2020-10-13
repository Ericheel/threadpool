package threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolExecutor implements Executor {

    /**
     * 线程池名称
     */
    private String name;

    /**
     * 核心线程数量
     */
    private int coreSize;

    /**
     * 最大线程数量
     */
    private int maxSize;

    /**
     * 任务队列
     */
    private BlockingQueue<Runnable> taskQueue;

    /**
     * 拒绝策略
     */
    private RejectPolicy rejectPolicy;

    /**
     * 线程序列号
     */
    private AtomicInteger sequence = new AtomicInteger(0);

    /**
     * 正在运行的线程数
     */
    private AtomicInteger runningCount = new AtomicInteger(0);

    public ThreadPoolExecutor(String name, int coreSize, int maxSize, BlockingQueue<Runnable> taskQueue, RejectPolicy rejectPolicy) {
        this.name = name;
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.taskQueue = taskQueue;
        this.rejectPolicy = rejectPolicy;
    }

    @Override
    public void execute(Runnable task) {
        int count = runningCount.get();

        if (count < coreSize) {
            //添加核心线程
            if (addWorker(task, true)) {
                return;
            }
        }

        //尝试入队
        if (!taskQueue.offer(task)) {
            //入队失败，添加非核心线程
            if (!addWorker(task, false)) {
                //走线程拒绝策略
                rejectPolicy.reject(task, this);
            }
        }

    }

    private boolean addWorker(Runnable newTask, boolean isCoreTask) {
        for (; ; ) {
            int count = runningCount.get();

            int size = isCoreTask ? coreSize : maxSize;
            if (count >= size) {
                return false;
            }

            //CAS成功，则创建线程
            if (runningCount.compareAndSet(count, count + 1)) {
                String threadName = (isCoreTask ? "core_" : "") + name + sequence.incrementAndGet();
                new Thread(() -> {
                    System.out.println("thread name: " + Thread.currentThread().getName());
                    Runnable task = newTask;

                    while (task != null || (task = getTask()) != null) {
                        try {
                            task.run();
                        } finally {
                            task = null;
                        }
                    }
                }, threadName).start();
                break;
            }
        }
        return true;
    }

    private Runnable getTask() {
        try {
            return taskQueue.take();
        } catch (InterruptedException e) {
            runningCount.decrementAndGet();
            return null;
        }
    }
}
