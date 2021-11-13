package org.testng.reporters.jq;

import static org.testng.reporters.jq.BasePanel.C;
import static org.testng.reporters.jq.BasePanel.D;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
import org.testng.reporters.Files;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlSuite;

public class Main implements IReporter {
  private static final String TESTNG_RESOURCE_PREFIX = "/org/testng/";
  private static final String[] RESOURCES =
      new String[] {
        "jquery.min.js",
        "testng-reports.css",
        "testng-reports.js",
        "testng-reports1.css",
        "testng-reports2.js",
        "passed.png",
        "failed.png",
        "skipped.png",
        "navigator-bullet.png",
        "bullet_point.png",
        "collapseall.gif"
      };
  public static final String REPORT_HEADER_FILE = "header";

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    Model m_model = new Model(suites);

    XMLStringBuffer xsb = new XMLStringBuffer("    ");

    // Generate the top banner
    new BannerPanel(m_model).generate(xsb);

    // All the panels selectable from the navigator
    List<INavigatorPanel> panels =
        Arrays.asList(
            new TestNgXmlPanel(m_model),
            new TestPanel(m_model),
            new GroupPanel(m_model),
            new TimesPanel(m_model),
            new ReporterPanel(m_model),
            new IgnoredMethodsPanel(m_model),
            new ChronologicalPanel(m_model));

    // Generate the navigator on the left hand side
    new NavigatorPanel(m_model, panels).generate(xsb);

    xsb.push(D, C, "wrapper");
    xsb.push(D, "class", "main-panel-root");

    //
    // Generate the main suite panel
    //
    new SuitePanel(m_model).generate(xsb);

    // Generate all the navigator panels
    for (INavigatorPanel panel : panels) {
      panel.generate(xsb);
    }

    xsb.pop(D); // main-panel-root
    xsb.pop(D); // wrapper

    xsb.addString("  </body>\n");
    xsb.addString("<script type=\"text/javascript\" src=\"testng-reports2.js\"></script>\n");
    xsb.addString("</html>\n");

    String all;
    try {
      try (InputStream header =
          getClass().getResourceAsStream(TESTNG_RESOURCE_PREFIX + REPORT_HEADER_FILE)) {
        if (header == null) {
          throw new RuntimeException("Couldn't find resource header");
        }
        for (String fileName : RESOURCES) {
          try (InputStream is = load(fileName)) {
            if (is == null) {
              throw new AssertionError("Couldn't find resource: " + fileName);
            }
            Files.copyFile(is, new File(outputDirectory, fileName));
          }
        }
        all = Files.readFile(header);
        Utils.writeUtf8File(outputDirectory, "index.html", xsb, all);
      }
    } catch (IOException e) {
      Logger.getLogger(Main.class).error(e.getMessage(), e);
    }
  }

  private InputStream load(String fileName) {
    String path;
    if (fileName.equals("jquery.min.js")) {
      path = "/META-INF/resources/webjars/jquery/3.5.1/jquery.min.js";
    } else {
      path = Main.TESTNG_RESOURCE_PREFIX + fileName;
    }
    return getClass().getResourceAsStream(path);
  }
}
