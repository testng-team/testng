package test.hook.samples

import java.lang.reflect.Method
import org.testng.IConfigurable
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeSuite
import org.testng.annotations.BeforeTest

abstract class BaseConfigurableSample : IConfigurable {

    @BeforeSuite
    fun bs() {
    }

    @BeforeTest
    fun bt() {
    }

    @BeforeMethod
    fun bm(m: Method) {
    }

    @BeforeClass
    fun bc() {
    }
}
