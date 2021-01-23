package test.guice.jitbinding;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.AbstractModule;
import javax.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = SecondModuleSample.SecondModule.class)
public class SecondModuleSample {

  @Inject
  String value;

  @Test
  public void testInject() {
    assertThat(value).isEqualTo("Hello");
  }

  static class SecondModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(String.class).toInstance(new String("Hello"));
    }
  }
}
