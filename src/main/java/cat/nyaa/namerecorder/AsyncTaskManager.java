package cat.nyaa.namerecorder;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncTaskManager {

    public final int limit;

    private final Plugin plugin;

    private ConcurrentLinkedQueue<Runnable> taskQueue;

    private AtomicInteger len;

    private AtomicReference<BukkitTask> running;

    public AsyncTaskManager(Plugin plugin, int limit) {
        this.limit = limit;
        this.plugin = plugin;
        this.taskQueue = new ConcurrentLinkedQueue<>();
        this.len = new AtomicInteger(0);
        this.running = new AtomicReference<>(null);
    }

    public boolean add(Runnable task) {
        if(this.len.get() < this.limit) {
            this.taskQueue.add(task);
            this.len.incrementAndGet();
            if(this.running.get() == null) {
                this.running.set(this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new InnerTask(this, this.getNextTask())));
            }
            return true;
        }
        return false;
    }

    Runnable getNextTask() {
        Runnable next = this.taskQueue.poll();
        if(next != null) {
            this.len.decrementAndGet();
        }
        return next;
    }

    Logger getLogger() {
        return this.plugin.getLogger();
    }

    void clearRunning() {
        this.running.set(null);
    }
}

class InnerTask implements Runnable {

    private final AsyncTaskManager main;
    private Runnable current;

    InnerTask(AsyncTaskManager main, Runnable current) {
        this.main = main;
        this.current = current;
    }

    @Override
    public void run() {
        while (this.current != null) {
            try {
                this.current.run();
            } catch (Exception e) {
                this.main.getLogger().log(Level.SEVERE, "task exception", e);
            }
            this.current = this.main.getNextTask();
        }
        this.main.clearRunning();
    }
}
