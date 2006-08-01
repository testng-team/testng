package org.testng.internal.thread.port;


import org.testng.internal.thread.IFutureResult;
import org.testng.internal.thread.ThreadExecutionException;

import edu.emory.mathcs.util.concurrent.ExecutionException;
import edu.emory.mathcs.util.concurrent.Future;

/**
 * IFutureResult implementation and Future adapter.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class FutureResultAdapter implements IFutureResult {
	Future m_future;

	public FutureResultAdapter(Future future) {
		m_future = future;
	}

	public Object get() throws InterruptedException, ThreadExecutionException {
		try {
			return m_future.get();
		} catch(ExecutionException ee) {
			throw new ThreadExecutionException(ee.getCause());
		}
	}
}