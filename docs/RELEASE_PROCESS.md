# TestNG Release Process

This document explains the complete release process for TestNG using the Maven Central Portal API.

> **Note:** This process uses the new Maven Central Portal (replacing the legacy OSSRH system that was sunset in June 2025).

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Release Workflow](#release-workflow)
  - [Option 1: Automatic Release (Recommended)](#option-1-automatic-release-recommended)
  - [Option 2: Manual Release](#option-2-manual-release)
- [Snapshot Publishing](#snapshot-publishing)
- [Post-Release Activities](#post-release-activities)
- [Troubleshooting](#troubleshooting)

## Overview

TestNG uses GitHub Actions to automate the release process. The workflow:

1. **Builds** all artifacts with the correct Java version
2. **Signs** artifacts with PGP keys
3. **Publishes** to Maven Central Portal
4. **Releases** automatically or waits for manual approval

```
┌─────────────────────────────────────────────────────────────────────┐
│                     TestNG Release Process                          │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
                    ┌─────────────────────────┐
                    │  Trigger GitHub Action  │
                    │  "Publish to Maven      │
                    │   Central"              │
                    └─────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
                    ▼                           ▼
        ┌───────────────────┐       ┌───────────────────┐
        │ AUTOMATIC         │       │ USER_MANAGED      │
        │ (Recommended)     │       │ (Manual Review)   │
        └───────────────────┘       └───────────────────┘
                    │                           │
                    ▼                           ▼
        ┌───────────────────┐       ┌───────────────────┐
        │ 1. Build & Sign   │       │ 1. Build & Sign   │
        │ 2. Upload         │       │ 2. Upload         │
        │ 3. Validate       │       │ 3. Validate       │
        │ 4. Auto-Release   │       │ 4. Wait for       │
        │                   │       │    Manual Publish │
        └───────────────────┘       └───────────────────┘
                    │                           │
                    │                           ▼
                    │               ┌───────────────────┐
                    │               │ Go to Portal:     │
                    │               │ central.sonatype  │
                    │               │ .com/publishing   │
                    │               │ → Review & Publish│
                    │               └───────────────────┘
                    │                           │
                    └───────────┬───────────────┘
                                ▼
                    ┌─────────────────────────┐
                    │ Artifacts on Maven      │
                    │ Central (~30 minutes)   │
                    └─────────────────────────┘
                                │
                                ▼
                    ┌─────────────────────────┐
                    │ Post-Release Tasks      │
                    │ - Create Git tag        │
                    │ - Promote changelog     │
                    │ - Create GitHub release │
                    │ - Send announcement     │
                    │ - Update README + docs  │
                    └─────────────────────────┘
```

## Prerequisites

### Required Permissions

You need:

- **GitHub**: Write access to the testng-team/testng repository
- **Maven Central**: Account with publishing rights for `org.testng` namespace
- **PGP Key**: For signing artifacts (already configured in GitHub Secrets)

### Required Secrets (Already Configured)

The following secrets must be configured in GitHub repository settings. The GitHub Actions workflows automatically map these secrets to the environment variables required by the build:

| GitHub Secret Name | Maps to Environment Variable | Description | Where to Get |
|-------------------|------------------------------|-------------|--------------|
| `NEXUS_USERNAME` | `CENTRAL_PORTAL_USERNAME` | Central Portal username/token | https://central.sonatype.com/ → Generate User Token |
| `NEXUS_PASSWORD` | `CENTRAL_PORTAL_PASSWORD` | Central Portal password/token | https://central.sonatype.com/ → Generate User Token |
| `GPG_PRIVATE_KEY` | `SIGNING_PGP_PRIVATE_KEY` | PGP private key for signing | Your PGP keyring |
| `GPG_PASSPHRASE` | `SIGNING_PGP_PASSPHRASE` | PGP key passphrase | Your PGP key passphrase |

**Note**: When publishing manually (outside of GitHub Actions), you must export the environment variable names shown in the second column (e.g., `CENTRAL_PORTAL_USERNAME`, not `NEXUS_USERNAME`).

## Release Workflow

Releases are made from `master`, and only from `master`. The branches that carry a release tag
further back, `release_7.5` above all, are history rather than a supported path: they explain how a
tag such as 7.5.1 came about, and nothing is published from them today.

### Option 1: Automatic Release (Recommended)

This is the simplest approach - artifacts are automatically published to Maven Central without manual intervention.

#### Step 1: Trigger the Workflow

1. Go to https://github.com/testng-team/testng/actions
2. Click on **"Publish to Maven Central"** workflow
3. Click **"Run workflow"** button
4. Select:

   - **Branch**: `master`
   - **Publishing type**: `AUTOMATIC`

5. Click **"Run workflow"**.

#### Step 2: Monitor Progress

The workflow will:

1. ✅ **Validate Gradle wrapper** (security check)
2. ✅ **Set up JDK 25** (required for building and nmcp plugin)
3. ✅ **Build all artifacts** (testng.jar, sources, javadoc)
4. ✅ **Sign artifacts** with PGP key
5. ✅ **Upload to Central Portal**
6. ✅ **Validate artifacts** (POM metadata, signatures, etc.)
7. ✅ **Automatically publish** to Maven Central

Watch the workflow logs for progress. The entire process takes approximately 5-10 minutes.

#### Step 3: Verify Publication

After the workflow completes:

1. **Check the workflow logs** for the completion message:

   ```
   ✅ Artifacts published to Maven Central and will be automatically released.
   Check status at: https://central.sonatype.com/publishing
   ```

2. **Wait ~30 minutes** for Maven Central to sync

3. **Verify the release** at:

   - https://central.sonatype.com/artifact/org.testng/testng
   - https://repo1.maven.org/maven2/org/testng/testng/

#### Step 4: Proceed to Post-Release Activities

See [Post-Release Activities](#post-release-activities) section below.

---

### Option 2: Manual Release

This approach uploads artifacts to Central Portal but waits for you to manually review and publish them.

**Use this when:**

- You want to review artifacts before publishing
- You're doing a major release and want extra caution
- You want to coordinate the release timing

#### Step 1: Trigger the Workflow

1. Go to https://github.com/testng-team/testng/actions
2. Click on **"Publish to Maven Central"** workflow
3. Click **"Run workflow"** button
4. Select:

   - **Branch**: `master`
   - **Publishing type**: `USER_MANAGED`

5. Click **"Run workflow"**

#### Step 2: Monitor Progress

The workflow will:

1. ✅ **Validate Gradle wrapper** (security check)
2. ✅ **Set up JDK 25** (required for building and nmcp plugin)
3. ✅ **Build all artifacts** (testng.jar, sources, javadoc)
4. ✅ **Sign artifacts** with PGP key
5. ✅ **Upload to Central Portal**
6. ✅ **Validate artifacts** (POM metadata, signatures, etc.)
7. ⏸️  **Wait for manual publish** (you do this next)

#### Step 3: Review and Publish

After the workflow completes:

1. **Check the workflow logs** for the completion message:

   ```
   ✅ Artifacts staged in Central Portal.
   👉 Go to https://central.sonatype.com/publishing to review and publish the deployment.
   ```

2. **Go to Central Portal**: https://central.sonatype.com/publishing

3. **Sign in** with your Sonatype account

4. **Find your deployment**:

   - You should see a deployment for `org.testng:testng`
   - Status will be "PENDING" or "VALIDATED"

5. **Review the deployment**:

   - Click on the deployment to see details
   - Verify the version number
   - Check the artifacts list (jar, sources, javadoc, pom)
   - Verify signatures are present

6. **Publish the deployment**:

   - Click the **"Publish"** button
   - Confirm the action
   - Wait for the status to change to "PUBLISHED"

#### Step 4: Verify Publication

1. **Wait ~30 minutes** for Maven Central to sync

2. **Verify the release** at:

   - https://central.sonatype.com/artifact/org.testng/testng
   - https://repo1.maven.org/maven2/org/testng/testng/

#### Step 5: Proceed to Post-Release Activities

See [Post-Release Activities](#post-release-activities) section below.

---

## Snapshot Publishing

Snapshots are automatically published to Maven Central Snapshots repository whenever code is pushed to the `master` branch.

### Automatic Snapshot Publishing

**Trigger**: Push to `master` branch

**Workflow**: `.github/workflows/publish-snapshot.yml`

**What happens**:

1. ✅ Build artifacts (no signing required for snapshots)
2. ✅ Upload to Central Snapshots repository
3. ✅ Immediately available

**Access snapshots**:

```xml
<!-- Maven -->
<repository>
    <id>central-snapshots</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
</repository>

<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.10.0-SNAPSHOT</version>
</dependency>
```

```kotlin
// Gradle
repositories {
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

dependencies {
    testImplementation("org.testng:testng:7.10.0-SNAPSHOT")
}
```

**Note**: As of June 30, 2025, the old OSSRH snapshot repository (`s01.oss.sonatype.org`) was shut down. Use the new Central Portal snapshot URL shown above.

### Manual Snapshot Publishing

If you need to manually publish a snapshot:

```bash
./gradlew publishAllPublicationsToCentralSnapshotsRepository \
  -Prelease=false \
  --no-daemon \
  --stacktrace
```

**Required environment variables** (see the [Required Secrets](#required-secrets-already-configured) table):

```bash
export CENTRAL_PORTAL_USERNAME="your-token-username"
export CENTRAL_PORTAL_PASSWORD="your-token-password"
```

**Note**: Use the environment variable names (`CENTRAL_PORTAL_*`), not the GitHub secret names (`NEXUS_*`).

---

## Post-Release Activities

After artifacts are published to Maven Central, complete these tasks:

### 1. Create Git Tag

Every release must be tagged in Git, and tagging comes first: the next step adds commits, and the
tag has to point at the commit the artifacts were built from.

That commit is not always your local `HEAD`. `Publish to Maven Central` is a `workflow_dispatch`,
and its checkout takes the head of the branch it was dispatched on, at the moment it was dispatched
— while you reach this step half an hour later, once Central has synced, on a branch that may have
moved. Read the commit off the run instead of assuming it:

Name the run outright. Picking the newest successful one would be a guess: a re-run, a
`USER_MANAGED` run that staged but was never published, and a second dispatch of the same version
are all successful runs, and all of them build a commit that declares this version.

```bash
VERSION="7.10.0"  # Replace with actual version

# Find the run you dispatched and watched above; its id is also the last path
# segment of its URL, .../actions/runs/<id>
gh run list --repo testng-team/testng --workflow "Publish to Maven Central" --limit 5 \
  --json databaseId,headSha,conclusion,createdAt

RUN_ID="21236984900"  # Replace with that run's id
```

Two guards before the tag is written. The first is the run, the second the commit, and neither is
implied by the other:

```bash
gh run view "${RUN_ID}" --repo testng-team/testng --json conclusion --jq .conclusion \
  | grep -qx success || { echo "Run ${RUN_ID} did not succeed"; exit 1; }

RELEASE_COMMIT=$(gh run view "${RUN_ID}" --repo testng-team/testng --json headSha --jq .headSha)

git show "${RELEASE_COMMIT}:gradle.properties" | grep -qx "testng.version=${VERSION}" \
  || { echo "Run ${RUN_ID} built $(git show "${RELEASE_COMMIT}:gradle.properties" \
       | grep '^testng.version='), not ${VERSION}"; exit 1; }

git log -1 --oneline "${RELEASE_COMMIT}"
git tag -a "v${VERSION}" "${RELEASE_COMMIT}" -m "TestNG ${VERSION}"
git push origin "v${VERSION}"
```

**Note**: The tag is the version with a `v` in front, `v7.10.0`. The `v` belongs to the tag name and
not to the version: the release is `7.10.0`, which is what `gradle.properties` and Maven Central
carry. Prefixing marks a ref as a version at a glance, keeps release tags apart from every other
ref, and is what the surrounding tooling expects — `npm version`, GoReleaser and Go modules all
assume it, and the Semantic Versioning FAQ names it as the usual way to write a version tag.

Releases up to and including `7.12.0` are tagged without the prefix, so the first `v` tag compares
against a bare one. Only that one link is mixed; the rest follow.

### 2. Promote the Changelog

`CHANGELOG.md` follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), so releasing turns
the section contributors have been filling in into a dated one, and opens a fresh one for the
version that comes next.

All of it happens on `master`, in a working copy, in one commit. The example below releases
`7.10.0` and opens `7.11.0`.

```bash
git checkout master
git pull origin master

# Still holding RELEASE_COMMIT from step 1. Anything the changelog gained since that
# commit is not in the release.
git diff "${RELEASE_COMMIT}" HEAD -- CHANGELOG.md
```

An empty diff means the whole `## [Unreleased]` section is what shipped, and the steps below apply
to it as written. A non-empty one means a pull request merged while Central was syncing: its entries
are under `## [Unreleased]` but its code is not in the artifacts. Dating the section wholesale would
file that work under a version that never carried it, and remove it from the version that will —
wrong in both directions, and invisible afterwards. Leave those entries under the new
`## [Unreleased]` you open in step 2.1, and date only the rest.

#### 2.1 Date the section being released

At the top of `CHANGELOG.md`, turn this:

```markdown
## [Unreleased]

Next release: 7.10.0.

### Added
```

into this, using the date the artifacts reached Maven Central:

```markdown
## [Unreleased]

Next release: 7.11.0.

## [7.10.0] - 2024-04-07

### Added
```

The `### Added`, `### Changed` and other subsections stay where they are: they describe the release
you just dated. The new `## [Unreleased]` starts empty, with only its `Next release:` line.

A release that had to be pulled keeps its section and gains a `[YANKED]` marker in the heading,
with the reason on the line underneath:

```markdown
## [7.10.0] - 2024-04-07 [YANKED]

Bad release: wrong internal version.
```

#### 2.2 Update the link definitions

At the foot of `CHANGELOG.md`, point `[Unreleased]` at the tag you just pushed and add a line for
the release itself. The base is the release this one follows in the commit graph, which for a
release made from `master` is the one before it there:

```markdown
[Unreleased]: https://github.com/testng-team/testng/compare/v7.10.0...HEAD
[7.10.0]: https://github.com/testng-team/testng/compare/7.9.0...v7.10.0
```

It is not the section above it in the file. Sections are ordered by release date, while a comparison
link describes a Git delta, and the two orders part company for the maintenance releases already in
the file: 7.5.1 was cut from a branch and shipped after 7.7.1, so its section sits between 7.8.0 and
7.7.1 while its changes belong to the 7.5 line. That is why `[7.8.0]` compares against 7.7.1 rather
than the section above it — taking 7.5.1 there would answer with 150 commits of divergence instead
of the 25 that release delivered. Since releases now come from `master` only, a new entry cannot
land in that position.

#### 2.3 Bump the version

In `gradle.properties`, set `testng.version` to the version you named in `Next release:`:

```properties
testng.version=7.11.0
```

#### 2.4 Commit and push

```bash
git add CHANGELOG.md gradle.properties
git commit -m "Bumping version for next release"
git push origin master
```

### 3. Create GitHub Release

1. Go to https://github.com/testng-team/testng/releases
2. Click **"Draft a new release"**
3. Select the tag you just created (`v7.10.0`)
4. Set release title: `TestNG 7.10.0`
5. Add release notes, taken from the section you just dated in `CHANGELOG.md`. That file is the
   source; the release page repeats it rather than restating it in other words.
6. Click **"Publish release"**

### 4. Send Release Announcement

Send an email to the TestNG users mailing list:

**To**: testng-users@googlegroups.com

**Subject**: `[ANNOUNCE] TestNG 7.10.0 Released`

**Template**:

```
Hi everyone,

I'm pleased to announce the release of TestNG 7.10.0!

This release includes:
- [Major feature 1]
- [Major feature 2]
- [Bug fix 1]
- [Bug fix 2]

Full release notes: https://github.com/testng-team/testng/releases/tag/v7.10.0

Maven coordinates:
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.10.0</version>
</dependency>

Gradle:
testImplementation("org.testng:testng:7.10.0")

The artifacts are available on Maven Central:
https://central.sonatype.com/artifact/org.testng/testng/7.10.0

Thanks to all contributors who made this release possible!

[Your Name]
```

### 5. Link the Announcement from README.md

The `### Release Notes` list in `README.md` points at one mailing list thread per release, because a
single release is hard to find in the archive otherwise. Add the thread you just sent, at the top:

```markdown
* [7.10.0](https://groups.google.com/g/testng-users/c/<thread-id>)
```

```bash
git add README.md
git commit -m "Add 7.10.0 release notes link"
git push origin master
```

### 6. Update Documentation

If there are documentation changes:

1. Update the TestNG website (if applicable)
2. Update any version-specific documentation
3. Update migration guides if needed

---

## Troubleshooting

### Workflow Fails: "Nmcp requires Java 17+"

**Problem**: The nmcp plugin requires Java 17 or higher.

**Solution**: The workflow already uses JDK 25. If you see this error, check:

- The workflow file uses `java-version: 25`
- The setup-java step completed successfully

### Workflow Fails: "Authentication failed"

**Problem**: Invalid Central Portal credentials.

**Solution**:

1. Verify GitHub secrets are set correctly:

   - `NEXUS_USERNAME`
   - `NEXUS_PASSWORD`

2. Test credentials at https://central.sonatype.com/
3. Regenerate token if needed:

   - Go to https://central.sonatype.com/
   - Profile → Generate User Token
   - Update GitHub secrets

### Workflow Fails: "Validation failed"

**Problem**: Artifacts don't meet Maven Central requirements.

**Solution**: Check the workflow logs for specific validation errors. Common issues:

- Missing POM metadata (name, description, url, licenses, developers, scm)
- Missing or invalid signatures
- Group ID doesn't match verified namespace (`org.testng`)
- Artifact files are corrupted

### Workflow Fails: "Publishing timeout"

**Problem**: Central Portal validation is taking too long.

**Solution**: This is rare. If it happens:

1. Check Central Portal status: https://status.central.sonatype.com/
2. Retry the workflow
3. If persistent, contact Sonatype support

### Artifacts Not Appearing on Maven Central

**Problem**: Workflow succeeded but artifacts aren't on Maven Central.

**Solution**:

1. **Wait longer**: Sync can take up to 2 hours (usually ~30 minutes)
2. **Check Central Portal**: https://central.sonatype.com/artifact/org.testng/testng
3. **Check deployment status**: https://central.sonatype.com/publishing
4. **For USER_MANAGED**: Did you manually publish from the portal?

### Wrong Version Published

**Problem**: Published the wrong version number.

**Solution**:

- **You cannot delete or replace** a version on Maven Central
- **Publish a new version** with the correct number
- **Document the issue** in release notes
- **Prevention**: Always verify the version in `gradle.properties` before releasing

### Need to Rollback a Release

**Problem**: Released version has critical bugs.

**Solution**:

- **You cannot remove** versions from Maven Central
- **Publish a new version** with fixes
- **Mark the bad version** as deprecated in documentation
- **Send announcement** to mailing list about the issue

---

## Comparison with Old Process

### Old Process (OSSRH - Deprecated)

```
1. Run "publish-maven-central" workflow → Creates RC build
2. Get staging repository URL from logs
3. Share staging URL for testing
4. Repeat steps 1-3 for additional RC builds
5. Run "release-maven-central" workflow with RC number and repo ID
6. Manually close and release in Nexus UI
7. Wait for sync to Maven Central
8. Create Git tag
9. Send announcement
```

### New Process (Central Portal - Current)

```
1. Run "Publish to Maven Central" workflow with AUTOMATIC
2. Wait ~5-10 minutes for workflow to complete
3. Wait ~30 minutes for Maven Central sync
4. Create Git tag
5. Send announcement
```

**Benefits**:

- ✅ **Simpler**: 5 steps instead of 9
- ✅ **Faster**: No manual staging/release steps
- ✅ **Safer**: Validation happens during upload
- ✅ **No RC builds needed**: Can test from snapshots instead
- ✅ **Better UI**: Modern Central Portal interface

---

## Visual Workflow Diagrams

### Complete Release Flow (AUTOMATIC)

```
Developer                    GitHub Actions              Central Portal           Maven Central
    │                              │                           │                        │
    │ 1. Trigger workflow          │                           │                        │
    │ (publishing_type=AUTOMATIC)  │                           │                        │
    ├─────────────────────────────>│                           │                        │
    │                              │                           │                        │
    │                              │ 2. Build & Sign           │                        │
    │                              │    artifacts              │                        │
    │                              │                           │                        │
    │                              │ 3. Upload artifacts       │                        │
    │                              ├──────────────────────────>│                        │
    │                              │                           │                        │
    │                              │                           │ 4. Validate            │
    │                              │                           │    - POM metadata      │
    │                              │                           │    - Signatures        │
    │                              │                           │    - Checksums         │
    │                              │                           │                        │
    │                              │ 5. Validation OK          │                        │
    │                              │<──────────────────────────┤                        │
    │                              │                           │                        │
    │                              │                           │ 6. Auto-publish        │
    │                              │                           ├───────────────────────>│
    │                              │                           │                        │
    │ 7. Workflow complete ✅      │                           │                        │
    │<─────────────────────────────┤                           │                        │
    │                              │                           │                        │
    │                              │                           │    7. Sync (~30 min)   │
    │                              │                           │                        │
    │ 8. Verify release            │                           │                        │
    ├──────────────────────────────────────────────────────────────────────────────────>│
    │                              │                           │                        │
    │ 9. Create Git tag            │                           │                        │
    │ 10. Send announcement        │                           │                        │
```

### Complete Release Flow (USER_MANAGED)

```
Developer                    GitHub Actions              Central Portal           Maven Central
    │                              │                           │                        │
    │ 1. Trigger workflow          │                           │                        │
    │ (publishing_type=            │                           │                        │
    │  USER_MANAGED)               │                           │                        │
    ├─────────────────────────────>│                           │                        │
    │                              │                           │                        │
    │                              │ 2. Build & Sign           │                        │
    │                              │    artifacts              │                        │
    │                              │                           │                        │
    │                              │ 3. Upload artifacts       │                        │
    │                              ├──────────────────────────>│                        │
    │                              │                           │                        │
    │                              │                           │ 4. Validate            │
    │                              │                           │    - POM metadata      │
    │                              │                           │    - Signatures        │
    │                              │                           │    - Checksums         │
    │                              │                           │                        │
    │                              │ 5. Validation OK          │                        │
    │                              │    (status: PENDING)      │                        │
    │                              │<──────────────────────────┤                        │
    │                              │                           │                        │
    │ 6. Workflow complete ✅      │                           │                        │
    │    "Go to portal to publish" │                           │                        │
    │<─────────────────────────────┤                           │                        │
    │                              │                           │                        │
    │ 7. Go to Central Portal      │                           │                        │
    │    Review deployment         │                           │                        │
    ├─────────────────────────────────────────────────────────>│                        │
    │                              │                           │                        │
    │ 8. Click "Publish"           │                           │                        │
    ├─────────────────────────────────────────────────────────>│                        │
    │                              │                           │                        │
    │                              │                           │ 9. Publish to Central  │
    │                              │                           ├───────────────────────>│
    │                              │                           │                        │
    │                              │                           │    10. Sync (~30 min)  │
    │                              │                           │                        │
    │ 11. Verify release           │                           │                        │
    ├──────────────────────────────────────────────────────────────────────────────────>│
    │                              │                           │                        │
    │ 12. Create Git tag           │                           │                        │
    │ 13. Send announcement        │                           │                        │
```

---

## Quick Reference

### Common Commands

**Note**: All publishing commands require environment variables to be set. See [Required Secrets](#required-secrets-already-configured) for details.

```bash
# Publish release (automatic)
./gradlew publishAggregationToCentralPortal \
  -Prelease=true \
  -PcentralPortal.publishingType=AUTOMATIC

# Publish release (manual)
./gradlew publishAggregationToCentralPortal \
  -Prelease=true \
  -PcentralPortal.publishingType=USER_MANAGED

# Publish snapshot
./gradlew publishAllPublicationsToCentralSnapshotsRepository \
  -Prelease=false

# Build without publishing
./gradlew build -Prelease=true
```

**Required environment variables for publishing**:
```bash
export CENTRAL_PORTAL_USERNAME="your-token-username"
export CENTRAL_PORTAL_PASSWORD="your-token-password"
export SIGNING_PGP_PRIVATE_KEY="your-pgp-private-key"
export SIGNING_PGP_PASSPHRASE="your-pgp-passphrase"
```

### Important URLs

| Purpose | URL |
|---------|-----|
| **Central Portal** | https://central.sonatype.com/ |
| **Publishing Dashboard** | https://central.sonatype.com/publishing |
| **TestNG on Central** | https://central.sonatype.com/artifact/org.testng/testng |
| **Maven Central Repository** | https://repo1.maven.org/maven2/org/testng/testng/ |
| **GitHub Actions** | https://github.com/testng-team/testng/actions |
| **GitHub Releases** | https://github.com/testng-team/testng/releases |
| **Mailing List** | https://groups.google.com/g/testng-users |

### Workflow Files

| Workflow | File | Purpose |
|----------|------|---------|
| **Publish to Maven Central** | `.github/workflows/publish-maven-central.yml` | Manual release publishing |
| **Publish Snapshot** | `.github/workflows/publish-snapshot.yml` | Automatic snapshot publishing on push to master |
| **Test** | `.github/workflows/test.yml` | Run tests on PRs and pushes |

---

## Additional Resources

- [Build System Documentation](BUILD_SYSTEM.md) - Complete build system architecture
- [CI Test Workflow](CI_TEST_WORKFLOW.md) - How the CI test matrix works
- [Java Versions Quick Reference](JAVA_VERSIONS_QUICK_REFERENCE.md) - Java version requirements
- [Central Portal Documentation](https://central.sonatype.org/publish/publish-portal-gradle/) - Official Maven Central Portal guide
- [Nmcp Plugin Documentation](https://gradleup.github.io/nmcp/) - Gradle plugin for Central Portal

---

## Questions?

If you have questions about the release process:

1. **Check this documentation** and related docs in the `docs/` directory
2. **Check GitHub Issues**: https://github.com/testng-team/testng/issues
3. **Ask on the mailing list**: testng-dev@googlegroups.com
4. **Contact maintainers**: See CONTRIBUTORS.md

---

**Last Updated**: 2024 (Updated for Maven Central Portal migration)


