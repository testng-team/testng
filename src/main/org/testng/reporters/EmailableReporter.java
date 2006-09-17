package org.testng.reporters;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;

import org.testng.log4testng.Logger;

import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Reported designed to render self-contained HTML top down view
 * of a testing suite.
 *
 * @author Paul Mendelson
 * @version $Revision: 112 $
 */
public class EmailableReporter implements IReporter
{
  //~ Instance fields ------------------------------------------------------

  Logger logger=Logger.getLogger(EmailableReporter.class);
  private PrintWriter out;
  int row;
  private int method_ptr;
  private int row_total;

  //~ Methods --------------------------------------------------------------

  /** Creates summary of the run */
  public void generateReport(
    List<XmlSuite> xml,
    List<ISuite> suites,
    String outdir)
  {
    try {
      out=
        new PrintWriter(
          new FileWriter(new File(outdir,"emailable-report.html")));
    } catch(IOException e) {
      logger.error("output file",e);
      return;
    }
    startHtml(out);
    summarize(suites);
    method_ptr=0;
    for(ISuite suite : suites) {
      out.println("<a id=\"summary\"></a>");
      Map<String,ISuiteResult> r=suite.getResults();
      startResultSummaryTable("passed");
      for(ISuiteResult r2 : r.values()) {
        ITestContext test=r2.getTestContext();
        resultSummary(
          test.getFailedTests(),
          test.getName(),
          "failed");
        resultSummary(
          test.getSkippedTests(),
          test.getName(),
          "skipped");
        resultSummary(
          test.getPassedTests(),
          test.getName(),
          "passed");
      }
    }
    out.println("</table>");
    method_ptr=0;
    for(ISuite suite : suites) {
      Map<String,ISuiteResult> r=suite.getResults();
      for(ISuiteResult r2 : r.values()) {
        if(r.values().size()>0) {
          out.println("<h1>"+r2.getTestContext().getName()+"</h1>");
        }
        resultDetail(
          r2.getTestContext().getFailedTests(),
          "failed");
        resultDetail(
          r2.getTestContext().getSkippedTests(),
          "skipped");
        resultDetail(
          r2.getTestContext().getPassedTests(),
          "passed");
      }
    }
    endHtml(out);
    out.close();
  }

  /**
   * @param tests
   */
  private void resultSummary(IResultMap tests,String testname,String style)
  {
    if(tests.getAllResults().size()>0) {
      StringBuffer buff=new StringBuffer();
      String lastc="";
      int mq=0;
      int cq=0;
      for(ITestNGMethod method : getMethodSet(tests)) {
        row+=1;
        method_ptr+=1;
        String cname=method.getTestClass().getName();
        if(mq==0) {
          titleRow(testname+" &#8212; "+style,4);
        }
        if(!cname.equalsIgnoreCase(lastc)) {
          if(mq>0) {
            cq+=1;
            out.println(
              "<tr class=\""+style+(cq%2==0
              ? "even"
              : "odd")+"\">"+"<td rowspan=\""+mq+"\">"+lastc+buff);
          }
          mq=0;
          buff.setLength(0);
          lastc=cname;
        }
        Set<ITestResult> result_set=tests.getResults(method);
        long end=Long.MIN_VALUE;
        long start=Long.MAX_VALUE;
        for(ITestResult ans : tests.getResults(method)) {
          if(ans.getEndMillis()>end) {
            end=ans.getEndMillis();
          }
          if(ans.getStartMillis()<start) {
            start=ans.getStartMillis();
          }
        }
        mq+=1;
        if(mq>1) {
          buff.append("<tr class=\""+style+(cq%2==0
            ? "odd"
            : "even")+"\">");
        }
        buff.append(
          "<td><a href=\"#m"+method_ptr+"\">"+qualifiedName(method)+"</a></td>"
          +"<td class=\"numi\">"+result_set.size()+"</td><td class=\"numi\">"
          +(end-start)+"</td></tr>");
      }
      if(mq>0) {
        cq+=1;
        out.println(
          "<tr class=\""+style+(cq%2==0
          ? "even"
          : "odd")+"\">"+"<td rowspan=\""+mq+"\">"+lastc+buff);
      }
    }
  }

  /**
   * @param style
   * @return
   */
  private void startResultSummaryTable(String style)
  {
    tableStart(style);
    out.println(
      "<tr><th>Class</th>"
      +"<th>Method</th><th># of<br/>Scenarios</th><th>Time<br/>(Msecs)</th></tr>");
    row=0;
  }

  private String qualifiedName(ITestNGMethod method)
  {
    String addon="";
    if(
      method.getGroups().length>0
          && !"basic".equalsIgnoreCase(method.getGroups()[0])) {
      addon=" ("+method.getGroups()[0]+")";
    }
    return method.getMethodName()+addon;
  }

  private void resultDetail(IResultMap tests,String style)
  {
    if(tests.getAllResults().size()>0) {
      int row=0;
      StringBuffer buff=new StringBuffer();
      for(ITestNGMethod method : getMethodSet(tests)) {
        row+=1;
        method_ptr+=1;
        String cname=method.getTestClass().getName();
        out.println(
          "<a id=\"m"+method_ptr+"\"></a><h2>"+cname+":"+method.getMethodName()
          +"</h2>");
        int rq=0;
        Set<ITestResult> result_set=tests.getResults(method);
        for(ITestResult ans : result_set) {
          rq+=1;
          Object[] pset=ans.getParameters();
          if(pset.length>0) {
            if(rq==1) {
              tableStart("param");
              out.print("<tr>");
              for(int x=1; x<=pset.length; x++) {
                out.print(
                  "<th style=\"padding-left:1em;padding-right:1em\">Parameter #"+x
                  +"</th>");
              }
              out.println("</tr>");
            }
            out.print("<tr>");
            for(Object p : pset) {
              out.println(
                "<td style=\"padding-left:.5em;padding-right:2em\">"+p+"</td>");
            }
            out.println("</tr>");
            if(rq==result_set.size()) {
              out.println("</table>");
            }
          }
          List<String> msgs=Reporter.getOutput(ans);
          if(msgs.size()>0) {
            out.println("<div style=\"padding-left:3em\">");
            for(String line : msgs) {
              out.println(line+"<br/>");
            }
            out.println("</div>");
          }
        }
        out.println("<p class=\"totop\"><a href=\"#summary\">back to summary</a></p>");
      }
    }
  }

  /**
   * @param tests
   * @return
   */
  private Collection<ITestNGMethod> getMethodSet(IResultMap tests)
  {
    Set r=new TreeSet<ITestNGMethod>(new TestSorter<ITestNGMethod>());
    r.addAll(tests.getAllMethods());
    return r;
  }

  private void summarize(List<ISuite> suites)
  {
    tableStart("param");
    out.print("<tr><th>Test</th>");
    tableColumnStart("Methods<br/>Passed");
    tableColumnStart("Scenarios<br/>Passed");
    tableColumnStart("# skipped");
    tableColumnStart("# failed");
    tableColumnStart("Total<br/>Time");
    tableColumnStart("Included<br/>Groups");
    tableColumnStart("Excluded<br/>Groups");
    out.println("</tr>");
    NumberFormat formatter=new DecimalFormat("#,##0.0");
    int qty_tests=0;
    int qty_pass_m=0;
    int qty_pass_s=0;
    int qty_skip=0;
    int qty_fail=0;
    long time_start=Long.MAX_VALUE;
    long time_end=Long.MIN_VALUE;
    for(ISuite suite : suites) {
      if(suites.size()>1) {
        titleRow(
          suite.getName(),
          7);
      }
      Map<String,ISuiteResult> tests=suite.getResults();
      for(ISuiteResult r : tests.values()) {
        qty_tests+=1;
        ITestContext overview=r.getTestContext();
        startSummaryRow(overview.getName());
        int q=getMethodSet(overview.getPassedTests()).size();
        qty_pass_m+=q;
        summaryCell(q);
        q=overview.getPassedTests().size();
        qty_pass_s+=q;
        summaryCell(q);
        q=getMethodSet(overview.getSkippedTests()).size();
        qty_skip+=q;
        summaryCell(q);
        q=getMethodSet(overview.getFailedTests()).size();
        qty_fail+=q;
        summaryCell(q);
        time_start=
          Math.min(
            overview.getStartDate().getTime(),
            time_start);
        time_end=
          Math.max(
            overview.getEndDate().getTime(),
            time_end);
        summaryCell(
          formatter.format(
            (overview.getEndDate().getTime()-overview.getStartDate().getTime())/1000.)
          +" seconds");
        summaryCell(overview.getIncludedGroups());
        summaryCell(overview.getExcludedGroups());
        out.println("</tr>");
      }
    }
    if(qty_tests>1) {
      out.println("<tr class=\"total\"><td>Total</td>");
      summaryCell(qty_pass_m);
      summaryCell(qty_pass_s);
      summaryCell(qty_skip);
      summaryCell(qty_fail);
      summaryCell(formatter.format((time_end-time_start)/1000.)+" seconds");
      out.println("<td colspan=\"2\">&nbsp;</td></tr>");
      out.println("</table>");
    }
  }

  private void summaryCell(String[] val)
  {
    StringBuffer b=new StringBuffer();
    for(String v : val)
      b.append(v+" ");
    summaryCell(b.toString());
  }

  private void summaryCell(String v)
  {
    out.print("<td class=\"numi\">"+v+"</td>");
  }

  private void startSummaryRow(String label)
  {
    row+=1;
    out.print(
      "<tr"+(row%2==0
      ? " class=\"stripe\""
      : "")+"><td style=\"text-align:left;padding-right:2em\">"+label+"</td>");
  }

  private void summaryCell(int v)
  {
    summaryCell(String.valueOf(v));
    row_total+=v;
  }

  /**
   *
   */
  private void tableStart(String cssclass)
  {
    out.println(
      "<table cellspacing=0 cellpadding=0"
      +(
        cssclass!=null
        ? " class=\""+cssclass+"\""
        : " style=\"padding-bottom:2em\""
      )+">");
    row=0;
  }

  private void tableStart(String cssclass,String title)
  {
    tableStart(cssclass);
    out.println("<caption>"+title+"</caption>");
    row+=1;
  }

  private void tableColumnStart(String label)
  {
    out.print("<th class=\"numi\">"+label+"</th>");
  }

  private void titleRow(String label,int cq)
  {
    out.println("<tr><th colspan=\""+cq+"\">"+label+"</th></tr>");
    row=0;
  }

  /** Starts HTML stream */
  protected void startHtml(PrintWriter out)
  {
    out.println(
      "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
    out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
    out.println("<head>");
    out.println("<title>TestNG:  Unit Test</title>");
    out.println("<style type=\"text/css\">");
    out.println(
      "table caption,table.info_table,table.param,table.passed,table.failed {margin-bottom:10px;border:1px solid #000099;border-collapse:collapse;empty-cells:show;}");
    out.println(
      "table.info_table td,table.info_table th,table.param td,table.param th,table.passed td,table.passed th,table.failed td,table.failed th {");
    out.println("border:1px solid #000099;padding:.25em .5em .25em .5em");
    out.println("}");
    out.println("table.param th {vertical-align:bottom}");
    out.println("td.numi,th.numi {");
    out.println("text-align:right");
    out.println("}");
    out.println("tr.total td {font-weight:bold}");
    out.println("table caption {");
    out.println("text-align:center;font-weight:bold;");
    out.println("}");
    out.println(
      "table.passed tr.stripe td,table tr.passedodd td {background-color: #00AA00;}");
    out.println(
      "table.passed td,table tr.passedeven td {background-color: #33FF33;}");
    out.println(
      "table.passed tr.stripe td,table tr.skippedodd td {background-color: #cccccc;}");
    out.println(
      "table.passed td,table tr.skippedodd td {background-color: #dddddd;}");

    out.println(
      "table.failed tr.stripe td,table tr.failedodd td {background-color: #FF3333;}");
    out.println(
      "table.failed td,table tr.failedeven td {background-color: #DD0000;}");
    out.println("tr.stripe td,tr.stripe th {background-color: #E6EBF9;}");
    out.println(
      "p.totop {font-size:85%;text-align:center;border-bottom:2px black solid}");
    out.println("div.shootout {padding:2em;border:3px #4854A8 solid}");
    out.println("</style>");
    out.println("</head>");
    out.println("<body>");
  }

  /** Finishes HTML stream */
  protected void endHtml(PrintWriter out)
  {
    out.println("</body></html>");
  }

  //~ Inner Classes --------------------------------------------------------

  /**
   * DOCUMENT ME!
   *
   * @author $author$
   * @version $Revision: 3 $
   *
   * @param <T> DOCUMENT ME!
   */
  private class TestSorter<T extends ITestNGMethod> implements Comparator
  {
    //~ Methods -------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param o1 DOCUMENT ME!
     * @param o2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int compare(Object o1,Object o2)
    {
      int r=
        ((T)o1).getTestClass().getName()
            .compareTo(((T)o2).getTestClass().getName());
      if(r==0) {
        r=((T)o1).getMethodName().compareTo(((T)o2).getMethodName());
      }
      return r;
    }
  }
}
