package test.test107;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

public class Test107 {

	@SuppressWarnings("static-access")
	public static void main(String []args){
		
		TestNG t = new TestNG();
		
		//String[] a={"src/test/java/test/test107/TestTestNGCounter_suite.xml"};
		Class [] a={TestTestngCounter.class};
		t.addListener(new MySuiteListener());
		t.setTestClasses(a);
		t.addListener(new IReporter() {
         
         @Override
         public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
           String outputDirectory) {
          for (ISuite s : suites){
           Map<String, ISuiteResult> results = s.getResults();
           if (results != null) {
            Collection<ISuiteResult> tempSuiteResult = results.values();
            for (ISuiteResult isr : tempSuiteResult) {
              ITestContext ctx = isr.getTestContext();
              int skipped = ctx.getSkippedTests().size();
              int failed = ctx.getFailedTests().size() + ctx.getFailedButWithinSuccessPercentageTests().size();
//              m_skipped += skipped;
//              m_failed += failed;
//              m_confFailures += ctx.getFailedConfigurations().size();
//              m_confSkips += ctx.getSkippedConfigurations().size();
//              m_total += ctx.getPassedTests().size() + failed + skipped;
              System.out.println("passed="+ctx.getPassedTests().size()+" + "+isr);
            }
          }
          }
          
         }
        });
		t.run();


		
	}
}
