# UPI Scam Shield - QR & Link Safety Checker

A production-ready Android application built with Kotlin and Jetpack Compose for scanning UPI QR codes and detecting potential scams.

## Features

### Core Features
- **QR Code Scanner**: Uses CameraX and ML Kit for fast and accurate barcode scanning
- **Risk Analysis Engine**: Advanced algorithm to detect suspicious UPI transactions
- **Scan History**: Complete history of all scanned QR codes with Room database
- **PDF Export**: Export scan reports with iText library
- **App Lock**: Biometric authentication to secure the app

### Premium Features (₹499/year)
- Unlimited QR code scans
- Advanced scam detection
- PDF report export
- Complete scan history
- App lock protection
- Priority support

## Technical Stack

### Architecture
- **MVVM Architecture**: Clean separation of concerns
- **Jetpack Compose**: Modern declarative UI
- **Repository Pattern**: Data layer abstraction
- **ViewModel**: Lifecycle-aware state management

### Key Technologies
- **Kotlin**: Primary language
- **Jetpack Compose**: UI framework
- **CameraX**: Camera integration
- **ML Kit**: Barcode scanning
- **Room**: Local database
- **DataStore**: Preferences storage
- **Google Play Billing v6**: Subscription management
- **Biometric API**: App lock authentication
- **iText PDF**: PDF generation
- **Coroutines & Flow**: Asynchronous operations

## Project Structure

```
app/
├── src/main/
│   ├── java/com/upiscamshield/
│   │   ├── data/
│   │   │   ├── database/      # Room database components
│   │   │   ├── model/         # Data models
│   │   │   └── repository/    # Repository implementations
│   │   ├── ui/
│   │   │   ├── screens/       # Compose screens
│   │   │   └── theme/         # Material3 theme
│   │   ├── utils/             # Utility classes
│   │   ├── viewmodel/         # ViewModels
│   │   ├── MainActivity.kt    # Main activity
│   │   └── UpiScamShieldApp.kt # Application class
│   ├── res/                   # Resources (strings, colors, etc.)
│   └── AndroidManifest.xml    # App manifest
└── build.gradle.kts           # Module build configuration
```

## Building the Project

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK API 34
- Gradle 8.2

### Build Instructions

1. Clone the repository:
```bash
git clone https://github.com/zaheerabbas7892034214-ai/UPI-Scam-Shield-QR-Link-Safety-Checker.git
cd UPI-Scam-Shield-QR-Link-Safety-Checker
```

2. Open in Android Studio

3. Sync Gradle files

4. Run the app:
   - Select a device/emulator
   - Click Run or press Shift+F10

### Build from Command Line
```bash
./gradlew assembleDebug
```

## Risk Analysis Algorithm

The app analyzes UPI QR codes based on multiple factors:
- UPI ID format validation
- Merchant name verification
- Transaction note analysis
- Amount validation
- Known scam keyword detection
- Bank/PSP verification

Risk levels: SAFE, LOW, MEDIUM, HIGH, CRITICAL

## Permissions

- **CAMERA**: Required for QR code scanning
- **INTERNET**: Required for Google Play Billing
- **USE_BIOMETRIC**: Required for app lock feature
- **STORAGE**: Required for PDF export (Android 9 and below)

## Google Play Billing Setup

To enable subscriptions:
1. Create a product in Google Play Console: `premium_yearly`
2. Set price to ₹499/year
3. Add the app's base64 public key to the billing configuration

## License

Copyright © 2024. All rights reserved.
