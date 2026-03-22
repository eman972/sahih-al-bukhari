# Build & Publish Guide — sahih-al-bukhari Java

This guide covers every step from zero to a live release on Maven Central,
for both Maven and Gradle.

---

## Prerequisites

| Tool    | Version | Install |
|---------|---------|---------|
| Java JDK | 11+    | https://adoptium.net |
| Maven   | 3.9+    | https://maven.apache.org/download.cgi |
| Gradle  | 8.7+ (wrapper included) | run `./gradlew` — auto-downloads |
| GnuPG   | 2.x     | `brew install gnupg` / `apt install gnupg` |

---

## Building Locally

No data-copy step is needed. Both `pom.xml` and `build.gradle` are configured
to read `bukhari.json.gz` directly from the monorepo's `data/` folder (one
level above `java/`) at build time.

### Run tests

```bash
cd java

mvn clean verify        # Maven
./gradlew clean test    # Gradle
```

All 35 tests should pass with zero warnings.

### Build the fat-JAR (CLI)

```bash
# Maven
mvn package -P fat-jar
java -jar target/sahih-al-bukhari-3.1.5-fat.jar --random

# Gradle
./gradlew shadowJar
java -jar build/libs/sahih-al-bukhari-3.1.5-all.jar --random
```

### Install to local Maven repo (test in other projects)

```bash
mvn install -DskipTests         # Maven
./gradlew publishToMavenLocal   # Gradle
```

---

## One-time Setup for Publishing

### 1. Register on Sonatype Central Portal

1. Go to https://central.sonatype.com and click **Sign Up**
2. After signing in, go to **Namespaces** → **Add Namespace**
3. Enter `io.github.senodroom`
4. Sonatype shows a verification code — create a **public GitHub repo**
   named exactly that code (e.g. `OSSRH-12345`) to prove ownership
5. Click **Verify Namespace** — takes a few minutes
6. Go to **Access** → **Generate User Token**
   - Save the **Username** and **Password** as `OSSRH_USERNAME` / `OSSRH_PASSWORD`

### 2. Generate a GPG Key

```bash
# Generate (use your real name and email)
gpg --gen-key

# List keys to get your KEY_ID
gpg --list-secret-keys --keyid-format LONG
# Example output:
#   sec   rsa4096/ABCD1234EFGH5678 2024-01-01 ...

# Upload public key to a keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234EFGH5678

# Export private key (paste into GitHub Secrets)
gpg --armor --export-secret-keys ABCD1234EFGH5678
```

### 3. Add GitHub Secrets

Repo → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**:

| Secret name      | Value |
|------------------|-------|
| `OSSRH_USERNAME` | Username token from Sonatype Central |
| `OSSRH_PASSWORD` | Password token from Sonatype Central |
| `GPG_PRIVATE_KEY`| Full `-----BEGIN PGP PRIVATE KEY BLOCK-----` block |
| `GPG_PASSPHRASE` | Passphrase chosen when generating the GPG key |

---

## Publishing to Maven Central

### Option A — GitHub Actions (recommended)

1. Make sure all four GitHub Secrets are set
2. Go to GitHub → **Releases** → **Draft a new release**
3. Create a tag like `v3.1.5`, add release notes, click **Publish release**
4. The **Publish Java to Maven Central** workflow fires automatically
5. Check the **Actions** tab — takes ~5–10 minutes
6. Confirm at https://central.sonatype.com → **Deployments**

### Option B — Manual from your machine

**Maven:**
```bash
cd java
mvn --batch-mode deploy -P release \
  -Dossrh.username=YOUR_TOKEN_USERNAME \
  -Dossrh.password=YOUR_TOKEN_PASSWORD \
  -Dgpg.passphrase=YOUR_GPG_PASSPHRASE
```

**Gradle:**
```bash
cd java
./gradlew publish \
  -PossrhUsername=YOUR_TOKEN_USERNAME \
  -PossrhPassword=YOUR_TOKEN_PASSWORD \
  -PsigningKey="$(gpg --armor --export-secret-keys YOUR_KEY_ID)" \
  -PsigningPassword=YOUR_GPG_PASSPHRASE
```

---

## Bumping the Version

Update the version string in **three places** and keep them in sync:

| File | Where |
|------|-------|
| `java/pom.xml` | `<version>3.1.5</version>` |
| `java/build.gradle` | `version = '3.1.5'` |
| `java/src/main/java/com/bukhari/BukhariCli.java` | `static final String VERSION = "3.1.5";` |
| `package.json` (monorepo root) | `"version": "3.1.5"` |
| `pyproject.toml` (monorepo root) | `version = "3.1.5"` |

---

## Verifying the Published Artifact

After publishing, wait ~15–30 min for Maven Central to sync, then verify:

```
https://central.sonatype.com/artifact/io.github.senodroom/sahih-al-bukhari
https://mvnrepository.com/artifact/io.github.senodroom/sahih-al-bukhari
```

---

## Troubleshooting

**`Bundled data not found in JAR`**
You are running tests from an IDE that bypasses the Maven/Gradle resource pipeline.
Either run `mvn test` / `./gradlew test` from the terminal, or run the copy script:
```bash
node java/scripts/copy-data-java.mjs   # (from the monorepo root)
```

**`gpg: signing failed: No secret key`**
GPG key not loaded. Run: `gpg --list-secret-keys` and `eval $(gpg-agent --daemon)`

**`401 Unauthorized` when publishing**
Wrong Sonatype credentials — use the **token** values from Central Portal, not your login.

**Namespace verification fails**
The GitHub repo you created for verification must be **public** and named exactly
as Sonatype specified.
