package test.groovy

import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

// Sample from http://groovy-lang.org/testing.html#_junit_4
class EasyJUnitGroovySample {

    @Test
    void indexOutOfBoundsAccess() {
        def numbers = [1, 2, 3, 4]
        shouldFail {
            numbers.get(4)
        }
    }
}
