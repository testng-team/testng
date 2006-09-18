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
 * Reported designed to render self-contained HTML top down view of a testing
 * suite.
 * 
 * @author Paul Mendelson
 * @since 5.2
 * @version $Revision$
 */
public class EmailableReporter implements IReporter {
  // ~ Instance fields ------------------------------------------------------

  Logger m_logger = Logger.getLogger(EmailableReporter.class);

  private PrintWriter m_out;

  int m_row;

  private int m_method_ptr;

  private int m_row_total;

  // ~ Methods --------------------------------------------------------------

  /** Creates summary of the run */
  public void generateReport(List<XmlSuite> xml, List<ISuite> suites,
      String outdir) {
    try {
      m_out = new PrintWriter(new FileWriter(new File(outdir,
          "emailable-report.html")));
    } catch (IOException e) {
      m_logger.error("output file", e);
      return;
    }
    startHtml(m_out);
    generateSuiteSummaryReport(suites);
    generateMethodSummaryReport(suites);
    generateMethodDetailReport(suites);
    endHtml(m_out);
    m_out.close();
  }

  /** Creates a table showing the highlights of each test method with links to the method details */
  protected void generateMethodSummaryReport(List<ISuite> suites) {
    m_method_ptr = 0;
    m_out.println("<a id=\"summary\"></a>");
    startResultSummaryTable("passed");
    for (ISuite suite : suites) {
      if(suites.size()>1) {
        titleRow(suite.getName(), 4);
      }
      Map<String, ISuiteResult> r = suite.getResults();
      for (ISuiteResult r2 : r.values()) {
        ITestContext test = r2.getTestContext();
        resultSummary(test.getFailedTests(), test.getName(), "failed");
        resultSummary(test.getSkippedTests(), test.getName(), "skipped");
        resultSummary(test.getPassedTests(), test.getName(), "passed");
      }
    }
    m_out.println("</table>");
  }

  /** Creates a section showing known results for each method */
  protected void generateMethodDetailReport(List<ISuite> suites) {
    m_method_ptr = 0;
    for (ISuite suite : suites) {
      Map<String, ISuiteResult> r = suite.getResults();
      for (ISuiteResult r2 : r.values()) {
        if (r.values().size() > 0) {
          m_out.println("<h1>" + r2.getTestContext().getName() + "</h1>");
        }
        resultDetail(r2.getTestContext().getFailedTests(), "failed");
        resultDetail(r2.getTestContext().getSkippedTests(), "skipped");
        resultDetail(r2.getTestContext().getPassedTests(), "passed");
      }
    }
  }

  /**
   * @param tests
   */
  private void resultSummary(IResultMap tests, String testname, String style) {
    if (tests.getAllResults().size() > 0) {
      StringBuffer buff = new StringBuffer();
      String lastc = "";
      int mq = 0;
      int cq = 0;
      for (ITestNGMethod method : getMethodSet(tests)) {
        m_row += 1;
        m_method_ptr += 1;
        String cname = method.getTestClass().getName();
        if (mq == 0) {
          titleRow(testname + " &#8212; " + style, 4);
        }
        if (!cname.equalsIgnoreCase(lastc)) {
          if (mq > 0) {
            cq += 1;
            m_out.println("<tr class=\"" + style
                + (cq % 2 == 0 ? "even" : "odd") + "\">" + "<td rowspan=\""
                + mq + "\">" + lastc + buff);
          }
          mq = 0;
          buff.setLength(0);
          lastc = cname;
        }
        Set<ITestResult> result_set = tests.getResults(method);
        long end = Long.MIN_VALUE;
        long start = Long.MAX_VALUE;
        for (ITestResult ans : tests.getResults(method)) {
          if (ans.getEndMillis() > end) {
            end = ans.getEndMillis();
          }
          if (ans.getStartMillis() < start) {
            start = ans.getStartMillis();
          }
        }
        mq += 1;
        if (mq > 1) {
          buff.append("<tr class=\"" + style + (cq % 2 == 0 ? "odd" : "even")
              + "\">");
        }
        buff.append("<td><a href=\"#m" + m_method_ptr + "\">"
            + qualifiedName(method) + "</a></td>" + "<td class=\"numi\">"
            + result_set.size() + "</td><td class=\"numi\">" + (end - start)
            + "</td></tr>");
      }
      if (mq > 0) {
        cq += 1;
        m_out.println("<tr class=\"" + style + (cq % 2 == 0 ? "even" : "odd")
            + "\">" + "<td rowspan=\"" + mq + "\">" + lastc + buff);
      }
    }
  }

  /** Starts and defines columns result summary table */
  private void startResultSummaryTable(String style) {
    tableStart(style);
    m_out
        .println("<tr><th>Class</th>"
            + "<th>Method</th><th># of<br/>Scenarios</th><th>Time<br/>(Msecs)</th></tr>");
    m_row = 0;
  }

  private String qualifiedName(ITestNGMethod method) {
    String addon = "";
    if (method.getGroups().length > 0
        && !"basic".equalsIgnoreCase(method.getGroups()[0])) {
      addon = " (" + method.getGroups()[0] + ")";
    }
    return method.getMethodName() + addon;
  }

  private void resultDetail(IResultMap tests, final String style) {
    if (tests.getAllResults().size() > 0) {
      int row = 0;
      StringBuffer buff = new StringBuffer();
      for (ITestNGMethod method : getMethodSet(tests)) {
        row += 1;
        m_method_ptr += 1;
        String cname = method.getTestClass().getName();
        m_out.println("<a id=\"m" + m_method_ptr + "\"></a><h2>" + cname + ":"
            + method.getMethodName() + "</h2>");
        int rq = 0;
        Set<ITestResult> result_set = tests.getResults(method);
        for (ITestResult ans : result_set) {
          rq += 1;
          Object[] pset = ans.getParameters();
          String output_tag = null;
          if (pset.length > 0) {
            if (rq == 1) {
              tableStart("param");
              m_out.print("<tr>");
              for (int x = 1; x <= pset.length; x++) {
                m_out
                    .print("<th style=\"padding-left:1em;padding-right:1em\">Parameter #"
                        + x + "</th>");
              }
              m_out.println("</tr>");
            }
            m_out.print("<tr" + (rq % 2 == 0 ? " class=\"stripe\"" : "") + ">");
            for (Object p : pset) {
              m_out
                  .println("<td style=\"padding-left:.5em;padding-right:2em\">"
                      + p + "</td>");
            }
            m_out.println("</tr>");
          }
          List<String> msgs = Reporter.getOutput(ans);
          if (msgs.size() > 0) {
            String indent = " style=\"padding-left:3em\"";
            if (pset.length > 0) {
              m_out.println("<tr" + (rq % 2 == 0 ? " class=\"stripe\"" : "")
                  + "><td" + indent + " colspan=\"" + pset.length + "\">");
            } else {
              m_out.println("<div" + indent + ">");
            }
            for (String line : msgs) {
              m_out.println(line + "<br/>");
            }
            if (pset.length > 0) {
              m_out.println("</td></tr>");
            } else {
              m_out.println("</div>");
            }
          }
          if (pset.length > 0) {
            if (rq == result_set.size()) {
              m_out.println("</table>");
            }
          }
        }
        m_out
            .println("<p class=\"totop\"><a href=\"#summary\">back to summary</a></p>");
      }
    }
  }

  /**
   * @param tests
   * @return
   */
  private Collection<ITestNGMethod> getMethodSet(IResultMap tests) {
    Set r = new TreeSet<ITestNGMethod>(new TestSorter<ITestNGMethod>());
    r.addAll(tests.getAllMethods());
    return r;
  }

  public void generateSuiteSummaryReport(List<ISuite> suites) {
    tableStart("param");
    m_out.print("<tr><th>Test</th>");
    tableColumnStart("Methods<br/>Passed");
    tableColumnStart("Scenarios<br/>Passed");
    tableColumnStart("# skipped");
    tableColumnStart("# failed");
    tableColumnStart("Total<br/>Time");
    tableColumnStart("Included<br/>Groups");
    tableColumnStart("Excluded<br/>Groups");
    m_out.println("</tr>");
    NumberFormat formatter = new DecimalFormat("#,##0.0");
    int qty_tests = 0;
    int qty_pass_m = 0;
    int qty_pass_s = 0;
    int qty_skip = 0;
    int qty_fail = 0;
    long time_start = Long.MAX_VALUE;
    long time_end = Long.MIN_VALUE;
    for (ISuite suite : suites) {
      if (suites.size() > 1) {
        titleRow(suite.getName(), 7);
      }
      Map<String, ISuiteResult> tests = suite.getResults();
      for (ISuiteResult r : tests.values()) {
        qty_tests += 1;
        ITestContext overview = r.getTestContext();
        startSummaryRow(overview.getName());
        int q = getMethodSet(overview.getPassedTests()).size();
        qty_pass_m += q;
        summaryCell(q,Integer.MAX_VALUE);
        q = overview.getPassedTests().size();
        qty_pass_s += q;
        summaryCell(q,Integer.MAX_VALUE);
        q = getMethodSet(overview.getSkippedTests()).size();
        qty_skip += q;
        summaryCell(q,0);
        q = getMethodSet(overview.getFailedTests()).size();
        qty_fail += q;
        summaryCell(q,0);
        time_start = Math.min(overview.getStartDate().getTime(), time_start);
        time_end = Math.max(overview.getEndDate().getTime(), time_end);
        summaryCell(formatter
            .format((overview.getEndDate().getTime() - overview.getStartDate()
                .getTime()) / 1000.)
            + " seconds",true);
        summaryCell(overview.getIncludedGroups());
        summaryCell(overview.getExcludedGroups());
        m_out.println("</tr>");
      }
    }
    if (qty_tests > 1) {
      m_out.println("<tr class=\"total\"><td>Total</td>");
      summaryCell(qty_pass_m,Integer.MAX_VALUE);
      summaryCell(qty_pass_s,Integer.MAX_VALUE);
      summaryCell(qty_skip,0);
      summaryCell(qty_fail,0);
      summaryCell(formatter.format((time_end - time_start) / 1000.)
          + " seconds",true);
      m_out.println("<td colspan=\"2\">&nbsp;</td></tr>");
      m_out.println("</table>");
    }
  }

  private void summaryCell(String[] val) {
    StringBuffer b = new StringBuffer();
    for (String v : val)
      b.append(v + " ");
    summaryCell(b.toString(),true);
  }

  private void summaryCell(String v,boolean isgood) {
    m_out.print("<td class=\"numi"+(isgood?"":"_attn")+"\">" + v + "</td>");
  }

  private void startSummaryRow(String label) {
    m_row += 1;
    m_out
        .print("<tr" + (m_row % 2 == 0 ? " class=\"stripe\"" : "")
            + "><td style=\"text-align:left;padding-right:2em\">" + label
            + "</td>");
  }

  private void summaryCell(int v,int maxexpected) {
    summaryCell(String.valueOf(v),v<=maxexpected);
    m_row_total += v;
  }

  /**
   * 
   */
  private void tableStart(String cssclass) {
    m_out.println("<table cellspacing=0 cellpadding=0"
        + (cssclass != null ? " class=\"" + cssclass + "\""
            : " style=\"padding-bottom:2em\"") + ">");
    m_row = 0;
  }

  private void tableColumnStart(String label) {
    m_out.print("<th class=\"numi\">" + label + "</th>");
  }

  private void titleRow(String label, int cq) {
    m_out.println("<tr><th colspan=\"" + cq + "\">" + label + "</th></tr>");
    m_row = 0;
  }
  
  protected void writeStyle(String[] formats,String[] targets) {
    
  }

  /** Starts HTML stream */
  protected void startHtml(PrintWriter out) {
    out
        .println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
    out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
    out.println("<head>");
    out.println("<title>TestNG:  Unit Test</title>");
    out.println("<style type=\"text/css\">");
    out
        .println("table caption,table.info_table,table.param,table.passed,table.failed {margin-bottom:10px;border:1px solid #000099;border-collapse:collapse;empty-cells:show;}");
    out
        .println("table.info_table td,table.info_table th,table.param td,table.param th,table.passed td,table.passed th,table.failed td,table.failed th {");
    out.println("border:1px solid #000099;padding:.25em .5em .25em .5em");
    out.println("}");
    out.println("table.param th {vertical-align:bottom}");
    out.println("td.numi,th.numi,td.numi_attn {");
    out.println("text-align:right");
    out.println("}");
    out.println("tr.total td {font-weight:bold}");
    out.println("table caption {");
    out.println("text-align:center;font-weight:bold;");
    out.println("}");
    out.println("table.passed tr.stripe td,table tr.passedodd td {background-color: #00AA00;}");
    out.println("table.passed td,table tr.passedeven td {background-color: #33FF33;}");
    out.println("table.passed tr.stripe td,table tr.skippedodd td {background-color: #cccccc;}");
    out.println("table.passed td,table tr.skippedodd td {background-color: #dddddd;}");
    out.println("table.failed tr.stripe td,table tr.failedodd td,table.param td.numi_attn {background-color: #FF3333;}");
    out.println("table.failed td,table tr.failedeven td,table.param tr.stripe td.numi_attn {background-color: #DD0000;}");
    out.println("tr.stripe td,tr.stripe th {background-color: #E6EBF9;}");
    out.println("p.totop {font-size:85%;text-align:center;border-bottom:2px black solid}");
    out.println("div.shootout {padding:2em;border:3px #4854A8 solid}");
    out.println("</style>");
    out.println("</head>");
    out.println("<body>");
  }

  /** Finishes HTML stream */
  protected void endHtml(PrintWriter out) {
    out.println("</body></html>");
  }

  // ~ Inner Classes --------------------------------------------------------

  /** Arranges methods by classname and method name */
  private class TestSorter<T extends ITestNGMethod> implements Comparator {
    // ~ Methods -------------------------------------------------------------

    /** Arranges methods by classname and method name */
    public int compare(Object o1, Object o2) {
      int r = ((T) o1).getTestClass().getName().compareTo(
          ((T) o2).getTestClass().getName());
      if (r == 0) {
        r = ((T) o1).getMethodName().compareTo(((T) o2).getMethodName());
      }
      return r;
    }
  }
}
