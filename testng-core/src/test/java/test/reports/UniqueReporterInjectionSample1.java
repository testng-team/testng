package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(UniqueReporterInjectionTest.ReporterListenerForIssue1227.class)
public class UniqueReporterInjectionSample1 {
  @Test
  public void testMethod() {
    assertThat(true).isTrue();
  }
}
