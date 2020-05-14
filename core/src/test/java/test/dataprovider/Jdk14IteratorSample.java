package test.dataprovider;

import java.util.Arrays;
import java.util.Iterator;

public class Jdk14IteratorSample {

  public Iterator createData() {
    return Arrays.asList(new Object[] {"Cedric", 36}, new Object[] {"Anne Marie", 37}).iterator();
  }

  public void verifyNames(String firstName, Integer age) {}
}
