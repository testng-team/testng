package test.groups.issue2232;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.Reporter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlGroups;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlRun;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {
  @Test(description = "GITHUB-2232", invocationCount = 2)
  // This test case doesn't vet out the fix completely because the bug by itself is very
  // sporadic and is not easy to reproduce. That is why this test is being executed 10 times
  // to ensure that the issue can be reproduced in one of the executions
  public void ensureNoNPEThrownWhenRunningGroups() throws InterruptedException {
    TestNG testng = create(constructSuite());
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
  }

  private XmlSuite constructSuite() {
    XmlSuite xmlsuite = createXmlSuite("2232_suite");
    xmlsuite.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
    xmlsuite.setThreadCount(256);
    xmlsuite.setParallel(XmlSuite.ParallelMode.CLASSES);
    XmlTest xmltest = createXmlTest(xmlsuite, "2232_test");
    XmlRun xmlrun = new XmlRun();
    xmlrun.onInclude("Group2");
    xmlrun.onExclude("Broken");
    XmlGroups xmlgroup = new XmlGroups();
    xmlgroup.setRun(xmlrun);
    xmltest.setGroups(xmlgroup);
    XmlPackage xmlpackage = new XmlPackage();
    xmlpackage.setName(getClass().getPackage().getName() + ".samples.*");
    xmltest.setPackages(Collections.singletonList(xmlpackage));
    return xmlsuite;
  }

  @Test(invocationCount = 2, description = "GITHUB-2232")
  // Ensuring that the bug doesn't surface even when tests are executed via the command line mode
  public void commandlineTest() throws IOException, InterruptedException {
    Path suitefile =
        Files.write(
            Files.createTempFile("testng", ".xml"),
            constructSuite().toXml().getBytes(Charset.defaultCharset()));
    List<String> args = Collections.singletonList(suitefile.toFile().getAbsolutePath());
    int status = exec(Collections.emptyList(), args);
    assertThat(status).isEqualTo(0);
  }

  private int exec(List<String> jvmArgs, List<String> args)
      throws IOException, InterruptedException {

    String javaHome = System.getProperty("java.home");
    String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
    String classpath = System.getProperty("java.class.path");
    String className = TestNG.class.getName();
    List<String> command = new ArrayList<>();
    command.add(javaBin);
    command.addAll(jvmArgs);
    command.add("-cp");
    command.add(classpath);
    command.add(className);
    command.addAll(args);
    Reporter.log("Executing the command " + command, 2, true);
    ProcessBuilder builder = new ProcessBuilder(command);
    Process process = builder.inheritIO().start();
    process.waitFor();

    return process.exitValue();
  }
}
