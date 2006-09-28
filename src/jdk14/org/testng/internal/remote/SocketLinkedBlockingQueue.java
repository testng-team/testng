package org.testng.internal.remote;

import java.net.Socket;

/**
 * <code>SocketLinkedBlockingQueue</code> is a wrapper on LinkedBlockingQueue so
 * we may factor out code common to JDK14 and JDK5+ using different implementation 
 * of LinkedBlockingQueue
 *
 * @author cquezel
 * @since 5.2
 */
public class SocketLinkedBlockingQueue extends edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue
{
    public Socket take() throws InterruptedException {
        return (Socket) super.take();
    }
}
