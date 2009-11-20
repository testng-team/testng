package org.testng.internal.thread;

import org.testng.internal.MapList;
import org.testng.internal.TestMethodWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An Executor that launches tasks per batches.
 */
public class GroupThreadPoolExecutor extends ThreadPoolExecutor {
    private int m_index = 0;
    MapList<Integer, TestMethodWorker> m_runnables;
    private List<Runnable> m_activeRunnables = new ArrayList<Runnable>();
    private List<Integer> m_indices;

    public GroupThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
            TimeUnit unit, BlockingQueue<Runnable> workQueue,
            MapList<Integer, TestMethodWorker> runnables) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        m_runnables = runnables;
        m_indices = runnables.getKeys();
    }

    public void run() {
        runRunnablesAtIndex(m_indices.get(m_index++));
    }
    
    private void runRunnablesAtIndex(int index) {
        List<TestMethodWorker> runnables = m_runnables.get(index);
        ppp("Adding runnables " + runnables);
        for(Runnable r : runnables) {
            m_activeRunnables.add(r);
            execute(r);
        }
    }

    @Override
    public void afterExecute(Runnable r, Throwable t) {
        ppp("Runnable " + r + " finished");
        m_activeRunnables.remove(r);
        if (m_activeRunnables.isEmpty() && m_index < m_runnables.getSize()) {
            runRunnablesAtIndex(m_index++);
        }
    }

    private void ppp(String string) {
        System.out.println("   [GroupThreadPoolExecutor] " + Thread.currentThread().getId() + " "
            + string);
    }

//    public void addRunnable(int i, Runnable runnable) {
//        List<TestMethodWorker> l = m_runnables.get(i);
//        if (l == null) {
//            l = new ArrayList<TestMethodWorker>();
//            m_runnables.put(i, l);
//        }
//        l.add(runnable);
//    }
}
