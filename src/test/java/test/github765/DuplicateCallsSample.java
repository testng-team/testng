package test.github765;

import com.beust.jcommander.internal.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class DuplicateCallsSample extends TestTemplate<Integer> {

    private int i = 0;
    static List<String> messages = Lists.newLinkedList();

    @Test(dataProvider = "testParameters")
    public void callExecuteTest(Integer testParameters) throws Exception {
        messages.add("Iteration [" + i++ + "], Test Parameter [" + testParameters + "]");
    }



    @DataProvider(name = "testParameters")
    public Object[][] getOnboardingTestParameters() {
        return new Object[][] {
            {4}
        };
    }
}
