/*
 * @(#)SocketLinkedBlockingQueue.java
 *
 * Copyright 1999-2004 by Taleo Corporation,
 * 330 St-Vallier East, Suite 400, Quebec city, Quebec, G1K 9C5, CANADA
 * All rights reserved
 */
package org.testng.internal.remote;

import java.net.Socket;

public class SocketLinkedBlockingQueue extends edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue
{
    public Socket take() throws InterruptedException {
        return (Socket) super.take();
    }
}
