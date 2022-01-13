package org.testng.internal.samples.classhelper.issue1339;

open class PapaBearSample : GrandpaBearSample() {
    private fun secretMethod(foo: String) {}

    fun announcer(foo: String) {}

    fun inheritable(foo: String) {}
}
