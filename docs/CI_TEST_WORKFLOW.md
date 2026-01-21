# CI Test Workflow Documentation

This document explains how TestNG's GitHub Actions test workflow (`.github/workflows/test.yml`) works, including the matrix builder and how it integrates with the build system.

> **Note on Java Versions:** This document uses placeholders like `<BUILD_VERSION>`, `<TEST_VERSION>`, `<MIN_SUPPORTED>`, etc. instead of specific version numbers. The actual Java versions used in the CI workflow change over time as new LTS versions are released. Always check the following files for current values:
> - `.github/workflows/matrix.js` - Defines which Java versions are tested
> - `build-logic/build-parameters/build.gradle.kts` - Defines default build parameters
> - `.github/workflows/test.yml` - The actual workflow configuration

## Table of Contents

1. [Overview](#overview)
2. [Workflow Architecture](#workflow-architecture)
3. [Matrix Builder (matrix.js)](#matrix-builder-matrixjs)
4. [Test Workflow Steps](#test-workflow-steps)
5. [How Multi-Version Testing Works](#how-multi-version-testing-works)
6. [Integration with Build Logic](#integration-with-build-logic)
7. [Customizing the Matrix](#customizing-the-matrix)

## Overview

The test workflow runs TestNG's test suite across multiple combinations of:

- **Java versions**: Multiple LTS versions plus Early Access builds
- **Java distributions**: Corretto, Liberica, Microsoft, Oracle, Temurin, Zulu
- **Operating systems**: Ubuntu, Windows, macOS
- **Timezones**: America/New_York, Pacific/Chatham, UTC
- **Locales**: Various locales including Turkish (for edge case testing)
- **Hash code modes**: Regular, Same (for testing hash collision scenarios)

This ensures TestNG works correctly across different environments and configurations.

## Workflow Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ GitHub Actions Workflow: test.yml                           │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ Job 1: matrix_prep                                          │
│                                                             │
│  Runs: matrix.js                                            │
│  Produces: JSON matrix with 7 job configurations            │
│                                                             │
│  Example output:                                            │
│  {                                                          │
│    "include": [                                             │
│      {                                                      │
│        "java_version": "<LTS_VERSION>",                     │
│        "java_distribution": "temurin",                      │
│        "os": "ubuntu-latest",                               │
│        "tz": "UTC",                                         │
│        "locale": {"language": "en", "country": "US"},       │
│        ...                                                  │
│      },                                                     │
│      ...                                                    │
│    ]                                                        │
│  }                                                          │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ Job 2: build (runs in parallel for each matrix entry)       │
│                                                             │
│  For each matrix configuration:                             │
│  1. Install Java versions (matrix version + build version)  │
│  2. Run Gradle build with specific parameters               │
│  3. Upload test reports if failed                           │
└─────────────────────────────────────────────────────────────┘
```

## Matrix Builder (matrix.js)

The matrix builder is a JavaScript file (`.github/workflows/matrix.js`) that generates a random subset of valid test configurations.

### Purpose

Instead of testing **every possible combination** (which would be thousands of jobs), the matrix builder:

1. Defines all possible axes (Java version, OS, timezone, etc.)
2. Randomly selects a subset of combinations
3. Ensures critical combinations are always included
4. Outputs a JSON matrix for GitHub Actions

### Matrix Axes

Let's look at each axis defined in `matrix.js`:

#### 1. Java Distribution

```javascript
matrix.addAxis({
  name: 'java_distribution',
  values: [
    {value: 'corretto', vendor: 'amazon', weight: 1},
    {value: 'liberica', vendor: 'bellsoft', weight: 1},
    {value: 'microsoft', vendor: 'microsoft', weight: 1},
    {value: 'oracle', vendor: 'oracle', weight: 1},
    {value: 'temurin', vendor: 'eclipse', weight: 1},
    {value: 'zulu', vendor: 'azul', weight: 1},
  ]
});
```

**What it does:** Defines which JDK vendor to use (Amazon Corretto, Eclipse Temurin, etc.)

**Why it matters:** Different JDK vendors may have subtle differences in behavior

#### 2. Java Version

```javascript
const eaJava = '<EARLY_ACCESS_VERSION>';  // e.g., '24', '25', etc.
matrix.addAxis({
  name: 'java_version',
  values: [
    '<MIN_SUPPORTED>',  // LTS - minimum supported version
    '<BUILD_VERSION>',  // LTS - version used for building
    '<LATEST_LTS>',     // LTS - latest LTS version
    eaJava,             // Early Access - future version
  ]
});
```

**What it does:** Defines which Java version to test with

**Why it matters:** TestNG must work on all supported Java versions

**Note:** Check `matrix.js` for the current list of Java versions being tested.

#### 3. Timezone

```javascript
matrix.addAxis({
  name: 'tz',
  values: [
    'America/New_York',  // EST/EDT
    'Pacific/Chatham',   // +12:45/+13:45 (unusual offset)
    'UTC'                // Standard reference
  ]
});
```

**What it does:** Sets the `TZ` environment variable

**Why it matters:** Date/time handling bugs can be timezone-specific

#### 4. Operating System

```javascript
matrix.addAxis({
  name: 'os',
  values: [
    'ubuntu-latest',   // Linux
    'windows-latest',  // Windows
    'macos-latest'     // macOS
  ]
});
```

**What it does:** Defines which OS to run tests on

**Why it matters:** Path separators, line endings, and file system behavior differ

#### 5. Hash Code Mode

```javascript
matrix.addAxis({
  name: 'hash',
  values: [
    {value: 'regular', title: '', weight: 42},
    {value: 'same', title: 'same hashcode', weight: 1}
  ]
});
```

**What it does:** Controls whether all objects get the same hash code

**Why it matters:** Tests hash collision handling (using `-XX:hashCode=2`)

#### 6. Locale

```javascript
matrix.addAxis({
  name: 'locale',
  values: [
    {language: 'de', country: 'DE'},  // German
    {language: 'fr', country: 'FR'},  // French
    {language: 'ru', country: 'RU'},  // Russian
    {language: 'tr', country: 'TR'},  // Turkish (special case)
  ]
});
```

**What it does:** Sets the user language and country

**Why it matters:** String operations (especially case conversion) are locale-dependent. Turkish is notorious for `i` vs `İ` issues.

### Guaranteed Combinations

The matrix builder ensures certain combinations are **always** included:

```javascript
// Ensure at least one job with "same" hashcode exists
matrix.generateRow({hash: {value: 'same'}});

// Ensure at least one windows and at least one linux job
matrix.generateRow({os: 'windows-latest'});
matrix.generateRow({os: 'ubuntu-latest'});

// Ensure there will be at least one job with each Java version
matrix.generateRow({java_version: "<MIN_SUPPORTED>"});
matrix.generateRow({java_version: "<BUILD_VERSION>"});
matrix.generateRow({java_version: "<LATEST_LTS>"});
matrix.generateRow({java_version: eaJava});
```

**Why:** These are critical configurations that must always be tested.

**Note:** The actual Java versions are defined in `matrix.js` and may change over time as new LTS versions are released.

### Constraints and Implications

The matrix builder also defines constraints:

```javascript
// Oracle JDK is only available for certain Java versions
matrix.imply({java_distribution: {value: 'oracle'}}, {java_version: v => v === eaJava || v >= <MIN_ORACLE_VERSION>});

// Early Access Java must use Oracle distribution
matrix.imply({java_version: eaJava}, {java_distribution: {value: 'oracle'}})

// Semeru (OpenJ9) doesn't support same hashcode mode
matrix.exclude({java_distribution: {value: 'semeru'}, hash: {value: 'same'}});
```

**What it does:** Ensures only valid combinations are generated

**Note:** Constraints may change based on JDK vendor availability and compatibility.

### Output

The matrix builder generates approximately 7 jobs (configurable via `MATRIX_JOBS` env var):

```json
{
  "include": [
    {
      "name": "<JAVA_VERSION>, temurin, ubuntu, UTC, en_US",
      "java_version": "<JAVA_VERSION>",
      "java_distribution": "temurin",
      "os": "ubuntu-latest",
      "tz": "UTC",
      "locale": {"language": "en", "country": "US"},
      "hash": {"value": "regular"},
      "extraGradleArgs": "-Duser.country=US -Duser.language=en",
      "testExtraJvmArgs": "-Duser.country=US -Duser.language=en"
    },
    ...
  ]
}
```

## Test Workflow Steps

Let's walk through what happens when the test workflow runs.

### Step 1: Matrix Preparation Job

```yaml
jobs:
  matrix_prep:
    name: Matrix Preparation
    runs-on: ubuntu-latest
    outputs:
      matrix: ${{ steps.set-matrix.outputs.matrix }}
    env:
      MATRIX_JOBS: 7  # Generate 7 test jobs
    steps:
      - uses: actions/checkout@v4
      - id: set-matrix
        run: node .github/workflows/matrix.js
```

**What happens:**

1. Checks out the repository
2. Runs `matrix.js` with Node.js
3. `matrix.js` generates a JSON matrix with 7 random (but guaranteed) configurations
4. Outputs the matrix for the next job

### Step 2: Build Job (Runs for Each Matrix Entry)

The build job runs in parallel for each configuration in the matrix.

#### Step 2a: Setup Java Versions

```yaml
- name: Set up Java <BUILD_VERSION> and ${{ matrix.non_ea_java_version }}, ${{ matrix.java_distribution }}
  uses: actions/setup-java@v4
  with:
    # The latest one will be the default, so we use the build version for launching Gradle
    java-version: |
      ${{ matrix.non_ea_java_version }}
      <BUILD_VERSION>
    distribution: ${{ matrix.java_distribution }}
```

**What happens:**

1. Installs **TWO** Java versions:
   - The matrix Java version (from the test matrix)
   - The build Java version (required to run Gradle)
2. The build version is listed **last**, so it becomes the **default**
3. When you run `java -version`, it shows the build version
4. When you run `./gradlew`, it uses the build version

**Why install both?**

- The build version is needed to run Gradle (nmcp plugin requirement)
- Matrix Java version is needed to run tests (via toolchains)

**Note:** The build version is defined in `matrix.js` and may change over time.

#### Step 2b: Run Tests

```yaml
- name: Test
  uses: burrunan/gradle-cache-action@v2
  with:
    arguments: |
      --no-parallel --no-daemon --scan
      build
      ${{ matrix.extraGradleArgs }}
    properties: |
      testng.test.extra.jvmargs=${{ matrix.testExtraJvmArgs }}
      testDisableCaching=${{ matrix.testDisableCaching }}
      jdkBuildVersion=<BUILD_VERSION>
      jdkTestVersion=${{ matrix.java_version }}
      jdkTestVendor=${{ matrix.java_vendor }}
      org.gradle.java.installations.auto-download=false
```

**What happens:**

1. Runs `./gradlew build` with specific parameters
2. Sets `jdkBuildVersion=<BUILD_VERSION>` → Gradle uses build version toolchain for compilation
3. Sets `jdkTestVersion=${{ matrix.java_version }}` → Tests run with matrix Java version
4. Passes extra JVM args (locale, hash mode, etc.)

**Example for a test job:**

```bash
./gradlew build \
  -PjdkBuildVersion=<BUILD_VERSION> \
  -PjdkTestVersion=<TEST_VERSION> \
  -PjdkTestVendor=<VENDOR> \
  -Ptestng.test.extra.jvmargs="-Duser.country=US -Duser.language=en" \
  -Duser.country=US \
  -Duser.language=en
```

## How Multi-Version Testing Works

Let's trace through a complete example: **Testing an older Java version on Ubuntu with Turkish locale**

### Matrix Entry

```json
{
  "java_version": "<OLDER_LTS_VERSION>",
  "java_distribution": "temurin",
  "os": "ubuntu-latest",
  "tz": "America/New_York",
  "locale": {"language": "tr", "country": "TR"},
  "hash": {"value": "regular"}
}
```

### Workflow Execution

```
┌─────────────────────────────────────────────────────────────┐
│ GitHub Actions Runner (ubuntu-latest)                       │
│                                                             │
│ Step 1: Install Java                                        │
│   - Install Java <OLDER_LTS_VERSION> (Temurin)              │
│   - Install Java <BUILD_VERSION> (Temurin)                  │
│   - Set Java <BUILD_VERSION> as default (JAVA_HOME)         │
│                                                             │
│ Step 2: Set Environment                                     │
│   - TZ=America/New_York                                     │
│                                                             │
│ Step 3: Run Gradle                                          │
│   $ ./gradlew build \                                       │
│       -PjdkBuildVersion=<BUILD_VERSION> \                   │
│       -PjdkTestVersion=<OLDER_LTS_VERSION> \                │
│       -PjdkTestVendor=eclipse \                             │
│       -Ptestng.test.extra.jvmargs="-Duser.country=TR ..." \ │
│       -Duser.country=TR \                                   │
│       -Duser.language=tr                                    │
│                                                             │
│   ┌────────────────────────────────────────────────────-─┐  │
│   │ Gradle Process (runs with <BUILD_VERSION>)           │  │
│   │                                                      │  │
│   │ ┌─────────────────────────────────────────────────┐  │  │
│   │ │ Task: compileJava                               │  │  │
│   │ │ Toolchain: Java <BUILD_VERSION>                 │  │  │
│   │ │ Target: Java <TARGET_VERSION> bytecode          │  │  │
│   │ │ Locale: tr_TR (from -Duser.country/language)    │  │  │
│   │ └─────────────────────────────────────────────────┘  │  │
│   │                                                      │  │
│   │ ┌─────────────────────────────────────────────────┐  │  │
│   │ │ Task: test                                      │  │  │
│   │ │ Reads: jdkTestVersion=<OLDER_LTS_VERSION>       │  │  │
│   │ │ Finds: Java <OLDER_LTS_VERSION> (Temurin)       │  │  │
│   │ │ Spawns: New JVM with <OLDER_LTS_VERSION>        │  │  │
│   │ │                                                 │  │  │
│   │ │   ┌─────────────────────────────────────────┐   │  │  │
│   │ │   │ Test JVM (Java <OLDER_LTS_VERSION>)     │   │  │  │
│   │ │   │ Timezone: America/New_York              │   │  │  │
│   │ │   │ Locale: tr_TR                           │   │  │  │
│   │ │   │ Runs: TestNG test suite                 │   │  │  │
│   │ │   └─────────────────────────────────────────┘   │  │  │
│   │ │                                                 │  │  │
│   │ └─────────────────────────────────────────────────┘  │  │
│   │                                                      │  │
│   └─────────────────────────────────────────────────────-┘  │
│                                                             │
│ Step 4: Upload test reports (if failed)                     │
└─────────────────────────────────────────────────────────────┘
```

### Key Points

1. **Gradle runs with the build version** (default on system)
2. **Compilation uses the build version toolchain** (via `jdkBuildVersion`)
3. **Bytecode targets the minimum supported version** (via `targetJavaVersion`)
4. **Tests run with the matrix version** (via `jdkTestVersion` → toolchain)
5. **Locale is Turkish** (via `-Duser.country=TR -Duser.language=tr`)
6. **Timezone is EST/EDT** (via `TZ=America/New_York`)

**Note:** The specific Java versions used for building and testing are defined in `matrix.js` and build parameters.

## Integration with Build Logic

The test workflow integrates with TestNG's build logic through build parameters and toolchains.

### Build Parameters Flow

```
GitHub Actions (test.yml)
    │
    │ Sets properties:
    │   -PjdkBuildVersion=<BUILD_VERSION>
    │   -PjdkTestVersion=<TEST_VERSION>
    │   -Ptestng.test.extra.jvmargs="..."
    │
    ▼
Build Parameters Plugin (build-logic/build-parameters)
    │
    │ Defines parameters:
    │   integer("jdkBuildVersion") { defaultValue.set(<BUILD_VERSION>) }
    │   integer("jdkTestVersion") { ... }
    │
    ▼
Build Logic Plugins (build-logic/jvm, build-logic/code-quality)
    │
    │ Uses parameters:
    │   java.toolchain.configureToolchain(buildParameters.buildJdk)
    │   tasks.withType<Test> {
    │       buildParameters.testJdk?.let {
    │           javaLauncher.convention(javaToolchains.launcherFor(it))
    │       }
    │   }
    │
    ▼
Gradle Toolchains
    │
    │ Finds Java installations:
    │   - Java <TEST_VERSION> at /path/to/java-<TEST_VERSION>
    │   - Java <BUILD_VERSION> at /path/to/java-<BUILD_VERSION>
    │
    ▼
Task Execution
    │
    ├─→ compileJava: Uses <BUILD_VERSION> toolchain
    └─→ test: Spawns JVM with <TEST_VERSION>
```

### Specific Plugin Integration

#### testng.java.gradle.kts

```kotlin
java {
    toolchain {
        configureToolchain(buildParameters.buildJdk)  // Uses jdkBuildVersion
    }
}

tasks.configureEach<JavaCompile> {
    options.release.set(buildParameters.targetJavaVersion)  // Targets minimum supported version
}
```

**Workflow provides:** `-PjdkBuildVersion=<BUILD_VERSION>`
**Plugin uses:** Build version toolchain for compilation
**Result:** Code compiled with build version, targeting minimum supported bytecode

#### testng.testing.gradle.kts

```kotlin
tasks.withType<Test>().configureEach {
    useTestNG()
    buildParameters.testJdk?.let {
        javaLauncher.convention(javaToolchains.launcherFor(it))  // Uses jdkTestVersion
    }
    providers.gradleProperty("testng.test.extra.jvmargs")
        .orNull?.toString()?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let {
            jvmArgs(it.split(Regex("\\s+")))  // Applies extra JVM args
        }
}
```

**Workflow provides:**

- `-PjdkTestVersion=<TEST_VERSION>`
- `-Ptestng.test.extra.jvmargs="-Duser.country=TR -Duser.language=tr"`

**Plugin uses:**

- Test version toolchain for test execution
- Passes locale settings to test JVM

**Result:** Tests run with the specified test version in Turkish locale

## Customizing the Matrix

### Changing the Number of Jobs

Edit the workflow file:

```yaml
env:
  MATRIX_JOBS: 7  # Change this number
```

Or set it in the workflow dispatch:

```yaml
on:
  workflow_dispatch:
    inputs:
      matrix_jobs:
        description: 'Number of matrix jobs to generate'
        default: '7'
```

### Adding a New Java Version

Edit `.github/workflows/matrix.js`:

```javascript
matrix.addAxis({
  name: 'java_version',
  values: [
    '<MIN_SUPPORTED>',
    '<BUILD_VERSION>',
    '<LATEST_LTS>',
    '<NEW_VERSION>',  // Add new version
    eaJava,
  ]
});

// Ensure at least one job with the new version
matrix.generateRow({java_version: "<NEW_VERSION>"});
```

### Adding a New Operating System

Edit `.github/workflows/matrix.js`:

```javascript
matrix.addAxis({
  name: 'os',
  values: [
    'ubuntu-latest',
    'windows-latest',
    'macos-latest',
    'ubuntu-20.04',  // Add specific version
  ]
});
```

### Testing Locally with Matrix Configuration

You can reproduce a CI job locally by using the same parameters:

```bash
# From the "Steps to reproduce" output in CI logs
TZ="America/New_York" ./gradlew build \
  -PjdkBuildVersion=<BUILD_VERSION> \
  -PjdkTestVersion=<TEST_VERSION> \
  -Ptestng.test.extra.jvmargs="-Duser.country=TR -Duser.language=tr" \
  -Duser.country=TR \
  -Duser.language=tr
```

## Special Test Configurations

### Hash Collision Testing

Some jobs run with `-XX:hashCode=2`, which makes all objects return the same hash code:

```javascript
if (v.hash.value === 'same') {
    jvmArgs.push('-XX:+UnlockExperimentalVMOptions', '-XX:hashCode=2');
}
```

**Why:** Tests that TestNG's hash-based collections (HashMap, HashSet) work correctly even with hash collisions.

### JIT Stress Testing

Some jobs randomize JIT compiler behavior:

```javascript
if (v.java_distribution !== 'semeru' && Math.random() > 0.5) {
    v.name += ', stress JIT';
    jvmArgs.push('-XX:+UnlockDiagnosticVMOptions');
    jvmArgs.push('-XX:+StressGCM');  // Randomize instruction scheduling
    jvmArgs.push('-XX:+StressLCM');
    if (v.java_version >= 16) {
        jvmArgs.push('-XX:+StressIGVN');
    }
    if (v.java_version >= <MIN_VERSION_FOR_STRESS_CCP>) {
        jvmArgs.push('-XX:+StressCCP');
    }
}
```

**Why:** Randomizing JIT compilation can reveal race conditions and synchronization bugs.

### Turkish Locale Testing

Turkish locale is specifically included because of the infamous "Turkish I problem":

```javascript
// In Turkish:
"i".toUpperCase() === "İ"  // Not "I"
"I".toLowerCase() === "ı"  // Not "i"
```

**Why:** Many bugs in Java code are caused by case-insensitive string comparisons in Turkish locale.

## Troubleshooting CI Failures

### Job Fails Only in CI

**Problem:** Tests pass locally but fail in CI

**Debugging steps:**

1. **Check the matrix configuration** in the failed job
2. **Reproduce locally** using the exact parameters:

   ```bash
   # Copy from "Steps to reproduce" in CI logs
   TZ="Pacific/Chatham" ./gradlew build -PjdkTestVersion=<TEST_VERSION> ...
   ```
   
3. **Check for environment-specific issues:**
   - Timezone-dependent tests
   - Locale-dependent string operations
   - OS-specific file paths

### Job Fails Only with Specific Java Version

**Problem:** Tests pass with one Java version but fail with another

**Debugging steps:**

1. **Test locally with that Java version:**

   ```bash
   ./gradlew test -PjdkTestVersion=<FAILING_VERSION>
   ```
   
2. **Check for version-specific APIs:**

   - Are you using APIs from newer Java versions?
   - Are you using deprecated APIs removed in newer versions?

3. **Check compiler warnings:**

   ```bash
   ./gradlew compileJava -PjdkBuildVersion=<FAILING_VERSION>
   ```

### Job Fails Only on Specific OS

**Problem:** Tests pass on Ubuntu but fail on Windows

**Debugging steps:**

1. **Check for path separator issues:**

   ```java
   // Bad
   String path = "src/test/resources";

   // Good
   String path = Paths.get("src", "test", "resources").toString();
   ```
2. **Check for line ending issues:**

   - Windows uses `\r\n`
   - Linux/macOS use `\n`

3. **Check for case-sensitive file system issues:**
   
   - Windows is case-insensitive
   - Linux/macOS are case-sensitive

### Matrix Job Timeout

**Problem:** Job runs for more than 6 hours and times out

**Solution:** Increase the timeout in the workflow:

```yaml
jobs:
  build:
    timeout-minutes: 360  # 6 hours (default)
    # or
    timeout-minutes: 480  # 8 hours
```

## Advanced Topics

### Understanding the Matrix Builder Algorithm

The matrix builder uses a weighted random selection algorithm:

1. **Define axes** with possible values and weights
2. **Apply constraints** (implications and exclusions)
3. **Generate guaranteed rows** (critical combinations)
4. **Fill remaining slots** with random combinations
5. **Sort by name** for consistent ordering

### Viewing the Generated Matrix

To see what matrix will be generated:

```bash
cd .github/workflows
MATRIX_JOBS=7 node matrix.js
```

This outputs the JSON matrix that GitHub Actions will use.

### Matrix Builder Dependencies

The matrix builder uses:

- `matrix_builder.js` - Core matrix generation logic
- `matrix.js` - TestNG-specific configuration

The `MatrixBuilder` class is defined in `matrix_builder.js` and provides:

- `addAxis()` - Add a new dimension to test
- `imply()` - Add constraints between axes
- `exclude()` - Exclude specific combinations
- `generateRow()` - Force a specific combination
- `generateRows()` - Generate N random combinations

## Summary

The test workflow is a sophisticated system that:

1. **Generates a matrix** of test configurations using `matrix.js`
2. **Runs tests in parallel** across different Java versions, OSes, and locales
3. **Uses Gradle toolchains** to test with different Java versions
4. **Integrates with build logic** through build parameters
5. **Ensures critical combinations** are always tested
6. **Randomizes other combinations** to maximize coverage

This ensures TestNG works correctly across a wide variety of environments while keeping CI time reasonable (7 jobs instead of thousands).

## Complete Flow Diagram

Here's the complete flow from workflow trigger to test execution:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. Workflow Trigger (push, pull_request, workflow_dispatch)                 │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. Matrix Preparation Job                                                   │
│                                                                             │
│    $ node .github/workflows/matrix.js                                       │
│                                                                             │
│    ┌─────────────────────────────────────────────────────────────────────┐  │
│    │ MatrixBuilder (matrix_builder.js)                                   │  │
│    │                                                                     │  │
│    │ • Defines 6 axes (java_version, java_distribution, os, tz,          │  │
│    │   locale, hash)                                                     │  │
│    │ • Applies constraints (implications, exclusions)                    │  │
│    │ • Generates guaranteed rows for each Java version being tested      │  │
│    │ • Fills remaining slots with random combinations                    │  │
│    │ • Total: 7 jobs (configurable via MATRIX_JOBS)                      │  │
│    └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│    Output: JSON matrix                                                      │
│    {                                                                        │
│      "include": [                                                           │
│        {"java_version": "<VERSION_1>", "os": "ubuntu-latest", ...},         │
│        {"java_version": "<VERSION_2>", "os": "windows-latest", ...},        │
│        ...                                                                  │
│      ]                                                                      │
│    }                                                                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. Build Jobs (7 jobs run in parallel)                                      │
│                                                                             │
│    For each matrix entry:                                                   │
│                                                                             │
│    ┌────────────────────────────────────────────────────────────────────-─┐ │
│    │ Step 1: Setup Java                                                   │ │
│    │                                                                      │ │
│    │ $ actions/setup-java@v4                                              │ │
│    │   java-version: |                                                    │ │
│    │     ${{ matrix.non_ea_java_version }}  # Matrix test version         │ │
│    │     <BUILD_VERSION>                     # Build version              │ │
│    │   distribution: ${{ matrix.java_distribution }}  # e.g., temurin     │ │
│    │                                                                      │ │
│    │ Result:                                                              │ │
│    │   - Java <TEST_VERSION> installed at /path/to/java-<TEST_VERSION>    │ │
│    │   - Java <BUILD_VERSION> installed at /path/to/java-<BUILD_VER>      │ │
│    │   - JAVA_HOME=/path/to/java-<BUILD_VERSION> (default)                │ │
│    └────────────────────────────────────────────────────────────────────-─┘ │
│                                                                             │
│    ┌───────────────────────────────────────────────────────────────────-──┐ │
│    │ Step 2: Set Environment                                              │ │
│    │                                                                      │ │
│    │ $ export TZ="${{ matrix.tz }}"  # e.g., America/New_York             │ │
│    └────────────────────────────────────────────────────────────────────-─┘ │
│                                                                             │
│    ┌───────────────────────────────────────────────────────────────────-──┐ │
│    │ Step 3: Run Gradle Build                                             │ │
│    │                                                                      │ │
│    │ $ ./gradlew build \                                                  │ │
│    │     -PjdkBuildVersion=<BUILD_VERSION> \                              │ │
│    │     -PjdkTestVersion=${{ matrix.java_version }} \                    │ │
│    │     -PjdkTestVendor=${{ matrix.java_vendor }} \                      │ │
│    │     -Ptestng.test.extra.jvmargs="${{ matrix.testExtraJvmArgs }}" \   │ │
│    │     ${{ matrix.extraGradleArgs }}                                    │ │
│    │                                                                      │ │
│    │ ┌───────────────────────────────────────────────────────────────┐    │ │
│    │ │ Gradle Daemon (Java <BUILD_VERSION>)                          │    │ │
│    │ │                                                               │    │ │
│    │ │ Reads build-logic/build-parameters/build.gradle.kts           │    │ │
│    │ │   → Defines jdkBuildVersion, jdkTestVersion parameters        │    │ │
│    │ │                                                               │    │ │
│    │ │ Applies build-logic/jvm/testng.java.gradle.kts                │    │ │
│    │ │   → Configures Java toolchain for compilation                 │    │ │
│    │ │   → Sets --release=<TARGET_VERSION> for javac                 │    │ │
│    │ │                                                               │    │ │
│    │ │ Applies build-logic/code-quality/testng.testing.gradle.kts    │    │ │
│    │ │   → Configures Test task with toolchain                       │    │ │
│    │ │   → Sets javaLauncher for test execution                      │    │ │
│    │ │                                                               │    │ │
│    │ │ ┌─────────────────────────────────────────────────────────┐   │    │ │
│    │ │ │ Task: compileJava                                       │   │    │ │
│    │ │ │                                                         │   │    │ │
│    │ │ │ Toolchain: Java <BUILD_VERSION> (from jdkBuildVersion)  │   │    │ │
│    │ │ │ Compiler: /path/to/java-<BUILD_VER>/bin/javac           │   │    │ │
│    │ │ │ Options: --release=<TARGET_VER> (targetJavaVersion)     │   │    │ │
│    │ │ │                                                         │   │    │ │
│    │ │ │ Result: Java <TARGET_VERSION> bytecode                  │   │    │ │
│    │ │ └─────────────────────────────────────────────────────────┘   │    │ │
│    │ │                                                               │    │ │
│    │ │ ┌─────────────────────────────────────────────────────────┐   │    │ │
│    │ │ │ Task: test                                              │   │    │ │
│    │ │ │                                                         │   │    │ │
│    │ │ │ Reads: jdkTestVersion=<TEST_VER> (build parameter)      │   │    │ │
│    │ │ │ Finds: Java <TEST_VERSION> toolchain                    │   │    │ │
│    │ │ │ Launcher: /path/to/java-<TEST_VER>/bin/java             │   │    │ │
│    │ │ │                                                         │   │    │ │
│    │ │ │ Spawns new JVM:                                         │   │    │ │
│    │ │ │ $ /path/to/java-<TEST_VER>/bin/java \                   │   │    │ │
│    │ │ │     -Duser.country=TR \                                 │   │    │ │
│    │ │ │     -Duser.language=tr \                                │   │    │ │
│    │ │ │     -XX:+UnlockExperimentalVMOptions \                  │   │    │ │
│    │ │ │     -XX:hashCode=2 \                                    │   │    │ │
│    │ │ │     -cp ... \                                           │   │    │ │
│    │ │ │     org.testng.TestNG ...                               │   │    │ │
│    │ │ │                                                         │   │    │ │
│    │ │ │ ┌───────────────────────────────────────────────────┐   │   │    │ │
│    │ │ │ │ Test JVM (Java <TEST_VERSION>)                    │   │   │    │ │
│    │ │ │ │                                                   │   │   │    │ │
│    │ │ │ │ Environment:                                      │   │   │    │ │
│    │ │ │ │   TZ=America/New_York                             │   │   │    │ │
│    │ │ │ │   user.country=TR                                 │   │   │    │ │
│    │ │ │ │   user.language=tr                                │   │   │    │ │
│    │ │ │ │   hashCode=2 (all objects same hash)              │   │   │    │ │
│    │ │ │ │                                                   │   │   │    │ │
│    │ │ │ │ Runs: TestNG test suite                           │   │   │    │ │
│    │ │ │ │   → test.SimpleBaseTest                           │   │   │    │ │
│    │ │ │ │   → test.FactoryTest                              │   │   │    │ │
│    │ │ │ │   → ... (all tests)                               │   │   │    │ │
│    │ │ │ │                                                   │   │   │    │ │
│    │ │ │ │ Result: Test reports                              │   │   │    │ │
│    │ │ │ └───────────────────────────────────────────────────┘   │   │    │ │
│    │ │ │                                                         │   │    │ │
│    │ │ └─────────────────────────────────────────────────────────┘   │    │ │
│    │ │                                                               │    │ │
│    │ └───────────────────────────────────────────────────────────────┘    │ │
│    │                                                                      │ │
│    └────────────────────────────────────────────────────────────────────-─┘ │
│                                                                             │
│    ┌─────────────────────────────────────────────────────────────────────┐  │
│    │ Step 4: Upload Test Reports (if failed)                             │  │
│    │                                                                     │  │
│    │ $ actions/upload-artifact@v4                                        │  │
│    │   name: test-reports-${{ matrix.name }}                             │  │
│    │   path: build/reports/tests/                                        │  │
│    └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. Results                                                                  │
│                                                                             │
│    ✅ All 7 jobs passed → Workflow succeeds                                 │
│    ❌ Any job failed → Workflow fails, test reports uploaded                │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Related Documentation

- [BUILD_SYSTEM.md](BUILD_SYSTEM.md) - Complete build system documentation
- [JAVA_VERSIONS_QUICK_REFERENCE.md](JAVA_VERSIONS_QUICK_REFERENCE.md) - Java version usage
- [GitHub Actions Matrix Documentation](https://docs.github.com/en/actions/using-jobs/using-a-matrix-for-your-jobs)
- [Gradle Toolchains Documentation](https://docs.gradle.org/current/userguide/toolchains.html)




