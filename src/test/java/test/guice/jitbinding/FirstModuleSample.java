package test.guice.jitbinding;

import com.google.inject.AbstractModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@Guice(modules = FirstModuleSample.FirstModule.class)
public class FirstModuleSample {
    static class FirstModule extends AbstractModule {
        @Override
        protected void configure() {
            // no explicit binding, so value will be just in time bound to `new String()`
        }
    }

    @Inject
    String value;

    @Test
    public void testInject() {
        assertThat(value).isEqualTo("");
    }
}
