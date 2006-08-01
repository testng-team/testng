/*
 * $Id$
 * $Date$
 */
package org.testng.internal.thread;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Class javadoc XXX
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>the_mindstorm</a>
 * @version $Revision$
 */
public class FutureResultAdapter implements IFutureResult {
   Future<?> m_future;

   public FutureResultAdapter(Future<?> future) {
      m_future = future;
   }

   public Object get() throws InterruptedException, ThreadExecutionException {
      try {
         return m_future.get();
      } catch(ExecutionException ee) {
         throw new ThreadExecutionException(ee.getCause()); // HINT there is no need to keep the EE
      }
   }
}