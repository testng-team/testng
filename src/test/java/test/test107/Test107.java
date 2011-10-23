package test.test107;

import java.util.Collection;
import java.util.List;
import java.util.Map;



import org.testng.Assert;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

public class Test107 {


	public static void main(String[] ar){
		
		TestNG t = new TestNG();
		
		//String[] a={"src/test/java/test/test107/TestTestNGCounter_suite.xml"};
		Class [] a={TestTestngCounter.class};
		t.addListener(new MySuiteListener());
		t.setTestClasses(a);
		t.run();


		
	}
}
