package org.testng.internal.samples.classhelper.issue1456

import org.testng.ITest

class TestClassSample(var name: String) : ITest {
    override fun getTestName() = name
}
