package org.testng.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.ITestOrConfiguration;
import org.testng.annotations.Lazy;
import org.testng.annotations.Test;
import org.testng.compatibility.LegacyApiConsumer;
import org.testng.internal.annotations.FactoryAnnotation;
import org.testng.internal.annotations.TestOrConfiguration;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.Parser;

/** Runs consumers compiled against TestNG 7.12.0 with the current merged jar. */
public class LegacyApiCompatibilityTest {

  @Test(description = "GITHUB-3615")
  public void legacyConfigurationKeepsTheDefaultPriority() {
    ITestOrConfiguration annotation = LegacyApiConsumer.configuration();
    assertThat(annotation.getPriority()).isZero();
    annotation.setPriority(-5);
    assertThat(annotation.getPriority()).isZero();
  }

  @Test(description = "GITHUB-3615")
  public void legacyFactoryLeavesLazyInstantiationUnset() {
    IFactoryAnnotation annotation = LegacyApiConsumer.factory();
    assertThat(annotation.getLazy()).isEqualTo(Lazy.UNSET);
    annotation.setLazy(Lazy.TRUE);
    annotation.setLazy(null);
    assertThat(annotation.getLazy()).isEqualTo(Lazy.UNSET);
  }

  @Test(description = "GITHUB-3615")
  public void currentAnnotationsStillStoreTheNewSettings() {
    ITestOrConfiguration configuration = new TestOrConfiguration();
    configuration.setPriority(-5);
    assertThat(configuration.getPriority()).isEqualTo(-5);

    IFactoryAnnotation factory = new FactoryAnnotation();
    factory.setLazy(Lazy.TRUE);
    assertThat(factory.getLazy()).isEqualTo(Lazy.TRUE);
    factory.setLazy(null);
    assertThat(factory.getLazy()).isEqualTo(Lazy.UNSET);
  }

  @Test(description = "GITHUB-3615")
  public void legacyDataProviderCreatesInstancesAndSkipsFailedRows() {
    Object[] instances =
        LegacyApiConsumer.wrapDataProvider(
            ConvertedTest.class,
            Arrays.asList(
                new Object[] {"first", 1}, new Object[] {"broken", 2}, new Object[] {"last", 3}));
    assertThat(instances)
        .extracting("name", "number")
        .containsExactly(tuple("first", 1), tuple("last", 3));
    assertThat(LegacyApiConsumer.wrapDataProvider(ConvertedTest.class, Collections.emptyList()))
        .isEmpty();
  }

  @Test(description = "GITHUB-3615")
  public void legacyConverterCreatesYamlAndXmlSuites() throws Exception {
    assertThat(LegacyApiConsumer.newConverter().getClass().getName())
        .isEqualTo("org.testng.Converter");
    Path directory = Files.createTempDirectory("legacy-converter");
    Path input = directory.resolve("suite.xml");
    Files.writeString(
        input,
        "<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">"
            + "<suite name=\"legacy\"><test name=\"converted\"><classes>"
            + "<class name=\"example.NotOnTheClasspath\"/></classes></test></suite>");
    Path yamlDirectory = directory.resolve("yaml");
    LegacyApiConsumer.convert(new String[] {"-d", yamlDirectory.toString(), input.toString()});
    Path yaml = yamlDirectory.resolve("suite.yaml");
    assertThat(yaml).exists();

    Path xmlDirectory = directory.resolve("xml");
    LegacyApiConsumer.convert(new String[] {"-d", xmlDirectory.toString(), yaml.toString()});
    Parser parser = new Parser(xmlDirectory.resolve("suite.xml").toString());
    parser.setLoadClasses(false);
    List<XmlSuite> suites = parser.parseToList();
    assertThat(suites).hasSize(1);
    assertThat(suites.get(0).getName()).isEqualTo("legacy");
    assertThat(suites.get(0).getTests().get(0).getName()).isEqualTo("converted");
    assertThat(suites.get(0).getTests().get(0).getXmlClasses().get(0).getName())
        .isEqualTo("example.NotOnTheClasspath");
  }

  public static class ConvertedTest {
    public final String name;
    public final int number;

    public ConvertedTest(String name, int number) {
      if ("broken".equals(name)) {
        throw new IllegalArgumentException("Cannot create this row");
      }
      this.name = name;
      this.number = number;
    }
  }
}
