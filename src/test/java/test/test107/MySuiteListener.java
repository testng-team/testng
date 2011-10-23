package test.test107;



import java.util.HashMap;
import java.util.Map;
import org.testng.IExecutionListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;

public class MySuiteListener implements  ISuiteListener {

	public void onStart(ISuite suite) {
		Map<String, String> xmlPropertiesAfter = new HashMap<String, String>();

		/*In our real case we will :
		 *   1. get all the parameters in xml suite file "suiteFileParam"
		 *   2. get all jvm parameters "jvmParam"
		 *   3. merge them by overwriting the "suiteFileParam" with "jvmParam"
		 *   4. set it back to argo
		 *  Here we just set sth into argo to make it simple
		 */
		xmlPropertiesAfter.put("hello", "abc");
		XmlSuite xmlSuite = suite.getXmlSuite();
		xmlSuite.setParameters(xmlPropertiesAfter);
		
	}

 @Override
 public void onFinish(ISuite suite) {
  // TODO Auto-generated method stub
  
 }
	

}