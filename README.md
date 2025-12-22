# Mini-S3 Object Storage Service

A custom object storage service similar to AWS S3, built with Spring Boot, PostgreSQL, and Gradle. This project is designed as a hands-on learning journey to understand object storage concepts through practical implementation.

## Table of Contents

- [Overview](#overview)
- [Project Management](#project-management)
- [Tech Stack](#tech-stack)
- [Core Features](#core-features)
- [Learning Objectives](#learning-objectives)
- [Project Structure](#project-structure)
- [Implementation Roadmap](#implementation-roadmap)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Post-Implementation](#post-implementation)
- [Resources & Links](#resources--links)
- [Contributing](#contributing)

---

## Overview

Mini-S3 is an educational project that implements core object storage functionality similar to AWS S3. It provides:

- **Bucket Management**: Create containers (buckets) to organize your files
- **Object Storage**: Upload, download, and manage files with metadata
- **Versioning**: Keep complete history of file changes
- **Metadata**: Attach custom key-value information to files
- **RESTful API**: Clean, well-documented REST endpoints

### Project Management

📋 **JIRA Board**: [Mini-S3 Project Board](https://rajatbhagat.atlassian.net/jira/software/projects/MS/boards/2)

Track progress, view tasks, and manage the implementation using our JIRA board with all 6 epics and 175+ tasks organized for structured learning.

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17+ | Core programming language |
| **Spring Boot** | 3.2.x | Application framework |
| **PostgreSQL** | 14+ | Database for storing BLOBs and metadata |
| **Gradle** | 8.x | Build automation tool |
| **Spring Data JPA** | - | Data persistence layer |
| **Lombok** | - | Reduce boilerplate code |
| **SpringDoc OpenAPI** | - | API documentation (Swagger) |

---

## Core Features

### 1. Bucket Management
- Create, list, view, and delete buckets
- Unique bucket naming
- Empty bucket validation before deletion

### 2. Object Storage
- Upload files to buckets
- Download files with proper content headers
- List all objects in a bucket
- Delete objects with cascade removal of versions

### 3. Metadata Management
- Attach custom key-value metadata to objects
- Update metadata independently
- Version-specific metadata storage

### 4. Object Versioning
- Automatic version creation on file updates
- View complete version history
- Download specific versions
- Restore previous versions
- Delete specific versions with safety checks

---

## Learning Objectives

By building this project, you will gain hands-on experience with:

1. **Spring Boot 3.x** - Modern Java application development
2. **JPA Entity Relationships** - Complex database mappings and associations
3. **BLOB Handling** - Storing and retrieving binary data in PostgreSQL
4. **RESTful API Design** - Building intuitive, standard-compliant APIs
5. **File Upload/Download** - MultipartFile handling and streaming
6. **Version Control Implementation** - Building history tracking systems
7. **Database Schema Design** - Designing schemas for storage systems
8. **Transaction Management** - Ensuring data consistency
9. **Error Handling** - Validation and exception strategies
10. **Integration Testing** - End-to-end testing with databases

---

## Project Structure

```
mini-s3/
├── src/
│   ├── main/
│   │   ├── java/com/example/minis3/
│   │   │   ├── MiniS3Application.java
│   │   │   ├── controller/          # REST API endpoints
│   │   │   │   ├── BucketController.java
│   │   │   │   ├── ObjectController.java
│   │   │   │   └── VersionController.java
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── BucketService.java
│   │   │   │   ├── ObjectService.java
│   │   │   │   └── VersionService.java
│   │   │   ├── repository/          # Data access layer
│   │   │   │   ├── BucketRepository.java
│   │   │   │   ├── StorageObjectRepository.java
│   │   │   │   ├── ObjectVersionRepository.java
│   │   │   │   └── ObjectMetadataRepository.java
│   │   │   ├── entity/              # JPA entities
│   │   │   │   ├── Bucket.java
│   │   │   │   ├── StorageObject.java
│   │   │   │   ├── ObjectVersion.java
│   │   │   │   └── ObjectMetadata.java
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   ├── exception/           # Custom exceptions
│   │   │   └── config/              # Configuration classes
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/                        # Unit and integration tests
├── build.gradle
├── settings.gradle
└── README.md
```

---

## Implementation Roadmap

The project is organized into **6 Epics**, each focusing on specific learning outcomes.

> 💡 **Tip**: All tasks are available in the [JIRA Project Board](https://rajatbhagat.atlassian.net/jira/software/projects/MS/boards/2) for easy tracking and progress management. Import the `mini-s3-jira-import.csv` file to get started!

### Epic 1: Project Foundation & Setup
**Goal:** Set up development environment and understand Spring Boot project structure

**Stories:**
- Initialize Gradle project with Spring Boot
- Configure PostgreSQL database
- Set up application configuration

**Duration:** Week 1

---

### Epic 2: Bucket Management (Core Concept)
**Goal:** Learn basic CRUD operations, JPA entities, and repository pattern

**Stories:**
- Create Bucket entity and repository
- Implement Bucket service layer
- Build Bucket REST API
- Add validation and error handling

**Duration:** Week 1-2

---

### Epic 3: Object Storage (File Handling)
**Goal:** Understand file uploads, BLOB storage, and streaming

**Stories:**
- Create Object entities (StorageObject, ObjectVersion)
- Implement file upload functionality
- Implement file download functionality
- Build Object REST API

**Duration:** Week 2-3

---

### Epic 4: Metadata Management (Key-Value Storage)
**Goal:** Learn flexible schema design and metadata associations

**Stories:**
- Create ObjectMetadata entity
- Implement metadata CRUD operations
- Add metadata endpoints to API

**Duration:** Week 4

---

### Epic 5: Versioning System (Advanced Concept)
**Goal:** Understand version control, history tracking, and state management

**Stories:**
- Implement version creation logic
- Build version listing and retrieval
- Add version deletion and restoration
- Create Version REST API

**Duration:** Week 5

---

### Epic 6: Testing & Documentation
**Goal:** Learn testing strategies and API documentation

**Stories:**
- Write unit tests for services
- Write integration tests
- Add API documentation with Swagger
- Create project README

**Duration:** Week 6

---

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL 14 or higher
- Gradle 8.x
- Git

### Database Setup

1. **Install PostgreSQL** (if not already installed)
   ```bash
   # macOS
   brew install postgresql

   # Start PostgreSQL service
   brew services start postgresql
   ```

2. **Create the database**
   ```bash
   psql postgres
   CREATE DATABASE minis3db;
   \q
   ```

3. **Test connection**
   ```bash
   psql -d minis3db -U postgres
   ```

### Application Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/mini-s3.git
   cd mini-s3
   ```

2. **Configure database connection**

   Edit `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/minis3db
       username: postgres
       password: your_password
   ```

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

5. **Access the application**
   - API: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## API Endpoints

### Bucket Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/buckets` | Create a new bucket |
| `GET` | `/api/buckets` | List all buckets |
| `GET` | `/api/buckets/{name}` | Get bucket details |
| `DELETE` | `/api/buckets/{name}` | Delete bucket |
| `GET` | `/api/buckets/{name}/objects` | List objects in bucket |

### Object Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/buckets/{bucket}/objects` | Upload object |
| `GET` | `/api/buckets/{bucket}/objects/{key}` | Download object |
| `GET` | `/api/buckets/{bucket}/objects/{key}/info` | Get object metadata |
| `PUT` | `/api/buckets/{bucket}/objects/{key}` | Update object (creates new version) |
| `DELETE` | `/api/buckets/{bucket}/objects/{key}` | Delete object |
| `PUT` | `/api/buckets/{bucket}/objects/{key}/metadata` | Update metadata |

### Version Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/buckets/{bucket}/objects/{key}/versions` | List all versions |
| `GET` | `/api/buckets/{bucket}/objects/{key}/versions/{version}` | Download specific version |
| `GET` | `/api/buckets/{bucket}/objects/{key}/versions/{version}/info` | Get version metadata |
| `DELETE` | `/api/buckets/{bucket}/objects/{key}/versions/{version}` | Delete specific version |

---

## Database Schema

### Entity Relationship Diagram

```
┌─────────────┐
│   Bucket    │
├─────────────┤
│ id (PK)     │
│ name (UK)   │
│ created_at  │
│ updated_at  │
└──────┬──────┘
       │
       │ 1:N
       │
┌──────▼──────────────┐
│  StorageObject      │
├─────────────────────┤
│ id (PK)             │
│ bucket_id (FK)      │
│ object_key (UK)     │
│ current_version_id  │
│ created_at          │
│ updated_at          │
└──────┬──────────────┘
       │
       │ 1:N
       │
┌──────▼──────────────┐
│  ObjectVersion      │
├─────────────────────┤
│ id (PK)             │
│ object_id (FK)      │
│ version_number      │
│ content (BYTEA)     │
│ content_type        │
│ size                │
│ etag                │
│ is_latest           │
│ created_at          │
└──────┬──────────────┘
       │
       │ 1:N
       │
┌──────▼──────────────┐
│  ObjectMetadata     │
├─────────────────────┤
│ id (PK)             │
│ version_id (FK)     │
│ meta_key            │
│ meta_value          │
└─────────────────────┘
```

### Key Tables

- **bucket**: Stores bucket information
- **storage_object**: Stores object metadata and current version reference
- **object_version**: Stores file content (BLOB) and version-specific data
- **object_metadata**: Stores custom key-value metadata for each version

---

## API Usage Examples

### Create a Bucket

```bash
curl -X POST http://localhost:8080/api/buckets \
  -H "Content-Type: application/json" \
  -d '{"name": "my-bucket"}'
```

### Upload a File

```bash
curl -X POST http://localhost:8080/api/buckets/my-bucket/objects \
  -F "file=@document.pdf" \
  -F "metadata={\"author\":\"John Doe\",\"department\":\"Engineering\"}"
```

### Download a File

```bash
curl -X GET http://localhost:8080/api/buckets/my-bucket/objects/document.pdf \
  --output document.pdf
```

### List Versions

```bash
curl -X GET http://localhost:8080/api/buckets/my-bucket/objects/document.pdf/versions
```

### Update Object Metadata

```bash
curl -X PUT http://localhost:8080/api/buckets/my-bucket/objects/document.pdf/metadata \
  -H "Content-Type: application/json" \
  -d '{"tags": "important,reviewed", "status": "approved"}'
```

---

## Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Integration Tests

```bash
./gradlew integrationTest
```

### Check Code Coverage

```bash
./gradlew jacocoTestReport
```

Coverage report: `build/reports/jacoco/test/html/index.html`

---

## Post-Implementation

After completing the core features, consider these enhancements:

1. **Docker Deployment** - Containerize with Docker Compose
2. **Performance Testing** - Test with large files and concurrent requests
3. **Authentication** - Add Spring Security with JWT
4. **Caching** - Implement Redis for frequently accessed objects
5. **Monitoring** - Add Spring Boot Actuator and metrics
6. **Cloud Storage** - Support for actual S3 or Azure Blob Storage backend
7. **Frontend** - Build a React-based UI
8. **Multi-part Upload** - Support for large files (>100MB)
9. **Pre-signed URLs** - Temporary access links
10. **Bucket Policies** - Fine-grained access control

---

## Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB        # Maximum file upload size
      max-request-size: 50MB     # Maximum request size

  datasource:
    url: jdbc:postgresql://localhost:5432/minis3db
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: update           # Auto-create/update schema
    show-sql: true               # Log SQL statements

server:
  port: 8080                     # Application port
```

---

## Troubleshooting

### Common Issues

**Database Connection Failed**
- Verify PostgreSQL is running: `brew services list`
- Check credentials in `application.yml`
- Ensure database `minis3db` exists

**File Upload Failed**
- Check `max-file-size` configuration
- Verify disk space available
- Check PostgreSQL `max_allowed_packet` setting

**Table Not Found**
- Ensure `spring.jpa.hibernate.ddl-auto=update` is set
- Run application once to auto-generate tables
- Check PostgreSQL logs for errors

---

## Contributing

This is a learning project, but contributions are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

- Inspired by AWS S3
- Built with Spring Boot and PostgreSQL
- Created as an educational project to learn object storage concepts

---

## Resources & Links

**Project Repository**: [https://github.com/yourusername/mini-s3](https://github.com/yourusername/mini-s3)

**JIRA Project Board**: [https://rajatbhagat.atlassian.net/jira/software/projects/MS/boards/2](https://rajatbhagat.atlassian.net/jira/software/projects/MS/boards/2)

**Issues & Questions**: Please use the [GitHub Issues](https://github.com/yourusername/mini-s3/issues) page

---

## Project Status

🚀 **Current Status**: In Development

**Completed Epics**: 0/6

- [ ] Epic 1: Project Foundation & Setup
- [ ] Epic 2: Bucket Management
- [ ] Epic 3: Object Storage
- [ ] Epic 4: Metadata Management
- [ ] Epic 5: Versioning System
- [ ] Epic 6: Testing & Documentation

---

Happy Coding! 🎉
