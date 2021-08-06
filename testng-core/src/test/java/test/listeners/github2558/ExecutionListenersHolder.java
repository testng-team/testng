package test.listeners.github2558;

import org.testng.IExecutionListener;

public class ExecutionListenersHolder {

  public static class ExecutionListenerA implements IExecutionListener {

    @Override
    public void onExecutionStart() {
      CallHolder.addCall(getClass().getName() + ".onExecutionStart()");
    }

    @Override
    public void onExecutionFinish() {
      CallHolder.addCall(getClass().getName() + ".onExecutionFinish()");
    }
  }

  public static class ExecutionListenerB implements IExecutionListener {

    @Override
    public void onExecutionStart() {
      CallHolder.addCall(getClass().getName() + ".onExecutionStart()");
    }

    @Override
    public void onExecutionFinish() {
      CallHolder.addCall(getClass().getName() + ".onExecutionFinish()");
    }
  }
}
