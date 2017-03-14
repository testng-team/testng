package org.testng.internal.thread;

import java.util.List;

/**
 * Reduced interface to mimic ThreadFactory.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 * @version $Revision$
 */
public interface IThreadFactory {
   Thread newThread(Runnable run);

   Object getThreadFactory();

   List<Thread> getThreads();
}
