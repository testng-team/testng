package org.testng.internal.objects.pojo;

import org.jspecify.annotations.Nullable;
import org.testng.ITestContext;
import org.testng.internal.invokers.objects.GuiceContext;

/** Represents the parameters that are associated with object creation. */
public class CreationAttributes {

  private final BasicAttributes basic;
  private final @Nullable DetailedAttributes detailed;
  private final @Nullable ITestContext context;
  private final @Nullable GuiceContext suiteContext;

  public CreationAttributes(
      ITestContext ctx, BasicAttributes basic, @Nullable DetailedAttributes detailed) {
    this.basic = basic;
    this.detailed = detailed;
    this.context = ctx;
    this.suiteContext = null;
  }

  public CreationAttributes(BasicAttributes basic, GuiceContext suiteContext) {
    this.basic = basic;
    this.detailed = null;
    this.context = null;
    this.suiteContext = suiteContext;
  }

  public @Nullable DetailedAttributes getDetailedAttributes() {
    return detailed;
  }

  public BasicAttributes getBasicAttributes() {
    return basic;
  }

  public @Nullable ITestContext getContext() {
    return context;
  }

  public @Nullable GuiceContext getSuiteContext() {
    return suiteContext;
  }
}
