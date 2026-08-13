package test.listeners.github1735;

import java.util.ArrayList;
import java.util.List;
import org.testng.IExecutionListener;

public class LocalExecutionListener implements IExecutionListener {
  private static final List<String> start = new ArrayList<>();
  private static final List<String> finish = new ArrayList<>();

  @Override
  public void onExecutionStart() {
    start.add("start");
  }

  @Override
  public void onExecutionFinish() {
    finish.add("finish");
  }

  public static List<String> getFinish() {
    return finish;
  }

  public static List<String> getStart() {
    return start;
  }
}
