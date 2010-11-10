package test;

import org.testng.remote.RemoteTestNG;
import org.testng.remote.strprotocol.IMessage;
import org.testng.remote.strprotocol.IMessageSender;
import org.testng.remote.strprotocol.MessageHub;
import org.testng.remote.strprotocol.SerializedMessageSender;
import org.testng.remote.strprotocol.StringMessageSender;

/**
 * A simple client that talks to RemoteTestNG. Launch RemoteTestNG as follows:
 *
 * java org.testng.RemoteTestNG -serport 12345 testng.xml
 *
 * and then launch this client. You should see all the messages as RemoteTestNG
 * runs through the suite.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class RemoteTest {

  public static void main(String[] args) throws Exception {
//    new RemoteTest().testString();
    new RemoteTest().testSerialized();
  }
  
  public void testSerialized() {
    runTest("-serport", new SerializedMessageSender("localhost", 12345));
  }
  
  public void testString() {
    runTest("-port", new StringMessageSender("localhost", 12345));
  }

  private void launchRemoteTestNG(final String portArg) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        RemoteTestNG.main(new String[] {
            portArg, "12345",
            "/Users/cbeust/java/testng/src/test/resources/testng-single.xml"});
        }
      }).start();
  }

  private void runTest(String arg, IMessageSender sms) {
    launchRemoteTestNG(arg);
    MessageHub mh = new MessageHub(sms);
    mh.initReceiver();
    IMessage message = mh.receiveMessage();
    while (message != null) {
      System.out.println("Received message:" + message);
      message = mh.receiveMessage();
    }
  }
}
