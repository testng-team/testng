package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmptyDataProviderConfigurationThreadPoolSample {

  public static final AtomicInteger BEFORE = new AtomicInteger(0);
  public static final AtomicInteger AFTER = new AtomicInteger(0);
  public static final AtomicInteger FIRST_TIME_ONLY = new AtomicInteger(0);

  @BeforeMethod
  public void before() {
    BEFORE.incrementAndGet();
  }

  @BeforeMethod(firstTimeOnly = true)
  public void firstTimeOnly() {
    FIRST_TIME_ONLY.incrementAndGet();
  }

  @AfterMethod
  public void after() {
    AFTER.incrementAndGet();
  }

  @DataProvider
  public Object[][] emptyDp() {
    return new Object[0][];
  }

  @Test(dataProvider = "emptyDp", invocationCount = 3, threadPoolSize = 2)
  public void pooled(String value) {
    fail();
  }
}
