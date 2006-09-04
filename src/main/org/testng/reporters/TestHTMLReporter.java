package org.testng.reporters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.internal.Utils;


/**
 * This class implements an HTML reporter for individual tests.
 *
 * @author Cedric Beust, May 2, 2004
 * 
 */
public class TestHTMLReporter extends TestListenerAdapter {
  private ITestContext m_testContext = null;
  
  /////
  // implements ITestListener
  //
  @Override
  public void onStart(ITestContext context) {
    m_testContext = context;
  }

  @Override
  public void onFinish(ITestContext context) {
    generateLog(m_testContext, 
                null /* host */,
                m_testContext.getOutputDirectory(), 
                getPassedTests(), 
                getFailedTests(),
                getSkippedTests(), 
                getFailedButWithinSuccessPercentageTests());
  }
  //
  // implements ITestListener
  /////

  private static File getOutputFile(String outputDirectory, ITestContext context) {
    String result = 
//      context.getSuite().getName() + File.separatorChar +
      outputDirectory + File.separator + context.getName() + ".html";
//    File result = new File(base);
//    String path = result.getAbsolutePath();
//    int index = path.lastIndexOf(".");
//    base = "file://" + path.substring(0, index) + ".out";
    
    return new File(result);
  }
  
  public static void generateTable(StringBuffer sb, String title, 
      Collection<ITestResult> tests, String cssClass)
  {
    sb.append("<table width='100%' border='1' class='invocation-").append(cssClass).append("'>\n")
      .append("<tr><td colspan='3' align='center'><b>").append(title).append("</b></td></tr>\n")    
      .append("<tr>")
      .append("<td><b>Test method</b></td>\n")
      .append("<td width=\"10%\"><b>Time (seconds)</b></td>\n")
      .append("<td width=\"30%\"><b>Exception</b></td>\n")
      .append("</tr>\n");
    
    Comparator testResultComparator = new Comparator<ITestResult>() {
      public int compare(ITestResult o1, ITestResult o2) {
        String c1 = o1.getName();
        String c2 = o2.getName();
        return c1.compareTo(c2);
      }
    };
    
    if (tests instanceof List) {
      Collections.sort((List) tests, testResultComparator);
    }

    int i = 0;
    
    // User output?
    String id = "";
    Throwable tw = null;
    
    for (ITestResult tr : tests) {
      sb.append("<tr>\n");

      // Test method
      ITestNGMethod method = tr.getMethod();

      String fqName = tr.getName();
      sb.append("<td title='").append(tr.getTestClass().getName()).append(".").append(fqName).append("()'>").append(fqName);
      
      // Method description
      if (! Utils.isStringEmpty(method.getDescription())) {
        sb.append("<br/><b>").append(method.getDescription()).append("</b>");
      }
      
      Object[] parameters = tr.getParameters();
      if (parameters != null && parameters.length > 0) {
        sb.append("<br/><b>Parameters:</b> ");
        for (int j = 0; j < parameters.length; j++) {
          if (j > 0) sb.append(", ");
          sb.append(parameters[j] == null ? "null" : parameters[j].toString());
        }
      }

      //
      // Output from the method, created by the user calling Reporter.log()
      //
      {
        List<String> output = Reporter.getOutput(tr);
        if (null != output && output.size() > 0) {
          sb.append("<br/>");
          // Method name
          String divId = "Output-" + tr.hashCode();
          sb.append("\n<a href=\"#").append(divId).append("\"")
            .append(" onClick=\"toggleBox('").append(divId).append("');\">")
            .append("Show output</a>\n")
            .append("\n<a href=\"#").append(divId).append("\"")
            .append(" onClick=\"toggleAllBoxes();\">Show all outputs</a>\n")
            ;
          
          // Method output
          sb.append("<div class='log' id=\"").append(divId).append("\">\n");
          for (String s : output) {
            sb.append(s).append("<br/>\n");
          }
          sb.append("</div>\n");
        }
      }
      
      sb.append("</td>\n");
            
      // Time
      long time = (tr.getEndMillis() - tr.getStartMillis()) / 1000;
      String strTime = new Long(time).toString();
      sb.append("<td>").append(strTime).append("</td>\n");
      
      // Exception
      tw = tr.getThrowable();
      String stackTrace = "";
      String fullStackTrace = "";
      
      id = "stack-trace" + tr.hashCode();
      sb.append("<td>");
      
      if (null != tw) {
        String[] stackTraces = Utils.stackTrace(tw, true);
        String shortStackTrace = stackTraces[0].replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        fullStackTrace = stackTraces[1].replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        stackTrace = "<div><pre>" + shortStackTrace  + "</pre></div>";
        
        sb.append(stackTrace);
        // JavaScript link
        sb.append("<a href='#' onClick='toggleBox(\"")
        .append(id).append("\")'>")
        .append("Click to show all stack frames").append("</a>\n")
        .append("<div class='stack-trace' id='" + id + "'>")
        .append("<pre>" + fullStackTrace + "</pre>")
        .append("</div>")
        ;
      }
      
      sb.append("</td>\n").append("</tr>\n");
    }
    
    sb.append("</table><p>\n");

  }
  
  private static String arrayToString(String[] array) {
    StringBuffer result = new StringBuffer("");
    for (int i = 0; i < array.length; i++) {
      result.append(array[i]).append(" ");
    }
    
    return result.toString();
  }
  
  private static String HEAD =
    "\n<style type=\"text/css\">\n" +
    ".log { display: none;} \n" +
    ".stack-trace { display: none;} \n" +
    "</style>\n" +
    "<script type=\"text/javascript\">\n" +
      "<!--\n" +
      "function flip(e) {\n" +
      "  current = e.style.display;\n" +
      "  if (current == 'block') {\n" +
      "    e.style.display = 'none';\n" +
      "  }\n" +
      "  else {\n" +
      "    e.style.display = 'block';\n" +
      "  }\n" +
      "}\n" +
      "\n" +
      "function toggleBox(szDivId)\n" +
      "{\n" +
      "  if (document.getElementById) {\n" +
      "    flip(document.getElementById(szDivId));\n" +
      "  }\n" +
      "  else if (document.all) {\n" +
      "    // this is the way old msie versions work\n" +
      "    var style2 = document.all[szDivId].style;\n" +
      "    style2.display = style2.display? \"\":\"block\";\n" +
      "  }\n" +
      "\n" +
      "}\n" +
      "\n" +
      "function toggleAllBoxes() {\n" +
      "  if (document.getElementsByTagName) {\n" +
      "    d = document.getElementsByTagName('div');\n" +
      "    for (i = 0; i < d.length; i++) {\n" +
      "      if (d[i].className == 'log') {\n" +
      "        flip(d[i]);\n" +
      "      }\n" +
      "    }\n" +
      "  }\n" +
      "}\n" +
      "\n" +
      "// -->\n" +
      "</script>\n" +
      "\n";
  
  public static void generateLog(ITestContext testContext, 
      String host, String outputDirectory,
      Collection<ITestResult> passedTests, Collection<ITestResult> failedTests,
      Collection<ITestResult> skippedTests, Collection<ITestResult> percentageTests)
  {
    File htmlOutputFile = getOutputFile(outputDirectory, testContext);
    StringBuffer sb = new StringBuffer();
    sb.append("<html>\n<head>\n")
      .append("<title>TestNG:  ").append(testContext.getName()).append("</title>\n")
      .append(HtmlHelper.getCssString())
      .append(HEAD)
      .append("</head>\n")
      .append("<body>\n");
    
    Date startDate = testContext.getStartDate();
    Date endDate = testContext.getEndDate();
    long duration = (endDate.getTime() - startDate.getTime()) / 1000;
    int passed = 
      testContext.getPassedTests().size() +
      testContext.getFailedButWithinSuccessPercentageTests().size();
    int failed = testContext.getFailedTests().size();
    int skipped = testContext.getSkippedTests().size();
    String hostLine = Utils.isStringEmpty(host) ? "" : "<tr><td>Remote host:</td><td>" + host
        + "</td>\n</tr>";
    
    sb
    .append("<h2 align='center'>").append(testContext.getName()).append("</h2>")
    .append("<table border='1' align=\"center\">\n")
    .append("<tr>\n")
//    .append("<td>Property file:</td><td>").append(m_testRunner.getPropertyFileName()).append("</td>\n")
//    .append("</tr><tr>\n")
    .append("<td>Tests passed/Failed/Skipped:</td><td>").append(passed).append("/").append(failed).append("/").append(skipped).append("</td>\n")
    .append("</tr><tr>\n")
    .append("<td>Started on:</td><td>").append(testContext.getStartDate().toString()).append("</td>\n")
    .append("</tr>\n")
    .append(hostLine)
    .append("<tr><td>Total time:</td><td>").append(duration).append(" seconds (").append(endDate.getTime() - startDate.getTime())
      .append(" ms)</td>\n")
    .append("</tr><tr>\n")
    .append("<td>Included groups:</td><td>").append(arrayToString(testContext.getIncludedGroups())).append("</td>\n")
    .append("</tr><tr>\n")
    .append("<td>Excluded groups:</td><td>").append(arrayToString(testContext.getExcludedGroups())).append("</td>\n")
    .append("</tr>\n")
    .append("</table><p/>\n")
    ;
    
    sb.append("<small><i>(Hover the method name to see the test class name)</i></small><p/>\n");
    if (failedTests.size() > 0) {
      generateTable(sb, "FAILED TESTS", failedTests, "failed");
    }
    if (percentageTests.size() > 0) {
      generateTable(sb, "FAILED TESTS BUT WITHIN SUCCESS PERCENTAGE",
          percentageTests, "percent");
    }
    if (passedTests.size() > 0) {
      generateTable(sb, "PASSED TESTS", passedTests, "passed");
    }
    if (skippedTests.size() > 0) {
      generateTable(sb, "SKIPPED TESTS", skippedTests, "skipped");
    }
    
    sb.append("</body>\n</html>");
    
    Utils.writeFile(htmlOutputFile, sb.toString());
  }

  private static void ppp(String s) {
    System.out.println("[TestHTMLReporter] " + s);
  }
  
}
