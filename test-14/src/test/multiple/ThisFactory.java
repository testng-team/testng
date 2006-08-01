package test.multiple;

import java.util.ArrayList;
import java.util.List;

public class ThisFactory {
  
  /**
   * @testng.factory
   */
  public Object[] create() {
    List result = new ArrayList();
    for (int i = 0; i < 10; i++) {
      result.add(new Test1());
    }
    
    return result.toArray();
  }

}
