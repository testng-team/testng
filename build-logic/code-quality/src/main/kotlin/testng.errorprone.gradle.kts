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

        // Without JSpecifyMode, NullAway reads declarations only and never looks inside a generic
        // type: Iterator<Object[]> and Iterator<Object @Nullable []> are the same thing to it, so a
        // container is free to claim non-null elements while yielding null ones. Turning it on is
        // what makes @NullMarked mean what JSpecify says it means rather than roughly half of it.
        option("NullAway:JSpecifyMode", true)

        if (testCompile) {
            // SelfAssertion only fires on TestNG's own sample/fixture classes, where trivial
            // assertions such as assertThat("abc").isEqualTo("abc") exist solely to give the
            // runner a passing method. Production code keeps the check enabled.
            disable("SelfAssertion")

            // NullAway stays on here: @NullMarked is per package, not per source set, so twelve
            // test packages are already marked by the main package-info.class on their compile
            // classpath.
            //
            // HandleTestAssertionLibraries teaches NullAway that assertThat(x).isNotNull() refines
            // x. It is keyed on the task name, so testng-test-kit -- test code that lives in a main
            // source set -- does not get it, even though its org.testng.xml half is marked and
            // checked today. That is inert only because the AssertJ use in that module sits in the
            // unmarked test package, so nothing there refines a nullable value yet.
            option("NullAway:HandleTestAssertionLibraries", true)
        }
    }
}
