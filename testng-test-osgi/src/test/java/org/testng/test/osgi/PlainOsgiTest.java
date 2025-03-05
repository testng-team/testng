package org.testng.test.osgi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.testng.test.osgi.DefaultTestngOsgiOptions.defaultTestngOsgiOptions;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.IModuleFactory;
import org.testng.TestNG;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.internal.Version;
import org.testng.internal.YamlParser;
import org.testng.xml.XmlSuite;

/**
 * The purpose of the class is to ensure {@code postgresql} bundle activation does not fail in case
 * {@code org.osgi.service.jdbc} is not available.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class PlainOsgiTest {
  @Configuration
  public Option[] config() {
    return options(defaultTestngOsgiOptions());
  }

  @Test
  public void versionShouldStartWithDigit() throws Exception {
    String version = String.valueOf(Version.getVersionString());

    assertThat(version)
        .matches(
            (v) -> !v.isEmpty() && Character.isDigit(v.charAt(0)),
            "Version.getVersionString() should start with a digit but was " + version);
  }

  @Test
  public void guiceModuleFactoryLoads() {
    assertThat(IModuleFactory.class.getMethods()).isNotEmpty();
  }

  @Test
  public void jcommanderLoads() {
    assertThat(TestNG.class.getFields()).isNotEmpty();
  }

  @Test
  public void yamlLoads() {
    YamlParser parser = new YamlParser();
    ByteArrayInputStream input =
        new ByteArrayInputStream("name: My_Suite\n".getBytes(StandardCharsets.UTF_8));
    XmlSuite suite = parser.parse("test.yml", input, false);
    assertThat(suite).extracting("name").isEqualTo("My_Suite");
  }
}
