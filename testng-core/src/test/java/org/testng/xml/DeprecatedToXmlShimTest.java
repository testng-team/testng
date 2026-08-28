package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.reporters.XMLStringBuffer.EOL;

import java.util.Arrays;
import java.util.Collections;
import org.testng.annotations.Test;

/**
 * Pins the {@code toXml(String)} methods the model classes still expose after the serialization
 * moved into {@code DefaultXmlWeaver}.
 *
 * <p>These need their own test: no caller left in the repository exercises them. Everything goes
 * through {@link XmlSuite#toXml()} or {@link XmlTest#toXml(String)}, so {@link XmlRoundTripTest}
 * covers the weaver but would stay green if every shim below returned the empty string. They are
 * kept only for third-party binary compatibility, and this module has no binary compatibility check
 * in CI, so the expectations are spelled out literally rather than compared against another call
 * into the same code.
 *
 * <p>Two quirks below are {@code XMLStringBuffer}'s, not typos: closing a tag that was pushed with
 * a {@code name} attribute echoes that name as a trailing XML comment, and attributes come out in
 * {@link java.util.Properties} iteration order rather than the order they were set, which is why
 * {@code depends-on} precedes {@code name}.
 */
@SuppressWarnings("deprecation")
public class DeprecatedToXmlShimTest {

  @Test
  public void xmlClassSerializesItsMethodsAndParameters() {
    XmlClass xmlClass = new XmlClass("com.example.SampleTest", 0, false);
    xmlClass.setParameters(Collections.singletonMap("browser", "firefox"));
    XmlInclude include = new XmlInclude("shouldRun");
    include.setDescription("a described method");
    xmlClass.setIncludedMethods(Collections.singletonList(include));
    xmlClass.setExcludedMethods(Collections.singletonList("shouldNotRun"));

    assertThat(xmlClass.toXml("  "))
        .isEqualTo(
            "  <class name=\"com.example.SampleTest\">" //
                + EOL
                + "    <parameter name=\"browser\" value=\"firefox\"/>"
                + EOL
                + "    <methods>"
                + EOL
                + "      <include name=\"shouldRun\" description=\"a described method\"/>"
                + EOL
                + "      <exclude name=\"shouldNotRun\"/>"
                + EOL
                + "    </methods>"
                + EOL
                + "  </class> <!-- com.example.SampleTest -->"
                + EOL);
  }

  @Test
  public void xmlClassWithoutMethodsOrParametersIsAnEmptyElement() {
    XmlClass xmlClass = new XmlClass("com.example.SampleTest", 0, false);

    assertThat(xmlClass.toXml("  ")).isEqualTo("  <class name=\"com.example.SampleTest\"/>" + EOL);
  }

  @Test
  public void xmlIncludeSerializesItsInvocationNumbers() {
    XmlInclude include = new XmlInclude("shouldRun", Arrays.asList(1, 3), 0);

    assertThat(include.toXml("  "))
        .isEqualTo("  <include name=\"shouldRun\" invocation-numbers=\"1 3\"/>" + EOL);
  }

  @Test
  public void xmlIncludeSerializesItsParameters() {
    XmlInclude include = new XmlInclude("shouldRun");
    include.setParameters(Collections.singletonMap("browser", "firefox"));

    assertThat(include.toXml("  "))
        .isEqualTo(
            "  <include name=\"shouldRun\">" //
                + EOL
                + "    <parameter name=\"browser\" value=\"firefox\"/>"
                + EOL
                + "  </include> <!-- shouldRun -->"
                + EOL);
  }

  @Test
  public void xmlPackageSerializesItsIncludesAndExcludes() {
    XmlPackage xmlPackage = new XmlPackage("com.example");
    xmlPackage.setInclude(Collections.singletonList("Included"));
    xmlPackage.setExclude(Collections.singletonList("Excluded"));

    assertThat(xmlPackage.toXml("  "))
        .isEqualTo(
            "  <package name=\"com.example\">" //
                + EOL
                + "    <include name=\"Included\"/>"
                + EOL
                + "    <exclude name=\"Excluded\"/>"
                + EOL
                + "  </package> <!-- com.example -->"
                + EOL);
  }

  @Test
  public void xmlPackageWithoutFiltersIsAnEmptyElement() {
    assertThat(new XmlPackage("com.example").toXml("  "))
        .isEqualTo("  <package name=\"com.example\"/>" + EOL);
  }

  @Test
  public void xmlGroupsSerializesDefinesRunAndDependencies() {
    XmlGroups groups = new XmlGroups();
    XmlDefine define = new XmlDefine();
    define.setName("all");
    define.onElement("fast");
    groups.addDefine(define);
    XmlRun run = new XmlRun();
    run.onInclude("fast");
    run.onExclude("slow");
    groups.setRun(run);
    XmlDependencies dependencies = new XmlDependencies();
    dependencies.onGroup("fast", "slow");
    groups.setXmlDependencies(dependencies);

    assertThat(groups.toXml("  "))
        .isEqualTo(
            "  <groups>" //
                + EOL
                + "    <define name=\"all\">"
                + EOL
                + "      <include name=\"fast\"/>"
                + EOL
                + "    </define> <!-- all -->"
                + EOL
                + "    <run>"
                + EOL
                + "      <include name=\"fast\"/>"
                + EOL
                + "      <exclude name=\"slow\"/>"
                + EOL
                + "    </run>"
                + EOL
                + "    <dependencies>"
                + EOL
                + "      <group depends-on=\"slow\" name=\"fast\"/>"
                + EOL
                + "    </dependencies>"
                + EOL
                + "  </groups>"
                + EOL);
  }

  @Test
  public void emptyXmlGroupsSerializesToNothing() {
    assertThat(new XmlGroups().toXml("  ")).isEmpty();
  }

  @Test
  public void xmlMethodSelectorOmitsTheDefaultPriority() {
    XmlMethodSelector selector = new XmlMethodSelector();
    selector.setName("com.example.Selector");
    selector.setPriority(XmlMethodSelector.DEFAULT_PRIORITY);

    assertThat(selector.toXml("  "))
        .isEqualTo(
            "  <method-selector>" //
                + EOL
                + "    <selector-class name=\"com.example.Selector\"/>"
                + EOL
                + "  </method-selector>"
                + EOL);
  }

  /**
   * A negative priority short-circuits {@code RunInfo#includeMethod}, so dropping it changes what
   * the suite does. It was dropped until #3315; the shim must not bring that back.
   */
  @Test
  public void xmlMethodSelectorWritesANegativePriority() {
    XmlMethodSelector selector = new XmlMethodSelector();
    selector.setName("com.example.Selector");
    selector.setPriority(-1);

    assertThat(selector.toXml("  "))
        .contains("<selector-class name=\"com.example.Selector\" priority=\"-1\"/>");
  }

  @Test
  public void xmlMethodSelectorsWrapsItsSelectors() {
    XmlMethodSelectors selectors = new XmlMethodSelectors();
    XmlMethodSelector selector = new XmlMethodSelector();
    selector.setName("com.example.Selector");
    selectors.setMethodSelector(selector);

    assertThat(selectors.toXml("  "))
        .isEqualTo(
            "  <method-selectors>" //
                + EOL
                + "    <method-selector>"
                + EOL
                + "      <selector-class name=\"com.example.Selector\"/>"
                + EOL
                + "    </method-selector>"
                + EOL
                + "  </method-selectors>"
                + EOL);
  }

  @Test
  public void xmlDependenciesSerializesItsGroups() {
    XmlDependencies dependencies = new XmlDependencies();
    dependencies.onGroup("fast", "slow");

    assertThat(dependencies.toXml("  "))
        .isEqualTo(
            "  <dependencies>" //
                + EOL
                + "    <group depends-on=\"slow\" name=\"fast\"/>"
                + EOL
                + "  </dependencies>"
                + EOL);
  }

  @Test
  public void emptyXmlDependenciesSerializesToNothing() {
    assertThat(new XmlDependencies().toXml("  ")).isEmpty();
  }

  @Test
  public void xmlDefineSerializesItsIncludes() {
    XmlDefine define = new XmlDefine();
    define.setName("all");
    define.onElement("fast");

    assertThat(define.toXml("  "))
        .isEqualTo(
            "  <define name=\"all\">" //
                + EOL
                + "    <include name=\"fast\"/>"
                + EOL
                + "  </define> <!-- all -->"
                + EOL);
  }

  @Test
  public void xmlRunSerializesItsIncludesAndExcludes() {
    XmlRun run = new XmlRun();
    run.onInclude("fast");
    run.onExclude("slow");

    assertThat(run.toXml("  "))
        .isEqualTo(
            "  <run>" //
                + EOL
                + "    <include name=\"fast\"/>"
                + EOL
                + "    <exclude name=\"slow\"/>"
                + EOL
                + "  </run>"
                + EOL);
  }

  /** The deprecated {@code XmlUtils} entry point must keep writing what it used to. */
  @Test
  public void xmlUtilsDumpParametersStillWrites() {
    org.testng.reporters.XMLStringBuffer xsb = new org.testng.reporters.XMLStringBuffer("  ");

    XmlUtils.dumpParameters(xsb, Collections.singletonMap("k", "v"));

    assertThat(xsb.toXML()).isEqualTo("  <parameter name=\"k\" value=\"v\"/>" + EOL);
  }
}
