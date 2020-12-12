package org.testng.internal.listeners.parameterindex;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterIndexTestListener implements ITestListener {

    private List<Integer> indexes = new ArrayList<>();

    @Override
    public void onTestSuccess(ITestResult testResult) {
        indexes.add(((TestResult) testResult).getParameterIndex());
    }

    @Override
    public void onFinish(ITestContext testContext) {
        assertThat(indexes).contains(0, 1, 2);
    }

}
