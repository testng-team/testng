package org.testng.internal.issue2426;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlSuite;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import test.SimpleBaseTest;

public class SuiteTest extends SimpleBaseTest {
    @Test (description = "ISSUE-2426")
    public void testGetFactoryMethodParamsInfo() throws IOException {
        final SuiteXmlParser parser = new SuiteXmlParser();
        final String testSuite = "src/test/resources/xml/github2426/test-suite.xml";
        final XmlSuite xmlSuite = parser.parse(testSuite, new FileInputStream(testSuite), true);
        final TestNG tng = create(xmlSuite);

        final Path temp = Files.createTempDirectory("tmp");
        tng.setOutputDirectory(temp.toAbsolutePath().toString());
        tng.run();
    }
}
