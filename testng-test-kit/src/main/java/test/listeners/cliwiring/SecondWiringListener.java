package test.listeners.cliwiring;

import org.testng.IExecutionListener;

public class SecondWiringListener implements IExecutionListener {
  @Override
  public void onExecutionStart() {
    WiringLog.record("second");
  }
}
