# News App - Android Technical Assessment

A modern Android news application built with Kotlin, MVVM architecture, Jetpack Compose, and the latest Android technologies. The app displays top headlines from BBC News using the NewsAPI.org service.

## 📋 Table of Contents
- [Features](#features)
- [Technical Stack](#technical-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
- [Product Flavors](#product-flavors)
- [Testing](#testing)
- [Design Decisions](#design-decisions)


## 📄 License

This project is created for technical assessment purpose with Critical Techworks.


## ✨ Features

### Story 1: Headlines List
- ✅ Display news provider name as screen title
- ✅ Headlines presented in list format
- ✅ Each cell shows headline title
- ✅ Headlines sorted by date (newest first)
- ✅ Scrollable list of headlines
- ✅ Headline images with caching (Coil library)

### Story 2: Article Details
- ✅ Tapping headline opens detail screen
- ✅ Display article image, title, description, and content
- ✅ Smooth navigation between screens

### Bonus Story 3: Biometric Authentication
- ✅ Fingerprint authentication on app launch (if available)
- ✅ Graceful fallback when biometric is not configured
- ✅ Uses BiometricPrompt API

### Bonus Story 4: Multiple Flavors
- ✅ BBC News flavor (default)
- ✅ CNN flavor for alternative news source
- ✅ Easy to add more flavors

### Additional Features
- ✅ Pull-to-refresh functionality
- ✅ Offline caching with Room Database
- ✅ Error handling with user-friendly messages
- ✅ Loading states with shimmer effects
- ✅ Material 3 Design
- ✅ Dark/Light theme support
- ✅ Portrait and landscape orientation support
- ✅ Responsive design for different screen sizes

## 🛠 Technical Stack

### Core Technologies
- **Language**: Kotlin 2.0.20
- **UI Framework**: Jetpack Compose (BOM 2024.09.02)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt/Dagger 2.51.1
- **Async Programming**: Kotlin Coroutines & Flow
- **Navigation**: Navigation Compose 2.8.1

### Networking & Data
- **HTTP Client**: Retrofit 2.11.0 + OkHttp 4.12.0
- **JSON Parsing**: Gson
- **Image Loading**: Coil 2.7.0
- **Local Database**: Room 2.6.1

### Testing
- **Unit Testing**: JUnit 4
- **Mocking**: MockK 1.13.12
- **Flow Testing**: Turbine 1.1.0
- **Assertions**: Google Truth 1.4.4
- **Architecture Testing**: AndroidX Core Testing 2.2.0

### Other
- **Biometric Auth**: AndroidX Biometric 1.2.0-alpha05
- **Build Tools**: Android Gradle Plugin 8.5.2
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35

## 🏗 Architecture

The app follows **Clean Architecture** principles with **MVVM** pattern:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Compose UI, ViewModels, Navigation)   │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│          Domain Layer                   │
│     (Use Cases, Domain Models)          │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│           Data Layer                    │
│  (Repository, API, Database, DTOs)      │
└─────────────────────────────────────────┘
```

### Key Components

1. **Presentation Layer**
   - `HeadlinesScreen`: Displays list of articles
   - `ArticleDetailScreen`: Shows full article details
   - `HeadlinesViewModel`: Manages UI state and business logic
   - `BiometricAuthManager`: Handles fingerprint authentication

2. **Domain Layer**
   - `Article`, `Source`: Domain models
   - `GetTopHeadlinesUseCase`: Business logic for fetching headlines
   - `NewsRepository`: Interface for data operations

3. **Data Layer**
   - `NewsRepositoryImpl`: Implements repository with caching logic
   - `NewsApiService`: Retrofit API interface
   - `NewsDatabase`: Room database for offline caching
   - `ArticleDao`: Database access object
   - Mappers: Convert between DTOs, Entities, and Domain models

## 📁 Project Structure

```
app/src/main/java/com/newsapp/
├── core/
│   └── util/
│       └── Resource.kt              # Sealed class for API states
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── ArticleDao.kt        # Room DAO
│   │   ├── entity/
│   │   │   └── ArticleEntity.kt     # Database entities
│   │   └── NewsDatabase.kt          # Room database
│   ├── mapper/
│   │   └── ArticleMapper.kt         # DTO/Entity/Domain mappers
│   ├── remote/
│   │   ├── dto/
│   │   │   └── NewsResponse.kt      # API response models
│   │   └── NewsApiService.kt        # Retrofit service
│   └── repository/
│       └── NewsRepositoryImpl.kt    # Repository implementation
├── di/
│   └── AppModule.kt                 # Hilt dependency injection
├── domain/
│   ├── model/
│   │   └── Article.kt               # Domain models
│   ├── repository/
│   │   └── NewsRepository.kt        # Repository interface
│   └── usecase/
│       └── GetTopHeadlinesUseCase.kt
├── presentation/
│   ├── auth/
│   │   └── BiometricAuthManager.kt  # Biometric authentication
│   ├── detail/
│   │   └── ArticleDetailScreen.kt   # Article detail UI
│   ├── headlines/
│   │   ├── HeadlinesScreen.kt       # Headlines list UI
│   │   └── HeadlinesViewModel.kt    # ViewModel
│   ├── navigation/
│   │   └── NewsNavGraph.kt          # Navigation setup
│   ├── theme/
│   │   ├── Color.kt                 # Material 3 colors
│   │   ├── Theme.kt                 # App theme
│   │   └── Type.kt                  # Typography
│   └── MainActivity.kt              # Main entry point
└── NewsApplication.kt               # Application class

app/src/test/java/com/newsapp/
├── data/repository/
│   └── NewsRepositoryImplTest.kt    # Repository tests
├── domain/usecase/
│   └── GetTopHeadlinesUseCaseTest.kt
└── presentation/headlines/
    └── HeadlinesViewModelTest.kt    # ViewModel tests
```

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Ladybug | 2024.2.1 or later
- JDK 17 or higher
- Android SDK with API level 35

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd NewsApp
   ```

2. **NewsAPI Key**
   - I already exposed the API key so you can easily cloe and run the app with the APIKey available
   - The "cnn" flavour was just added for the sake of creating a flavour as a Bonus story
   
3. **Configure API Key**
   
   Open `app/build.gradle.kts` and replace `YOUR_API_KEY_HERE` with your actual API key:
   
   ```kotlin
   productFlavors {
       create("bbc") {
           // ...
           buildConfigField("String", "API_KEY", "\"YOUR_ACTUAL_API_KEY\"")
       }
       create("cnn") {
           // ...
           buildConfigField("String", "API_KEY", "\"YOUR_ACTUAL_API_KEY\"")
       }
   }
   ```

4. **Sync Gradle**
   - Open the project in Android Studio
   - Wait for Gradle sync to complete
   - There shouldn't be any dependency issues if cloned with the latest versions necessary with regards to Gradle

5. **Build and Run**
   - Select build variant: `bbcDebug` or `cnnDebug`
   - Click Run or use the run button
   - Choose your device/emulator

### Build Variants

The app has the following build variants:

- **bbcDebug**: BBC News source in debug mode
- **bbcRelease**: BBC News source in release mode
- **cnnDebug**: CNN source in debug mode
- **cnnRelease**: CNN source in release mode

To switch variants:
1. Go to `Build` → `Select Build Variant`
2. Choose your desired variant from the dropdown

## 🎨 Product Flavors

### BBC News Flavor (Default)
```gradle
bbc {
    applicationIdSuffix = ".bbc"
    NEWS_SOURCE = "bbc-news"
    NEWS_SOURCE_NAME = "BBC News"
}
```

### CNN Flavor (Just for the sake of Flavour for another news source)
```gradle
cnn {
    applicationIdSuffix = ".cnn"
    NEWS_SOURCE = "cnn"
    NEWS_SOURCE_NAME = "CNN"
}
```

### Adding a New Flavor

To add a new news source:

1. Open `app/build.gradle.kts`
2. Add a new flavor in the `productFlavors` block:

```kotlin
create("theVerge") {
    dimension = "source"
    applicationIdSuffix = ".theverge"
    buildConfigField("String", "NEWS_SOURCE", "\"the-verge\"")
    buildConfigField("String", "NEWS_SOURCE_NAME", "\"The Verge\"")
    buildConfigField("String", "API_KEY", "\"YOUR_API_KEY\"")
}
```

3. Sync Gradle
4. Select the new build variant

## 🧪 Testing

### Running Tests

**Run all unit tests:**
```bash
./gradlew test
```

**Run tests for specific flavor:**
```bash
./gradlew testBbcDebugUnitTest
```

**Run with coverage:**
```bash
./gradlew testBbcDebugUnitTestCoverage
```

### Test Coverage

The project includes comprehensive unit tests for:

1. **Repository Layer** (`NewsRepositoryImplTest`)
   - API success scenarios
   - Error handling
   - Cache management
   - Sorting functionality

2. **Domain Layer** (`GetTopHeadlinesUseCaseTest`)
   - Use case execution
   - Parameter passing
   - State propagation

3. **Presentation Layer** (`HeadlinesViewModelTest`)
   - ViewModel state management
   - Loading states
   - Error handling
   - User interactions

### Test Technologies Used
- **MockK**: Mocking framework for Kotlin
- **Turbine**: Testing Kotlin Flows
- **Truth**: Fluent assertion library
- **Coroutines Test**: Testing coroutines

## 🎯 Design Decisions

### 1. **Clean Architecture with MVVM**
- Separation of concerns for better testability
- Easy to maintain and scale
- Clear dependency flow (Presentation → Domain → Data)

### 2. **Jetpack Compose**
- Modern declarative UI
- Less boilerplate than XML
- Better state management
- Easier to create responsive layouts

### 3. **Offline-First with Room**
- App works without internet
- Better user experience
- Cache expiration (1 hour) for fresh content
- Old cache cleanup (7 days)

### 4. **Hilt Dependency Injection**
- Reduced boilerplate
- Compile-time verification
- Better testing support
- AndroidX integration

### 5. **Kotlin Flow for Reactive Programming**
- Type-safe reactive streams
- Lifecycle-aware
- Easy to test
- Better than LiveData for this use case

### 6. **Coil for Image Loading**
- Kotlin-first library
- Jetpack Compose support
- Built-in caching
- Memory efficient

### 7. **Material 3 Design**
- Modern UI components
- Dynamic theming
- Better accessibility
- Consistent user experience

### 8. **Biometric Authentication**
- Enhanced security
- Better UX than PIN/password
- Graceful fallback
- Platform-standard implementation

### 9. **Product Flavors**
- Easy to manage multiple sources
- Separate app IDs for parallel installation
- Build-time configuration
- No runtime overhead

## 📱 Screen Orientations

The app supports both **portrait** and **landscape** orientations:

- Automatic layout adjustments
- State preservation during rotation
- No crashes on configuration changes
- Optimized UI for different aspect ratios

## 🔒 Permissions

Required permissions in `AndroidManifest.xml`:
- `INTERNET`: For API calls
- `ACCESS_NETWORK_STATE`: For network status checking
- `USE_BIOMETRIC`: For fingerprint authentication

## 📦 Dependencies

All dependencies are managed in `app/build.gradle.kts` with version management in the root `build.gradle.kts`.

Key libraries:
- AndroidX Core KTX
- Compose BOM
- Material 3
- Navigation Compose
- Hilt
- Retrofit & OkHttp
- Room
- Coil
- Biometric
- Coroutines

## 🐛 Known Issues & Future Improvements

### Potential Improvements
1. Add pagination for large article lists
2. Implement search functionality
3. Add article bookmarking
4. Share article feature
5. More comprehensive error messages
6. Analytics integration
7. Crash reporting (Firebase Crashlytics)
8. WebView for reading full articles
9. Push notifications for breaking news
10. Widget support

---

**Built by Ezekiel using Kotlin and Jetpack Compose**
