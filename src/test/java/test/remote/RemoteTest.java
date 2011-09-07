package test.remote;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.remote.RemoteTestNG;
import org.testng.remote.strprotocol.IMessage;
import org.testng.remote.strprotocol.IMessageSender;
import org.testng.remote.strprotocol.MessageHub;
import org.testng.remote.strprotocol.SerializedMessageSender;
import org.testng.remote.strprotocol.StringMessageSender;

import test.SimpleBaseTest;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple client that launches RemoteTestNG and then talks to it via the
 * two supported protocols, String and Serialized.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class RemoteTest extends SimpleBaseTest {
  // Note: don't use the ports used by the plug-in or the RemoteTestNG processes
  // launched in this test will interfere with the plug-in.
  private static final int PORT1 = 1243;
  private static final int PORT2 = 1242;
  private static final List<String> EXPECTED_MESSAGES = new ArrayList<String>() {{
    add("GenericMessage"); // method and test counts
    add("SuiteMessage");  // suite started
    add("TestMessage");  // test started
    add("TestResultMessage"); // status: started
    add("TestResultMessage"); // status: success
    add("TestResultMessage"); // status: started
    add("TestResultMessage"); // status: success
    add("TestMessage"); // test finished
    add("SuiteMessage"); // suite finished
  }};

  @Test
  public void testSerialized() {
    runTest("-serport", PORT1, new SerializedMessageSender("localhost", PORT1));
  }

  @Test
  public void testString() {
    runTest("-port", PORT2, new StringMessageSender("localhost", PORT2));
  }

  private void launchRemoteTestNG(final String portArg, final int portValue) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        RemoteTestNG.main(new String[] {
            portArg, Integer.toString(portValue), "-dontexit",
            getPathToResource("testng-remote.xml")
          });
        }
      }).start();
  }

  private void runTest(String arg, int portValue, IMessageSender sms) {
    p("Launching RemoteTestNG on port " + portValue);
    launchRemoteTestNG(arg, portValue);
    MessageHub mh = new MessageHub(sms);
    List<String> received = Lists.newArrayList();
    try {
      mh.initReceiver();
      IMessage message = mh.receiveMessage();
      while (message != null) {
        received.add(message.getClass().getSimpleName());
        message = mh.receiveMessage();
      }

      Assert.assertEquals(received, EXPECTED_MESSAGES);
    }
    catch(SocketTimeoutException ex) {
      Assert.fail("Time out");
    }
  }

  private static void p(String s) {
    if (false) {
      System.out.println("[RemoteTest] " + s);
    }
  }
}
