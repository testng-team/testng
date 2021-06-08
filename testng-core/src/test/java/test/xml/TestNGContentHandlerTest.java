package test.xml;

import java.io.FileInputStream;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.TestNGContentHandler;
import org.testng.xml.XmlInclude;
import test.SimpleBaseTest;

public class TestNGContentHandlerTest extends SimpleBaseTest {
  @Test
  public void testDescriptionInclusion() throws Exception {
    final String xml = getPathToResource("samples/xml/simple-suite-with-method-desc.xml");
    SuiteXmlParser parser = new SuiteXmlParser();
    TestNGContentHandler handler = new TestNGContentHandler(xml, false);
    parser.parse(new FileInputStream(xml), handler);
    List<XmlInclude> includes =
        handler.getSuite().getTests().get(0).getXmlClasses().get(0).getIncludedMethods();
    String desc = includes.get(0).getDescription();
    Assert.assertEquals("simple-description", desc);
  }

  public static class LocalTestClass {
    @Test
    public void helloWorld() {}
  }
}
