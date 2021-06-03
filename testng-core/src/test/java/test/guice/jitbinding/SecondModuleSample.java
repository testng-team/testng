package test.guice.jitbinding;

import com.google.inject.AbstractModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@Guice(modules = SecondModuleSample.SecondModule.class)
public class SecondModuleSample {
    static class SecondModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).toInstance(new String("Hello"));
        }
    }

    @Inject
    String value;

    @Test
    public void testInject() {
        assertThat(value).isEqualTo("Hello");
    }
}
