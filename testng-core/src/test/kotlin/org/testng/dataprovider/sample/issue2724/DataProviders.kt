package org.testng.dataprovider.sample.issue2724

import org.testng.annotations.DataProvider

class DataProviders {
    @DataProvider
    fun data() : Array<Array<Any>> {
        return arrayOf(
            arrayOf("Mike", 34, "student"),
            arrayOf("Mike", 23, "driver"),
            arrayOf("Paul", 20, "director")
        )
    }
}
