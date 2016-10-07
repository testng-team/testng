package org.testng.xml;

import org.testng.Assert;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import java.util.Arrays;

public class XmlSuiteTest extends SimpleBaseTest {

    @Test
    public void testIncludedAndExcludedGroups() {
        XmlSuite suite = new XmlSuite();
        suite.addIncludedGroup("foo");
        suite.addExcludedGroup("bar");
        Assert.assertEquals(Arrays.asList("foo"), suite.getIncludedGroups());
        Assert.assertEquals(Arrays.asList("bar"), suite.getExcludedGroups());
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
        Assert.assertEquals(Arrays.asList("foo"), suite.getIncludedGroups());
        Assert.assertEquals(Arrays.asList("bar"), suite.getExcludedGroups());
    }

}
