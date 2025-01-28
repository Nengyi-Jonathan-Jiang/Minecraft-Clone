package util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TaskManager<T, C> {
    private final C tasks;
    private final Function<C, T> take;
    private final BiConsumer<C, T> supply;
    private final Predicate<C> isEmpty;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    public TaskManager(C tasks, BiConsumer<C, T> supply, Function<C, T> take, Predicate<C> isEmpty) {
        this.tasks = tasks;
        this.supply = supply;
        this.take = take;
        this.isEmpty = isEmpty;
    }

    public C getInner() {
        return this.tasks;
    }

    public void offer(T task) {
        final ReentrantLock lock = this.lock;
        lock.lock();

        try {
            supply.accept(tasks, task);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        T result;
        try {
            while (isEmpty.test(tasks)) {
                notEmpty.await();
            }
            result = take.apply(tasks);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public void doOperation(Consumer<C> c) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        c.accept(tasks);
        lock.unlock();
    }
}
