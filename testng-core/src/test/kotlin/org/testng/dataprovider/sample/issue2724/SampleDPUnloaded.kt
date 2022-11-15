package org.testng.dataprovider.sample.issue2724

import jlibs.core.lang.RuntimeUtil
import org.testng.annotations.AfterClass
import org.testng.annotations.Test
import org.testng.dataprovider.DynamicDataProviderLoadingTest

class SampleDPUnloaded {
    @Suppress("UNUSED_PARAMETER")
    @Test(
        dataProviderDynamicClass = "org.testng.dataprovider.sample.issue2724.DataProviders",
        dataProvider = "data"
    )
    fun testDynamicDataProvider(name: String, age: Int, status: String) {

    }

    @AfterClass
    fun afterTest() {
        RuntimeUtil.gc(10)
        DynamicDataProviderLoadingTest.saveMemDump(System.getProperty("memdump.path"))
    }
}
