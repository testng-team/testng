package test.xml.issue2231;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.TestListenerAdapter;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;
import org.testng.xml.internal.Parser;

public class IssueTest {

  @Test(description = "GITHUB-2231")
  public void ensureSuiteGenerationAdheresToDTD() throws IOException {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("CustomSuiteFile");
    xmlSuite.setParallel(ParallelMode.TESTS);
    xmlSuite.setThreadCount(1);
    List<String> group = Arrays.asList("Regression", "Smoke");
    xmlSuite.setIncludedGroups(group);
    xmlSuite.addListener(TestListenerAdapter.class.getName());
    Map<String, String> parameters = new HashMap<>();
    parameters.put("reportPath", "somePath");
    xmlSuite.setParameters(parameters);
    XmlTest xmlTest = new XmlTest(xmlSuite);
    xmlTest.setName("Chrome");
    xmlTest.setXmlClasses(Collections.singletonList(new XmlClass(ExampleTestCase.class.getName())));
    File file = File.createTempFile("sample", ".xml");
    Files.write(file.toPath(), xmlSuite.toXml().getBytes());
    new Parser(file.getAbsolutePath()).parse();
  }

  public static class ExampleTestCase {

    @Test
    public void testMethod() {}
  }
}
