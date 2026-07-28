# Java Versions Quick Reference

This is a quick reference guide for understanding how TestNG uses different Java versions during the build process.

## TL;DR

- **You need Java 25 installed** to run any Gradle command
- **TestNG artifacts target Java 11** (they work on Java 11+)
- **Tests can run on Java 11, 17, 21, 25, or 26** (plus early-access 27 and 28) to verify compatibility

## The Three Java Versions

TestNG's build uses **three different Java versions** for different purposes:

```
┌─────────────────────────────────────────────────────────┐
│                    Your Machine                         │
│                                                         │
│  Java 25 (default) ──────────────┐                      │
│  Java 11 (installed)             │                      │
│                                  │                      │
│  ┌───────────────────────────────▼─────────────────-─┐  │
│  │ Gradle Process (runs with Java 25)                │  │
│  │                                                   │  │
│  │  ┌───────────────────────────────────────────--─┐ │  │
│  │  │ Compile Task                                 │ │  │
│  │  │ • Uses: Java 25 toolchain                    │ │  │
│  │  │ • Produces: Java 11 bytecode                 │ │  │
│  │  └──────────────────────────────────────────--──┘ │  │
│  │                                                   │  │
│  │  ┌──────────────────────────────────────────-─-─┐ │  │
│  │  │ Test Task                                    │ │  │
│  │  │ • Spawns new JVM with: Java 11               │ │  │
│  │  │ • Runs tests in that JVM                     │ │  │
│  │  └──────────────────────────────────────────--──┘ │  │
│  │                                                   │  │
│  └───────────────────────────────────────────────-───┘  │
└─────────────────────────────────────────────────────────┘
```

### 1. Gradle Runtime (Java 25)

**What:** The Java version that runs Gradle itself

**Why Java 25:** Gradle 9 runs on JDK 25, the `com.gradleup.nmcp` plugin requires Java 17+, and we use Java 25 (latest LTS) for modern tooling

**How to check:**

```bash
java -version
# Should show Java 25 or higher
```

**How to set:**

```bash
# Using mise (recommended)
mise use java@temurin-25

# Using SDKMAN
sdk use java 25.0.3-tem

# Using jenv
jenv local 25

# Or set JAVA_HOME
export JAVA_HOME=/path/to/java-25
```

### 2. Build Toolchain (Java 25)

**What:** The Java version used to compile TestNG code

**Configured by:** `-PjdkBuildVersion=25` (default: 25)

**Why Java 25:** Latest LTS version with modern Java features and tooling

**Note:** Even though we compile with Java 25, we use `--release=11` flag to ensure the bytecode is Java 11 compatible

### 3. Target Bytecode (Java 11)

**What:** The Java version that TestNG artifacts are compatible with

**Configured by:** `-PtargetJavaVersion=11` (default: 11)

**Why Java 11:** TestNG supports users running Java 11+

**How it works:** The `--release=11` flag tells javac to:

- Only use Java 11 APIs
- Generate Java 11 bytecode
- Ensure compatibility with Java 11 JVMs

### 4. Test Runtime (Configurable)

**What:** The Java version used to run tests

**Configured by:** `-PjdkTestVersion=X` (default: same as build toolchain)

**Why configurable:** To verify TestNG works correctly on different Java versions

**Example:**

```bash
# Test with Java 11
./gradlew test -PjdkTestVersion=11

# Test with Java 21
./gradlew test -PjdkTestVersion=21
```

## Common Scenarios

### Scenario 1: Local Development

**You have:** Java 25 installed

**You run:** `./gradlew build`

**What happens:**

- ✅ Gradle runs with Java 25
- ✅ Code compiles with Java 25 toolchain
- ✅ Bytecode targets Java 11
- ✅ Tests run with Java 25 (default)

### Scenario 2: Testing Java 11 Compatibility

**You have:** Java 25 and Java 11 installed

**You run:** `./gradlew test -PjdkTestVersion=11`

**What happens:**

- ✅ Gradle runs with Java 25
- ✅ Code compiles with Java 25 toolchain
- ✅ Bytecode targets Java 11
- ✅ Tests run with Java 11 (via toolchain)

### Scenario 3: CI Testing Multiple Java Versions

**GitHub Actions installs:** Java 25 (for Gradle) + one test version (11, 17, 21, 25, or 26; early-access 27 and 28 come from jdk.java.net)

**For a Java 11 test job, the workflow runs:** `./gradlew build -PjdkBuildVersion=25 -PjdkTestVersion=11`

**What happens:**

- ✅ Gradle runs with Java 25 (default, listed last in setup-java)
- ✅ Code compiles with Java 25 toolchain
- ✅ Bytecode targets Java 11
- ✅ Tests run with Java 11 (via toolchain)

**The CI runs separate jobs for each test version:** Java 11, 17, 21, 25, 26, plus early-access 27 and 28

## FAQ

### Q: Why can't I use Java 11 to run Gradle?

**A:** Gradle 9 runs on JDK 25, and the `com.gradleup.nmcp.aggregation` plugin (for Maven Central publishing) requires Java 17+. We use Java 25 (latest LTS) for modern tooling. This plugin is applied in the root `build.gradle.kts`, so it's loaded for every Gradle command, not just publishing.

### Q: If we compile with Java 25, how are the artifacts Java 11 compatible?

**A:** We use the `--release=11` flag, which tells the Java 25 compiler to:

1. Only allow Java 11 APIs (compilation error if you use Java 12+ APIs)
2. Generate Java 11 bytecode
3. Use Java 11 standard library signatures

This is better than the old `-source 11 -target 11` approach.

### Q: How does Gradle run tests with Java 11 when Gradle itself runs with Java 25?

**A:** Gradle uses **toolchains**. When you set `-PjdkTestVersion=11`, Gradle:

1. Finds Java 11 on your system
2. Spawns a new JVM process using Java 11's executable
3. Runs tests in that Java 11 process
4. Collects results back to the main Gradle process

### Q: What if I don't have Java 11 installed but want to test with it?

**A:** Gradle can auto-download JDKs. Add to `gradle.properties`:

```properties
org.gradle.java.installations.auto-download=true
```

Then run:
```bash
./gradlew test -PjdkTestVersion=11
```

Gradle will automatically download Java 11 and use it for tests.

### Q: Do I need to install Java 25 if I only want to run TestNG in my project?

**A:** No! This is only for **building** TestNG itself. If you're just **using** TestNG in your project, you only need Java 11+ (whatever version your project uses).

## Quick Command Reference

```bash
# Build with default settings (Java 25 for build, Java 25 for tests)
./gradlew build

# Build and test with Java 11
./gradlew build -PjdkTestVersion=11

# Build and test with Java 21
./gradlew build -PjdkTestVersion=21

# See all available build parameters
./gradlew parameters

# Check which Java Gradle is using
./gradlew --version
```

## Visual Summary

```
┌──────────────────────────────────────────────────────────────┐
│ Java Version Usage in TestNG Build                           │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Gradle Runtime:     Java 25   (latest LTS)                  │
│  Build Toolchain:    Java 25   (modern compilation)          │
│  Target Bytecode:    Java 11   (compatibility)               │
│  Test Runtime:       Java 11+  (configurable via parameter)  │
│                                                              │
│  Result: TestNG artifacts work on Java 11+                   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

