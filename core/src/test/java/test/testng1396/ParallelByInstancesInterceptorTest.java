package test.testng1396;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;

import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.fail;

public class ParallelByInstancesInterceptorTest {

    @Test(description = "test for https://github.com/cbeust/testng/issues/1396")
    public void should_honor_interceptor_order_when_running_parallel_instances() {
        TestNG tng = new TestNG();
        tng.setVerbose(0);

        // 10 instances is enough that we see the non-deterministic ordering of hashmaps more often than not
        tng.setTestClasses(new Class[]{
                SampleFor1396.Sample9For1396.class,
                SampleFor1396.Sample8For1396.class,
                SampleFor1396.Sample7For1396.class,
                SampleFor1396.Sample6For1396.class,
                SampleFor1396.Sample5For1396.class,
                SampleFor1396.Sample4For1396.class,
                SampleFor1396.Sample3For1396.class,
                SampleFor1396.Sample2For1396.class,
                SampleFor1396.Sample1For1396.class,
                SampleFor1396.Sample0For1396.class
        });

        // bug only exists when running parallel by instances with this flag set to false
        tng.setParallel(XmlSuite.ParallelMode.INSTANCES);
        tng.setPreserveOrder(false);
        // thread count of 1 forces sequential execution so we can verify ordering
        tng.setThreadCount(1);

        TestListenerAdapter adapter = new TestListenerAdapter();
        tng.addListener((ITestNGListener) adapter);

        // Runs tests annotated with @TestNG1396HighPriority before tests without it
        ReverseOrderTestInterceptor listener = new ReverseOrderTestInterceptor();
        tng.addListener(listener);

        tng.run();

        List<ITestResult> passedTests = adapter.getPassedTests();

        boolean lowPrioritySeen = false;

        for (ITestResult testResult : passedTests) {
            if (isHighPriority(testResult.getMethod())) {
                if (lowPrioritySeen) {
                    fail("high priority should be first");
                }
            } else {
                lowPrioritySeen = true;
            }
        }
    }

    private static boolean isHighPriority(IMethodInstance instance) {
        return instance.getInstance().getClass().getAnnotation(TestNG1396HighPriority.class) != null;
    }

    private static boolean isHighPriority(ITestNGMethod method) {
        return method.getInstance().getClass().getAnnotation(TestNG1396HighPriority.class) != null;
    }

    public class ReverseOrderTestInterceptor implements IMethodInterceptor {
        @Override
        public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
            List<IMethodInstance> sorted = Lists.newArrayList(methods);
            sorted.sort(new PriorityComparator());
            return sorted;
        }
    }

    public class PriorityComparator implements Comparator<IMethodInstance> {
        @Override
        public int compare(IMethodInstance o1, IMethodInstance o2) {
            if (isHighPriority(o1)) {
                if (isHighPriority(o2)) {
                    return compareMethodNames(o1, o2);
                }
                return -1;
            }
            if (isHighPriority(o2)) {
                return 1;
            }
            return compareMethodNames(o1, o2);
        }

        private int compareMethodNames(IMethodInstance o1, IMethodInstance o2) {
            return o2.getMethod().getQualifiedName().compareTo(o1.getMethod().getQualifiedName());
        }
    }
}
