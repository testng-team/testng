package test.custom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;
import org.testng.collections.Lists;

public class CustomAttributesListener implements IInvokedMethodListener {

  private final List<CustomAttribute> attributes = Lists.newArrayList();

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    this.attributes.addAll(Arrays.asList(method.getTestMethod().getAttributes()));
  }

  public List<CustomAttribute> getAttributes() {
    return Collections.unmodifiableList(attributes);
  }
}
