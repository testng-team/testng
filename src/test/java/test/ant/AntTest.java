package test.ant;

import org.apache.tools.ant.BuildFileRule;
import org.apache.tools.ant.FileUtilities;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class AntTest {

    private final BuildFileRule rule = new BuildFileRule();

    @Test
    public void testSimple() throws IOException {
        rule.configureProject("src/test/resources/ant/build-simple.xml");
        rule.executeTarget("testng");
        File expected = rule.getProject().resolveFile("expected/ant-simple.test");
        assertThat(rule.getLog()).isEqualToIgnoringNewLines(FileUtilities.getFileContents(expected));
    }

    @Test
    public void testReporter() {
        MyReporter.expectedFilter = "*insert*";
        MyReporter.expectedFiltering = true;

        rule.configureProject("src/test/resources/ant/build-reporter-config.xml");
        rule.executeTarget("testng");
    }
}
