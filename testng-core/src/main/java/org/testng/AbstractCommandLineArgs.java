package org.testng;

import org.testng.collections.Lists;
import org.testng.internal.ClassHelper;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlMethodSelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCommandLineArgs implements CommandLineArgs {

  private static final Logger LOGGER = Logger.getLogger(TestNG.class);

  protected abstract String[] getListenerValues();

  public List<Class<? extends ITestNGListener>> getListener() {
    String[] strs = getListenerValues();
    if (strs == null) {
      return null;
    }
    List<Class<? extends ITestNGListener>> classes = Lists.newArrayList();
    for (String cls : strs) {
      Class<?> clazz = ClassHelper.fileToClass(cls);
      if (ITestNGListener.class.isAssignableFrom(clazz)) {
        classes.add((Class<? extends ITestNGListener>) clazz);
      }
    }

    return classes;
  }

  protected abstract String getMethodSelectorsValue();

  @Override
  public List<XmlMethodSelector> getMethodSelectors() {
    String methodSelectors = getMethodSelectorsValue();
    if (methodSelectors == null) {
      return null;
    }
    String[] strs = Utils.split(methodSelectors, ",");
    List<XmlMethodSelector> selectors = new ArrayList<>(strs.length);
    for (String cls : strs) {
      String[] sel = Utils.split(cls, ":");
      try {
        if (sel.length == 2) {
          XmlMethodSelector selector = new XmlMethodSelector();
          selector.setName(sel[0]);
          selector.setPriority(Integer.parseInt(sel[1]));
          selectors.add(selector);
        } else {
          LOGGER.error("Method selector value was not in the format org.example.Selector:4");
        }
      } catch (NumberFormatException nfe) {
        LOGGER.error("Method selector value was not in the format org.example.Selector:4");
      }
    }
    return selectors;
  }

  protected abstract String getObjectFactoryValue();

  @Override
  public Class<? extends ITestObjectFactory> getObjectFactory() {
    String objectFactory = getObjectFactoryValue();
    if (objectFactory == null) {
      return null;
    }
    return (Class<? extends ITestObjectFactory>) ClassHelper.fileToClass(objectFactory);
  }

  protected abstract String getTestClassValue();

  @Override
  public List<Class<?>> getTestClass() {
    String testClass = getTestClassValue();
    if (testClass == null) {
      return null;
    }
    String[] strClasses = testClass.split(",");
    List<Class<?>> classes = Lists.newArrayList();
    for (String c : strClasses) {
      classes.add(ClassHelper.fileToClass(c));
    }

    return classes;
  }

  protected abstract String getTestNamesValue();

  @Override
  public List<String> getTestNames() {
    String testNames = getTestNamesValue();
    if (testNames == null) {
      return null;
    }
    return Arrays.asList(testNames.split(","));
  }

  protected abstract String getTestRunnerFactoryValue();

  @Override
  public Class<? extends ITestRunnerFactory> getTestRunnerFactory() {
    String testRunnerFactory = getObjectFactoryValue();
    if (testRunnerFactory == null) {
      return null;
    }
    return (Class<? extends ITestRunnerFactory>) ClassHelper.fileToClass(testRunnerFactory);
  }

  protected abstract String getSpiListenersToSkipValue();

  @Override
  public List<String> getSpiListenersToSkip() {
    String spiListenersToSkip = getSpiListenersToSkipValue();
    return Arrays.asList(spiListenersToSkip.split(","));
  }
}
