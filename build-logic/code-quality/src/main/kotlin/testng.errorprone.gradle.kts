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

        // Which compiles carry test code. The Error Prone plugin derives compilingTestOnlyCode
        // from the *source set* name and lets a module override it, so a module whose main source
        // set holds test fixtures can declare that rather than be classified by whether its task
        // name happens to contain "Test". testng-test-kit is exactly that module.
        //
        // orElse: a JavaCompile task that belongs to no source set gets no convention, and feeding
        // an absent provider to the options below would make them unresolvable.
        val testCode = compilingTestOnlyCode.orElse(false)

        // SelfAssertion only fires on TestNG's own sample/fixture classes, where trivial
        // assertions such as assertThat("abc").isEqualTo("abc") exist solely to give the runner a
        // passing method. Production code keeps the check enabled.
        check("SelfAssertion", testCode.map { if (it) CheckSeverity.OFF else CheckSeverity.DEFAULT })

        // NullAway stays on for test code: @NullMarked is per package, not per source set, so the
        // test half of every marked main package is already marked by the package-info.class on
        // its compile classpath. HandleTestAssertionLibraries is what teaches NullAway that
        // assertThat(x).isNotNull() refines x.
        option("NullAway:HandleTestAssertionLibraries", testCode.map { it.toString() })
    }
}
