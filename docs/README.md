# TestNG Build Documentation

This directory contains documentation about TestNG's build system, development workflow, and publishing process.

## Audience

This documentation is intended for:

- **TestNG Contributors** - Developers who want to contribute code, fix bugs, or add features to TestNG
- **TestNG Maintainers** - Core team members responsible for releases, CI/CD, and project maintenance
- **Build System Developers** - Anyone interested in understanding TestNG's Gradle build architecture
- **Advanced Users** - Developers who need to build TestNG from source for custom modifications

**Note:** If you're just **using** TestNG in your project, you don't need this documentation. Simply add TestNG as a dependency to your project - see the [main README](../README.md) for usage instructions.

## When to Read This

Read this documentation when you need to:

- **Set up a development environment** to contribute to TestNG
- **Build TestNG from source** for the first time
- **Understand the build system** architecture and conventions
- **Publish a release** to Maven Central (maintainers only)
- **Troubleshoot build issues** or CI failures
- **Modify build configuration** or add new modules
- **Understand Java version requirements** for building vs. using TestNG

**Quick navigation:**
- 🚀 **First-time contributors**: Start with [Quick Start](#quick-start) below
- 📦 **Publishing a release**: Go directly to [RELEASE_PROCESS.md](RELEASE_PROCESS.md)
- ☕ **Confused about Java versions**: See [JAVA_VERSIONS_QUICK_REFERENCE.md](JAVA_VERSIONS_QUICK_REFERENCE.md)
- 🔧 **Build system deep dive**: Read [BUILD_SYSTEM.md](BUILD_SYSTEM.md)

## Documentation Index

### 📚 Core Documentation

- **[RELEASE_PROCESS.md](RELEASE_PROCESS.md)** ⭐ **START HERE FOR RELEASES** - Complete release process guide
  - Step-by-step release instructions (automatic and manual workflows)
  - Snapshot publishing
  - Post-release activities (Git tags, announcements, etc.)
  - Visual workflow diagrams
  - Troubleshooting releases
  - Comparison with old OSSRH process

- **[BUILD_SYSTEM.md](BUILD_SYSTEM.md)** - Comprehensive guide to TestNG's Gradle build system
  - Build parameters and configuration
  - Gradle toolchains and multi-version Java support
  - Build logic plugins and architecture
  - Publishing to Maven Central
  - CI/CD workflows
  - Common build commands and troubleshooting

- **[JAVA_VERSIONS_QUICK_REFERENCE.md](JAVA_VERSIONS_QUICK_REFERENCE.md)** - Quick reference for Java version usage
  - Why TestNG requires Java 21+ to build
  - How toolchains enable multi-version testing
  - Common scenarios and FAQ
  - Visual diagrams and examples

- **[CI_TEST_WORKFLOW.md](CI_TEST_WORKFLOW.md)** - CI test workflow and matrix builder
  - How the GitHub Actions test workflow works
  - Matrix builder (matrix.js) explained
  - Multi-version testing architecture
  - Integration with build logic
  - Customizing the test matrix
  - Troubleshooting CI failures

## Quick Start

### Prerequisites

- **Java 21+** installed (required to run Gradle)
- **Git** for version control

### Building TestNG

```bash
# Clone the repository
git clone https://github.com/testng-team/testng.git
cd testng

# Build everything
./gradlew build

# Build without tests
./gradlew build -x test

# Run tests with specific Java version
./gradlew test -PjdkTestVersion=11
```

### Common Tasks

```bash
# View all build parameters
./gradlew parameters

# Run code formatting
./gradlew autostyleApply

# Publish to local Maven repository
./gradlew publishToMavenLocal

# Run specific test
./gradlew :testng-core:test --tests "test.SimpleBaseTest"
```

## Key Concepts

### Java Version Requirements

TestNG's build uses **three different Java versions**:

1. **Gradle Runtime**: Java 21+ (required by nmcp plugin)
2. **Build Toolchain**: Java 21 (for compilation)
3. **Target Bytecode**: Java 11 (for compatibility)
4. **Test Runtime**: Configurable (11, 17, 21, 24)

**Result:** TestNG artifacts work on Java 11+, but you need Java 21+ to build.

See [JAVA_VERSIONS_QUICK_REFERENCE.md](JAVA_VERSIONS_QUICK_REFERENCE.md) for details.

### Build Parameters

TestNG uses the `org.gradlex.build-parameters` plugin for flexible configuration:

```bash
# Java versions
-PjdkBuildVersion=21          # JDK for building (default: 21)
-PtargetJavaVersion=11        # Target bytecode (default: 11)
-PjdkTestVersion=11           # JDK for tests (default: buildJdk)

# Publishing
-Prelease=true                # Create release version
-PcentralPortal.publishingType=AUTOMATIC  # Auto-release to Maven Central

# Code quality
-PskipAutostyle=true          # Skip formatting checks
-PskipErrorProne=true         # Skip Error Prone
```

### Project Structure

```
testng/
├── build-logic/              # Composite build for build logic
│   ├── build-parameters/     # Build parameter definitions
│   ├── basics/               # Toolchain utilities
│   ├── jvm/                  # Java/Kotlin plugins
│   ├── code-quality/         # Testing plugins
│   └── publishing/           # Maven publishing
├── testng-core/              # Core implementation
├── testng-api/               # Public API
├── testng/                   # Main distribution
└── docs/                     # This documentation
```

## For Contributors

### Setting Up Development Environment

1. **Install Java 21+**
   ```bash
   # Using SDKMAN
   sdk install java 21.0.5-tem
   sdk use java 21.0.5-tem

   # Verify
   java -version
   ```

2. **Clone and build**
   ```bash
   git clone https://github.com/testng-team/testng.git
   cd testng
   ./gradlew build
   ```

3. **Import into IDE**
   - IntelliJ IDEA: Open the project directory
   - Eclipse: Import as Gradle project

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :testng-core:test

# Specific test class
./gradlew :testng-core:test --tests "test.SimpleBaseTest"

# With Java 11
./gradlew test -PjdkTestVersion=11

# With extra JVM args
./gradlew test -Ptestng.test.extra.jvmargs="-Xmx2g"
```

### Code Quality

```bash
# Check code formatting
./gradlew autostyleCheck

# Apply code formatting
./gradlew autostyleApply

# Run all checks
./gradlew check
```

### Testing Locally

```bash
# Publish to local Maven repository
./gradlew publishToMavenLocal

# Then in another project, use:
# repositories {
#     mavenLocal()
# }
# dependencies {
#     testImplementation("org.testng:testng:7.12.0-SNAPSHOT")
# }
```

## For Maintainers

### Publishing Releases

See [RELEASE_PROCESS.md](RELEASE_PROCESS.md) for complete step-by-step release instructions.

#### Quick Reference

```bash
# Automatic release (recommended)
./gradlew publishAggregationToCentralPortal \
  -Prelease=true \
  -PcentralPortal.publishingType=AUTOMATIC

# Manual release (requires portal approval)
./gradlew publishAggregationToCentralPortal \
  -Prelease=true \
  -PcentralPortal.publishingType=USER_MANAGED
```

#### Required Environment Variables

```bash
export CENTRAL_PORTAL_USERNAME="your-token-username"
export CENTRAL_PORTAL_PASSWORD="your-token-password"
export SIGNING_PGP_PRIVATE_KEY="-----BEGIN PGP PRIVATE KEY BLOCK-----..."
export SIGNING_PGP_PASSPHRASE="your-passphrase"
```

### Publishing Snapshots

```bash
./gradlew publishAllPublicationsToCentralSnapshotsRepository
```

Snapshots are automatically published on every push to master via GitHub Actions.

## Troubleshooting

### Common Issues

1. **"Nmcp requires Java 21+"**
   - Solution: Use Java 21+ to run Gradle
   - Check: `java -version`

2. **"Could not find matching toolchain"**
   - Solution: Install the required Java version or enable auto-download
   - Add to `gradle.properties`: `org.gradle.java.installations.auto-download=true`

3. **Tests fail with different Java version**
   - Solution: Test locally with the same version
   - Example: `./gradlew test -PjdkTestVersion=11`

4. **Build fails in CI but works locally**
   - Check: Are you using the same Java version?
   - Check: Are you using the same Gradle version?
   - Run: `./gradlew --version` to compare

See [BUILD_SYSTEM.md](BUILD_SYSTEM.md#troubleshooting) for more troubleshooting tips.

## Additional Resources

- [TestNG Website](https://testng.org/)
- [GitHub Repository](https://github.com/testng-team/testng)
- [Issue Tracker](https://github.com/testng-team/testng/issues)
- [Gradle Documentation](https://docs.gradle.org/)
- [Maven Central Portal](https://central.sonatype.com/)

## Questions?

If you have questions about the build system:

1. Check the documentation in this directory
2. Search existing [GitHub Issues](https://github.com/testng-team/testng/issues)
3. Ask in [GitHub Discussions](https://github.com/testng-team/testng/discussions)
4. Open a new issue with the `build` label

