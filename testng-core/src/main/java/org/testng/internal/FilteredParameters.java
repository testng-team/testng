package org.testng.internal;

import java.util.Iterator;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;

/**
 * Hides the parameter rows an {@code indices} restriction excludes, by answering {@code null} for
 * them rather than skipping them, so that a position survives even when its row does not. The five
 * consumers do three different things with such a placeholder:
 *
 * <ul>
 *   <li>{@code MethodRunner.runInSequence} and {@code runInParallel} count it into {@code
 *       parametersIndex}. That is what keeps a reported -- and re-run -- index in the data
 *       provider's own numbering, and it is the reason the placeholder exists at all.
 *   <li>{@code TestInvoker}, when reporting a data-driven method as skipped, drops it.
 *   <li>{@code FactoryMethod} drops it too, which deliberately renumbers the *factory* axis:
 *       {@code @Factory(indices = ...)} selects among the rows that survived, not among the
 *       original ones. {@code test.dataprovider.IndicesTest.testIndicesFactory} pins that.
 *   <li>{@code TestInvoker.retryFailed} does neither: it walks to the target index and, if that
 *       position was excluded, keeps the values the retry came in with.
 * </ul>
 */
class FilteredParameters implements Iterator<Object @Nullable []> {

  private int index = 0;
  private boolean hasWarn = false;
  private final Iterator<Object @Nullable []> parameters;
  private final ITestNGMethod testMethod;
  private final String dataProviderName;
  private final List<Integer> indices;

  public FilteredParameters(
      Iterator<Object @Nullable []> parameters,
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
  public Object @Nullable [] next() {
    testMethod.setParameterInvocationCount(index);
    Object @Nullable [] next = parameters.next();
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
}
