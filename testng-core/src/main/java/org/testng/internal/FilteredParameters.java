package org.testng.internal;

import java.util.Iterator;
import java.util.List;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;

class FilteredParameters implements Iterator<Object[]> {

  private int index = 0;
  private boolean hasWarn = false;
  private final Iterator<Object[]> parameters;
  private final ITestNGMethod testMethod;
  private final String dataProviderName;
  private final List<Integer> indices;

  public FilteredParameters(
      Iterator<Object[]> parameters,
      ITestNGMethod testMethod,
      String dataProviderName,
      List<Integer> indices) {
    this.parameters = parameters;
    this.testMethod = testMethod;
    this.dataProviderName = dataProviderName;
    this.indices = indices;
  }

  @Override
  public boolean hasNext() {
    if (index == 0 && !parameters.hasNext() && !hasWarn) {
      hasWarn = true;
      String msg =
          String.format(
              "The test method '%s' will be skipped since its "
                  + "data provider '%s' "
                  + "returned an empty array or iterator. ",
              testMethod.getQualifiedName(), dataProviderName);
      Utils.warn(msg);
    }
    return parameters.hasNext();
  }

  @Override
  public Object[] next() {
    testMethod.setParameterInvocationCount(index);
    Object[] next = parameters.next();
    if (next == null) {
      throw new TestNGException("Parameters must not be null");
    }
    if (!indices.isEmpty() && !indices.contains(index)) {
      // Skip parameters
      next = null;
    }
    index++;
    return next;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove");
  }
}
