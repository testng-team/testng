package test.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.reporters.XMLStringBuffer.EOL;

import java.util.Collections;
import org.testng.annotations.Test;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.CommentDisabledXmlWeaver;
import org.testng.xml.DefaultXmlWeaver;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Proves that {@code -Dtestng.xml.weaver} is an extension point a third party can actually reach.
 *
 * <p>Before this, {@code IWeaveXml} was advertised as pluggable while every implementation of it
 * was package-private, so the only way to change how one element is written was to reimplement the
 * whole serializer. The weavers below live outside {@code org.testng.xml} on purpose: if the class,
 * its constructor or the per-element hooks were not visible from another package, this file would
 * not compile.
 *
 * <p>The system property itself is exercised by {@link XmlVerifyTest}. It is deliberately not
 * exercised here: {@code XmlWeaver} resolves a non-shipped weaver to {@code null} when {@code
 * testng.testmode} is on, and that flag is set process-wide by another test in this suite.
 */
public class XmlWeaverExtensionTest {

  private static final String MARKER = "<!-- woven by the test -->";

  /** Overrides one element and inherits every other. */
  private static final class ClassOnlyWeaver extends DefaultXmlWeaver {
    @Override
    protected String asXml(XmlClass xmlClass, String indent) {
      return indent + "<class name=\"" + xmlClass.getName() + "\"/> " + MARKER + EOL;
    }
  }

  /** Uses the helper the base class exposes to subclasses. */
  private static final class IncludeParametersWeaver extends DefaultXmlWeaver {
    @Override
    protected String asXml(XmlInclude xmlInclude, String indent) {
      XMLStringBuffer xsb = new XMLStringBuffer(indent);
      xsb.push("include", "name", xmlInclude.getName());
      dumpParameters(xsb, xmlInclude.getLocalParameters());
      xsb.pop("include");
      return xsb.toXML();
    }
  }

  @Test
  public void aSubclassCanReplaceASingleElement() {
    String xml = new ClassOnlyWeaver().asXml(createSuite());

    assertThat(xml).contains("<class name=\"" + XmlWeaverExtensionTest.class.getName() + "\"/> ");
    assertThat(xml).contains(MARKER);
  }

  @Test
  public void theRestOfTheSuiteStillComesFromTheBaseClass() {
    String xml = new ClassOnlyWeaver().asXml(createSuite());

    assertThat(xml).contains("<!DOCTYPE suite SYSTEM");
    assertThat(xml).contains("<suite name=\"Default Suite\"");
    assertThat(xml).contains("name=\"command_line_test\"");
    assertThat(xml).contains("</suite>");
  }

  @Test
  public void aSubclassCanReuseDumpParameters() {
    XmlSuite suite = createSuite();
    XmlClass xmlClass = suite.getTests().get(0).getXmlClasses().get(0);
    XmlInclude include = new XmlInclude("shouldRun");
    include.setParameters(Collections.singletonMap("browser", "firefox"));
    xmlClass.setIncludedMethods(Collections.singletonList(include));

    String xml = new IncludeParametersWeaver().asXml(suite);

    assertThat(xml).contains("<parameter name=\"browser\" value=\"firefox\"/>");
  }

  @Test
  public void theCommentDisabledWeaverIsDirectlyConstructible() {
    String withComments = new DefaultXmlWeaver().asXml(createSuite());
    String withoutComments = new CommentDisabledXmlWeaver().asXml(createSuite());

    assertThat(withComments).contains("<!-- command_line_test -->");
    assertThat(withoutComments).doesNotContain("<!-- command_line_test -->");
  }

  /**
   * The comment was only suppressed on {@code </suite>} and {@code </test>}, because those are the
   * two buffers the weaver configured. Every leaf element built its own, so {@code </class>} still
   * carried the class name.
   */
  @Test
  public void theCommentDisabledWeaverAlsoSilencesLeafElements() {
    XmlSuite suite = createSuite();
    XmlClass xmlClass = suite.getTests().get(0).getXmlClasses().get(0);
    xmlClass.setIncludedMethods(Collections.singletonList(new XmlInclude("shouldRun")));

    String withComments = new DefaultXmlWeaver().asXml(suite);
    String withoutComments = new CommentDisabledXmlWeaver().asXml(suite);

    String classComment = "<!-- " + XmlWeaverExtensionTest.class.getName() + " -->";
    assertThat(withComments).contains(classComment);
    assertThat(withoutComments).doesNotContain(classComment);
    assertThat(withoutComments).doesNotContain("<!--");
  }

  private static XmlSuite createSuite() {
    XmlSuite suite = new XmlSuite();
    XmlTest test = new XmlTest(suite);
    test.setName("command_line_test");
    test.getXmlClasses().add(new XmlClass(XmlWeaverExtensionTest.class));
    return suite;
  }
}
