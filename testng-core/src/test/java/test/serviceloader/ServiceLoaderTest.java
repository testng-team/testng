package test.serviceloader;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.testng.Assert;
import org.testng.CommandLineArgs;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.listeners.ListenerAssert;

public class ServiceLoaderTest extends SimpleBaseTest {

  @Test
  public void serviceLoaderShouldWork() {
    TestNG tng = create(ServiceLoaderSampleTest.class);
    URL url = getClass().getClassLoader().getResource("serviceloader.jar");
    URLClassLoader ucl = URLClassLoader.newInstance(new URL[] {url});
    tng.setServiceLoaderClassLoader(ucl);
    tng.run();

    ListenerAssert.assertListenerType(tng.getServiceLoaderListeners(), TmpSuiteListener.class);
  }

  @Test(description = "GITHUB-2259")
  public void ensureSpiLoadedListenersCanBeSkipped() {
    TestNG tng = create(ServiceLoaderSampleTest.class);
    URL url = getClass().getClassLoader().getResource("serviceloader.jar");
    URLClassLoader ucl = URLClassLoader.newInstance(new URL[] {url});
    tng.setServiceLoaderClassLoader(ucl);
    String dontLoad = "test.serviceloader.TmpSuiteListener";
    tng.setListenersToSkipFromBeingWiredInViaServiceLoaders(dontLoad);
    tng.run();
    List<String> loaded =
        tng.getServiceLoaderListeners().stream()
            .map(l -> l.getClass().getName())
            .collect(Collectors.toList());
    assertThat(loaded).doesNotContain(dontLoad);
  }

  @Test(description = "GITHUB-2259")
  @SuppressWarnings("deprecation")
  public void ensureSpiLoadedListenersCanBeSkipped2() {
    TestNG tng = create(ServiceLoaderSampleTest.class);
    URL url = getClass().getClassLoader().getResource("serviceloader.jar");
    URLClassLoader ucl = URLClassLoader.newInstance(new URL[] {url});
    tng.setServiceLoaderClassLoader(ucl);
    String dontLoad = "test.serviceloader.TmpSuiteListener";
    Map<String, String> cli = new HashMap<>();
    cli.put(CommandLineArgs.LISTENERS_TO_SKIP_VIA_SPI, dontLoad);
    tng.configure(cli);
    tng.run();
    List<String> loaded =
        tng.getServiceLoaderListeners().stream()
            .map(l -> l.getClass().getName())
            .collect(Collectors.toList());
    assertThat(loaded).doesNotContain(dontLoad);
  }

  @Test
  public void serviceLoaderWithNoClassLoader() {
    // Here ServiceLoader is expected to rely on the current context class loader to load the
    // service loader file
    // Since serviceloader.jar doesn't seem to be visible to the current thread's contextual class
    // loader
    // resorting to pushing in a class loader into the current thread that can load the resource
    URL url = getClass().getClassLoader().getResource("serviceloader.jar");
    URLClassLoader ucl = URLClassLoader.newInstance(new URL[] {url});
    Thread.currentThread().setContextClassLoader(ucl);
    TestNG tng = create(ServiceLoaderSampleTest.class);
    tng.run();

    ListenerAssert.assertListenerType(tng.getServiceLoaderListeners(), TmpSuiteListener.class);
  }

  @Test(description = "GITHUB-491")
  public void serviceLoaderShouldWorkWithConfigurationListener() {
    TestNG tng = create(ServiceLoaderSampleTest.class);
    tng.run();

    Assert.assertEquals(2, tng.getServiceLoaderListeners().size());
    ListenerAssert.assertListenerType(
        tng.getServiceLoaderListeners(), MyConfigurationListener.class);
  }
}
