# Quick Setup Guide

## Prerequisites
- Android Studio Ladybug (2024.2.1) or later
- JDK 17+
- Android SDK API Level 35

## Setup Steps

### 1. Get NewsAPI Key
1. Go to https://newsapi.org/register
2. Create a free account
3. Copy your API key

### 2. Configure the Project
1. Open `app/build.gradle.kts`
2. Find the `productFlavors` section
3. Replace `YOUR_API_KEY_HERE` with your actual API key in both flavors:

```kotlin
buildConfigField("String", "API_KEY", "\"your_actual_key_here\"")
```

### 3. Build & Run
1. Open project in Android Studio
2. Wait for Gradle sync
3. Select build variant: `bbcDebug` or `cnnDebug`
4. Click Run (▶️) or press Shift+F10

## Testing Biometric Authentication

### On Real Device
- Ensure fingerprint is enrolled in device settings
- App will prompt for fingerprint on launch

### On Emulator
1. Open Extended Controls (...)
2. Go to Fingerprint section
3. Enroll a fingerprint
4. Test authentication when app launches

## Build Variants

| Variant | News Source | Purpose |
|---------|-------------|---------|
| bbcDebug | BBC News | Development with BBC |
| bbcRelease | BBC News | Production with BBC |
| cnnDebug | CNN | Development with CNN |
| cnnRelease | CNN | Production with CNN |

## Running Tests

```bash
# All tests
./gradlew test

# Specific flavor
./gradlew testBbcDebugUnitTest

# With coverage
./gradlew testBbcDebugUnitTestCoverage
```

## Troubleshooting

### Gradle Sync Fails
- Check internet connection
- Update Android Studio
- Invalidate Caches: File → Invalidate Caches / Restart

### Build Error: API Key
- Ensure API key is properly set in build.gradle.kts
- API key must be in quotes
- Rebuild project

### Biometric Not Working
- Check device has fingerprint sensor
- Ensure fingerprint is enrolled
- Grant biometric permission in settings

### No Articles Loading
- Verify API key is correct
- Check internet connection
- Try pull-to-refresh
- Check NewsAPI rate limits (100 requests/day for free tier)

## Key Features to Test

1. **Headlines List**
   - ✓ Articles load on app start
   - ✓ Images display correctly
   - ✓ Pull to refresh works
   - ✓ Scroll through list

2. **Article Detail**
   - ✓ Tap article to view details
   - ✓ Back button returns to list
   - ✓ All content displays

3. **Biometric Auth**
   - ✓ Prompt appears on launch (if enrolled)
   - ✓ App opens after successful auth
   - ✓ Cancel closes app

4. **Offline Mode**
   - ✓ Enable airplane mode
   - ✓ View cached articles
   - ✓ See "offline" message

5. **Orientation**
   - ✓ Rotate device
   - ✓ UI adjusts properly
   - ✓ No crashes
   - ✓ State preserved

## Support

For issues or questions:
1. Check README.md for detailed documentation
2. Review code comments
3. Check test files for examples

---

Happy Testing! 🚀
