package threadpool;

public class DiscardRejectPolicy implements RejectPolicy {
    @Override
    public void reject(Runnable task, ThreadPoolExecutor threadPoolExecutor) {
        System.out.println("Reject..." + Thread.currentThread().getName());
    }
}
