import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("net.ltgt.errorprone")
    id("build-logic.build-params")
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.50.0")
    errorprone("com.uber.nullaway:nullaway:0.13.8")
}

tasks.withType<JavaCompile>().configureEach {
    val testCompile = name.contains("Test")

    options.errorprone {
        disableWarningsInGeneratedCode.set(true)

        // OnlyNullMarked leaves NullAway inert until a package-info.java opts in with JSpecify's
        // @NullMarked, so the check can be turned on before any package is ready for it.
        //
        // Both lines are unconditional on purpose. Error Prone instantiates NullAway as soon as
        // its jar is on the processor path, even for a task that disables the check below, and
        // NullAway refuses to start unless one of OnlyNullMarked or AnnotatedPackages is set.
        // Moving them into an else branch fails every test compile.
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:OnlyNullMarked", true)

        if (testCompile) {
            // SelfAssertion only fires on TestNG's own sample/fixture classes, where trivial
            // assertions such as assertThat("abc").isEqualTo("abc") exist solely to give the
            // runner a passing method. Production code keeps the check enabled.
            //
            // NullAway is off here because @NullMarked applies to a package, not a source set:
            // nine test packages share a name with a main one, so marking org.testng or
            // org.testng.internal would sweep in their test halves too.
            disable("SelfAssertion", "NullAway")
        }
    }
}
