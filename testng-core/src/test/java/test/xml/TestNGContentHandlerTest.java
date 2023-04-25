package test.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.TestNGContentHandler;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.xml.sax.SAXException;
import test.SimpleBaseTest;

public class TestNGContentHandlerTest extends SimpleBaseTest {
  @Test
  public void testDescriptionInclusion() throws Exception {
    final String xml = getPathToResource("xml/simple-suite-with-method-desc.xml");
    SuiteXmlParser parser = new SuiteXmlParser();
    TestNGContentHandler handler = new TestNGContentHandler(xml, false);
    parser.parse(new FileInputStream(xml), handler);
    List<XmlInclude> includes =
        handler.getSuite().getTests().get(0).getXmlClasses().get(0).getIncludedMethods();
    String desc = includes.get(0).getDescription();
    Assert.assertEquals("simple-description", desc);
  }

  @Test(description = "GITHUB-2501")
  public void ensureAppropriateConnectionObjectsAreUsed() throws IOException, SAXException {
    String xml = getPathToResource("xml/issue2501/2501.xml");
    SuiteXmlParser parser = new SuiteXmlParser();
    TestNGContentHandler handler = new TestNGContentHandler(xml, false);
    parser.parse(new FileInputStream(xml), handler);
    XmlClass xmlClass = handler.getSuite().getTests().get(0).getXmlClasses().get(0);
    assertThat(xmlClass.getSupportClass()).isEqualTo(test.xml.issue2501.TestClassSample.class);
  }

  public static class LocalTestClass {
    @Test
    public void helloWorld() {}
  }
}
