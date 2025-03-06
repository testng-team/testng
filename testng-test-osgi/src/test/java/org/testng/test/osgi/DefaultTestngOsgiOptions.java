/*
 * Copyright (c) 2021, PostgreSQL Global Development Group
 * See the LICENSE.pgjdbc file in testng-test-osgi folder for more information.
 */

package org.testng.test.osgi;

import static org.ops4j.pax.exam.CoreOptions.*;

import org.ops4j.pax.exam.options.ModifiableCompositeOption;

/** Pulls repository URLs from system properties and passes them to pax-exam test container. */
public class DefaultTestngOsgiOptions {
  public static ModifiableCompositeOption defaultTestngOsgiOptions() {
    return composite(
        // This declares "remote" repositories where the container would fetch artifacts from.
        // It is testng built in the current build + central for other dependencies
        systemProperty("org.ops4j.pax.url.mvn.repositories")
            .value(System.getProperty("testng.org.ops4j.pax.url.mvn.repositories")),
        // This is a repository where osgi container would cache resolved maven artifacts
        systemProperty("org.ops4j.pax.url.mvn.localRepository")
            .value(System.getProperty("testng.org.ops4j.pax.url.mvn.localRepository")),
        mavenBundle("org.testng", "testng").versionAsInProject(),
        mavenBundle("org.jcommander", "jcommander").versionAsInProject(),
        mavenBundle("org.assertj", "assertj-core").versionAsInProject(),
        mavenBundle("net.bytebuddy", "byte-buddy").versionAsInProject(),
        mavenBundle("com.google.inject", "guice").versionAsInProject(),
        mavenBundle("org.yaml", "snakeyaml").versionAsInProject(),
        mavenBundle("com.google.guava", "guava").versionAsInProject(),
        mavenBundle("com.google.guava", "failureaccess").versionAsInProject(),
        mavenBundle("com.google.guava", "listenablefuture").versionAsInProject(),
        mavenBundle("com.google.code.findbugs", "jsr305").versionAsInProject(),
        mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.aopalliance")
            .versionAsInProject(),
        mavenBundle("org.checkerframework", "checker-qual").versionAsInProject(),
        mavenBundle("com.google.errorprone", "error_prone_annotations").versionAsInProject(),
        mavenBundle("com.google.j2objc", "j2objc-annotations").versionAsInProject(),
        mavenBundle(
                "org.apache.aries.spifly", "org.apache.aries.spifly.dynamic.framework.extension")
            .versionAsInProject(),
        systemProperty("logback.configurationFile")
            .value(System.getProperty("logback.configurationFile")),
        mavenBundle("org.ops4j.pax.logging", "pax-logging-api").versionAsInProject(),
        mavenBundle("org.ops4j.pax.logging", "pax-logging-logback").versionAsInProject());
  }
}
