package test.tmp;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class C {
  @Test 
  public void testMethod() {
    System.out.print("C.testMethod " + Thread.currentThread().getId() + " ");
  }

  @DataProvider(name = "data", parallel = true) 
  public Object[][] data() { 
    final ArrayList<Integer[]> resa = new ArrayList<Integer[]>(); 
    for(int i = 0; i < 1000; i ++) { 
      resa.add(new Integer[]{i}); 
    } 
    return resa.toArray(new Object[1][resa.size()]); 
  }
}
