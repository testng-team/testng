package test.multiple;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Factory;

/**
 * This class
 *
 * @author cbeust
 */
public class ThisFactory {

  @Factory
  public Object[] create() {
    List<Test1> result = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      result.add(new Test1());
    }

    return result.toArray();
  }
}
