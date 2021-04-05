package org.testng.internal.objects.pojo;

import java.util.Objects;
import org.testng.ITestContext;

/**
 * Represents the parameters that are associated with object creation.
 */
public class CreationAttributes {

  private final BasicAttributes basic;
  private final DetailedAttributes detailed;
  private final ITestContext context;

  public CreationAttributes(ITestContext ctx, BasicAttributes basic,
      DetailedAttributes detailed) {
    Objects.requireNonNull(ctx);
    this.basic = basic;
    this.detailed = detailed;
    this.context = ctx;
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
}
