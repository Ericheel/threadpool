package threadpool;

public interface RejectPolicy {

    void reject(Runnable task, ThreadPoolExecutor threadPoolExecutor);
}
