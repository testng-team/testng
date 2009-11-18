package org.testng.internal.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An Executor that launches tasks per batches.
 */
public class GroupThreadPoolExecutor extends ThreadPoolExecutor {

    private int m_index = 0;
    Map<Integer, List<Runnable>> m_runnables = new HashMap<Integer, List<Runnable>>();
    private List<Runnable> m_activeRunnables = new ArrayList<Runnable>();

    public GroupThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
            TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

//    public void setRunnables(List<List<Runnable>> runnables) {
//        m_runnables = runnables;
//    }

    public void run() {
        runRunnablesAtIndex(m_index++);
    }
    
    private void runRunnablesAtIndex(int index) {
        List<Runnable> runnables = m_runnables.get(index);
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
        if (m_activeRunnables.isEmpty() && m_index < m_runnables.size()) {
            runRunnablesAtIndex(m_index++);
        }
    }

    private void ppp(String string) {
        System.out.println("[GroupThreadPoolExecutor] " + Thread.currentThread().getId() + " "
            + string);
    }

    public void addRunnable(int i, Runnable runnable) {
        List<Runnable> l = m_runnables.get(i);
        if (l == null) {
            l = new ArrayList<Runnable>();
            m_runnables.put(i, l);
        }
        l.add(runnable);
    }
}
