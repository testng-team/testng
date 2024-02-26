package test.listeners.factory;

import java.util.concurrent.atomic.AtomicBoolean;
import org.testng.IExecutionListener;

public class ExampleListener implements IExecutionListener {

  public static ExampleListener instance;

  private static final AtomicBoolean once = new AtomicBoolean(false);

  public ExampleListener() {
    setInstance(this);
  }

  private static void setInstance(ExampleListener instance) {
    if (once.compareAndSet(false, true)) {
      if (ExampleListener.instance == null) {
        ExampleListener.instance = instance;
      }
    }
  }
}
