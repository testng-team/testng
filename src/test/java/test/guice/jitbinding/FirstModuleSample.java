package test.guice.jitbinding;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.AbstractModule;
import javax.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = FirstModuleSample.FirstModule.class)
public class FirstModuleSample {

  @Inject
  String value;

  @Test
  public void testInject() {
    assertThat(value).isEqualTo("");
  }

  static class FirstModule extends AbstractModule {

    @Override
    protected void configure() {
      // no explicit binding, so value will be just in time bound to `new String()`
    }
  }
}
