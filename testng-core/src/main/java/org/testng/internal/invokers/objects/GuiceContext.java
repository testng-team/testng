package org.testng.internal.invokers.objects;

import org.testng.IInjectorFactory;
import org.testng.internal.IConfiguration;
import org.testng.xml.XmlSuite;

public class GuiceContext {

  private final String parentModule;
  private final String guiceStage;
  private final String name;
  private final IInjectorFactory injectorFactory;

  public GuiceContext(XmlSuite s, IConfiguration cfg) {
    parentModule = s.getParentModule();
    guiceStage = s.getGuiceStage();
    name = s.getName();
    injectorFactory = cfg.getInjectorFactory();
  }

  public String getParentModule() {
    return parentModule;
  }

  public String getGuiceStage() {
    return guiceStage;
  }

  public String getName() {
    return name;
  }

  public IInjectorFactory getInjectorFactory() {
    return injectorFactory;
  }
}
