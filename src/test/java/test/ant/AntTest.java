package test.ant;

import org.apache.tools.ant.BuildFileRule;
import org.apache.tools.ant.FileUtilities;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class AntTest {

    private final BuildFileRule rule = new BuildFileRule();

    @BeforeMethod
    public void setUp() {
        rule.configureProject("src/test/resources/ant/build-simple.xml");
    }

    @Test
    public void test() throws IOException {
        rule.executeTarget("testng");
        File expected = rule.getProject().resolveFile("expected/ant-simple.test");
        assertThat(rule.getLog()).isEqualToIgnoringNewLines(FileUtilities.getFileContents(expected));
    }
}
