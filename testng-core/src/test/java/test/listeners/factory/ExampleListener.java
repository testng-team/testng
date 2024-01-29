package test.listeners.factory;

import org.testng.IExecutionListener;

public class ExampleListener implements IExecutionListener {

  public static ExampleListener instance;

  public ExampleListener() {
    setInstance(this);
  }

  private static synchronized void setInstance(ExampleListener instance) {
    if (ExampleListener.instance == null) {
      ExampleListener.instance = instance;
    }
  }
}
