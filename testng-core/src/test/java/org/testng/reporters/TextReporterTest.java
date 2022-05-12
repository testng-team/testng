package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.issue2725.TestClassSample;
import test.SimpleBaseTest;

public class TextReporterTest extends SimpleBaseTest {
  @Test(description = "GITHUB-2725")
  public void testCustomAttributes() {
    TestNG testng = create(TestClassSample.class);
    PrintStream currentStream = System.out;
    final Charset charset = StandardCharsets.UTF_8;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos, true, charset);
      System.setOut(ps);
      TextReporter reporter = new TextReporter("Example_Test", 2);
      testng.addListener(reporter);
      testng.run();
      String content = baos.toString(charset);
      String expected =
          "Test Attributes: <code_name, [dragon_warrior-1]>, <code_name, "
              + "[dragon_warrior-2]>, <code_name, [dragon_warrior-3]>, <code_name, [dragon_warrior-4]>,"
              + " <code_name, [dragon_warrior-5]>, <code_name, [dragon_warrior-6]>, <code_name, "
              + "[dragon_warrior-7]>, <code_name, [dragon_warrior-8]>, <code_name, [dragon_warrior-9]>,"
              + " <code_name, [dragon_warrior-10]>";
      assertThat(content).contains(expected);
    } finally {
      System.setOut(currentStream);
    }
  }
}
