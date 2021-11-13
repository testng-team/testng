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
        mavenBundle("org.assertj", "assertj-core").versionAsInProject(),
        systemProperty("logback.configurationFile")
            .value(System.getProperty("logback.configurationFile")),
        mavenBundle("org.slf4j", "slf4j-api").versionAsInProject(),
        mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(),
        mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject());
  }
}
