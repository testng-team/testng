package org.testng.dataprovider.sample.issue2724

import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SampleSimpleDP {
    @Suppress("UNUSED_PARAMETER")
    @Test(dataProvider = "data")
    fun testDynamicDataProvider(name: String, age: Int, status: String) {

    }

    @DataProvider
    fun data() : Array<Array<Any>> {
        return arrayOf(
            arrayOf("Mike", 34, "student"),
            arrayOf("Mike", 23, "driver"),
            arrayOf("Paul", 20, "director")
        )
    }
}
