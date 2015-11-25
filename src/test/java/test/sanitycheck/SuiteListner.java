package test.sanitycheck;

import java.util.ArrayList;
import java.util.List;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;

public class SuiteListner implements ISuiteListener {

	List<XmlSuite> allSuite = new ArrayList<XmlSuite>();
	
	@Override
	public void onStart(ISuite suite) {
		allSuite.add(suite.getXmlSuite());	
	}

	@Override
	public void onFinish(ISuite suite) {
		
	}
	
	public List<XmlSuite> getAllTestSuite(){
		return allSuite;
	}

}
