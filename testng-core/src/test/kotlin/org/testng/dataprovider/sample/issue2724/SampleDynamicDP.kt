package org.testng.dataprovider.sample.issue2724

import org.testng.annotations.Test

class SampleDynamicDP {

    @Suppress("UNUSED_PARAMETER")
    @Test(
        dataProviderDynamicClass = "org.testng.dataprovider.sample.issue2724.DataProviders",
        dataProvider = "data"
    )
    fun testDynamicDataProvider(name: String, age: Int, status: String) {

    }
}
