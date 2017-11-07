package org.testng.xml;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import test.SimpleBaseTest;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlSuiteTest extends SimpleBaseTest {

    @Test
    public void testIncludedAndExcludedGroups() {
        XmlSuite suite = new XmlSuite();
        suite.addIncludedGroup("foo");
        suite.addExcludedGroup("bar");
        assertThat(suite.getIncludedGroups()).containsExactly("foo");
        assertThat(suite.getExcludedGroups()).containsExactly("bar");
    }

    @Test
    public void testIncludedAndExcludedGroupsWithRun() {
        XmlRun xmlRun = new XmlRun();
        xmlRun.onInclude("foo");
        xmlRun.onExclude("bar");
        XmlGroups groups = new XmlGroups();
        groups.setRun(xmlRun);
        XmlSuite suite = new XmlSuite();
        suite.setGroups(groups);
        assertThat(suite.getIncludedGroups()).containsExactly("foo");
        assertThat(suite.getExcludedGroups()).containsExactly("bar");
    }

    @Test(dataProvider = "dp", description = "GITHUB-778")
    public void testTimeOut(String timeout, int size, int lineNumber) throws IOException {
        XmlSuite suite = new XmlSuite();
        suite.setTimeOut(timeout);
        StringReader stringReader = new StringReader(suite.toXml());
        List<String> resultLines = Lists.newArrayList();
        List<Integer> lineNumbers = grep(stringReader, "time-out=\"1000\"", resultLines);
        assertThat(lineNumbers).size().isEqualTo(size);
        assertThat(resultLines).size().isEqualTo(size);
        if (size > 0) {
            assertThat(lineNumbers.get(size-1)).isEqualTo(lineNumber);
        }
    }

    @DataProvider(name = "dp")
    public Object[][] getData() {
        return new Object[][]{
                {"1000", 1, 2},
                {"", 0, 0}
        };
    }

}
