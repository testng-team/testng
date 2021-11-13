package test.thread.parallelization.sample;

import java.util.ArrayList;
import java.util.List;
import org.testng.TestNGException;
import org.testng.annotations.Factory;
import org.testng.internal.objects.InstanceCreator;

public class FactoryForTestClassBFourMethodsWithNoDepsFiveInstancesSample {
  @Factory
  public Object[] init() {
    List<Object> instances = new ArrayList<>();

    try {
      instances.add(InstanceCreator.newInstance(TestClassBFourMethodsWithNoDepsSample.class));
      instances.add(InstanceCreator.newInstance(TestClassBFourMethodsWithNoDepsSample.class));
      instances.add(InstanceCreator.newInstance(TestClassBFourMethodsWithNoDepsSample.class));
      instances.add(InstanceCreator.newInstance(TestClassBFourMethodsWithNoDepsSample.class));
      instances.add(InstanceCreator.newInstance(TestClassBFourMethodsWithNoDepsSample.class));
    } catch (TestNGException e) {
      throw new RuntimeException(
          "Could not instantiate an instance of TestClassBFourMethodsWithNoDepsSample", e);
    }

    return instances.toArray();
  }
}
