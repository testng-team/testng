package test.guice.issue2570;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.lang.annotation.Retention;
import javax.inject.Qualifier;
import org.testng.IRetryAnalyzer;

public class GuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(IRetryAnalyzer.class).to(GuicePoweredConstructorInjectedRetry.class);
  }

  @Provides
  @DragonWarrior
  public static String dragonWarriorName() {
    return "Kungfu-Panda";
  }

  @Provides
  @Terminator
  public static String terminatorName() {
    return "Arnold";
  }

  @Qualifier
  @Retention(RUNTIME)
  @interface DragonWarrior {}

  @Qualifier
  @Retention(RUNTIME)
  @interface Terminator {}
}
