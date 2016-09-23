package test.dataprovider;

import java.util.Arrays;
import java.util.Iterator;

public class Jdk14IteratorSample {

  /**
   * @testng.data-provider name="test1"
   */
  public Iterator createData() {
    return Arrays.asList(
        new Object[]{"Cedric", 36},
        new Object[]{"Anne Marie", 37}
    ).iterator();
  }

  /**
   * @testng.test dataProvider="test1"
   */
  public void verifyNames(String firstName, Integer age) {
  }
}


