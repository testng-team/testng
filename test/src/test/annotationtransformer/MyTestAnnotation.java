package test.annotationtransformer;

import java.lang.reflect.Method;

import org.testng.internal.annotations.ITest;

public class MyTestAnnotation extends TestDelegateAnnotation {

  public MyTestAnnotation(ITest annotation) {
    super(annotation);
  }

  public int getInvocationCount() {
    Method m = getMethod();
    
    if (m.getName().equals("four")) {
      return 4;
    }
    else if (m.getName().equals("three")) {
      return 3;
    }
    else {
      return super.getInvocationCount();
    }
  }

}
