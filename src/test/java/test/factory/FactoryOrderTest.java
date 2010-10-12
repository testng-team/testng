package test.factory;


import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

public class FactoryOrderTest {

   public FactoryOrderTest() {
//       System.out.println("inside testFactory constructor");
   }

   @Factory
   public static Object[] testF()
   throws Exception {
       List result = new ArrayList();
//       System.out.println("inside factory: ");
       int i = 0;
       while (i < 5) {
           result.add(new FactoryOrderSampleTest(i));
           i++;
       }
       return result.toArray();
   }
}