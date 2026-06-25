# Migrating away from `org.testng.Assert`

`org.testng.Assert` is **deprecated for removal** as of TestNG 7.13.0. TestNG no longer wants to
maintain its own assertion library and instead recommends a dedicated one such as
[AssertJ](https://assertj.github.io/doc/).

This is the first step of a longer plan:

- [GITHUB-2649](https://github.com/testng-team/testng/issues/2649) — deprecate the internal
  assertion API and let users choose their preferred library.
- [GITHUB-3197](https://github.com/testng-team/testng/issues/3197) — make the `testng-asserts`
  module optional.

Eventually the `testng-asserts` module (which contains `org.testng.Assert`) will leave the main
`org.testng:testng` artifact and become an **optional, standalone artifact**. Migrating now keeps
your test suite working across that transition.

## What replaces `org.testng.Assert`?

[AssertJ](https://assertj.github.io/doc/). Its fluent API covers everything `org.testng.Assert`
offers, with better failure messages:

```java
// Before — org.testng.Assert
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

assertEquals(actual, expected);
assertTrue(result);

// After — AssertJ
import static org.assertj.core.api.Assertions.assertThat;

assertThat(actual).isEqualTo(expected);
assertThat(result).isTrue();
```

> **Note the parameter order.** `org.testng.Assert` uses `(actual, expected)`. AssertJ reads
> `assertThat(actual).isEqualTo(expected)`. The automated recipe below handles this for you.

Add AssertJ to your build:

```xml
<!-- Maven -->
<dependency>
  <groupId>org.assertj</groupId>
  <artifactId>assertj-core</artifactId>
  <scope>test</scope>
</dependency>
```

```kotlin
// Gradle
testImplementation("org.assertj:assertj-core:<latest>")
```

## Automated migration with OpenRewrite

[OpenRewrite](https://docs.openrewrite.org/) provides a recipe that rewrites **the entire**
`org.testng.Assert` API to AssertJ. The full Assert coverage is available since
[rewrite-testing-frameworks v3.39.0](https://github.com/openrewrite/rewrite-testing-frameworks/releases/tag/v3.39.0).

Recipe: `org.openrewrite.java.testing.testng.TestNgToAssertj` — *"Migrate TestNG assertions to
AssertJ"*. It handles both qualified (`Assert.assertEquals(...)`) and static-import
(`assertEquals(...)`) usages.

### Gradle

Apply the OpenRewrite plugin and activate the recipe:

```kotlin
plugins {
    id("org.openrewrite.rewrite") version "<latest>"
}

dependencies {
    rewrite("org.openrewrite.recipe:rewrite-testing-frameworks:<3.39.0-or-newer>")
}

rewrite {
    activeRecipe("org.openrewrite.java.testing.testng.TestNgToAssertj")
}
```

Run it:

```bash
./gradlew rewriteRun
```

### Maven

```xml
<plugin>
  <groupId>org.openrewrite.maven</groupId>
  <artifactId>rewrite-maven-plugin</artifactId>
  <version><!-- latest --></version>
  <configuration>
    <activeRecipes>
      <recipe>org.openrewrite.java.testing.testng.TestNgToAssertj</recipe>
    </activeRecipes>
  </configuration>
  <dependencies>
    <dependency>
      <groupId>org.openrewrite.recipe</groupId>
      <artifactId>rewrite-testing-frameworks</artifactId>
      <version><!-- 3.39.0 or newer --></version>
    </dependency>
  </dependencies>
</plugin>
```

Run it:

```bash
mvn rewrite:run
```

> Check the [OpenRewrite docs](https://docs.openrewrite.org/) for the current plugin and recipe
> versions. After the recipe runs, add the `org.assertj:assertj-core` test dependency (shown above)
> and run your test suite to confirm everything passes.

## Manual migration

If you prefer to migrate by hand, the most common mappings are:

| `org.testng.Assert`                     | AssertJ                                          |
| --------------------------------------- | ------------------------------------------------ |
| `assertEquals(actual, expected)`        | `assertThat(actual).isEqualTo(expected)`         |
| `assertNotEquals(actual, expected)`     | `assertThat(actual).isNotEqualTo(expected)`      |
| `assertTrue(condition)`                 | `assertThat(condition).isTrue()`                 |
| `assertFalse(condition)`                | `assertThat(condition).isFalse()`                |
| `assertNull(object)`                    | `assertThat(object).isNull()`                    |
| `assertNotNull(object)`                 | `assertThat(object).isNotNull()`                 |
| `assertSame(actual, expected)`          | `assertThat(actual).isSameAs(expected)`          |
| `assertNotSame(actual, expected)`       | `assertThat(actual).isNotSameAs(expected)`       |
| `assertThrows(Type.class, runnable)`    | `assertThatThrownBy(runnable).isInstanceOf(Type.class)` |
| `fail(message)`                         | `fail(message)` (`org.assertj.core.api.Assertions.fail`) |

See the [AssertJ documentation](https://assertj.github.io/doc/) for the full API.

## Packaging notes

`org.testng.Assert` is no longer bundled in the main `org.testng:testng` jar. To keep using it, add
the standalone artifact, which brings AssertJ transitively:

```xml
<dependency>
  <groupId>org.testng</groupId>
  <artifactId>testng-asserts</artifactId>
  <scope>test</scope>
</dependency>
```

> **OSGi:** `testng-asserts` currently ships as a plain JAR, not an OSGi bundle. `org.testng.Assert`
> and `org.testng.FileAssert` share the `org.testng` package with the core classes exported by the
> `testng` bundle, so giving `testng-asserts` its own bundle would create a split package. Proper
> OSGi support is tracked as a follow-up ([GITHUB-3197](https://github.com/testng-team/testng/issues/3197));
> OSGi users should prefer migrating to AssertJ.
