package test.multiple;

import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class
 *
 * @author cbeust
 */
public class ThisFactory {

  @Factory
  public Object[] create() {
    List result = new ArrayList();
    for (int i = 0; i < 10; i++) {
      result.add(new Test1());
    }

    return result.toArray();
  }

}
