# sahih-al-bukhari — Java

Complete Sahih al-Bukhari for Java — 7,277 hadiths with full Arabic text and
English translations. Offline-first, minimal dependencies.

Provides the **same API** as the [npm](https://www.npmjs.com/package/sahih-al-bukhari)
and [PyPI](https://pypi.org/project/sahih-al-bukhari/) packages.

---

## Installation

### Maven
```xml
<dependency>
  <groupId>io.github.senodroom</groupId>
  <artifactId>sahih-al-bukhari</artifactId>
  <version>3.1.5</version>
</dependency>
```

### Gradle (Kotlin DSL)
```kotlin
implementation("io.github.senodroom:sahih-al-bukhari:3.1.5")
```

### Gradle (Groovy DSL)
```groovy
implementation 'io.github.senodroom:sahih-al-bukhari:3.1.5'
```

---


## First-time Gradle Setup (Windows)

The `gradle-wrapper.jar` is not included in the zip (it is normally committed
to Git but cannot be distributed in a zip). Download it once:

```powershell
# From the java/ directory:
powershell -ExecutionPolicy Bypass -File setup-gradle-wrapper.ps1
```

Or manually with curl:
```bash
curl -L -o gradle/wrapper/gradle-wrapper.jar ^
  https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar
```

After that, `./gradlew` works normally — you never need to do this again.

## Quick Start

```java
import com.bukhari.Bukhari;
import com.bukhari.Hadith;

Bukhari bukhari = Bukhari.getInstance();

// Get by ID
bukhari.get(1).ifPresent(h -> System.out.println(h.getText()));

// Search
List<Hadith> results = bukhari.search("prayer");

// Random
Hadith random = bukhari.getRandom();
System.out.println(random.getNarrator() + ": " + random.getText());

// Chapter
List<Hadith> chapter1 = bukhari.getByChapter(1);

// Iterate
for (Hadith h : bukhari) {
    System.out.println(h.getId());
}

// Java Streams
bukhari.stream()
       .filter(h -> h.getText().contains("Ramadan"))
       .forEach(h -> System.out.println(h.getId()));
```

---

## API

See [docs/api.md](docs/api.md) for the full API reference.

| Method | Description |
|--------|-------------|
| `get(int id)` | Get hadith by 1-based ID → `Optional<Hadith>` |
| `getByIndex(int i)` | Get by 0-based index (like `bukhari[0]`) |
| `getByChapter(int chapterId)` | All hadiths in a chapter |
| `search(String query)` | Full-text search (English text + narrator) |
| `search(String query, int limit)` | Search with result limit |
| `getRandom()` | Random hadith |
| `find(Predicate<Hadith>)` | First match |
| `filter(Predicate<Hadith>)` | All matches |
| `map(Function<Hadith, T>)` | Transform all hadiths |
| `forEach(BiConsumer<Hadith, Integer>)` | Iterate with index |
| `slice(int start, int end)` | Sublist |
| `stream()` | Java Stream API |
| `length()` | Total count (7277) |
| `getChapters()` | All chapters |
| `getMetadata()` | Collection metadata |

---

## CLI

```bash
# Build fat-JAR
mvn package -P fat-jar           # Maven
./gradlew shadowJar              # Gradle

# Use it
java -jar target/sahih-al-bukhari-3.1.5-fat.jar 1
java -jar target/sahih-al-bukhari-3.1.5-fat.jar --random -b
java -jar target/sahih-al-bukhari-3.1.5-fat.jar --search "prayer" --all
java -jar target/sahih-al-bukhari-3.1.5-fat.jar --chapter 3
```

---

## Building from Source

```bash
# Step 1 — copy data from monorepo root
node scripts/copy-data-java.mjs

# Step 2 — build and test
mvn clean verify          # Maven
./gradlew clean test      # Gradle
```

---

## License

AGPL-3.0 — see [LICENSE](../LICENSE)
