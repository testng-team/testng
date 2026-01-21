# Local Testing Guide for Publishing and Signing

This guide explains how to test publishing snapshots, releases, and artifact signing locally without actually publishing to Maven Central.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Testing Snapshot Publishing](#testing-snapshot-publishing)
- [Testing Release Publishing](#testing-release-publishing)
- [Testing Artifact Signing](#testing-artifact-signing)
- [Publishing to Local Maven Repository](#publishing-to-local-maven-repository)
- [Verifying Artifacts](#verifying-artifacts)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Java Version

You need **Java 21** installed to run any Gradle command:

```bash
# Check your Java version
java -version
# Should show Java 21

# If using SDKMAN (recommended - there's a .sdkmanrc file in the project)
sdk env install  # Install the Java version specified in .sdkmanrc
sdk env          # Use the Java version from .sdkmanrc

# Or manually
sdk use java 21.0.5-amzn

# If using jenv
jenv local 21
```

**Note:** The TestNG project includes a `.sdkmanrc` file that specifies `java=21.0.5-amzn`. If you have SDKMAN installed with `sdkman_auto_env=true` in `~/.sdkman/etc/config`, it will automatically switch to the correct Java version when you `cd` into the project directory.

### Optional: PGP Key for Signing Tests

To test signing, you need a PGP key. You can either:

1. **Use an existing PGP key** (if you have one)
2. **Generate a test PGP key** (for local testing only)

#### Generate a Test PGP Key

```bash
# Generate a new key (use test values)
gpg --full-generate-key

# Follow the prompts:
# - Key type: (1) RSA and RSA
# - Key size: 4096
# - Expiration: 0 (does not expire) or 1y (1 year)
# - Real name: TestNG Test
# - Email: test@testng.org
# - Passphrase: test123 (or any test passphrase)

# List your keys to get the key ID
gpg --list-secret-keys --keyid-format=long

# Export the private key (replace KEY_ID with your actual key ID)
gpg --armor --export-secret-keys KEY_ID > test-private-key.asc
```

## Testing Snapshot Publishing

### Option 1: Publish to Local Maven Repository (Recommended)

This is the safest way to test - it publishes to your local `~/.m2/repository`:

```bash
# Build and publish snapshot to local Maven repository
./gradlew publishToMavenLocal -Prelease=false

# Or just use the default (which is already snapshot)
./gradlew publishToMavenLocal
```

**What this does:**
- Builds all TestNG modules
- Creates snapshot artifacts (version ends with `-SNAPSHOT`)
- Publishes to `~/.m2/repository/org/testng/`
- **Does NOT** sign artifacts (signing is only for releases)
- **Does NOT** upload to any remote repository

**Verify the artifacts:**

```bash
# Check the local Maven repository
ls -la ~/.m2/repository/org/testng/testng/7.12.0-SNAPSHOT/

# You should see:
# - testng-7.12.0-SNAPSHOT.jar
# - testng-7.12.0-SNAPSHOT.pom
# - testng-7.12.0-SNAPSHOT-sources.jar
# - testng-7.12.0-SNAPSHOT-javadoc.jar
```

### Option 2: Publish to Project-Local Repository

This publishes to a local directory within the project (useful for testing without polluting your `~/.m2`):

```bash
# Publish to build/local-maven-repo
./gradlew publishAllPublicationsToLocalRepository -Prelease=false
```

**Verify the artifacts:**

```bash
# Check the project-local repository (in the testng module's build directory)
ls -la testng/build/local-maven-repo/org/testng/testng/7.12.0-SNAPSHOT/

# Other modules will have their own local-maven-repo directories
ls -la testng-core/build/local-maven-repo/org/testng/testng-core/7.12.0-SNAPSHOT/
```

### Option 3: Test Snapshot Publishing Workflow (Advanced)

To test what tasks would run for snapshot publishing (without actually executing them):

```bash
# Dry-run shows what tasks would execute (doesn't actually run them)
./gradlew publishAllPublicationsToCentralSnapshotsRepository \
  -Prelease=false \
  --dry-run
```

**Note:** `--dry-run` only shows the task execution plan. To actually test the build and see if it would fail (without uploading), you need credentials set but can expect it to fail at the upload step:

```bash
# This will build artifacts but fail at upload (expected - no credentials)
./gradlew publishAllPublicationsToCentralSnapshotsRepository \
  -Prelease=false \
  --stacktrace

# Or just build the artifacts without any publishing
./gradlew build -Prelease=false
```

## Testing Release Publishing

### Option 1: Publish Release to Local Maven Repository (Recommended)

**Without signing (easiest for testing):**

```bash
# Build and publish release to local Maven repository (no signing)
./gradlew publishToMavenLocal -Prelease=true -Psigning.pgp.enabled=OFF
```

**With signing (requires PGP key configuration):**

```bash
# This will FAIL with "no configured signatory" unless you set up signing keys first
./gradlew publishToMavenLocal -Prelease=true

# Error you'll see:
# > Cannot perform signing task ':testng:signMavenPublication' because it has no configured signatory
```

To enable signing, see the [Testing Artifact Signing](#testing-artifact-signing) section below.

**What this does:**
- Builds all TestNG modules
- Creates release artifacts (version WITHOUT `-SNAPSHOT`, e.g., `7.12.0`)
- Signs artifacts (if signing is enabled and configured)
- Publishes to `~/.m2/repository/org/testng/`

## Testing Artifact Signing

### Configure Signing Keys

To test signing, set these environment variables:

```bash
# Export your PGP private key (replace with your actual key file)
export SIGNING_PGP_PRIVATE_KEY="$(cat ~/.gnupg/private-key.asc)"
export SIGNING_PGP_PASSPHRASE="your-passphrase"

# Now publish with signing
./gradlew publishToMavenLocal -Prelease=true
```

### Verify Signatures

```bash
# Check that .asc signature files were created
ls -la ~/.m2/repository/org/testng/testng/7.12.0/*.asc

# Verify a signature
gpg --verify \
  ~/.m2/repository/org/testng/testng/7.12.0/testng-7.12.0.jar.asc \
  ~/.m2/repository/org/testng/testng/7.12.0/testng-7.12.0.jar
```

### Disable Signing

```bash
# Build release without signing
./gradlew publishToMavenLocal -Prelease=true -Psigning.pgp.enabled=OFF
```

## Publishing to Local Maven Repository

The `publishToMavenLocal` task is the most useful for local testing. It publishes artifacts to your local `~/.m2/repository` directory.

### Basic Usage

```bash
# Publish snapshot (default)
./gradlew publishToMavenLocal

# Publish release
./gradlew publishToMavenLocal -Prelease=true

# Publish specific module only
./gradlew :testng-core:publishToMavenLocal

# Publish all modules
./gradlew publishToMavenLocal
```

### Using Published Artifacts in Another Project

After publishing to `~/.m2/repository`, use them in another project:

**Gradle (`build.gradle.kts`):**
```kotlin
repositories {
    mavenLocal()  // Add this to use local Maven repository
    mavenCentral()
}

dependencies {
    testImplementation("org.testng:testng:7.12.0-SNAPSHOT")
}
```

**Maven (`pom.xml`):**
```xml
<dependencies>
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>7.12.0-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Clean Local Maven Repository

To remove previously published artifacts:

```bash
# Remove all TestNG artifacts from local Maven
rm -rf ~/.m2/repository/org/testng/

# Or just the specific version
rm -rf ~/.m2/repository/org/testng/testng/7.12.0-SNAPSHOT/
```

## Verifying Artifacts

### Check Artifact Contents

```bash
# List contents of the JAR
jar tf ~/.m2/repository/org/testng/testng/7.12.0-SNAPSHOT/testng-7.12.0-SNAPSHOT.jar | head -20

# Check Java version of compiled classes (should be Java 11)
javap -verbose -cp ~/.m2/repository/org/testng/testng/7.12.0-SNAPSHOT/testng-7.12.0-SNAPSHOT.jar org.testng.TestNG | grep "major version"
# Should show: major version: 55 (Java 11)
```

### Verify POM Metadata

```bash
# View the POM file
cat ~/.m2/repository/org/testng/testng/7.12.0-SNAPSHOT/testng-7.12.0-SNAPSHOT.pom

# Check for required Maven Central metadata:
# - name, description, url
# - licenses
# - developers
# - scm (source control)
```

### Verify Signatures (if signing is enabled)

```bash
# Check that signature files exist
ls -la ~/.m2/repository/org/testng/testng/7.12.0/*.asc

# Verify JAR signature
gpg --verify \
  ~/.m2/repository/org/testng/testng/7.12.0/testng-7.12.0.jar.asc \
  ~/.m2/repository/org/testng/testng/7.12.0/testng-7.12.0.jar

# Verify POM signature
gpg --verify \
  ~/.m2/repository/org/testng/testng/7.12.0/testng-7.12.0.pom.asc \
  ~/.m2/repository/org/testng/testng/7.12.0/testng-7.12.0.pom

# Should output: "Good signature from ..."
```

## Troubleshooting

### "Nmcp requires Java 17+"

**Problem:** You're running Gradle with Java 11 or earlier.

**Solution:** Use Java 21:

```bash
java -version  # Should show Java 21
sdk use java 21.0.5-tem  # If using SDKMAN
```

### "Cannot perform signing task ... signing.secretKeyRingFile"

**Problem:** Signing is enabled but no keys are configured.

**Solution:** Either configure signing keys or disable signing:

```bash
# Option 1: Disable signing
./gradlew publishToMavenLocal -Prelease=true -Psigning.pgp.enabled=OFF

# Option 2: Configure signing keys
export SIGNING_PGP_PRIVATE_KEY="$(cat test-private-key.asc)"
export SIGNING_PGP_PASSPHRASE="test123"
./gradlew publishToMavenLocal -Prelease=true
```

### "Task 'publishToMavenLocal' not found"

**Problem:** The task doesn't exist for the root project.

**Solution:** Run it without a project prefix:

```bash
# Correct - publishes all modules
./gradlew publishToMavenLocal

# Or publish specific module
./gradlew :testng:publishToMavenLocal
```

### Artifacts Not Found in Local Maven

**Problem:** Published artifacts don't appear in `~/.m2/repository`.

**Solution:** Check the build output for errors:

```bash
# Run with more logging
./gradlew publishToMavenLocal --info

# Check the exact location
ls -la ~/.m2/repository/org/testng/testng/
```

### "Execution failed for task ':testng:signMavenPublication'"

**Problem:** Signing failed due to invalid key or passphrase.

**Solution:** Verify your signing configuration:

```bash
# Test your GPG key
gpg --list-secret-keys

# Verify the exported key
cat test-private-key.asc | head -5
# Should start with: -----BEGIN PGP PRIVATE KEY BLOCK-----

# Try with signing disabled first
./gradlew publishToMavenLocal -Prelease=true -Psigning.pgp.enabled=OFF
```

### "Could not find matching toolchain"

**Problem:** Gradle can't find the requested Java version.

**Solution:** Install the required Java version:

```bash
# Using SDKMAN
sdk install java 21.0.5-tem
sdk use java 21.0.5-tem

# Or set JAVA_HOME
export JAVA_HOME=/path/to/java-21
```

## Quick Reference

```bash
# Snapshot to local Maven (most common)
./gradlew publishToMavenLocal

# Release to local Maven with signing
export SIGNING_PGP_PRIVATE_KEY="$(cat private-key.asc)"
export SIGNING_PGP_PASSPHRASE="passphrase"
./gradlew publishToMavenLocal -Prelease=true

# Release without signing
./gradlew publishToMavenLocal -Prelease=true -Psigning.pgp.enabled=OFF

# Clean local artifacts
rm -rf ~/.m2/repository/org/testng/
```

## Related Documentation

- [Release Process](RELEASE_PROCESS.md) - Publishing to Maven Central
- [Build System](BUILD_SYSTEM.md) - Build system details
- [Java Versions](JAVA_VERSIONS_QUICK_REFERENCE.md) - Java requirements
