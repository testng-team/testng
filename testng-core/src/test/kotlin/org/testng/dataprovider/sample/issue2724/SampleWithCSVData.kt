package org.testng.dataprovider.sample.issue2724

import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SampleWithCSVData {
    @Suppress("UNUSED_PARAMETER")
    @Test(dataProvider = "data")
    fun testDynamicDataProvider(name: String, age: Int, status: String) {

    }

    @DataProvider
    fun data() : Array<Array<Any>> {
        val fileInputStream = this::class.java.classLoader
            .getResourceAsStream("test/issue2724/data.csv")
        return fileInputStream.reader().readLines().map {
            it.split(",").toTypedArray<Any>()
        }.toTypedArray()
    }
}
