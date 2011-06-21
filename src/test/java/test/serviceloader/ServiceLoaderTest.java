package test.serviceloader;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.Assert;

public class ServiceLoaderTest extends SimpleBaseTest {

  @Test
  public void serviceLoaderShouldWork() throws MalformedURLException {
    TestNG tng = create(ServiceLoaderSampleTest.class);
    String jarPath = getPathToResource("serviceloader.jar");
    URL url = new URL("file://" + new File(jarPath).getAbsolutePath());
    URLClassLoader ucl = URLClassLoader.newInstance(new URL[] { url });
    tng.setServiceLoaderClassLoader(ucl);
    tng.run();

    Assert.assertEquals(1, tng.getServiceLoaderListeners().size());
  }
}
