package org.testng.xml;

import org.testng.annotations.Test;
import test.SimpleBaseTest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlTestTest extends SimpleBaseTest {
    @Test
    public void testNameMatchesAny() {
        XmlSuite xmlSuite = createDummySuiteWithTestNamesAs("test1");
        XmlTest xmlTest = xmlSuite.getTests().get(0);
        assertThat(xmlTest.nameMatchesAny(Collections.singletonList("test1"))).isTrue();
        assertThat(xmlTest.nameMatchesAny(Collections.singletonList("test2"))).isFalse();
    }
}
