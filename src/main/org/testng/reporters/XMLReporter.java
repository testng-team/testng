package org.testng.reporters;

import java.util.Map;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;


/**
 * An XML reporter for TestNG.
 *
 * @author Cedric Beust, Aug 6, 2004
 * 
 */
public class XMLReporter implements ISuiteListener {
//  private List<ITestResult> m_passedTests = new ArrayList<ITestResult>();
//  private List<ITestResult> m_failedTests = new ArrayList<ITestResult>();
//  
//  public void onTestSuccess(ITestResult tr) {
//    m_passedTests.add(tr);
//  }
//
//  public void onTestFailure(ITestResult tr) {
//    m_failedTests.add(tr);
//  }

  public void onStart(ISuite suite) {
    
  }
  
  public void onFinish(ISuite context) {
    XMLStringBuffer xsb = new XMLStringBuffer("");
    
    xsb.push("suite-results");
    
    Map<String, ISuiteResult> results = context.getResults();
    
    for (String name : results.keySet()) {
      ISuiteResult sr = results.get(name);
      ITestContext tc = sr.getTestContext();
      
      xsb.push("test");
      
      xsb.addRequired("name", tc.getName());
      
      xsb.push("passed");
      for (ITestResult tr : tc.getPassedTests().getAllResults()) {
        xsb.addRequired("method-name", tr.getName());
      }
      xsb.pop("passed");
      
  
      xsb.push("failed");
      for (ITestResult tr : tc.getFailedTests().getAllResults()) {
        xsb.addRequired("method-name", tr.getName());
      }
      xsb.pop("failed"); 

      xsb.pop("test");
    }

    xsb.pop("suite-results");
    
    ppp("\n" + xsb.toXML());
  }
  
  private static void ppp(String s) {
    System.out.println("[XMLReporter] " + s);
  }



}
