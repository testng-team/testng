package test.dataprovider.issue2504;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class SampleTestCaseListener implements ITestListener {

  private final List<Integer> parameters = new ArrayList<>();

  @Override
  public void onTestStart(ITestResult result) {
    parameters.addAll(
        Arrays.stream(result.getParameters())
            .mapToInt(each -> (int) each)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
  }

  public List<Integer> getParameters() {
    return parameters;
  }
}
