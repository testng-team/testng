package org.testng.remote.adapter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.testng.ISuite;
import org.testng.internal.Utils;
import org.testng.remote.ConnectionInfo;
import org.testng.xml.XmlSuite;


/**
 * Default Slave adapter, provides an adapter based on static port.
 *
 *
 * @author	Guy Korland
 * @since 	April 20, 2007
 */
public class DefaultWorkerAdapter implements IWorkerAdapter
{
	public static final String SLAVE_PORT = "slave.port";

	private ConnectionInfo m_connectionInfo;
	private int m_clientPort;

	@Override
  public void init( Properties prop) throws Exception
	{
		m_clientPort = Integer.parseInt( prop.getProperty(SLAVE_PORT, "0"));
		m_connectionInfo = resetSocket( m_clientPort, null);
	}

	/*
	 * @see org.testng.remote.adapter.IWorkerApadter#getSuite(long)
	 */
	@Override
  public XmlSuite getSuite(long timeout) throws InterruptedException, IOException
	{
      try {
        return (XmlSuite) m_connectionInfo.getOis().readObject();
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace(System.out);
        throw new RuntimeException( e);
      }
      catch(IOException ex) {
        log("Connection closed " + ex.getMessage());
        m_connectionInfo = resetSocket(m_clientPort, m_connectionInfo);
        throw ex;
      }
	}

	/*
	 * @see org.testng.remote.adapter.IWorkerApadter#returnResult(org.testng.ISuite)
	 */
	@Override
  public void returnResult(ISuite result) throws IOException
	{
		try
		{
			m_connectionInfo.getOos().writeObject(result);
		}
		catch(IOException ex) {
			log("Connection closed " + ex.getMessage());
			m_connectionInfo = resetSocket(m_clientPort, m_connectionInfo);
			throw ex;
		}
	}

	private static ConnectionInfo resetSocket(int clientPort, ConnectionInfo oldCi)
	throws IOException
	{
		ConnectionInfo result = new ConnectionInfo();
		ServerSocket serverSocket = new ServerSocket(clientPort);
		serverSocket.setReuseAddress(true);
		log("Waiting for connections on port " + clientPort);
		Socket socket = serverSocket.accept();
		result.setSocket(socket);

		return result;
	}

	private static void log(String string) {
		Utils.log("", 2, string);
	}
}
