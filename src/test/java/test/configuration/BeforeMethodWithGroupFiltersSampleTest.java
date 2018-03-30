package test.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class BeforeMethodWithGroupFiltersSampleTest {

    static final List<String[]> EXPECTED_INVOCATION_SEQUENCES = Arrays.asList(
            new String[] {"beforeGroup1", "g1m1"},
            new String[] {"beforeGroup1", "g1m2"},
            new String[] {"beforeGroup2", "g2m1"},
            new String[] {"beforeGroup2", "g2m1"},
            new String[] {"beforeGroup2", "g2m1"}
        );
    static final int EXPECTED_TOTAL_INVOCATIONS = 10;
    static List<String> invocations;

    @BeforeSuite
    public void init() {
        invocations = new ArrayList<>();
    }

    @BeforeMethod(onlyForGroups = {"group1"})
    public void beforeGroup1() {
        invocations.add("beforeGroup1");
    }

    @Test(groups = "group1")
    public void g1m1() {
        invocations.add("g1m1");
    }

    @Test(groups = "group1")
    public void g1m2() {
        invocations.add("g1m2");
    }

    @BeforeMethod(onlyForGroups = {"group2"})
    public void beforeGroup2() {
        invocations.add("beforeGroup2");
    }

    @Test(groups = "group2")
    public void g2m1() {
        invocations.add("g2m1");
    }

    @Test(groups = "group2")
    public void g2m2() {
        invocations.add("g2m2");
    }

    @Test(groups = "group2")
    public void g2m3() {
        invocations.add("g2m3");
    }

}
