package test.name.github1046;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.collections.Sets;
import org.testng.xml.XmlSuite;

import java.util.List;
import java.util.Set;

public class LocalTestNameGatherer implements IReporter {
    private Set<String> testnames = Sets.newHashSet();
    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        for (ISuite suite: suites) {
            for(ISuiteResult suiteResult : suite.getResults().values()) {
                List<ITestResult> testResults = Lists.newArrayList();
                testResults.addAll(suiteResult.getTestContext().getPassedTests().getAllResults());
                testResults.addAll(suiteResult.getTestContext().getFailedTests().getAllResults());
                testResults.addAll(suiteResult.getTestContext().getSkippedTests().getAllResults());
                for (ITestResult result : testResults) {
                    testnames.add(result.getName());
                }
            }
        }

    }

    public Set<String> getTestnames() {
        return testnames;
    }
}
