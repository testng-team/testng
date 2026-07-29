package test.listeners.cliwiring;

import org.testng.IExecutionListener;

public class FirstWiringListener implements IExecutionListener {
  @Override
  public void onExecutionStart() {
    WiringLog.record("first");
  }
}
