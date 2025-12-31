# TestNG Build System Documentation

This document explains how TestNG's Gradle build system works, including the use of Java toolchains, build parameters, and publishing configuration.

## Table of Contents

1. [Overview](#overview)
2. [Java Version Requirements](#java-version-requirements)
3. [Build Parameters](#build-parameters)
4. [Gradle Toolchains](#gradle-toolchains)
5. [Build Logic Plugins](#build-logic-plugins)
6. [Publishing to Maven Central](#publishing-to-maven-central)
7. [CI/CD Workflows](#cicd-workflows)
8. [Common Build Commands](#common-build-commands)

## Overview

TestNG uses a sophisticated Gradle build system with:
- **Composite builds** for build logic organization
- **Gradle toolchains** for multi-version Java support
- **Build parameters** for flexible configuration
- **Convention plugins** for consistent project setup

### Project Structure

```
testng/
├── build-logic/              # Composite build for build logic
│   ├── build-parameters/     # Defines build parameters
│   ├── basics/               # Basic utilities and toolchain config
│   ├── jvm/                  # Java/Kotlin compilation plugins
│   ├── code-quality/         # Testing and quality plugins
│   └── publishing/           # Maven publishing and signing
├── testng-core/              # Core TestNG implementation
├── testng-api/               # Public API
├── testng/                   # Main distribution (merged jar)
└── [other modules]/
```

## Java Version Requirements

TestNG's build uses **three different Java versions** for different purposes:

| Purpose | Java Version | Configured By | Why? |
|---------|--------------|---------------|------|
| **Running Gradle** | Java 17+ | Default JDK on system | Required by nmcp plugin for Maven Central publishing |
| **Compiling Code** | Java 17 | `jdkBuildVersion=17` | Modern toolchain for compilation |
| **Target Bytecode** | Java 11 | `targetJavaVersion=11` | TestNG artifacts are Java 11 compatible |
| **Running Tests** | Java 11/17/21/24 | `jdkTestVersion=X` | Verify compatibility across Java versions |

### Why Java 17 is Required for ALL Builds

* The `com.gradleup.nmcp.aggregation` plugin (used for Maven Central publishing) requires Java 17+. 
* This plugin is applied in the root `build.gradle.kts`, which means it's loaded during Gradle's configuration phase for **every** build command, not just publishing tasks.

**Result:** You must use Java 17+ to run any Gradle command, including `./gradlew build`, `./gradlew test`, etc.

## Build Parameters

Build parameters are defined in `build-logic/build-parameters/build.gradle.kts` using the `org.gradlex.build-parameters` plugin.

### Key Build Parameters

```bash
# Java Versions
-PjdkBuildVersion=17          # JDK version for building (default: 17)
-PtargetJavaVersion=11        # Target bytecode version (default: 11)
-PjdkTestVersion=11           # JDK version for running tests (default: buildJdk)

# Publishing
-Prelease=true                # Create release version (no -SNAPSHOT)
-PcentralPortal.publishingType=AUTOMATIC  # Auto-release to Maven Central

# Signing
-Psigning.pgp.enabled=AUTO    # Enable PGP signing for releases

# Code Quality
-PskipAutostyle=true          # Skip code formatting checks
-PskipErrorProne=true         # Skip Error Prone checks
```

### Viewing All Parameters

```bash
./gradlew parameters
```

## Gradle Toolchains

Gradle toolchains allow using different Java versions for different tasks, even though Gradle itself runs with one Java version.

### How Toolchains Work

```
┌────────────────────────────────────────────────────────┐
│ Gradle Process (runs with Java 17)                     │
│                                                        │
│  ┌──────────────────────────────────────────┐          │
│  │ Task: compileJava                        │          │
│  │ Toolchain: Java 17 (jdkBuildVersion)     │          │
│  │ Output: Java 11 bytecode (--release=11)  │          │
│  └──────────────────────────────────────────┘          │
│                                                        │
│  ┌──────────────────────────────────────────┐          │
│  │ Task: test                               │          │
│  │ Toolchain: Java 11 (jdkTestVersion)      │          │
│  │ Spawns: New JVM process with Java 11     │          │
│  └──────────────────────────────────────────┘          │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Toolchain Configuration

The toolchain configuration is in `build-logic/jvm/src/main/kotlin/testng.java.gradle.kts`:

```kotlin
java {
    toolchain {
        configureToolchain(buildParameters.buildJdk)  // Uses jdkBuildVersion
    }
}

tasks.configureEach<JavaCompile> {
    options.release.set(buildParameters.targetJavaVersion)  // Targets Java 11
}

tasks.configureEach<Test> {
    buildParameters.testJdk?.let {
        javaLauncher.convention(javaToolchains.launcherFor(it))  // Uses jdkTestVersion
    }
}
```

### What This Means

- **Gradle runs with:** The default Java on your system (must be 17+)
- **Code compiles with:** Java 17 toolchain (via `jdkBuildVersion`)
- **Code targets:** Java 11 bytecode (via `targetJavaVersion` and `--release=11`)
- **Tests run with:** Configurable Java version (via `jdkTestVersion`)

## Build Logic Plugins

TestNG uses convention plugins organized in the `build-logic` composite build.

### Plugin Hierarchy

```
testng.java-library (most projects use this)
├── testng.java
│   ├── java-base
│   ├── testng.versioning
│   ├── testng.style
│   └── testng.repositories
└── testng.testing
    └── java-library

testng.published-java-library (for published modules)
├── testng.java-library
└── testng.maven-publish
    └── maven-publish
```

### Key Plugins

#### `testng.java.gradle.kts`
Configures Java compilation with toolchains:

- Sets build toolchain to `jdkBuildVersion`
- Configures `--release` flag for target compatibility
- Configures JavaExec tasks to use test toolchain

#### `testng.testing.gradle.kts`
Configures test execution:

- Applies TestNG test framework
- Sets test toolchain to `jdkTestVersion`
- Passes system properties to tests
- Configures test JVM arguments

#### `testng.maven-publish.gradle.kts`
Configures Maven publishing:

- Sets up Maven publications
- Configures POM metadata
- Configures snapshot repository for Central Snapshots

#### `testng.signing.gradle.kts`
Configures PGP signing:

- Signs artifacts for release builds
- Uses in-memory PGP keys from environment variables
- Only signs when `release=true` and `signing.pgp.enabled=AUTO`

## Publishing to Maven Central

TestNG uses the new **Maven Central Portal API** (replacing the legacy OSSRH system).

### Publishing Architecture

```
┌───────────────────────────────────────────────-──────────┐
│ Root Project (build.gradle.kts)                          │
│                                                          │
│  plugins {                                               │
│      id("com.gradleup.nmcp.aggregation") version "1.0.2" │
│  }                                                       │
│                                                          │
│  nmcpAggregation {                                       │
│      centralPortal {                                     │
│          username = CENTRAL_PORTAL_USERNAME              │
│          password = CENTRAL_PORTAL_PASSWORD              │
│          publishingType = AUTOMATIC or USER_MANAGED      │
│      }                                                   │
│      publishAllProjectsProbablyBreakingProjectIsolation()│
│  }                                                       │
└────────────────────────────────────────────────────────-─┘
                          │
                          │ Aggregates publications from:
                          ▼
┌─────────────────────────────────────────────────────────┐
│ Subprojects (testng, testng-core, testng-api, etc.)     │
│                                                         │
│  plugins {                                              │
│      id("testng.published-java-library")                │
│  }                                                      │
│                                                         │
│  publishing {                                           │
│      publications {                                     │
│          create<MavenPublication>("maven") { ... }      │
│      }                                                  │
│  }                                                      │
└─────────────────────────────────────────────────────────┘
```

### Publishing Tasks

```bash
# Publish release to Maven Central Portal
./gradlew publishAggregationToCentralPortal \
  -Prelease=true \
  -PcentralPortal.publishingType=AUTOMATIC

# Publish snapshot to Central Snapshots repository
./gradlew publishAllPublicationsToCentralSnapshotsRepository
```

### Environment Variables Required

```bash
# Maven Central Portal credentials
export CENTRAL_PORTAL_USERNAME="your-token-username"
export CENTRAL_PORTAL_PASSWORD="your-token-password"

# PGP signing (for releases)
export SIGNING_PGP_PRIVATE_KEY="-----BEGIN PGP PRIVATE KEY BLOCK-----..."
export SIGNING_PGP_PASSPHRASE="your-passphrase"
```

### Publishing Types

- **AUTOMATIC**: Artifacts are automatically published to Maven Central after validation
- **USER_MANAGED**: Artifacts are staged in the portal; you manually publish from https://central.sonatype.com/publishing

## CI/CD Workflows

TestNG uses GitHub Actions for continuous integration and publishing.

### Test Workflow (`.github/workflows/test.yml`)

The test workflow uses a matrix strategy to test across multiple Java versions, operating systems, and configurations.

#### Matrix Configuration

The matrix is generated by `.github/workflows/matrix.js`:

- **Java versions**: 11, 17, 21, 24 (EA)
- **Java distributions**: Corretto, Liberica, Microsoft, Oracle, Temurin, Zulu
- **Operating systems**: Ubuntu, Windows, macOS
- **Timezones**: America/New_York, Pacific/Chatham, UTC
- **Locales**: de_DE, fr_FR, ru_RU, tr_TR

#### How Multi-Version Testing Works

For each matrix job (e.g., testing Java 11):

```yaml
- name: Set up Java 17 and ${{ matrix.non_ea_java_version }}
  uses: actions/setup-java@v4
  with:
    java-version: |
      ${{ matrix.non_ea_java_version }}  # e.g., 11
      17                                  # Always install Java 17
    distribution: ${{ matrix.java_distribution }}
```

**Key points:**

1. **Two Java versions are installed**: The matrix version (e.g., 11) AND Java 17
2. **Java 17 is listed last**, so it becomes the default
3. **Gradle runs with Java 17** (satisfies nmcp plugin requirement)
4. **Tests run with the matrix version** (via `-PjdkTestVersion=11`)

```yaml
- name: Test
  uses: burrunan/gradle-cache-action@v2
  with:
    arguments: build
    properties: |
      jdkBuildVersion=17              # Compile with Java 17
      jdkTestVersion=${{ matrix.java_version }}  # Test with matrix version
```

### Publish Workflow (`.github/workflows/publish-maven-central.yml`)

Manually triggered workflow for publishing releases:

```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: 17  # Required for nmcp plugin

- name: Publish to Central Portal
  run: |
    ./gradlew publishAggregationToCentralPortal \
      -Prelease=true \
      -PcentralPortal.publishingType=${{ github.event.inputs.publishing_type }}
```

### Snapshot Workflow (`.github/workflows/publish-snapshot.yml`)

Automatically publishes snapshots on every push to master:

```yaml
- name: Publish Snapshot to Central
  run: |
    ./gradlew publishAllPublicationsToCentralSnapshotsRepository \
      -Prelease=false
```

## Common Build Commands

### Building

```bash
# Build everything
./gradlew build

# Build without tests
./gradlew build -x test

# Build with specific Java version for tests
./gradlew build -PjdkTestVersion=11

# Clean build
./gradlew clean build
```

### Testing

```bash
# Run all tests
./gradlew test

# Run tests with Java 11
./gradlew test -PjdkTestVersion=11

# Run specific test class
./gradlew :testng-core:test --tests "test.SimpleBaseTest"

# Run tests with extra JVM args
./gradlew test -Ptestng.test.extra.jvmargs="-Xmx2g -XX:+HeapDumpOnOutOfMemoryError"
```

### Publishing

```bash
# Publish to local Maven repository
./gradlew publishToMavenLocal

# Publish snapshot to Central Snapshots
./gradlew publishAllPublicationsToCentralSnapshotsRepository

# Publish release to Maven Central (automatic)
./gradlew publishAggregationToCentralPortal \
  -Prelease=true \
  -PcentralPortal.publishingType=AUTOMATIC

# Publish release to Maven Central (manual approval)
./gradlew publishAggregationToCentralPortal \
  -Prelease=true \
  -PcentralPortal.publishingType=USER_MANAGED
```

### Code Quality

```bash
# Run code formatting checks
./gradlew autostyleCheck

# Apply code formatting
./gradlew autostyleApply

# Skip code quality checks
./gradlew build -PskipAutostyle=true -PskipErrorProne=true
```

### Viewing Configuration

```bash
# View all build parameters
./gradlew parameters

# View project dependencies
./gradlew :testng-core:dependencies

# View task dependencies
./gradlew :testng-core:test --dry-run
```

## Troubleshooting

### "Nmcp requires Java 17+"

**Problem:** You're running Gradle with Java 11 or earlier.

**Solution:** Use Java 17+ to run Gradle:

```bash
# Check your Java version
java -version

# If using SDKMAN
sdk use java 17.0.14-tem

# If using jenv
jenv local 17
```

### "Could not find matching toolchain"

**Problem:** Gradle can't find the requested Java version.

**Solution:** Install the required Java version or let Gradle auto-download:

```bash
# Enable auto-download in gradle.properties
org.gradle.java.installations.auto-download=true

# Or install manually and let Gradle detect it
sdk install java 11.0.12-tem
```

### Tests fail with different Java version

**Problem:** Tests pass locally but fail in CI with a different Java version.

**Solution:** Test locally with the same Java version:

```bash
./gradlew test -PjdkTestVersion=11
```

## Additional Resources

- [Gradle Toolchains Documentation](https://docs.gradle.org/current/userguide/toolchains.html)
- [Maven Central Portal Documentation](https://central.sonatype.org/publish/publish-portal-gradle/)
- [Nmcp Plugin Documentation](https://gradleup.github.io/nmcp/)
- [Build Parameters Plugin](https://github.com/gradlex-org/build-parameters)



