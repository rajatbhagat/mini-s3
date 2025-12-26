# Spring Singleton Beans for Database Connection - Explained

> **Learning Reference**: Understanding Spring Bean Scopes, Dependency Injection, and Database Connection Management

**Created**: December 26, 2024

---

## 🎯 What Are Singleton Beans?

### Definition

A **singleton bean** in Spring is an object that:
- Is created **ONCE** when the application starts
- Lives for the **entire application lifecycle**
- Is **shared** by all components that need it
- Is **thread-safe** (when properly designed)

### Visual Representation

```
Application Startup
    ↓
[Spring Container Creates Singleton Beans]
    ↓
    ├─> DataSource (singleton) ──┐
    ├─> EntityManager (singleton) ─┤
    ├─> BucketService (singleton) ─┤
    └─> ObjectService (singleton) ─┘
                                    │
                              [All share same instances]
                                    │
    ┌───────────────────────────────┘
    ↓
[Application runs using these singletons]
    ↓
Application Shutdown
    ↓
[Beans are destroyed]
```

---

## 🔑 Key Concept: Bean Scopes

Spring supports several bean scopes:

| Scope | Instances | Lifecycle | Use Case |
|-------|-----------|-----------|----------|
| **singleton** | 1 per container | Application lifetime | **Default**, services, configs |
| prototype | New for each request | Created on demand | Stateful objects |
| request | 1 per HTTP request | HTTP request lifetime | Web apps only |
| session | 1 per HTTP session | HTTP session lifetime | Web apps only |

**In our mini-s3 project:**
- 99% of beans are **singleton** (default)
- This is what you want for database connections!

---

## 🗄️ Database Connection in Spring Boot

### What Spring Boot Already Creates (Automatically!)

When you add these to `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/minis3db
    username: postgres
    password: postgres
```

Spring Boot **automatically** creates these **singleton beans**:

#### 1. DataSource (HikariCP)

```java
// Spring Boot creates this automatically!
@Bean
public DataSource dataSource() {
    return new HikariDataSource(config);
}
```

**What it does:**
- Manages a **pool** of database connections
- Keeps 5-10 connections open and ready
- Much faster than creating new connections
- Thread-safe and optimized

#### 2. EntityManagerFactory

```java
// Spring Boot creates this automatically!
@Bean
public EntityManagerFactory entityManagerFactory() {
    // JPA entity manager for database operations
}
```

**What it does:**
- Manages JPA entities
- Handles object-relational mapping
- Creates EntityManager instances

#### 3. TransactionManager

```java
// Spring Boot creates this automatically!
@Bean
public PlatformTransactionManager transactionManager() {
    // Manages database transactions
}
```

**What it does:**
- Handles `@Transactional` annotations
- Manages commit/rollback
- Ensures data consistency

### How These Beans Are Singletons

```java
// You can inject the DataSource anywhere in your app
@Service
public class BucketService {
    private final DataSource dataSource;  // ← Same instance everywhere

    public BucketService(DataSource dataSource) {
        this.dataSource = dataSource;  // Spring injects the singleton
    }
}

@Service
public class ObjectService {
    private final DataSource dataSource;  // ← Same instance as above!

    public ObjectService(DataSource dataSource) {
        this.dataSource = dataSource;  // Same singleton injected
    }
}
```

**Visual:**
```
[DataSource Singleton Bean]
        ↓
    ┌───┴───┬────────┬────────┐
    ↓       ↓        ↓        ↓
Service1 Service2 Service3 Service4
(same    (same    (same    (same
instance) instance) instance) instance)
```

---

## 📝 Custom Singleton Bean: DatabaseConfig

I created a custom `DatabaseConfig` class to show you how singleton beans work. Here's what it does:

### The Configuration Class

```java
@Configuration  // ← Marks this as a configuration class
public class DatabaseConfig {

    @Bean  // ← This method creates a singleton bean
    public DataSource dataSource() {
        // Spring calls this method ONCE at startup
        // Returns a singleton DataSource
        return new HikariDataSource(config);
    }

    @Bean
    public DatabaseHealthCheck databaseHealthCheck(DataSource dataSource) {
        // This bean depends on the DataSource bean
        // Spring automatically injects it
        return new DatabaseHealthCheck(dataSource);
    }
}
```

### How @Bean Works

**1. Application Startup:**
```
Spring scans for @Configuration classes
    ↓
Finds DatabaseConfig
    ↓
Calls dataSource() method ONCE
    ↓
Stores the returned object in Spring Container
    ↓
Creates DatabaseHealthCheck bean (depends on DataSource)
    ↓
Ready to inject into other beans
```

**2. When Other Beans Need It:**
```java
@Service
public class MyService {
    private final DataSource dataSource;

    // Spring injects the singleton created earlier
    public MyService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
```

**3. Singleton Guarantee:**
```java
// These all get the SAME DataSource instance
MyService service1 = new MyService(dataSource);  // dataSource@12345
MyService service2 = new MyService(dataSource);  // dataSource@12345 ← Same!
MyService service3 = new MySource(dataSource);   // dataSource@12345 ← Same!
```

---

## 🔍 Deep Dive: HikariCP Connection Pool

### What is HikariCP?

- **Hikari** (光) = "Light" in Japanese
- Fastest, lightest JDBC connection pool
- Default in Spring Boot
- Thread-safe singleton

### How Connection Pooling Works

**Without Connection Pool (Slow):**
```
Request 1:
    Create connection → Execute query → Close connection (500ms)
Request 2:
    Create connection → Execute query → Close connection (500ms)
Request 3:
    Create connection → Execute query → Close connection (500ms)

Total: 1500ms for 3 requests
```

**With Connection Pool (Fast):**
```
Startup:
    Create 5 connections → Keep them alive

Request 1:
    Borrow connection from pool → Execute query → Return to pool (10ms)
Request 2:
    Borrow connection from pool → Execute query → Return to pool (10ms)
Request 3:
    Borrow connection from pool → Execute query → Return to pool (10ms)

Total: 30ms for 3 requests (50x faster!)
```

### Visual Representation

```
┌─────────────────────────────────────┐
│   HikariCP DataSource (Singleton)   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Connection Pool           │   │
│  │                             │   │
│  │  [Conn1] [Conn2] [Conn3]   │   │
│  │  [Conn4] [Conn5]            │   │
│  │   ↑ Available ↑             │   │
│  │   ↓ In Use ↓                │   │
│  │  [Conn6] [Conn7]            │   │
│  │   (busy)  (busy)            │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
        ↑           ↑           ↑
        │           │           │
   Service1    Service2    Service3
   (borrows)   (borrows)   (borrows)
```

### Configuration Parameters

```java
config.setMaximumPoolSize(10);     // Max connections
config.setMinimumIdle(5);          // Min connections kept alive
config.setConnectionTimeout(20000); // Wait 20s for connection
config.setIdleTimeout(300000);     // Close idle conn after 5 min
config.setMaxLifetime(1200000);    // Close conn after 20 min
```

**What these mean:**

- **maximumPoolSize=10**: Create at most 10 connections
  - More connections = more concurrent requests
  - Too many = wastes database resources
  - 10 is good for small-medium apps

- **minimumIdle=5**: Always keep 5 connections ready
  - Ready for immediate use
  - Prevents creating connections on demand

- **connectionTimeout=20000**: Wait 20 seconds for a connection
  - If all 10 connections are busy, wait for one to free up
  - After 20 seconds, throw exception

- **idleTimeout=300000**: Close connection after 5 minutes of no use
  - Frees up database resources
  - Connection recreated when needed

- **maxLifetime=1200000**: Close connection after 20 minutes
  - Prevents stale connections
  - Database might close long-lived connections

---

## 🏗️ Dependency Injection Example

### How Spring Injects Singleton Beans

**Step 1: Define the Bean**
```java
@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource(config);
    }
}
```

**Step 2: Spring Creates Singleton**
```
Application Startup
    ↓
Spring creates DataSource singleton
    ↓
Stores in ApplicationContext
```

**Step 3: Inject into Service**
```java
@Service
public class BucketService {
    private final DataSource dataSource;

    // Constructor injection (recommended)
    public BucketService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
```

**Step 4: Spring Wires Everything**
```
Spring sees BucketService needs DataSource
    ↓
Finds DataSource singleton in ApplicationContext
    ↓
Calls: new BucketService(dataSourceSingleton)
    ↓
BucketService is ready
```

### Three Ways to Inject Beans

**1. Constructor Injection (Recommended):**
```java
@Service
public class BucketService {
    private final DataSource dataSource;

    public BucketService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
```
**Why recommended:**
- Immutable (final field)
- Required dependencies are obvious
- Easier to test
- Prevents circular dependencies

**2. Field Injection (Not recommended):**
```java
@Service
public class BucketService {
    @Autowired
    private DataSource dataSource;  // ← Not ideal
}
```
**Why not recommended:**
- Can't be final
- Hidden dependencies
- Harder to test
- Can cause circular dependency issues

**3. Setter Injection (Rarely used):**
```java
@Service
public class BucketService {
    private DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
```
**When to use:**
- Optional dependencies
- Configuration changes at runtime

---

## 🔬 DatabaseHealthCheck Example

The `DatabaseHealthCheck` bean demonstrates:

### 1. Bean Dependency

```java
@RequiredArgsConstructor  // Lombok generates constructor
public class DatabaseHealthCheck {
    private final DataSource dataSource;  // Injected singleton

    public boolean isHealthy() {
        try (Connection connection = dataSource.getConnection()) {
            // Gets connection from pool
            // Tests database
            // Returns connection to pool
        }
    }
}
```

### 2. How It's Created

```java
@Bean
public DatabaseHealthCheck databaseHealthCheck(DataSource dataSource) {
    // Spring automatically passes the DataSource singleton
    return new DatabaseHealthCheck(dataSource);
}
```

**Spring's process:**
```
1. Create DataSource singleton
2. Pass DataSource to databaseHealthCheck() method
3. Create DatabaseHealthCheck singleton
4. Store in ApplicationContext
5. Ready to inject into other beans
```

### 3. Using the Bean

```java
@RestController
public class HealthController {
    private final DatabaseHealthCheck healthCheck;

    public HealthController(DatabaseHealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    @GetMapping("/health")
    public String health() {
        return healthCheck.isHealthy() ? "OK" : "FAILED";
    }
}
```

---

## 🧪 Testing with Singleton Beans

### Problem: Tests Should Use H2, Not PostgreSQL

**Solution: Use @Profile**

```java
@Bean
@Profile("!test")  // ← Don't create this bean when profile=test
public DataSource dataSource() {
    return new HikariDataSource(config);
}
```

**How it works:**

**Production (default profile):**
```
Spring creates DataSource from DatabaseConfig
    ↓
Uses PostgreSQL (from application.yml)
```

**Testing (@ActiveProfiles("test")):**
```
Spring skips DataSource from DatabaseConfig
    ↓
Uses H2 (from application-test.yml)
```

### Test Example

```java
@SpringBootTest
@ActiveProfiles("test")  // ← Activates test profile
class BucketServiceTest {
    @Autowired
    private DataSource dataSource;  // ← Gets H2 DataSource, not PostgreSQL

    @Test
    void testDatabase() {
        // Uses H2 in-memory database
    }
}
```

---

## 💡 Real-World Usage Examples

### Example 1: Service Using DataSource

```java
@Service
@RequiredArgsConstructor
public class BucketService {
    private final BucketRepository bucketRepository;
    private final DataSource dataSource;  // ← Singleton injected

    public void checkDatabaseConnection() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("Database: " +
                conn.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }

    public BucketDto createBucket(String name) {
        // Use repository (which uses the same DataSource singleton)
        Bucket bucket = new Bucket();
        bucket.setName(name);
        return toDto(bucketRepository.save(bucket));
    }
}
```

### Example 2: Multiple Services Share Same Connection Pool

```java
@Service
public class BucketService {
    private final DataSource dataSource;  // ← Singleton A

    public BucketService(DataSource dataSource) {
        this.dataSource = dataSource;  // dataSource@12345
    }
}

@Service
public class ObjectService {
    private final DataSource dataSource;  // ← Singleton A (same instance!)

    public ObjectService(DataSource dataSource) {
        this.dataSource = dataSource;  // dataSource@12345 (same!)
    }
}

@Service
public class VersionService {
    private final DataSource dataSource;  // ← Singleton A (same instance!)

    public VersionService(DataSource dataSource) {
        this.dataSource = dataSource;  // dataSource@12345 (same!)
    }
}
```

**All three services share:**
- Same DataSource instance
- Same connection pool
- Same 10 connections

### Example 3: Startup Event Listener

```java
@Component
@RequiredArgsConstructor
public class DatabaseStartupCheck {
    private final DatabaseHealthCheck healthCheck;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        System.out.println("Database Info: " + healthCheck.getDatabaseInfo());
        System.out.println("Database Healthy: " + healthCheck.isHealthy());
        healthCheck.testConnectionPool();
    }
}
```

**What happens:**
```
Application starts
    ↓
Spring creates all singleton beans
    ↓
Application ready
    ↓
DatabaseStartupCheck.onStartup() called
    ↓
Uses DatabaseHealthCheck singleton
    ↓
Tests connection pool
```

---

## 🎯 Key Takeaways

### 1. Singleton is the Default

```java
@Bean
public DataSource dataSource() {
    // This is a singleton by default
    // Created once, shared everywhere
}
```

### 2. Spring Boot Auto-Configuration

You usually **DON'T** need to create DataSource manually:
```yaml
# Just configure in application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/minis3db
```

Spring Boot creates the singleton for you!

### 3. When to Create Custom Bean

Create custom bean when you need:
- Custom HikariCP configuration
- Multiple data sources
- Conditional bean creation
- Complex initialization logic

### 4. Thread Safety

Singletons must be thread-safe:
- ✅ DataSource - thread-safe (managed by HikariCP)
- ✅ Repositories - thread-safe (managed by Spring Data)
- ✅ Services - thread-safe (if stateless)
- ❌ Don't store request-specific state in singleton beans

**Bad example:**
```java
@Service
public class BucketService {
    private String currentUser;  // ← BAD! Shared across all threads!

    public void processRequest(String user) {
        this.currentUser = user;  // ← Thread A sets it
        // Thread B might change it before you use it!
        doSomething();  // ← Uses wrong user!
    }
}
```

**Good example:**
```java
@Service
public class BucketService {
    public void processRequest(String user) {
        String currentUser = user;  // ← Good! Local variable
        doSomething(currentUser);
    }
}
```

---

## 📊 Bean Lifecycle

### Singleton Bean Lifecycle

```
Application Startup
    ↓
1. Spring scans for @Configuration classes
    ↓
2. Instantiates configuration classes
    ↓
3. Calls @Bean methods (ONCE for singletons)
    ↓
4. Stores beans in ApplicationContext
    ↓
5. Injects beans into dependent beans
    ↓
6. Calls @PostConstruct methods (if any)
    ↓
[Application Running - beans are reused]
    ↓
Application Shutdown
    ↓
7. Calls @PreDestroy methods (if any)
    ↓
8. Closes connections, releases resources
    ↓
9. Destroys beans
```

### Example with Lifecycle Hooks

```java
@Component
@Slf4j
public class DatabaseLifecycle {
    private final DataSource dataSource;

    public DatabaseLifecycle(DataSource dataSource) {
        this.dataSource = dataSource;
        log.info("Constructor called");
    }

    @PostConstruct
    public void init() {
        log.info("PostConstruct called - bean initialized");
        // Verify database connection
        try (Connection conn = dataSource.getConnection()) {
            log.info("Database connected: {}", conn.isValid(1));
        } catch (SQLException e) {
            log.error("Database connection failed", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("PreDestroy called - bean shutting down");
        // Cleanup resources if needed
    }
}
```

**Output:**
```
Constructor called
PostConstruct called - bean initialized
Database connected: true
[Application runs]
PreDestroy called - bean shutting down
```

---

## 🔍 Debugging Tips

### 1. Check Bean Creation

```java
@Bean
public DataSource dataSource() {
    System.out.println("Creating DataSource singleton!");  // ← Should see ONCE
    return new HikariDataSource(config);
}
```

### 2. Verify Same Instance

```java
@Service
@RequiredArgsConstructor
public class BucketService {
    private final DataSource dataSource;

    @PostConstruct
    public void init() {
        System.out.println("BucketService DataSource: " + dataSource.hashCode());
    }
}

@Service
@RequiredArgsConstructor
public class ObjectService {
    private final DataSource dataSource;

    @PostConstruct
    public void init() {
        System.out.println("ObjectService DataSource: " + dataSource.hashCode());
    }
}
```

**Output should show same hashCode:**
```
BucketService DataSource: 123456789
ObjectService DataSource: 123456789  ← Same!
```

### 3. View All Beans

```java
@Component
@RequiredArgsConstructor
public class BeanDebugger {
    private final ApplicationContext context;

    @EventListener(ApplicationReadyEvent.class)
    public void printBeans() {
        String[] beans = context.getBeanDefinitionNames();
        for (String bean : beans) {
            System.out.println(bean);
        }
    }
}
```

---

## ❓ Common Questions

### Q: Why is DataSource a singleton?

**A:** Performance and resource efficiency!
- Creating database connections is expensive (100-500ms)
- Connection pool reuses connections (1-5ms)
- One pool shared by entire application
- Thread-safe and optimized

### Q: Can I have multiple DataSource beans?

**A:** Yes! For multiple databases:
```java
@Bean
@Primary  // ← Default DataSource
public DataSource primaryDataSource() {
    // PostgreSQL
}

@Bean
public DataSource analyticsDataSource() {
    // MySQL for analytics
}
```

### Q: What if I want a new instance every time?

**A:** Use prototype scope:
```java
@Bean
@Scope("prototype")  // ← New instance for each injection
public MyService myService() {
    return new MyService();
}
```

### Q: Is Spring's singleton the same as Singleton pattern?

**A:** Similar but not identical:

**Spring Singleton:**
- One instance per Spring ApplicationContext
- Multiple apps = multiple instances
- Can have different scopes

**Singleton Pattern:**
- One instance per JVM
- Private constructor
- Static getInstance() method

---

## 🎓 Summary

### What You Learned

✅ **Singleton beans** - One instance per application
✅ **@Bean annotation** - Creates singleton beans
✅ **DataSource** - Connection pool singleton
✅ **HikariCP** - Fast connection pooling
✅ **Dependency Injection** - How Spring wires beans
✅ **Bean scopes** - singleton, prototype, request, session
✅ **Thread safety** - Singletons must be stateless/thread-safe
✅ **Bean lifecycle** - Creation, injection, destruction

### Files Created

1. **DatabaseConfig.java** - Custom DataSource configuration bean
2. **DatabaseHealthCheck.java** - Utility bean using DataSource
3. **This documentation** - Understanding singleton beans

### Remember

- Spring Boot auto-creates DataSource from `application.yml`
- Custom config is optional (only if you need fine control)
- All beans are singletons by default
- Singletons are shared across the entire application
- Connection pooling is your friend (fast and efficient!)

---

**Happy Learning! 🚀**
