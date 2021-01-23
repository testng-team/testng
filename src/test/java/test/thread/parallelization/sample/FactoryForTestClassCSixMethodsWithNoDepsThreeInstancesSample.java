package test.thread.parallelization.sample;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Factory;

public class FactoryForTestClassCSixMethodsWithNoDepsThreeInstancesSample {

  @Factory
  public Object[] init() {
    List<Object> instances = new ArrayList<>();

    try {
      instances.add(TestClassCSixMethodsWithNoDepsSample.class.newInstance());
      instances.add(TestClassCSixMethodsWithNoDepsSample.class.newInstance());
      instances.add(TestClassCSixMethodsWithNoDepsSample.class.newInstance());
    } catch (InstantiationException e) {
      throw new RuntimeException(
          "Could not instantiate an instance of TestClassCSixMethodsWithNoDepsSample because it is "
              +
              "abstract or for some other reason", e
      );
    } catch (IllegalAccessException e) {
      throw new RuntimeException(
          "Could not instantiate an instance of TestClassCSixMethodsWithNoDepsSample " +
              "FactoryForTestClassCSixMethodsWithNoDepsThreeInstancesSample does not have access to its "
              +
              "class definition", e
      );
    }

    return instances.toArray();
  }
}
