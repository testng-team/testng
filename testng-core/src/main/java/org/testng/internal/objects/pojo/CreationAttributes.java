package org.testng.internal.objects.pojo;

import java.util.Objects;
import org.testng.ITestContext;
import org.testng.internal.invokers.objects.GuiceContext;

/** Represents the parameters that are associated with object creation. */
public class CreationAttributes {

  private final BasicAttributes basic;
  private final DetailedAttributes detailed;
  private final ITestContext context;
  private final GuiceContext suiteContext;

  public CreationAttributes(ITestContext ctx, BasicAttributes basic, DetailedAttributes detailed) {
    Objects.requireNonNull(ctx);
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

  public DetailedAttributes getDetailedAttributes() {
    return detailed;
  }

  public BasicAttributes getBasicAttributes() {
    return basic;
  }

  public ITestContext getContext() {
    return context;
  }

  public GuiceContext getSuiteContext() {
    return suiteContext;
  }
}
