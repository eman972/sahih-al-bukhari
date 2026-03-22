# Java API Reference — sahih-al-bukhari

The Java package provides the exact same API as the npm and PyPI packages,
using Java idioms (Optional, Stream, lambda predicates).

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

## Quick Start

```java
import com.bukhari.Bukhari;
import com.bukhari.Hadith;
import com.bukhari.Chapter;
import com.bukhari.Metadata;

// Singleton backed by bundled data (recommended)
Bukhari bukhari = Bukhari.getInstance();

// Or load a custom file
Bukhari bukhari = new Bukhari(Paths.get("/path/to/bukhari.json.gz"));
```

---

## `Bukhari` class

### Core API

| Method | Returns | Description |
|--------|---------|-------------|
| `Bukhari.getInstance()` | `Bukhari` | Thread-safe singleton |
| `new Bukhari()` | `Bukhari` | Load from bundled JAR data |
| `new Bukhari(Path)` | `Bukhari` | Load from a custom file |
| `length()` | `int` | Total hadith count (7277) |
| `get(int id)` | `Optional<Hadith>` | Get by 1-based ID |
| `getByIndex(int i)` | `Hadith` | Get by 0-based index (like `bukhari[0]` in JS) |
| `getByChapter(int chapterId)` | `List<Hadith>` | All hadiths in a chapter |
| `search(String query)` | `List<Hadith>` | Case-insensitive full-text search |
| `search(String query, int limit)` | `List<Hadith>` | Search with max results |
| `getRandom()` | `Hadith` | Random hadith |
| `getHadiths()` | `List<Hadith>` | All hadiths (unmodifiable) |
| `getChapters()` | `List<Chapter>` | All chapters (unmodifiable) |
| `getMetadata()` | `Metadata` | Collection metadata |

### Array-style helpers

These mirror `Array.prototype` methods in JS and the Python `Bukhari` class:

| Method | Returns | JS / Python equivalent |
|--------|---------|----------------------|
| `find(Predicate<Hadith>)` | `Optional<Hadith>` | `bukhari.find(h => ...)` |
| `filter(Predicate<Hadith>)` | `List<Hadith>` | `bukhari.filter(h => ...)` |
| `map(Function<Hadith, T>)` | `List<T>` | `bukhari.map(h => ...)` |
| `forEach(BiConsumer<Hadith, Integer>)` | `void` | `bukhari.forEach((h, i) => ...)` |
| `slice(int start, int end)` | `List<Hadith>` | `bukhari.slice(0, 10)` |
| `slice(int start)` | `List<Hadith>` | `bukhari.slice(100)` |
| `stream()` | `Stream<Hadith>` | Full Java Stream API |
| `iterator()` | `Iterator<Hadith>` | `for (Hadith h : bukhari)` |

---

## Data Models

### `Hadith`

```java
hadith.getId()         // int   — 1-based global ID
hadith.getChapterId()  // int   — chapter this hadith belongs to
hadith.getArabic()     // String — full Arabic text
hadith.getText()       // String — English translation (shortcut)
hadith.getNarrator()   // String — English narrator (shortcut)
hadith.getEnglish()    // HadithEnglish — { narrator, text }
```

### `Chapter`

```java
chapter.getId()        // int
chapter.getEnglish()   // String — English title
chapter.getArabic()    // String — Arabic title
```

### `Metadata`

```java
metadata.getId()       // int
metadata.getLength()   // int — 7277
metadata.getEnglish()  // Map<String,String> — keys: title, author, introduction
metadata.getArabic()   // Map<String,String> — keys: title, author, introduction
```

---

## Examples

```java
Bukhari bukhari = Bukhari.getInstance();

// --- Core usage ---
bukhari.get(1).ifPresent(h -> System.out.println(h.getText()));
bukhari.getByChapter(3).forEach(h -> System.out.println(h.getId()));

List<Hadith> results = bukhari.search("prayer");
List<Hadith> first10 = bukhari.search("fasting", 10);

Hadith random = bukhari.getRandom();
System.out.println(random.getNarrator() + ": " + random.getText());

// --- Array-style ---
Optional<Hadith> found = bukhari.find(h -> h.getId() == 42);

List<Hadith> chapter1 = bukhari.filter(h -> h.getChapterId() == 1);

List<String> texts = bukhari.map(Hadith::getText);

bukhari.forEach((h, i) -> System.out.println(i + ": " + h.getId()));

List<Hadith> first50 = bukhari.slice(0, 50);

// --- Java Streams ---
long count = bukhari.stream()
                    .filter(h -> h.getText().contains("Ramadan"))
                    .count();

Map<Integer, List<Hadith>> byChapter = bukhari.stream()
    .collect(Collectors.groupingBy(Hadith::getChapterId));

// --- For-each loop ---
for (Hadith h : bukhari) {
    System.out.println(h.getId() + ": " + h.getNarrator());
}

// --- Metadata ---
Metadata meta = bukhari.getMetadata();
System.out.println(meta.getEnglish().get("title"));   // Sahih al-Bukhari
System.out.println(meta.getEnglish().get("author"));  // Muhammad al-Bukhari
System.out.println(meta.getLength());                  // 7277

// --- Custom data file ---
Bukhari custom = new Bukhari(Paths.get("/my/custom/bukhari.json.gz"));
```

---

## CLI Usage

Build the fat-JAR and run as a CLI tool:

```bash
# Maven
mvn package -P fat-jar
java -jar target/sahih-al-bukhari-3.1.5-fat.jar --random

# Gradle
./gradlew shadowJar
java -jar build/libs/sahih-al-bukhari-3.1.5-all.jar --random
```

### CLI Commands

```
bukhari <id>                  Get hadith by ID
bukhari <id> -a               Arabic text only
bukhari <id> -b               Arabic + English
bukhari <id1> <id2> ...       Multiple hadiths
bukhari --search <query>      Full-text search (shows 10 results)
bukhari --search <query> --all  Show all results
bukhari --chapter <id>        All hadiths in a chapter
bukhari --random              Random hadith
bukhari --random -b           Random hadith, Arabic + English
bukhari --count               Total hadith count
bukhari --version             Print version
bukhari --help                Show help
```
