package test.factory.github2428;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

public class Reporter implements IReporter {
  private final Set<Object> results = Collections.newSetFromMap(new IdentityHashMap<>());

  public Set<Object> getResults() {
    return results;
  }

  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    suites
        .get(0)
        .getResults()
        .get("Command line test")
        .getTestContext()
        .getPassedConfigurations()
        .getAllResults()
        .forEach(x -> results.add(x.getInstance()));
  }
}
