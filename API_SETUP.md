# NewsAPI Configuration Guide

## Getting Your API Key

### Step 1: Register
1. Visit https://newsapi.org/register
2. Fill in the registration form:
   - Email address
   - Password
   - First name
   - Select "I'm an individual"
   
3. Click "Submit"
4. Check your email for verification

### Step 2: Get API Key
1. Log in to https://newsapi.org
2. Go to your account page
3. Copy your API key (it looks like: `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`)

## Configuring the App

### Method 1: Direct in build.gradle.kts (Recommended for testing)

Open `app/build.gradle.kts` and find this section:

```kotlin
productFlavors {
    create("bbc") {
        dimension = "source"
        applicationIdSuffix = ".bbc"
        buildConfigField("String", "NEWS_SOURCE", "\"bbc-news\"")
        buildConfigField("String", "NEWS_SOURCE_NAME", "\"BBC News\"")
        buildConfigField("String", "API_KEY", "\"YOUR_API_KEY_HERE\"")  // ← Replace this
    }
    create("cnn") {
        dimension = "source"
        applicationIdSuffix = ".cnn"
        buildConfigField("String", "NEWS_SOURCE", "\"cnn\"")
        buildConfigField("String", "NEWS_SOURCE_NAME", "\"CNN\"")
        buildConfigField("String", "API_KEY", "\"YOUR_API_KEY_HERE\"")  // ← Replace this
    }
}
```

Replace `YOUR_API_KEY_HERE` with your actual API key in both places.

**Example:**
```kotlin
buildConfigField("String", "API_KEY", "\"a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6\"")
```

### Method 2: Using local.properties (Recommended for production)

More secure - keeps API key out of version control.

1. Create/Edit `local.properties` in project root:
```properties
newsapi.key=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

2. Update `app/build.gradle.kts`:
```kotlin
// At the top of the file
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val apiKey = localProperties.getProperty("newsapi.key", "YOUR_API_KEY_HERE")

// In productFlavors section
create("bbc") {
    // ...
    buildConfigField("String", "API_KEY", "\"$apiKey\"")
}
```

3. Ensure `local.properties` is in `.gitignore` ✓ (already done)

## Testing API Configuration

### Quick Test
1. Open the app
2. Grant biometric permission (if prompted)
3. You should see headlines loading

### If Headlines Don't Load

**Check 1: API Key Format**
- Must be wrapped in quotes: `"key"`
- No extra spaces
- No special characters

**Check 2: Rebuild Project**
```bash
Build → Clean Project
Build → Rebuild Project
```

**Check 3: Check Logs**
In Android Studio Logcat, filter by "NewsApp" to see:
- API requests
- Error messages
- Response codes

### Common Issues

#### Issue: "401 Unauthorized"
**Solution**: API key is incorrect
- Verify key from newsapi.org
- Check for typos
- Ensure key is active

#### Issue: "429 Too Many Requests"
**Solution**: Rate limit exceeded
- Free tier: 100 requests/day
- Wait or upgrade account
- Use cached data

#### Issue: "No articles loading"
**Solution**: Multiple possibilities
1. Check internet connection
2. Verify API key
3. Try pull-to-refresh
4. Check if source is valid

## API Endpoints Used

### Top Headlines
```
GET https://newsapi.org/v2/top-headlines
```

**Parameters:**
- `sources`: News source ID (e.g., "bbc-news", "cnn")
- `apiKey`: Your API key

**Example Request:**
```
https://newsapi.org/v2/top-headlines?sources=bbc-news&apiKey=YOUR_KEY
```

## Supported News Sources

### Pre-configured
- **bbc-news**: BBC News (UK)
- **cnn**: CNN (US)

### Adding More Sources

Find source IDs at: https://newsapi.org/sources

Popular sources:
- `techcrunch` - TechCrunch
- `the-verge` - The Verge
- `google-news` - Google News
- `al-jazeera-english` - Al Jazeera
- `bloomberg` - Bloomberg
- `reuters` - Reuters
- `the-wall-street-journal` - WSJ

**To add:**
1. Open `app/build.gradle.kts`
2. Add new flavor:
```kotlin
create("techcrunch") {
    dimension = "source"
    applicationIdSuffix = ".techcrunch"
    buildConfigField("String", "NEWS_SOURCE", "\"techcrunch\"")
    buildConfigField("String", "NEWS_SOURCE_NAME", "\"TechCrunch\"")
    buildConfigField("String", "API_KEY", "\"$apiKey\"")
}
```
3. Sync Gradle
4. Select new build variant

## Rate Limits

### Free Tier
- 100 requests per day
- 1 request per second
- Development use only

### Paid Tiers
Visit https://newsapi.org/pricing for:
- Higher request limits
- Commercial use
- Support

## Testing Tips

### Use Cached Data
- App caches for 1 hour
- Saves API calls
- Works offline

### Monitor Usage
Check usage at: https://newsapi.org/account

### Development Best Practices
1. Use local.properties for API key
2. Don't commit API key to git
3. Monitor rate limits
4. Use caching effectively

## Troubleshooting Steps

1. **Verify API Key**
   - Log in to newsapi.org
   - Copy fresh key
   - Update build.gradle.kts

2. **Clean & Rebuild**
   - File → Invalidate Caches
   - Build → Clean Project
   - Build → Rebuild Project

3. **Check Internet**
   - Disable VPN
   - Use WiFi (not mobile data for testing)
   - Check firewall

4. **Test in Browser**
   ```
   https://newsapi.org/v2/top-headlines?sources=bbc-news&apiKey=YOUR_KEY
   ```
   If this works, app should work

5. **Check Logs**
   - Android Studio → Logcat
   - Filter: "NewsApp"
   - Look for error messages

## Security Notes

⚠️ **Important:**
- Never commit API key to public repository
- Use local.properties for keys
- Don't share your API key
- Rotate key if exposed

✅ **This project includes:**
- local.properties in .gitignore
- BuildConfig for secure access
- No hardcoded keys in source

## Support

### NewsAPI Help
- Docs: https://newsapi.org/docs
- FAQ: https://newsapi.org/faq
- Support: support@newsapi.org

### App Issues
1. Check this guide
2. Review README.md
3. Check SETUP_GUIDE.md
4. Review code comments

---

**Quick Start Checklist:**
- [ ] Register at newsapi.org
- [ ] Copy API key
- [ ] Update build.gradle.kts
- [ ] Sync Gradle
- [ ] Build & Run
- [ ] Test with pull-to-refresh

Happy Coding! 🚀
