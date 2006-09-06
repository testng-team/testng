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
 * @version $Revision: 3 $
  */
public class EmailableReporter implements IReporter
{
  Logger logger=Logger.getLogger(EmailableReporter.class);
  //~ Instance fields ------------------------------------------------------

  private PrintWriter out;
  int row;

  //~ Methods --------------------------------------------------------------

  /** Creates summary of the run */
  public void generateReport(
    List<XmlSuite> xml,
    List<ISuite> suites,
    String outdir)
  {
    try {
      out=new PrintWriter(new FileWriter(new File(outdir,"emailable-report.html")));
    } catch(IOException e) {
      logger.error("output file",e);
      return;
    }
    startHtml(out);
    for(ISuite suite : suites) {
      summarize(suite);
      out.println("<a id=summary></a>");
      Map<String,ISuiteResult> r=suite.getResults();
      for(ISuiteResult r2 : r.values()) {
        resultSummary(
          r2.getTestContext().getFailedTests(),
          "failed");
        resultSummary(
          r2.getTestContext().getPassedTests(),
          "passed");
        resultDetail(
          r2.getTestContext().getFailedTests(),
          "failed");
        resultDetail(
          r2.getTestContext().getPassedTests(),
          "passed");
      }
    }
    out.println("</body></html>");
    out.close();
  }

  /**
   * @param tests
   */
  private void resultSummary(IResultMap tests,String style)
  {
    if(tests.getAllResults().size()>0) {
      tableStart(style);
      out.println(
        "<tr><th>Class</th>"
        +"<th>Method</th><th># of<br/>Scenarios</th><th>Time<br/>(Msecs)</th></tr>");
      int row=0;
      StringBuffer buff=new StringBuffer();
      String lastc="";
      int mq=0;
      int cq=0;
      for(ITestNGMethod method : getMethodSet(tests)) {
        row+=1;
        String cname=method.getTestClass().getName();
        if(!cname.equalsIgnoreCase(lastc)) {
          if(mq>0) {
            cq+=1;
            out.println(
              "<tr"+(cq%2==0
              ? " class=\"stripe\""
              : "")+">"+"<td rowspan=\""+mq+"\">"+lastc+buff);
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
          buff.append("<tr"+(cq%2==1
            ? " class=\"stripe\""
            : "")+">");
        }
        buff.append(
          "<td><a href=\"#m"+row+"\">"+qualifiedName(method)+"</a></td>"
          +"<td class=\"numi\">"+result_set.size()+"</td><td class=\"numi\">"
          +(end-start)+"</td></tr>");
      }
      if(mq>0) {
        row+=1;
        out.println(
          "<tr"+(row%2==0
          ? " class=\"stripe\""
          : "")+">"+"<td rowspan=\""+mq+"\">"+lastc+buff);
      }
      out.println("</table>");
    }
  }

  private String qualifiedName(ITestNGMethod method) {
    String addon="";
    if(method.getGroups().length>0 && ! "basic".equalsIgnoreCase(method.getGroups()[0])) {
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
        String cname=method.getTestClass().getName();
        out.println(
          "<a id=\"m"+row+"\"></a><h2>"+cname+":"+method.getMethodName()
          +"</h2>");
        //        Set<ITestResult> result_set=tests.getResults(method);
        //        long end=Long.MIN_VALUE;
        //        long start=Long.MAX_VALUE;
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
        //        mq+=1;
        //        if(mq>1)
        //          buff.append("<tr"+(row%2==1?" class=\"stripe\"":"")+">");
        //        buff.append("<td><a href=\"#"+method.getId()+">"+method.getMethodName()+"</a></td>"
        //          +"<td class=\"numi\">"+result_set.size()+"</td><td class=\"numi\">"
        //          +(end-start)
        //          +"</td></tr>");
        //      }
        //      if(mq>0) {
        //        row+=1;
        //        out.println("<tr"+(row%2==0?" class=\"stripe\"":"")+">"
        //          +"<td rowspan=\""+mq+"\">"+lastc+buff);
        out.println("<p class=\"totop\"><a href=#top>back to summary</a></p>");
      }

      //      out.println("</table>");
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

  private void summarize(ISuite suite)
  {
    tableStart("param");
    Map<String,ISuiteResult> r=suite.getResults();
    for(ISuiteResult r2 : r.values()) {
      ITestContext overview=r2.getTestContext();
      tableRow(
        "# of Tests passed",
        getMethodSet(overview.getPassedTests()).size());
      tableRow(
        "# of Scenarios passed",
        overview.getPassedTests().size());
      tableRow(
        "# failed",
        overview.getFailedTests().size());
      tableRow(
        "# skipped",
        overview.getSkippedTests().size());
      NumberFormat formatter=new DecimalFormat("#,##0.0");
      tableRow(
        "Total Time",
        formatter.format(
          (overview.getEndDate().getTime()-overview.getStartDate().getTime())/1000.)
        +" seconds");
      tableRow(
        "Included Groups",
        overview.getIncludedGroups());
      tableRow(
        "Excluded Groups",
        overview.getExcludedGroups());
    }
    out.println("</table>");
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

  private void tableRow(String label,String val)
  {
    row+=1;
    out.println(
      "<tr"+(row%2==0
      ? " class=\"stripe\""
      : "")+"><th style=\"text-align:left;padding-right:2em\">"+label
      +"</th><td style=\"text-align:right\">"+val+"</td></tr>");
  }

  private void tableRow(String label,long val)
  {
    tableRow(
      label,
      String.valueOf(val));
  }

  private void tableRow(String label,String[] val)
  {
    StringBuffer b=new StringBuffer();
    for(String v : val)
      b.append(v+" ");
    tableRow(
      label,
      b.toString().trim());
  }

  private void startHtml(PrintWriter out)
  {
    out.println("<html>");
    out.println("<head>");
    out.println("<title>TestNG:  Unit Test</title>");
    out.println("<style type=\"text/css\">");
    out.println(
      "table.info_table,table.param,table.passed,table.failed {margin-bottom:10px;border:1px solid #000099;border-collapse:collapse;empty-cells:show;}");
    out.println(
      "table.info_table td,table.info_table th,table.param td,table.param th,table.passed td,table.passed th,table.failed td,table.failed th {");
    out.println("border:1px solid #000099;padding:.25em .5em .25em .5em");
    out.println("}");
    out.println("td.numi {");
    out.println("text-align:right");
    out.println("}");
    out.println("table.passed td {background-color: #00AA00;}");
    out.println("table.passed tr.stripe td {background-color: #33FF33;}");
    out.println("table.failed td {background-color: #DD0000;}");
    out.println("table.failed tr.stripe td {background-color: #FF3333;}");
    out.println("tr.stripe td,tr.stripe th {background-color: #E6EBF9;}");
    out.println(
      "p.totop {font-size:85%;text-align:center;border-bottom:2px black solid}");
    out.println("div.shootout {padding:2em;border:3px #4854A8 solid}");
    out.println("</style>");
    out.println("</head>");
    out.println("<body>");
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
