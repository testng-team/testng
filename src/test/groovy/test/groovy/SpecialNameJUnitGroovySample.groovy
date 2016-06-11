package test.groovy

import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

// Sample from http://groovy-lang.org/testing.html#_junit_4
class SpecialNameJUnitGroovySample {

    @Test
    void "index Out Of Bounds Access"() {
        def numbers = [1, 2, 3, 4]
        shouldFail {
            numbers.get(4)
        }
    }
}
