plugins {
    id("org.gradlex.build-parameters") version "1.4.4"
    id("com.github.vlsi.gradle-extensions") version "1.90"
    id("build-logic.kotlin-dsl-gradle-plugin")
}

buildParameters {
    // Other plugins can contribute parameters, so below list is not exhaustive
    enableValidation.set(false)
    pluginId("build-logic.build-params")
    bool("enableMavenLocal") {
        defaultValue.set(true)
        description.set("Add mavenLocal() to repositories")
    }
    integer("targetJavaVersion") {
        defaultValue.set(11)
        mandatory.set(true)
        description.set("Java version for source and target compatibility")
    }
    integer("jdkBuildVersion") {
        defaultValue.set(21)
        mandatory.set(true)
        description.set("JDK version to use for building JMeter. If the value is 0, then the current Java is used. (see https://docs.gradle.org/8.0/userguide/toolchains.html#sec:consuming)")
    }
    string("jdkBuildVendor") {
        description.set("JDK vendor to use building JMeter (see https://docs.gradle.org/8.0/userguide/toolchains.html#sec:vendors)")
    }
    string("jdkBuildImplementation") {
        description.set("Vendor-specific virtual machine implementation to use building JMeter (see https://docs.gradle.org/8.0/userguide/toolchains.html#selecting_toolchains_by_virtual_machine_implementation)")
    }
    integer("jdkTestVersion") {
        description.set("JDK version to use for testing JMeter. If the value is 0, then the current Java is used. (see https://docs.gradle.org/8.0/userguide/toolchains.html#sec:consuming)")
    }
    string("jdkTestVendor") {
        description.set("JDK vendor to use testing JMeter (see https://docs.gradle.org/8.0/userguide/toolchains.html#sec:vendors)")
    }
    string("jdkTestImplementation") {
        description.set("Vendor-specific virtual machine implementation to use testing JMeter (see https://docs.gradle.org/8.0/userguide/toolchains.html#selecting_toolchains_by_virtual_machine_implementation)")
    }
    bool("sonarqube") {
        defaultValue.set(false)
        description.set("Report verification results to Sonarqube")
    }
    bool("skipAutostyle") {
        defaultValue.set(false)
        description.set("Skip AutoStyle verifications")
    }
    bool("failOnJavadocWarning") {
        defaultValue.set(true)
        description.set("Fail build on javadoc warnings")
    }
    bool("skipErrorProne") {
        defaultValue.set(false)
        description.set("Skip Error Prone verifications")
    }
    bool("release") {
        defaultValue.set(false)
        description.set("Create a release version (without -SNAPSHOT suffix)")
    }
    group("centralPortal") {
        enumeration("publishingType") {
            defaultValue.set("AUTOMATIC")
            values.addAll("AUTOMATIC", "USER_MANAGED")
            description.set("Publishing type for Central Portal: AUTOMATIC (auto-release) or USER_MANAGED (manual release from portal)")
        }
    }
    group("signing") {
        group("pgp") {
            enumeration("enabled") {
                defaultValue.set("AUTO")
                values.addAll("AUTO", "OFF")
                description.set("Configures whether PGP signing should be enabled. AUTO enables signing for release versions, OFF disables signing")
            }
        }
    }
}
