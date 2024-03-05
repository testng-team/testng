package test.listeners.factory;

import java.util.concurrent.atomic.AtomicReference;
import org.testng.IExecutionListener;

public class ExampleListener implements IExecutionListener {

  private static final AtomicReference<ExampleListener> instance = new AtomicReference<>();

  public ExampleListener() {
    setInstance(this);
  }

  private static void setInstance(ExampleListener instance) {
    ExampleListener.instance.compareAndSet(null, instance);
  }

  public static ExampleListener getInstance() {
    return instance.get();
  }
}
