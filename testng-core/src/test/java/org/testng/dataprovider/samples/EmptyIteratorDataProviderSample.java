package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import java.util.Collections;
import java.util.Iterator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmptyIteratorDataProviderSample {

  @DataProvider
  public Iterator<Object[]> emptyDp() {
    return Collections.<Object[]>emptyList().iterator();
  }

  @Test(dataProvider = "emptyDp")
  public void test(String value) {
    fail();
  }
}
