# Travel Diary

A feature-rich Android application for capturing, organizing, and sharing your travel memories. Built with Kotlin and Material Design, Travel Diary enables users to create beautiful travel journals with photos, videos, locations, and detailed notes all in one place.

## 📱 Overview

Travel Diary is a personal travel journaling app that transforms the way users document their journeys. Whether you're planning your next adventure, recording memories during a trip, or reminiscing about past travels, this app provides an intuitive and visually appealing platform to preserve your travel stories.

## ✨ Features

### Core Features

- **📝 Create Travel Journals**
  - Add trip title, location, and date
  - Write detailed travel descriptions
  - Upload cover photos for each trip
  - Make journals public or keep them private

- **📸 Media Management**
  - Add photos from gallery
  - Capture photos with camera
  - Record and upload videos
  - Organize media within trips

- **🗺️ Location Tracking**
  - Search and add destinations
  - API integration for location discovery
  - Track multiple locations per trip

- **🔒 Privacy Control**
  - Toggle between public and private journals
  - Public journals appear in the Explore feed
  - Private journals remain visible only to you

### Home Screen
- Personal dashboard with statistics (Trips, Photos, Places count)
- Search functionality to find specific trips
- Trip timeline view showing recent journeys
- Quick access to all trips

### Explore Section
- Discover public travel journals from other users worldwide
- Filter by categories: All, Public, Trending, Recent
- View global travel statistics (3k+ trips, 50.5k+ photos, 3.5k+ places)
- Community-driven travel inspiration

### Profile Management
- View personalized profile with statistics
- Edit profile information
- Change password
- Manage notification preferences
- Configure location services
- Storage & media management
- Help & FAQ section
- App information

### Additional Features
- **Notifications** - Get updates on likes, comments, and followers
- **Authentication** - Secure login and sign-up with email or social accounts (Facebook, Google)
- **Forgot Password** - Password recovery functionality
- **Responsive UI** - Beautiful Material Design with smooth animations
- **Bottom Navigation** - Easy access to Home, Explore, Notifications, and Profile

## 🏗️ Architecture

### Tech Stack

- **Language**: Kotlin (100%)
- **UI Framework**: Android Material Design 3
- **Layout Engine**: ConstraintLayout, LinearLayout, ScrollView
- **Components**: Material Cards, EditText, ImageView, Chips
- **Build System**: Gradle Kotlin DSL
- **Target Android**: API 30+ (Min SDK: 30, Target SDK: 36)
- **Compose Support**: Prepared for future Jetpack Compose integration

### Project Structure

```
app/
├── src/main/
│   ├── java/com/example/traveldiary/
│   │   ├── MainActivity.kt              # Entry point
│   │   └── ui/theme/
│   │       ├── Color.kt                 # Color definitions
│   │       ├── Theme.kt                 # Theme configuration
│   │       └── Type.kt                  # Typography settings
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_addtrip.xml     # New trip creation screen
│   │   │   ├── activity_home.xml        # Home/Dashboard
│   │   │   ├── activity_explore.xml     # Explore public journals
│   │   │   ├── activity_profile.xml     # User profile page
│   │   │   ├── activity_login.xml       # Login/Sign-up
│   │   │   ├── activity_signup.xml      # Registration
│   │   │   ├── activity_splash.xml      # Splash screen
│   │   │   ├── activity_noti.xml        # Notifications
│   │   │   ├── activity_forgot.xml      # Password recovery
│   │   │   ├── trip_card.xml            # Trip card component
│   │   │   ├── item_explore_card.xml    # Explore card component
│   │   │   └── activity_noti_unread.xml # Unread notifications
│   │   ├── values/
│   │   │   ├── strings.xml              # App strings and labels
│   │   │   ├── colors.xml               # Color palette
│   │   │   ├── themes.xml               # Theme styles
│   │   │   └── ic_image_replacer_background.xml
│   │   ├── drawable/                    # Icon assets
│   │   └── mipmap/                      # App icons (various densities)
│   └── AndroidManifest.xml              # App configuration
├── build.gradle.kts                     # App-level dependencies
└── proguard-rules.pro                   # Obfuscation rules
```

### Key Screens & Layouts

1. **Splash Screen** (`activity_splash.xml`)
   - Welcome screen with app branding
   - "Capture Your Journey" tagline

2. **Login Screen** (`activity_login.xml`)
   - Email and password input
   - Social login (Facebook, Google)
   - "Remember Me" option
   - Forgot password link
   - Sign-up redirection

3. **Home/Dashboard** (`activity_home.xml`)
   - Header with welcome message
   - Search bar for trip discovery
   - Statistics cards (Trips, Photos, Places)
   - Trip timeline with recent entries
   - Bottom navigation bar

4. **New Trip Screen** (`activity_addtrip.xml`)
   - Cover photo upload
   - Trip title input
   - Location/destination field
   - Start date picker
   - Description text area
   - Media options (Photos, Camera, Video)
   - Privacy toggle (Public/Private)
   - Save button

5. **Explore Screen** (`activity_explore.xml`)
   - Filter chips (All, Public, Trending, Recent)
   - Global statistics dashboard
   - Public journal cards
   - Search functionality

6. **Profile Screen** (`activity_profile.xml`)
   - User avatar with edit option
   - User statistics
   - Settings sections:
     - Edit Profile
     - Change Password
     - Notifications
     - Location Services
     - Storage & Media
     - Help & FAQ
     - About

## 🎨 Design

- **Color Scheme**
  - Primary Blue: Used throughout navigation and accent elements
  - Supporting Colors: Green (#0F9D58) for photos, Purple (#9C27B0) for places
  - Neutral grays for secondary content

- **Typography**
  - Bold headings for section titles
  - Regular text for content
  - Smaller text for metadata

- **Components**
  - Material Cards for content containers
  - Material Buttons for actions
  - Material Chips for filtering
  - Rounded corners throughout for modern look
  - Consistent padding and spacing

## 🚀 Getting Started

### Prerequisites

- Android Studio (Jellyfish or later recommended)
- JDK 11 or higher
- Android SDK API 30+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/mAbdullah02-bit/Travel_Diary.git
   cd Travel_Diary
   ```

2. **Open in Android Studio**
   - File → Open → Select the Travel_Diary directory
   - Let Android Studio sync the Gradle files

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click "Run" or use: `./gradlew installDebug`

## 📦 Dependencies

The project uses Gradle with version catalogs for dependency management:

- **AndroidX Core**: `androidx.core.ktx`
- **Android AppCompat**: `androidx.appcompat`
- **Lifecycle Runtime**: `androidx.lifecycle.runtime.ktx`
- **Jetpack Compose**: `androidx.activity.compose`
- **Material Components**: `androidx.compose.material3`, `material`
- **ConstraintLayout**: `androidx.constraintlayout`

### Testing Dependencies
- **JUnit**: For unit testing
- **Espresso**: For UI testing
- **Compose Test**: For Compose UI testing

## 🔧 Build Configuration

- **Min SDK**: 30
- **Target SDK**: 36
- **Java Compatibility**: Java 11
- **Build Features**: Compose enabled for future development

## 📊 Application Statistics

- **Total Trips**: 3 (user's personal library)
- **Total Photos**: 8+
- **Total Places**: 9+
- **Global Content**: 3k+ public trips, 50.5k+ photos, 3.5k+ places

## 🎯 Future Enhancements

Based on the prepared infrastructure, planned features include:

- Jetpack Compose full migration
- Real-time Firebase integration for data sync
- Advanced map integration for route tracking
- Social features (comments, likes, sharing)
- Trip itinerary planning
- Offline mode support
- Photo filters and editing tools
- Multi-language support

## 📝 Project Status

**Current Branch**: Prototype  
**Latest Update**: May 5, 2026  
**Status**: Active Development

## 📄 License

This project is open source. Please refer to the LICENSE file for more details.

## 🤝 Contributing

Contributions are welcome! Please feel free to:
- Report bugs
- Suggest features
- Submit pull requests

## 👤 Author

**Muhammad Abdullah**  
GitHub: [@mAbdullah02-bit](https://github.com/mAbdullah02-bit)

## 📞 Support

For issues, questions, or suggestions, please open an issue on the GitHub repository.

---

**Made with ❤️ for travel enthusiasts**
