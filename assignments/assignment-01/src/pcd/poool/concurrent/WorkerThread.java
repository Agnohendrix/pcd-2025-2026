package pcd.poool.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WorkerThread extends Thread {

    private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public WorkerThread(int index) {
        super("game-worker-" + index);
    }

    public void submit(Runnable task) {
        if (running) {
            tasks.add(task);
        }
    }

    public void stopWorker() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                tasks.take().run();
            } catch (InterruptedException ex) {
                if (!running) {
                    return;
                }
            }
        }
    }
}