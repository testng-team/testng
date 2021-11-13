package org.testng.test.osgi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.testng.test.osgi.DefaultTestngOsgiOptions.defaultTestngOsgiOptions;

import java.lang.reflect.Method;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

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
    Class<?> versionClass = Class.forName("org.testng.internal.Version");
    Method getVersionStringMethod = versionClass.getMethod("getVersionString");
    Object version = getVersionStringMethod.invoke(null);

    assertThat(version)
        .matches(
            (v) -> String.valueOf(v).length() > 0 && Character.isDigit(String.valueOf(v).charAt(0)),
            "Version.getVersionString() should start with a digit");
  }
}
