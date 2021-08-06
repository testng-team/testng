package test.listeners.github2558;

import java.util.List;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

public class ReportersHolder {

  public static class ReporterA implements IReporter {

    @Override
    public void generateReport(
        List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      CallHolder.addCall(getClass().getName() + ".generateReport()");
    }
  }

  public static class ReporterB implements IReporter {

    @Override
    public void generateReport(
        List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      CallHolder.addCall(getClass().getName() + ".generateReport()");
    }
  }
}
