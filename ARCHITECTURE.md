# Architecture Documentation

## Overview

This News App follows **Clean Architecture** principles with **MVVM (Model-View-ViewModel)** pattern, ensuring separation of concerns, testability, and maintainability.

## Architecture Layers

### 1. Presentation Layer

**Purpose**: Handle UI and user interactions

**Components**:
- **Composables**: Jetpack Compose UI components
  - `HeadlinesScreen`: Display list of articles
  - `ArticleDetailScreen`: Show article details
  - `BiometricAuthScreen`: Authentication UI
  
- **ViewModels**: Manage UI state and handle business logic
  - `HeadlinesViewModel`: Manages headlines state, loading, errors

- **Navigation**: Screen routing
  - `NewsNavGraph`: Navigation graph setup

**Key Technologies**:
- Jetpack Compose
- ViewModel
- StateFlow for state management
- Navigation Compose

### 2. Domain Layer

**Purpose**: Business logic and use cases (platform-independent)

**Components**:
- **Models**: Pure Kotlin data classes
  - `Article`: Represents a news article
  - `Source`: Represents news source

- **Use Cases**: Single responsibility operations
  - `GetTopHeadlinesUseCase`: Fetch top headlines

- **Repository Interfaces**: Define data contracts
  - `NewsRepository`: Data operations contract

**Key Principles**:
- No Android dependencies
- Pure Kotlin/Java
- Business rules
- Use case orchestration

### 3. Data Layer

**Purpose**: Handle data sources (API, Database, Cache)

**Components**:
- **Repository Implementation**
  - `NewsRepositoryImpl`: Implements repository with caching

- **Remote Data Source**
  - `NewsApiService`: Retrofit API interface
  - DTOs: `NewsResponse`, `ArticleDto`, `SourceDto`

- **Local Data Source**
  - `NewsDatabase`: Room database
  - `ArticleDao`: Database operations
  - Entities: `ArticleEntity`, `SourceEntity`

- **Mappers**
  - Convert between DTOs ↔ Entities ↔ Domain Models

**Key Technologies**:
- Retrofit + OkHttp
- Room Database
- Kotlin Coroutines + Flow

## Data Flow

### Fetching Headlines

```
User Action (Pull to Refresh)
         ↓
    HeadlinesScreen
         ↓
    HeadlinesViewModel
         ↓
 GetTopHeadlinesUseCase
         ↓
    NewsRepository (Interface)
         ↓
  NewsRepositoryImpl
         ↓
    ┌────────────────┐
    │  Check Cache   │
    └────────────────┘
           ↓
    Is Cache Valid?
       ↙        ↘
     Yes         No
      ↓           ↓
   Return    Fetch from API
   Cache          ↓
      ↓      NewsApiService
      ↓           ↓
      ↓      Parse Response
      ↓           ↓
      ↓      Save to Cache
      ↓           ↓
      └───────────┘
           ↓
    Map to Domain
         ↓
   Emit as Flow
         ↓
    ViewModel Updates State
         ↓
   UI Re-composes
```

## State Management

### Resource Pattern

```kotlin
sealed class Resource<T> {
    class Success<T>(data: T) : Resource<T>()
    class Error<T>(message: String, data: T?) : Resource<T>()
    class Loading<T>(data: T?) : Resource<T>()
}
```

**Benefits**:
- Type-safe state representation
- Handle all scenarios (loading, success, error)
- Can carry cached data during loading/error

### ViewModel State

```kotlin
data class HeadlinesState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sourceName: String = ""
)
```

**Flow**:
1. ViewModel exposes StateFlow<HeadlinesState>
2. Composables collect state
3. UI updates automatically on state change

## Dependency Injection

**Framework**: Hilt (Dagger)

**Modules**:
- `AppModule`: Provides app-level dependencies
  - Network (Retrofit, OkHttp)
  - Database (Room)
  - Repository

**Benefits**:
- Compile-time dependency resolution
- Automatic dependency graph generation
- Easy testing with mock dependencies
- Lifecycle-aware injection

## Caching Strategy

### Two-Level Cache

1. **Memory Cache** (Coil)
   - Images cached in RAM
   - Fast access
   - Automatic memory management

2. **Disk Cache** (Room)
   - Articles cached in SQLite
   - Survives app restart
   - Time-based expiration

### Cache Policy

```
Request Articles
      ↓
Check Local Cache
      ↓
Is Cache Fresh? (< 1 hour)
   ↙        ↘
 Yes         No
  ↓           ↓
Return    Fetch from Network
Cache          ↓
           Update Cache
              ↓
           Return Fresh Data
```

**Cache Expiration**:
- Articles: 1 hour
- Old cache cleanup: 7 days

## Error Handling

### Network Errors

```kotlin
try {
    val response = api.getTopHeadlines()
    emit(Resource.Success(response))
} catch (e: HttpException) {
    emit(Resource.Error("Network error", cachedData))
} catch (e: IOException) {
    emit(Resource.Error("No internet", cachedData))
} catch (e: Exception) {
    emit(Resource.Error("Unknown error", cachedData))
}
```

**Strategy**:
- Always try to return cached data on error
- User-friendly error messages
- Graceful degradation

## Testing Strategy

### Unit Tests

**Repository Tests**:
- Mock API and DAO
- Test success scenarios
- Test error handling
- Test cache logic

**ViewModel Tests**:
- Mock use case
- Test state transitions
- Test user actions
- Test error states

**Use Case Tests**:
- Mock repository
- Test flow emissions
- Test parameter passing

### Testing Tools

- **MockK**: Mocking framework
- **Turbine**: Flow testing
- **Truth**: Assertions
- **Coroutines Test**: Test dispatcher

## Security

### API Key Protection

```kotlin
// Build config (not in source control)
buildConfigField("String", "API_KEY", "\"${API_KEY}\"")

// Usage
api.getTopHeadlines(apiKey = BuildConfig.API_KEY)
```

### Biometric Authentication

```kotlin
// Check availability
BiometricManager.canAuthenticate(BIOMETRIC_STRONG)

// Authenticate
BiometricPrompt.authenticate(promptInfo)
```

**Features**:
- Device-level security
- Graceful fallback
- User cancellation handling

## Performance Optimizations

### Image Loading (Coil)

```kotlin
SubcomposeAsyncImage(
    model = imageUrl,
    loading = { CircularProgressIndicator() },
    error = { ErrorIcon() },
    contentScale = ContentScale.Crop
)
```

**Benefits**:
- Lazy loading
- Memory caching
- Disk caching
- Automatic request cancellation

### List Rendering

```kotlin
LazyColumn {
    items(articles, key = { it.url }) { article ->
        ArticleCard(article)
    }
}
```

**Benefits**:
- Only visible items rendered
- Automatic recycling
- Smooth scrolling
- Key-based diffing

### Database Optimization

```kotlin
@Query("SELECT * FROM articles WHERE sourceId = :sourceId 
        ORDER BY publishedAt DESC")
fun getArticlesBySource(sourceId: String): Flow<List<ArticleEntity>>
```

**Features**:
- Indexed queries
- Flow-based reactive queries
- Automatic background threading

## Multi-Module Potential

Current structure supports easy migration to multi-module:

```
:app (Application module)
├── :feature-headlines (Headlines feature)
├── :feature-detail (Article detail feature)
├── :core-ui (Shared UI components)
├── :core-domain (Domain layer)
├── :core-data (Data layer)
└── :core-network (Network layer)
```

## Scalability

### Adding New Features

1. **New Screen**: Create Composable + ViewModel
2. **New Data Source**: Add to Repository
3. **New Use Case**: Create in domain layer
4. **Navigation**: Update NavGraph

### Adding New News Source

1. Add product flavor in build.gradle
2. Configure source ID and name
3. Build variant automatically created

## Design Patterns Used

1. **Repository Pattern**: Abstract data sources
2. **Use Case Pattern**: Single responsibility
3. **Observer Pattern**: Flow for reactive updates
4. **Dependency Injection**: Hilt for DI
5. **Mapper Pattern**: DTO/Entity/Domain conversions
6. **State Pattern**: Resource sealed class

## Best Practices Implemented

✅ Single Responsibility Principle
✅ Dependency Inversion Principle
✅ Separation of Concerns
✅ Immutable State
✅ Reactive Programming
✅ Offline-First Architecture
✅ Error Handling
✅ Unit Testing
✅ Clean Code
✅ Documentation

## Future Enhancements

1. **Pagination**: Implement paging for large datasets
2. **Search**: Add article search functionality
3. **Bookmarks**: Save favorite articles
4. **Sharing**: Share articles to other apps
5. **Analytics**: Track user behavior
6. **Crash Reporting**: Monitor app stability
7. **Multi-module**: Split into feature modules
8. **CI/CD**: Automated testing and deployment

---

This architecture ensures:
- **Testability**: Easy to write unit tests
- **Maintainability**: Clear structure and separation
- **Scalability**: Easy to add features
- **Flexibility**: Easy to change implementations
- **Performance**: Optimized for smooth UX
