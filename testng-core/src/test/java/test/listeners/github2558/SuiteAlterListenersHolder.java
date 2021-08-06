package test.listeners.github2558;

import java.util.List;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

public class SuiteAlterListenersHolder {

  public static class SuiteAlterA implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
      CallHolder.addCall(getClass().getName() + ".alter()");
    }
  }

  public static class SuiteAlterB implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
      CallHolder.addCall(getClass().getName() + ".alter()");
    }
  }
}
