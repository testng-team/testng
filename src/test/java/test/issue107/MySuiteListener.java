package test.issue107;

import java.util.HashMap;
import java.util.Map;

import org.testng.IExecutionListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class MySuiteListener implements IExecutionListener, ISuiteListener {
  public void onFinish(ISuite arg0) {
  }

  public void onStart(ISuite arg0) {
    Map<String, String> xmlPropertiesAfter = new HashMap<String, String>();

    /*In our real case we will :
     *   1. get all the parameters in xml suite file "suiteFileParam"
     *   2. get all jvm parameters "jvmParam"
     *   3. merge them by overwriting the "suiteFileParam" with "jvmParam"
     *   4. set it back to argo
     *  Here we just set sth into argo to make it simple
     */
    xmlPropertiesAfter.put("hello", "abc");
    arg0.getXmlSuite().setParameters(xmlPropertiesAfter);
  }

  public void onExecutionFinish() {
  }

  public void onExecutionStart() {
  }

}
