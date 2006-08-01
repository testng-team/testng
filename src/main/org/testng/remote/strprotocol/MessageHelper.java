package org.testng.remote.strprotocol;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.ITestResult;


/**
 * Marshal/unmarshal tool for <code>IStringMessage</code>s.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class MessageHelper {
  public static final char DELIMITER = '\u0001';
  
  public static final int GENERIC_SUITE_COUNT = 1;
  
  public static final int SUITE = 10;
  public static final int SUITE_START = 11;
  public static final int SUITE_FINISH = 12;
  
  public static final int TEST = 100;
  public static final int TEST_START = 101;
  public static final int TEST_FINISH = 102;
  
  public static final int TEST_RESULT = 1000;
  public static final int PASSED_TEST = TEST_RESULT + ITestResult.SUCCESS;
  public static final int FAILED_TEST = TEST_RESULT + ITestResult.FAILURE;
  public static final int SKIPPED_TEST = TEST_RESULT + ITestResult.SKIP;
  public static final int FAILED_ON_PERCENTAGE_TEST = TEST_RESULT + ITestResult.SUCCESS_PERCENTAGE_FAILURE;
  public static final int TEST_STARTED = TEST_RESULT + ITestResult.STARTED;
  
  public static final String STOP_MSG = ">STOP";
  public static final String ACK_MSG = ">ACK";

  
  
  public static int getMessageType(final String message) {
    int idx = message.indexOf(DELIMITER);
    
    return idx == -1 ? Integer.parseInt(message) : Integer.parseInt(message.substring(0, idx));
  }
  
  public static GenericMessage unmarshallGenericMessage(final String message) {
    String[] messageParts = parseMessage(message);
    if(messageParts.length == 1) {
      return new GenericMessage(Integer.parseInt(messageParts[0]));
    }
    else {
      Map props = new HashMap();
      
      for(int i = 1; i < messageParts.length; i+=2) {
        props.put(messageParts[i], messageParts[i + 1]);
      }
      
      return new GenericMessage(Integer.parseInt(messageParts[0]), props);
    }
  }
  
  public static SuiteMessage createSuiteMessage(final String message) {
    int type = getMessageType(message);
    String[] messageParts = parseMessage(message);
    
    return new SuiteMessage(messageParts[1],
                            MessageHelper.SUITE_START == type,
                            Integer.parseInt(messageParts[2]));
  }
  
  public static TestMessage createTestMessage(final String message) {
    int type = getMessageType(message);
    String[] messageParts = parseMessage(message);
    
    return new TestMessage(MessageHelper.TEST_START == type,
                           messageParts[1],
                           messageParts[2],
                           Integer.parseInt(messageParts[3]),
                           Integer.parseInt(messageParts[4]),
                           Integer.parseInt(messageParts[5]),
                           Integer.parseInt(messageParts[6]),
                           Integer.parseInt(messageParts[7]));
  }
  
  public static TestResultMessage unmarshallTestResultMessage(final String message) {
    String[] messageParts = parseMessage(message);
    
    
    return new TestResultMessage(Integer.parseInt(messageParts[0]),
                                 messageParts[1],
                                 messageParts[2],
                                 messageParts[3],
                                 messageParts[4],
                                 Long.parseLong(messageParts[5]),
                                 Long.parseLong(messageParts[6]),
                                 replaceNewLineReplacer(messageParts[7])
            );
  }
  
  public static String replaceNewLine(String message) {
    if(null == message) {
      return message;
    }
    
    return message.replace('\n', '\u0002').replace('\r', '\u0003');
  }
  
  public static String replaceNewLineReplacer(String message) {
    if(null == message) {
      return message;
    }
    
    return message.replace('\u0002', '\n').replace('\u0003', '\r');
  }
  
  private static String[] parseMessage(final String message) {
    if(null == message) {
      return new String[0];
    }
    
    List tokens = new ArrayList();
    int start = 0;
    for(int i = 0; i < message.length(); i++) {
      if(DELIMITER == message.charAt(i)) {
        tokens.add(message.substring(start, i));
        start = i + 1;
      }
    }
    if(start < message.length()) {
      tokens.add(message.substring(start, message.length()));
    }
    
    return (String[]) tokens.toArray(new String[tokens.size()]);
  }
}
