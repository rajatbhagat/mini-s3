# Epic 1 & 2 Implementation Guide: Understanding the Foundation

> **Learning Reference**: Comprehensive explanation of the project foundation and entity layer implementation for mini-s3

**Last Updated**: December 26, 2024
**Epics Covered**: Epic 1 (Project Foundation & Setup), Epic 2 Story 1 (Entity & Repository Layer)

---

## 🎯 Overview: What We Built

We completed the **foundation** of your mini-s3 project:
1. **Epic 1**: Set up the development environment (database, dependencies, configuration)
2. **Epic 2 Story 1**: Created the data model (how data is structured and stored)

Think of it like building a house:
- Epic 1 = Laying the foundation and installing utilities (water, electricity)
- Epic 2 Story 1 = Building the frame and basic structure

---

## 📦 Part 1: Dependencies (build.gradle)

### What I Added:

```gradle
dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Database
    runtimeOnly 'org.postgresql:postgresql'

    // Utilities
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'commons-codec:commons-codec:1.16.0'

    // Testing
    testImplementation 'com.h2database:h2'
}
```

### Why Each Dependency:

#### 1. Spring Data JPA
- **What**: Framework for database access using Java Persistence API
- **Why**: Handles all the database operations (saving, retrieving, updating, deleting) without writing SQL
- **Example**: Instead of writing `INSERT INTO bucket VALUES (...)`, you just call `bucketRepository.save(bucket)`

#### 2. Spring Boot Validation
- **What**: Validates data before it hits the database
- **Why**: Ensures data integrity (e.g., bucket names are between 3-63 characters)
- **Example**: `@NotNull`, `@Size(min=3, max=63)` annotations

#### 3. PostgreSQL Driver
- **What**: The actual connector that talks to PostgreSQL database
- **Why**: Spring needs this to communicate with your local PostgreSQL server
- **Analogy**: Like a USB driver that lets your computer talk to a printer

#### 4. Lombok
- **What**: Automatically generates boilerplate code (getters, setters, constructors)
- **Why**: Reduces code by ~70% - you don't write repetitive code
- **Example**: `@Data` annotation generates getters, setters, toString, equals, hashCode automatically

**Lombok Example:**

Without Lombok:
```java
public class Bucket {
    private Long id;
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    // ... equals, hashCode, toString methods (50+ lines)
}
```

With Lombok:
```java
@Data
public class Bucket {
    private Long id;
    private String name;
}
// That's it! Lombok generates everything else
```

#### 5. Commons Codec
- **What**: Library for encoding/decoding, including MD5 hashing
- **Why**: S3 uses MD5 hashes (called ETags) to verify file integrity
- **Example**: Calculate checksum to ensure file wasn't corrupted during upload

#### 6. H2 Database
- **What**: In-memory database for testing
- **Why**: Tests run fast without needing PostgreSQL running
- **Analogy**: Like a scratch pad vs. a real notebook

---

## ⚙️ Part 2: Configuration (application.yml)

### Database Configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/minis3db
    username: postgres
    password: postgres
```

**What This Does:**
- Tells Spring where your PostgreSQL database is located
- Like giving someone an address: "localhost" = your computer, "5432" = PostgreSQL's default port

### HikariCP Connection Pool:

```yaml
hikari:
  maximum-pool-size: 10
  minimum-idle: 5
  connection-timeout: 20000
  idle-timeout: 300000
  max-lifetime: 1200000
```

**What This Does:**
- Manages database connections efficiently
- **Why**: Opening a database connection is slow. Connection pooling keeps 5-10 connections ready to use
- **Analogy**: Like having multiple phone lines instead of one - multiple requests can happen simultaneously

**How Connection Pooling Works:**

```
Without Pool:
Request 1 → Open Connection → Query → Close Connection (SLOW)
Request 2 → Open Connection → Query → Close Connection (SLOW)
Request 3 → Open Connection → Query → Close Connection (SLOW)

With Pool:
Startup → Open 5 connections → Keep them open
Request 1 → Borrow connection from pool → Query → Return to pool (FAST)
Request 2 → Borrow connection from pool → Query → Return to pool (FAST)
Request 3 → Borrow connection from pool → Query → Return to pool (FAST)
```

### JPA/Hibernate Configuration:

```yaml
jpa:
  hibernate:
    ddl-auto: update
  show-sql: true
  properties:
    hibernate:
      dialect: org.hibernate.dialect.PostgreSQLDialect
      format_sql: true
```

**What This Does:**
- `ddl-auto: update` - Automatically creates/updates database tables based on your entity classes
- `show-sql: true` - Prints SQL queries to console (helpful for learning/debugging)
- `dialect` - Tells Hibernate to use PostgreSQL-specific SQL features

**How `ddl-auto: update` works:**

1. You write a Java class with `@Entity`
2. Hibernate reads the class
3. Hibernate creates the corresponding table in PostgreSQL
4. Magic! ✨

**Example:**
```java
@Entity
@Table(name = "bucket")
public class Bucket {
    @Id
    private Long id;
    private String name;
}
```

Hibernate generates:
```sql
CREATE TABLE bucket (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255)
);
```

### File Upload Configuration:

```yaml
servlet:
  multipart:
    enabled: true
    max-file-size: 50MB
    max-request-size: 50MB
    file-size-threshold: 2KB
```

**What This Does:**
- Limits file uploads to 50MB
- Prevents users from uploading huge files that crash your server
- `file-size-threshold: 2KB` - Files smaller than 2KB are kept in memory, larger ones are written to disk temporarily

---

## 🗃️ Part 3: JPA Auditing

### What I Added:

```java
@SpringBootApplication
@EnableJpaAuditing  // ← This line
public class Minis3Application {
    public static void main(String[] args) {
        SpringApplication.run(Minis3Application.class, args);
    }
}
```

**What This Does:**
- Automatically tracks when records are created/updated
- You don't need to manually set `createdAt` or `updatedAt` fields
- Spring does it for you automatically

**How it works:**
```java
@Entity
public class Bucket {
    @CreatedDate
    private LocalDateTime createdAt;  // Spring sets this automatically when saving

    @LastModifiedDate
    private LocalDateTime updatedAt;  // Spring updates this on every save
}
```

**Behind the scenes:**
```java
// You write:
Bucket bucket = new Bucket();
bucket.setName("my-bucket");
bucketRepository.save(bucket);

// Spring automatically does:
bucket.setCreatedAt(LocalDateTime.now());  // Sets current time
bucket.setUpdatedAt(LocalDateTime.now());  // Sets current time
// Then saves to database
```

---

## 🏗️ Part 4: Entity Classes (The Data Model)

This is the heart of your application. Let me explain each entity and how they relate to each other.

### Entity 1: Bucket

```java
@Entity
@Table(name = "bucket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Bucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @OneToMany(mappedBy = "bucket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StorageObject> objects = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Understanding the Annotations:

**Class-level Annotations:**

- `@Entity` - "This is a database table"
- `@Table(name = "bucket")` - "Call the table 'bucket'"
- `@Data` - Lombok: Generate getters, setters, toString, equals, hashCode
- `@NoArgsConstructor` - Lombok: Generate constructor with no arguments
- `@AllArgsConstructor` - Lombok: Generate constructor with all fields
- `@EntityListeners(AuditingEntityListener.class)` - Enable JPA auditing for this entity

**Field-level Annotations:**

- `@Id` - "This field is the primary key"
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` - "Auto-increment the ID (1, 2, 3...)"
- `@Column(nullable = false)` - "This field cannot be NULL in database"
- `@Column(unique = true)` - "Bucket names must be unique"
- `@Column(length = 255)` - "VARCHAR(255) in database"
- `@OneToMany(mappedBy = "bucket")` - "One bucket can have many objects"
- `@CreatedDate` - "Auto-set when entity is first saved"
- `@LastModifiedDate` - "Auto-update on every save"
- `@Column(updatable = false)` - "Cannot be changed after first insert"

#### Real-world Analogy:

- Bucket = A folder on your computer
- It has a unique name
- It can contain multiple files
- You can see when it was created and last modified

---

### Entity 2: StorageObject

```java
@Entity
@Table(name = "storage_object",
       uniqueConstraints = @UniqueConstraint(columnNames = {"bucket_id", "object_key"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StorageObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bucket_id", nullable = false)
    private Bucket bucket;

    @Column(name = "object_key", nullable = false, length = 1024)
    private String objectKey;

    @OneToOne
    @JoinColumn(name = "current_version_id")
    private ObjectVersion currentVersion;

    @OneToMany(mappedBy = "storageObject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ObjectVersion> versions = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Why it's called "StorageObject" not "Object":

- `Object` is a reserved word in Java (the base class of everything)
- Would cause naming conflicts
- `StorageObject` is clearer and more descriptive

#### Understanding the Relationships:

**1. @ManyToOne with Bucket**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "bucket_id", nullable = false)
private Bucket bucket;
```

- Many storage objects can belong to one bucket
- Like: Many files in one folder
- `FetchType.LAZY` - Don't load the bucket automatically (performance optimization)
- `JoinColumn(name = "bucket_id")` - Creates foreign key column named "bucket_id"

**2. @OneToOne with currentVersion**
```java
@OneToOne
@JoinColumn(name = "current_version_id")
private ObjectVersion currentVersion;
```

- Points to the "active" version of the file
- When you download a file, you get the current version
- Think of it as a bookmark pointing to the latest version

**3. @OneToMany with versions**
```java
@OneToMany(mappedBy = "storageObject", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ObjectVersion> versions = new ArrayList<>();
```

- Keeps history of all versions
- Like Google Docs version history
- `cascade = CascadeType.ALL` - When you delete a StorageObject, delete all its versions too
- `orphanRemoval = true` - If a version is removed from the list, delete it from database

#### Unique Constraint:

```java
uniqueConstraints = @UniqueConstraint(columnNames = {"bucket_id", "object_key"})
```

- Same object key can exist in different buckets
- But within a bucket, object keys must be unique

**Examples:**
- ✅ `bucket1/file.txt` and `bucket2/file.txt` - OK (different buckets)
- ❌ `bucket1/file.txt` and `bucket1/file.txt` - NOT OK (same bucket)

---

### Entity 3: ObjectVersion

```java
@Entity
@Table(name = "object_version",
       uniqueConstraints = @UniqueConstraint(columnNames = {"object_id", "version_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ObjectVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "object_id", nullable = false)
    private StorageObject storageObject;

    @Column(nullable = false)
    private Integer versionNumber;

    @Lob
    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] content;

    @Column(length = 255)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(length = 64)
    private String etag;

    @Column(nullable = false)
    private Boolean isLatest = true;

    @OneToMany(mappedBy = "objectVersion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ObjectMetadata> metadata = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### Key Fields Explained:

**1. versionNumber**
```java
@Column(nullable = false)
private Integer versionNumber;
```
- Sequential number: 1, 2, 3, 4...
- When you upload a file: version 1
- Update it: version 2, etc.
- Combined with object_id, must be unique

**2. content (BYTEA = byte array)**
```java
@Lob
@Column(nullable = false, columnDefinition = "BYTEA")
private byte[] content;
```
- The actual file stored as binary data
- `@Lob` = "Large Object" - can store big files
- `columnDefinition = "BYTEA"` - PostgreSQL's binary data type
- Works for any file type: images, PDFs, videos, etc.

**How it works:**
```java
// Upload a file
MultipartFile file = ...; // From HTTP request
byte[] fileContent = file.getBytes(); // Convert to byte array
objectVersion.setContent(fileContent); // Store in database
```

**3. contentType**
```java
@Column(length = 255)
private String contentType;
```
- MIME type of the file
- Tells browsers how to handle the file
- Examples:
  - `image/png` - PNG image
  - `application/pdf` - PDF document
  - `text/plain` - Text file
  - `video/mp4` - MP4 video

**4. size**
```java
@Column(nullable = false)
private Long size;
```
- File size in bytes
- Helps with:
  - Download progress bars
  - Storage quota management
  - Preventing storage abuse

**5. etag**
```java
@Column(length = 64)
private String etag;
```
- MD5 hash of the content
- Verifies file integrity
- AWS S3 does this too
- Like a fingerprint for your file
- Format: 32-character hexadecimal string (e.g., "5d41402abc4b2a76b9719d911017c592")

**How ETag works:**
```java
// Calculate MD5 hash
String etag = DigestUtils.md5Hex(fileContent);

// Client can verify:
// 1. Download file
// 2. Calculate MD5 of downloaded file
// 3. Compare with ETag
// 4. If match → file is intact, else → corrupted
```

**6. isLatest**
```java
@Column(nullable = false)
private Boolean isLatest = true;
```
- `true` for the current version
- `false` for old versions
- Only one version per object should be `isLatest = true`

**Version lifecycle:**
```
Upload file.txt → Version 1 (isLatest = true)
Update file.txt → Version 1 (isLatest = false), Version 2 (isLatest = true)
Update file.txt → Version 2 (isLatest = false), Version 3 (isLatest = true)
```

---

### Entity 4: ObjectMetadata

```java
@Entity
@Table(name = "object_metadata",
       uniqueConstraints = @UniqueConstraint(columnNames = {"version_id", "meta_key"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private ObjectVersion objectVersion;

    @Column(name = "meta_key", nullable = false, length = 255)
    private String key;

    @Column(name = "meta_value", columnDefinition = "TEXT")
    private String value;
}
```

#### What is Metadata?

- Extra information about the file
- Key-value pairs
- User-defined, flexible

**Examples:**
```
author = "John Doe"
department = "Engineering"
status = "approved"
tags = "important,reviewed"
project = "Q4-2024"
reviewed-by = "Jane Smith"
expiry-date = "2025-12-31"
```

#### Why separate table?

**Benefits:**
1. **Flexible schema** - Can add any metadata without changing database structure
2. **Version-specific** - Each version can have different metadata
3. **Easy to query** - Find all objects where `status = "approved"`
4. **No NULL columns** - Only store metadata that exists

**Alternative (worse) approach:**
```java
// BAD: Fixed columns in ObjectVersion
private String author;
private String department;
private String status;
// What if user wants "project"? "reviewer"? "category"?
// Would need to alter table structure!
```

**Our approach (better):**
```java
// GOOD: Flexible key-value pairs
List<ObjectMetadata> metadata = [
    {key: "author", value: "John"},
    {key: "project", value: "Q4-2024"},
    // Can add any metadata without schema changes!
]
```

---

## 🔗 How the Entities Relate (The Big Picture)

### Visual Representation:

```
Bucket (e.g., "my-photos")
    │
    └─> StorageObject (e.g., "vacation.jpg")
            │
            ├─> currentVersion ──────> ObjectVersion (version 3) [isLatest=true]
            │                               │
            │                               └─> ObjectMetadata (year=2024, location=Hawaii)
            │
            └─> versions (complete history)
                    │
                    ├─> ObjectVersion (version 1) [isLatest=false]
                    │       └─> ObjectMetadata (year=2024)
                    │
                    ├─> ObjectVersion (version 2) [isLatest=false]
                    │       └─> ObjectMetadata (year=2024, edited=true)
                    │
                    └─> ObjectVersion (version 3) [isLatest=true] ← current
                            └─> ObjectMetadata (year=2024, location=Hawaii)
```

### Example Scenario:

**Timeline of operations:**

1. **Create bucket "documents"**
   ```sql
   INSERT INTO bucket (name) VALUES ('documents');
   ```

2. **Upload "report.pdf" (version 1)**
   ```sql
   INSERT INTO storage_object (bucket_id, object_key) VALUES (1, 'report.pdf');
   INSERT INTO object_version (object_id, version_number, content, is_latest)
   VALUES (1, 1, <pdf-bytes>, true);
   ```

3. **Update "report.pdf" (version 2 created, version 1 kept)**
   ```sql
   UPDATE object_version SET is_latest = false WHERE id = 1;
   INSERT INTO object_version (object_id, version_number, content, is_latest)
   VALUES (1, 2, <new-pdf-bytes>, true);
   UPDATE storage_object SET current_version_id = 2 WHERE id = 1;
   ```

4. **Add metadata: `status=draft`**
   ```sql
   INSERT INTO object_metadata (version_id, meta_key, meta_value)
   VALUES (2, 'status', 'draft');
   ```

5. **Update again (version 3 created)**
   ```sql
   UPDATE object_version SET is_latest = false WHERE id = 2;
   INSERT INTO object_version (object_id, version_number, content, is_latest)
   VALUES (1, 3, <newer-pdf-bytes>, true);
   UPDATE storage_object SET current_version_id = 3 WHERE id = 1;
   ```

6. **Change metadata to `status=final`**
   ```sql
   INSERT INTO object_metadata (version_id, meta_key, meta_value)
   VALUES (3, 'status', 'final');
   ```

### Database State After All Operations:

**Bucket Table:**
```
+----+-------------+---------------------+---------------------+
| id | name        | created_at          | updated_at          |
+----+-------------+---------------------+---------------------+
| 1  | documents   | 2024-12-26 10:00:00 | 2024-12-26 10:00:00 |
+----+-------------+---------------------+---------------------+
```

**StorageObject Table:**
```
+----+-----------+-------------+--------------------+---------------------+---------------------+
| id | bucket_id | object_key  | current_version_id | created_at          | updated_at          |
+----+-----------+-------------+--------------------+---------------------+---------------------+
| 1  | 1         | report.pdf  | 3                  | 2024-12-26 10:01:00 | 2024-12-26 10:15:00 |
+----+-----------+-------------+--------------------+---------------------+---------------------+
```

**ObjectVersion Table:**
```
+----+-----------+----------------+---------+-----------+------+-----------+---------------------+
| id | object_id | version_number | content | size      | etag | is_latest | created_at          |
+----+-----------+----------------+---------+-----------+------+-----------+---------------------+
| 1  | 1         | 1              | <bytes> | 1024000   | abc1 | false     | 2024-12-26 10:01:00 |
| 2  | 1         | 2              | <bytes> | 1025000   | abc2 | false     | 2024-12-26 10:05:00 |
| 3  | 1         | 3              | <bytes> | 1026000   | abc3 | true      | 2024-12-26 10:15:00 |
+----+-----------+----------------+---------+-----------+------+-----------+---------------------+
```

**ObjectMetadata Table:**
```
+----+------------+----------+------------+
| id | version_id | meta_key | meta_value |
+----+------------+----------+------------+
| 1  | 2          | status   | draft      |
| 2  | 3          | status   | final      |
+----+------------+----------+------------+
```

### Query Examples:

**Get current version of report.pdf:**
```java
StorageObject obj = storageObjectRepository.findByBucketAndObjectKey(bucket, "report.pdf");
ObjectVersion current = obj.getCurrentVersion(); // Version 3
```

**Get all versions of report.pdf:**
```java
List<ObjectVersion> versions = objectVersionRepository
    .findByStorageObjectOrderByVersionNumberDesc(obj);
// Returns: [Version 3, Version 2, Version 1]
```

**Get metadata for current version:**
```java
List<ObjectMetadata> metadata = objectMetadataRepository
    .findByObjectVersion(current);
// Returns: [{key: "status", value: "final"}]
```

---

## 📚 Part 5: Repository Interfaces

Repositories are your data access layer - they talk to the database.

### Spring Data JPA Magic

Spring Data JPA automatically implements repository methods based on naming conventions.

**How it works:**
1. You write an interface (not a class!)
2. You write method names following a pattern
3. Spring generates the implementation at runtime
4. You get working database queries without writing SQL

### BucketRepository Example:

```java
@Repository
public interface BucketRepository extends JpaRepository<Bucket, Long> {

    Optional<Bucket> findByName(String name);

    boolean existsByName(String name);
}
```

#### What JpaRepository gives you for FREE:

```java
// CRUD operations (you don't write these!)
bucket = bucketRepository.save(bucket);           // Create or update
bucket = bucketRepository.findById(1L).get();     // Find by ID
List<Bucket> all = bucketRepository.findAll();    // Get all buckets
bucketRepository.deleteById(1L);                  // Delete by ID
long count = bucketRepository.count();            // Count all buckets
boolean exists = bucketRepository.existsById(1L); // Check if exists
```

#### Custom Methods We Added:

**1. findByName**
```java
Optional<Bucket> findByName(String name);
```

Spring generates:
```sql
SELECT * FROM bucket WHERE name = ?
```

Usage:
```java
Optional<Bucket> bucket = bucketRepository.findByName("my-bucket");
if (bucket.isPresent()) {
    System.out.println("Bucket found: " + bucket.get());
} else {
    System.out.println("Bucket not found");
}
```

**2. existsByName**
```java
boolean existsByName(String name);
```

Spring generates:
```sql
SELECT COUNT(*) > 0 FROM bucket WHERE name = ?
```

Usage:
```java
if (bucketRepository.existsByName("my-bucket")) {
    throw new BucketAlreadyExistsException("Bucket already exists");
}
```

### StorageObjectRepository Example:

```java
@Repository
public interface StorageObjectRepository extends JpaRepository<StorageObject, Long> {

    Optional<StorageObject> findByBucketAndObjectKey(Bucket bucket, String objectKey);

    List<StorageObject> findByBucket(Bucket bucket);

    boolean existsByBucketAndObjectKey(Bucket bucket, String objectKey);
}
```

#### Query Method Naming Conventions:

**Pattern:** `findBy` + `FieldName` + `And` + `AnotherFieldName`

**Examples:**

```java
// Find by single field
findByName(String name)
→ SELECT * FROM bucket WHERE name = ?

// Find by multiple fields (AND)
findByBucketAndObjectKey(Bucket bucket, String objectKey)
→ SELECT * FROM storage_object WHERE bucket_id = ? AND object_key = ?

// Find by multiple fields (OR)
findByNameOrId(String name, Long id)
→ SELECT * FROM bucket WHERE name = ? OR id = ?

// Check existence
existsByName(String name)
→ SELECT COUNT(*) > 0 FROM bucket WHERE name = ?

// Count
countByBucket(Bucket bucket)
→ SELECT COUNT(*) FROM storage_object WHERE bucket_id = ?

// Order by
findByBucketOrderByCreatedAtDesc(Bucket bucket)
→ SELECT * FROM storage_object WHERE bucket_id = ? ORDER BY created_at DESC

// Limit results
findTop10ByBucket(Bucket bucket)
→ SELECT * FROM storage_object WHERE bucket_id = ? LIMIT 10
```

### ObjectVersionRepository Example:

```java
@Repository
public interface ObjectVersionRepository extends JpaRepository<ObjectVersion, Long> {

    List<ObjectVersion> findByStorageObjectOrderByVersionNumberDesc(StorageObject storageObject);

    Optional<ObjectVersion> findByStorageObjectAndVersionNumber(StorageObject storageObject, Integer versionNumber);

    Optional<ObjectVersion> findByStorageObjectAndIsLatest(StorageObject storageObject, Boolean isLatest);

    long countByStorageObject(StorageObject storageObject);
}
```

#### Real-world Usage Examples:

**Get version history:**
```java
List<ObjectVersion> versions = objectVersionRepository
    .findByStorageObjectOrderByVersionNumberDesc(obj);

// Returns versions in reverse order: [v3, v2, v1]
for (ObjectVersion version : versions) {
    System.out.println("Version " + version.getVersionNumber() +
                      " - Created: " + version.getCreatedAt());
}
```

**Get specific version:**
```java
Optional<ObjectVersion> v2 = objectVersionRepository
    .findByStorageObjectAndVersionNumber(obj, 2);
```

**Get current version:**
```java
Optional<ObjectVersion> current = objectVersionRepository
    .findByStorageObjectAndIsLatest(obj, true);
```

**Count versions:**
```java
long versionCount = objectVersionRepository.countByStorageObject(obj);
System.out.println("This object has " + versionCount + " versions");
```

### ObjectMetadataRepository Example:

```java
@Repository
public interface ObjectMetadataRepository extends JpaRepository<ObjectMetadata, Long> {

    List<ObjectMetadata> findByObjectVersion(ObjectVersion objectVersion);

    Optional<ObjectMetadata> findByObjectVersionAndKey(ObjectVersion objectVersion, String key);

    void deleteByObjectVersion(ObjectVersion objectVersion);
}
```

#### Usage Examples:

**Get all metadata for a version:**
```java
List<ObjectMetadata> metadata = objectMetadataRepository.findByObjectVersion(version);
for (ObjectMetadata meta : metadata) {
    System.out.println(meta.getKey() + " = " + meta.getValue());
}
```

**Get specific metadata value:**
```java
Optional<ObjectMetadata> author = objectMetadataRepository
    .findByObjectVersionAndKey(version, "author");

if (author.isPresent()) {
    System.out.println("Author: " + author.get().getValue());
}
```

**Delete all metadata for a version:**
```java
objectMetadataRepository.deleteByObjectVersion(version);
```

---

## 🧪 Part 6: Test Configuration

### Why We Need Separate Test Configuration

**Problem:** Tests need a database, but:
- We don't want to pollute the production database
- Tests should be fast and isolated
- Tests should work without PostgreSQL running

**Solution:** Use H2 in-memory database for tests

### Test Configuration File

**File:** `src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # In-memory database
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop  # Create schema on startup, drop on shutdown
    show-sql: false          # Don't log SQL in tests (cleaner output)
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

  sql:
    init:
      mode: never
```

### How It Works

**1. During Development:**
```
Application runs → Uses application.yml → Connects to PostgreSQL
```

**2. During Testing:**
```
Test runs → Uses application-test.yml → Creates H2 in-memory database
```

**3. Test Lifecycle:**
```
@SpringBootTest
@ActiveProfiles("test")  ← Activates application-test.yml
class MyTest {
    @Test
    void testSomething() {
        // 1. H2 database is created in memory
        // 2. Tables are created based on entities
        // 3. Test runs
        // 4. Database is destroyed
    }
}
```

### Benefits of H2 for Testing

**Speed:**
- In-memory: No disk I/O
- Typical test: 50ms vs 500ms with real database

**Isolation:**
- Each test gets a fresh database
- No leftover data from previous tests
- Tests can run in parallel

**Portability:**
- Works on any machine
- No PostgreSQL installation needed
- CI/CD friendly

**Simplicity:**
- No setup required
- No cleanup needed
- Just run the test!

### Example Test:

```java
@SpringBootTest
@ActiveProfiles("test")
class BucketRepositoryTest {

    @Autowired
    private BucketRepository bucketRepository;

    @Test
    void shouldSaveBucket() {
        // Arrange
        Bucket bucket = new Bucket();
        bucket.setName("test-bucket");

        // Act
        Bucket saved = bucketRepository.save(bucket);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("test-bucket", saved.getName());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void shouldFindBucketByName() {
        // Arrange
        Bucket bucket = new Bucket();
        bucket.setName("my-bucket");
        bucketRepository.save(bucket);

        // Act
        Optional<Bucket> found = bucketRepository.findByName("my-bucket");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("my-bucket", found.get().getName());
    }
}
```

---

## 🎓 Key Learning Concepts

### 1. ORM (Object-Relational Mapping)

**What is ORM?**
- Maps Java objects ↔ Database tables
- You work with objects, ORM handles SQL

**Without ORM (JDBC):**
```java
// BAD: Manual SQL, error-prone
String sql = "INSERT INTO bucket (name) VALUES (?)";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setString(1, "my-bucket");
stmt.executeUpdate();

String sql2 = "SELECT * FROM bucket WHERE name = ?";
PreparedStatement stmt2 = connection.prepareStatement(sql2);
stmt2.setString(1, "my-bucket");
ResultSet rs = stmt2.executeQuery();
if (rs.next()) {
    Bucket bucket = new Bucket();
    bucket.setId(rs.getLong("id"));
    bucket.setName(rs.getString("name"));
    // ... manual mapping
}
```

**With ORM (JPA/Hibernate):**
```java
// GOOD: Clean, simple
Bucket bucket = new Bucket();
bucket.setName("my-bucket");
bucketRepository.save(bucket);  // SQL generated automatically

Bucket found = bucketRepository.findByName("my-bucket")
    .orElseThrow();  // Query generated automatically
```

**What Hibernate generates:**
```sql
-- On save()
INSERT INTO bucket (name, created_at, updated_at)
VALUES ('my-bucket', '2024-12-26 10:00:00', '2024-12-26 10:00:00');

-- On findByName()
SELECT id, name, created_at, updated_at
FROM bucket
WHERE name = 'my-bucket';
```

### 2. Lazy vs Eager Loading

**The Problem:**
```java
Bucket bucket = bucketRepository.findById(1L).get();
// Do we load all objects in the bucket immediately?
// What if there are 10,000 objects?
```

**Lazy Loading (Default for collections):**
```java
@OneToMany(fetch = FetchType.LAZY)  // Default
private List<StorageObject> objects;

// Usage:
Bucket bucket = bucketRepository.findById(1L).get();
// SQL: SELECT * FROM bucket WHERE id = 1
// objects are NOT loaded yet

List<StorageObject> objects = bucket.getObjects();
// SQL: SELECT * FROM storage_object WHERE bucket_id = 1
// objects loaded NOW when accessed
```

**Eager Loading:**
```java
@OneToMany(fetch = FetchType.EAGER)
private List<StorageObject> objects;

// Usage:
Bucket bucket = bucketRepository.findById(1L).get();
// SQL: SELECT * FROM bucket WHERE id = 1
// SQL: SELECT * FROM storage_object WHERE bucket_id = 1
// objects loaded IMMEDIATELY (even if you don't use them)
```

**When to use each:**

| Use LAZY when | Use EAGER when |
|---------------|----------------|
| Collection might be large | Collection is always small |
| Often don't need the data | Always need the data |
| Performance is critical | Simplicity is preferred |
| **Most cases (default)** | Rare cases |

**Our choices:**
```java
@OneToMany(fetch = FetchType.LAZY)  // ← We use LAZY
private List<StorageObject> objects;  // Could be 1000s of objects

@ManyToOne(fetch = FetchType.LAZY)   // ← We use LAZY
private Bucket bucket;  // Avoid loading entire bucket when loading object
```

### 3. Cascade Operations

**The Problem:**
```java
Bucket bucket = new Bucket();
bucket.setName("my-bucket");

StorageObject obj = new StorageObject();
obj.setObjectKey("file.txt");
obj.setBucket(bucket);

// Do I need to save both separately?
bucketRepository.save(bucket);  // Save bucket
objectRepository.save(obj);     // Save object
```

**With Cascade:**
```java
@OneToMany(cascade = CascadeType.ALL)
private List<StorageObject> objects;

// Usage:
Bucket bucket = new Bucket();
bucket.setName("my-bucket");

StorageObject obj = new StorageObject();
obj.setObjectKey("file.txt");
bucket.getObjects().add(obj);

bucketRepository.save(bucket);  // Saves bucket AND object!
// No need to save object separately
```

**Cascade Types:**

```java
CascadeType.PERSIST  // Save child when saving parent
CascadeType.REMOVE   // Delete child when deleting parent
CascadeType.MERGE    // Update child when updating parent
CascadeType.REFRESH  // Reload child when reloading parent
CascadeType.DETACH   // Detach child when detaching parent
CascadeType.ALL      // All of the above
```

**Orphan Removal:**
```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
private List<StorageObject> objects;

// Usage:
Bucket bucket = bucketRepository.findById(1L).get();
bucket.getObjects().remove(0);  // Remove first object from list
bucketRepository.save(bucket);
// Object is automatically DELETED from database
```

**Our configuration:**
```java
@OneToMany(
    mappedBy = "bucket",
    cascade = CascadeType.ALL,     // Save/delete objects with bucket
    orphanRemoval = true,           // Delete orphaned objects
    fetch = FetchType.LAZY          // Don't load immediately
)
private List<StorageObject> objects;
```

**What this means:**
- Delete bucket → All objects deleted
- Remove object from list → Object deleted from database
- Save bucket → All new objects saved

### 4. Database Normalization

**Why 4 tables instead of 1?**

**Bad Approach (Single Table):**
```java
// BAD: Everything in one table
@Entity
public class BucketAndObject {
    private Long id;
    private String bucketName;
    private String objectKey;
    private byte[] content;
    private Integer versionNumber;
    private String metadataKey1;
    private String metadataValue1;
    private String metadataKey2;
    private String metadataValue2;
    // Problems:
    // - Duplicate bucket names (bucketName repeated for every object)
    // - Fixed metadata columns (what if user needs metadataKey3?)
    // - Can't have multiple versions efficiently
}
```

**Good Approach (Normalized):**
```java
// GOOD: Separate tables
Bucket → StorageObject → ObjectVersion → ObjectMetadata

// Benefits:
// - Bucket name stored once
// - Unlimited metadata keys
// - Efficient version storage
// - Easy to query
```

**Normalization Example:**

**Before (Denormalized):**
```
+----+---------+------+-------+-----------+----------+
| id | bucket  | key  | v_num | meta_key  | meta_val |
+----+---------+------+-------+-----------+----------+
| 1  | photos  | a.jpg| 1     | author    | John     |
| 2  | photos  | a.jpg| 1     | year      | 2024     |
| 3  | photos  | a.jpg| 2     | author    | John     |
| 4  | photos  | a.jpg| 2     | year      | 2024     |
| 5  | photos  | b.jpg| 1     | author    | Jane     |
+----+---------+------+-------+-----------+----------+
// Problems:
// - "photos" repeated 5 times
// - "a.jpg" repeated 4 times
// - Wasted space
// - Update anomalies
```

**After (Normalized):**
```
Bucket:
+----+---------+
| id | name    |
+----+---------+
| 1  | photos  |
+----+---------+

StorageObject:
+----+-----------+------+
| id | bucket_id | key  |
+----+-----------+------+
| 1  | 1         | a.jpg|
| 2  | 1         | b.jpg|
+----+-----------+------+

ObjectVersion:
+----+-----------+-------+
| id | object_id | v_num |
+----+-----------+-------+
| 1  | 1         | 1     |
| 2  | 1         | 2     |
| 3  | 2         | 1     |
+----+-----------+-------+

ObjectMetadata:
+----+------------+--------+-------+
| id | version_id | key    | value |
+----+------------+--------+-------+
| 1  | 1          | author | John  |
| 2  | 1          | year   | 2024  |
| 3  | 2          | author | John  |
| 4  | 2          | year   | 2024  |
| 5  | 3          | author | Jane  |
+----+------------+--------+-------+

// Benefits:
// - No duplication
// - Less storage
// - Easier to update
// - Referential integrity
```

---

## 🏁 What Can You Do Now?

### Your Database Schema is Ready!

When you run the application:

**1. Spring Boot starts**
```
Starting Minis3Application...
```

**2. Hibernate reads your entity classes**
```
Scanning for entity classes...
Found: Bucket, StorageObject, ObjectVersion, ObjectMetadata
```

**3. Automatically creates tables in PostgreSQL:**
```sql
CREATE TABLE bucket (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE storage_object (
    id BIGSERIAL PRIMARY KEY,
    bucket_id BIGINT REFERENCES bucket(id),
    object_key VARCHAR(1024) NOT NULL,
    current_version_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(bucket_id, object_key)
);

CREATE TABLE object_version (
    id BIGSERIAL PRIMARY KEY,
    object_id BIGINT REFERENCES storage_object(id),
    version_number INTEGER NOT NULL,
    content BYTEA NOT NULL,
    content_type VARCHAR(255),
    size BIGINT NOT NULL,
    etag VARCHAR(64),
    is_latest BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(object_id, version_number)
);

CREATE TABLE object_metadata (
    id BIGSERIAL PRIMARY KEY,
    version_id BIGINT REFERENCES object_version(id),
    meta_key VARCHAR(255) NOT NULL,
    meta_value TEXT,
    UNIQUE(version_id, meta_key)
);
```

**4. Application is ready** to accept data

### Verify It Works

**Start the application:**
```bash
./gradlew bootRun
```

**Check PostgreSQL:**
```bash
psql -d minis3db -c "\dt"
```

**You should see:**
```
              List of relations
 Schema |      Name       | Type  |  Owner
--------+-----------------+-------+----------
 public | bucket          | table | postgres
 public | object_metadata | table | postgres
 public | object_version  | table | postgres
 public | storage_object  | table | postgres
```

### Next Steps (Epic 2 Story 2)

Now that the data layer is complete, you need to add:

**Service Layer (Business Logic):**
```java
@Service
public class BucketServiceImpl {
    public BucketDto createBucket(String name) {
        // Validate name
        // Check if exists
        // Save to database
        // Return DTO
    }
}
```

**Then REST API:**
```java
@RestController
public class BucketController {
    @PostMapping("/api/buckets")
    public BucketDto createBucket(@RequestBody CreateBucketRequest request) {
        return bucketService.createBucket(request.getName());
    }
}
```

---

## ❓ Self-Assessment Questions

Test your understanding:

### Question 1: Why did we use `@Lob` for the content field?

<details>
<summary>Click to reveal answer</summary>

**Answer:** Because it's binary file data that can be very large (images, videos, documents, etc.). `@Lob` tells JPA this is a "Large Object" and maps it to PostgreSQL's `BYTEA` type, which can store binary data of any size.

**Without @Lob:**
- Field would be limited to standard column size
- Large files would fail to save
- Binary data might be corrupted

</details>

### Question 2: What's the difference between StorageObject and ObjectVersion?

<details>
<summary>Click to reveal answer</summary>

**Answer:**
- **StorageObject**: Represents the file metadata/pointer. Contains the object key (filename) and points to the current version. Think of it as the "file entry" in a directory.

- **ObjectVersion**: Stores the actual file content (bytes). One StorageObject can have many ObjectVersions (history). Each time you update a file, a new ObjectVersion is created while the StorageObject remains the same.

**Example:**
```
StorageObject: "report.pdf" (metadata)
    ├─ ObjectVersion 1: <old-content> (100 KB)
    ├─ ObjectVersion 2: <newer-content> (105 KB)
    └─ ObjectVersion 3: <latest-content> (110 KB) ← current
```

</details>

### Question 3: Why do we need ObjectMetadata as a separate entity?

<details>
<summary>Click to reveal answer</summary>

**Answer:**

**1. Flexible schema** - Can attach any custom key-value data without changing the database structure:
```java
// Can add any metadata:
"author" = "John"
"project" = "Q4-2024"
"custom-field" = "anything"
// No need to add columns!
```

**2. Version-specific** - Each version can have different metadata:
```java
Version 1: status=draft, author=John
Version 2: status=review, author=John, reviewer=Jane
Version 3: status=final, author=John
```

**3. No NULL columns** - Only store metadata that exists:
```java
// Object 1: author=John, department=Sales (2 metadata entries)
// Object 2: tags=important (1 metadata entry)
// No wasted columns with NULL values
```

**Alternative (worse):**
```java
// BAD: Fixed columns in ObjectVersion
class ObjectVersion {
    private String metaAuthor;
    private String metaDepartment;
    private String metaTags;
    // What if user needs "project"? Must alter table!
}
```

</details>

### Question 4: What does `@EnableJpaAuditing` do?

<details>
<summary>Click to reveal answer</summary>

**Answer:** Automatically populates `@CreatedDate` and `@LastModifiedDate` fields when entities are saved or updated.

**Without it:**
```java
Bucket bucket = new Bucket();
bucket.setName("my-bucket");
bucket.setCreatedAt(LocalDateTime.now());  // Manual
bucket.setUpdatedAt(LocalDateTime.now());  // Manual
bucketRepository.save(bucket);
```

**With it:**
```java
Bucket bucket = new Bucket();
bucket.setName("my-bucket");
// createdAt and updatedAt set automatically!
bucketRepository.save(bucket);
```

Spring intercepts the save operation and sets the timestamps for you.

</details>

### Question 5: Why use FetchType.LAZY instead of EAGER?

<details>
<summary>Click to reveal answer</summary>

**Answer:** Performance and memory efficiency.

**LAZY (Good):**
```java
@ManyToOne(fetch = FetchType.LAZY)
private Bucket bucket;

// Load object:
StorageObject obj = repository.findById(1L).get();
// SQL: SELECT * FROM storage_object WHERE id = 1
// Only loads StorageObject, not Bucket

// Access bucket only when needed:
String bucketName = obj.getBucket().getName();
// SQL: SELECT * FROM bucket WHERE id = ?
// Bucket loaded NOW
```

**EAGER (Bad for performance):**
```java
@ManyToOne(fetch = FetchType.EAGER)
private Bucket bucket;

// Load object:
StorageObject obj = repository.findById(1L).get();
// SQL: SELECT * FROM storage_object WHERE id = 1
// SQL: SELECT * FROM bucket WHERE id = ?
// Loads StorageObject AND Bucket (even if you don't need bucket)
```

**Why LAZY is better:**
- Only loads data when you actually need it
- Saves memory
- Faster queries
- Avoids loading entire object graphs

**When to use EAGER:**
- You ALWAYS need the related data
- The collection is small (1-5 items)
- Rare cases only

</details>

### Question 6: What does `cascade = CascadeType.ALL` mean?

<details>
<summary>Click to reveal answer</summary>

**Answer:** Operations on the parent entity are cascaded (propagated) to child entities.

**Example:**
```java
@OneToMany(cascade = CascadeType.ALL)
private List<StorageObject> objects;
```

**What happens:**

**1. Save cascades:**
```java
Bucket bucket = new Bucket();
bucket.setName("my-bucket");

StorageObject obj = new StorageObject();
obj.setObjectKey("file.txt");
bucket.getObjects().add(obj);

bucketRepository.save(bucket);
// Saves bucket AND automatically saves obj
// No need: objectRepository.save(obj)
```

**2. Delete cascades:**
```java
bucketRepository.deleteById(1L);
// Deletes bucket AND all its objects
```

**3. Update cascades:**
```java
Bucket bucket = bucketRepository.findById(1L).get();
bucket.getObjects().get(0).setObjectKey("new-name.txt");
bucketRepository.save(bucket);
// Updates bucket AND updates the object
```

**Without cascade:**
```java
// You'd have to save everything manually:
bucketRepository.save(bucket);
objectRepository.save(obj1);
objectRepository.save(obj2);
objectRepository.save(obj3);
// Tedious and error-prone!
```

</details>

### Question 7: Why do we have both `currentVersion` and `versions` in StorageObject?

<details>
<summary>Click to reveal answer</summary>

**Answer:**

**currentVersion** - Quick access to the active version:
```java
// Fast: Direct reference to current version
ObjectVersion current = storageObject.getCurrentVersion();
byte[] content = current.getContent();  // Download latest version
```

**versions** - Complete history:
```java
// Get all versions for history/rollback
List<ObjectVersion> history = storageObject.getVersions();
for (ObjectVersion v : history) {
    System.out.println("Version " + v.getVersionNumber());
}
```

**Why both?**

**Performance:**
- Getting current version is O(1) - single lookup
- Without currentVersion, you'd need to:
  ```java
  // Slow: Must load all versions and filter
  ObjectVersion current = versions.stream()
      .filter(v -> v.getIsLatest())
      .findFirst()
      .get();
  ```

**Common use case:**
- 99% of requests need current version (download file)
- 1% of requests need version history (view history, restore old version)
- Optimized for the common case

**Database perspective:**
```
storage_object:
    current_version_id → 3  (foreign key, indexed, fast lookup)

object_version:
    id=1, object_id=1, version=1, is_latest=false
    id=2, object_id=1, version=2, is_latest=false
    id=3, object_id=1, version=3, is_latest=true  ← currentVersion points here
```

</details>

---

## 🎯 Summary

### What You Built:

✅ **Complete database schema** with 4 interconnected tables
✅ **JPA entities** with proper relationships and constraints
✅ **Repository layer** with custom query methods
✅ **Automatic auditing** for created/updated timestamps
✅ **BLOB support** for storing file content
✅ **Version tracking** system
✅ **Flexible metadata** storage
✅ **Test configuration** with H2 database

### Key Technologies Mastered:

- Spring Data JPA
- Hibernate ORM
- PostgreSQL
- Lombok
- Entity relationships (@OneToMany, @ManyToOne, @OneToOne)
- Lazy loading
- Cascade operations
- Repository pattern
- H2 for testing

### Next Steps:

**Epic 2 Story 2: Implement Bucket Service Layer**
- Create BucketService interface
- Implement BucketServiceImpl
- Add business logic (validation, error handling)
- Create DTOs for API responses

**Epic 2 Story 3: Build Bucket REST API**
- Create BucketController
- Add CRUD endpoints
- Handle HTTP requests/responses

**Epic 2 Story 4: Add validation and error handling**
- Custom exceptions
- GlobalExceptionHandler
- Input validation

---

## 📖 Additional Resources

**Learn More:**

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate User Guide](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html)
- [Lombok Features](https://projectlombok.org/features/)
- [PostgreSQL BYTEA Documentation](https://www.postgresql.org/docs/current/datatype-binary.html)
- [JPA Cascade Types](https://www.baeldung.com/jpa-cascade-types)
- [Lazy vs Eager Loading](https://www.baeldung.com/hibernate-lazy-eager-loading)

---

**Happy Learning! 🚀**
