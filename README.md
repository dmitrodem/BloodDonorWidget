# Blood Donor Status Widget

A simple Android widget that displays blood donation urgency status for different blood types. The widget fetches a real-time image from a blood donation center, analyzes specific regions to determine current need levels, and displays color-coded status indicators on the home screen.

## Features

- **1x1 Home Screen Widget**: Compact widget showing blood type and urgency status
- **Real-time Status Updates**: Fetches current donation status image every 30 minutes
- **Color-coded Status**:
  - 🔴 **Red Circle**: Critical need (urgent donation required)
  - 🟡 **Yellow Circle**: Moderate need (donations welcome)
  - 🟢 **Green Circle**: No urgent need (sufficient supply)
- **Manual Refresh**: Tap widget to force immediate update
- **Last Update Date**: Shows date of last status check (format: "03 Feb")
- **Blood Type Selection**: Configurable through main activity

## How It Works

1. **Image Fetching**: Downloads 900×450 JPEG image from `https://fnkc.ru/donor_tl2.jsp`
2. **ROI Analysis**: Analyzes specific regions of interest (ROI) based on selected blood type
3. **Color Classification**: Calculates average RGB color in ROI and classifies as red/yellow/green
4. **Widget Update**: Updates home screen widget with colored status indicator

## ROI Coordinates

The widget analyzes specific regions on the 900×450 image. Each blood type has predefined ROI coordinates:

| Blood Type | ROI Coordinates (x1, y1, x2, y2) | Region Size |
|------------|-----------------------------------|-------------|
| **O+**     | `50, 160, 135, 195`               | 85×35 pixels |
| **O-**     | `150, 160, 235, 195`              | 85×35 pixels |
| **A+**     | `250, 160, 340, 195`              | 90×35 pixels |
| **A-**     | `350, 160, 440, 195`              | 90×35 pixels |
| **B+**     | `455, 160, 540, 195`              | 85×35 pixels |
| **B-**     | `555, 160, 645, 195`              | 90×35 pixels |
| **AB+**    | `660, 160, 750, 195`              | 90×35 pixels |
| **AB-**    | `760, 160, 850, 195`              | 90×35 pixels |

*Note: Coordinates are in format (top-left x, top-left y, bottom-right x, bottom-right y)*

## Color Classification Logic

The app uses RGB analysis to determine status:

1. **Red (Critical)**:
   - Red ratio > 50% of total RGB
   - Red > Green × 1.5 AND Red > Blue × 1.5

2. **Green (No Need)**:
   - Green ratio > 40% of total RGB
   - Green > Red × 1.2 AND Green > Blue × 1.2

3. **Yellow (Need Some)**:
   - Red > 150 AND Green > 150 AND Blue < 100

4. **Default**: Green (if no other conditions match)

## Technical Details

- **Language**: Java
- **Minimum SDK**: API 16 (Android 4.1)
- **Target SDK**: API 30 (Android 11)
- **Build System**: Gradle
- **Dependencies**: None (no androidx/jetpack - pure Android framework)

### Key Components

1. **`BloodDonorWidget.java`** - AppWidgetProvider with image analysis logic
2. **`MainActivity.java`** - Blood type selection interface
3. **`blood_donor_widget.xml`** - Widget layout with Unicode circle indicator
4. **`activity_main.xml`** - Blood type selection UI

## Project Structure

```
blood_donor_widget/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/blooddonorwidget/
│   │   │   ├── BloodDonorWidget.java    # Widget provider with analysis logic
│   │   │   └── MainActivity.java        # Blood type selection activity
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── blood_donor_widget.xml  # Widget layout
│   │   │   │   └── activity_main.xml       # Main activity layout
│   │   │   ├── xml/
│   │   │   │   └── blood_donor_widget_info.xml  # Widget configuration
│   │   │   └── values/
│   │   │       ├── strings.xml           # String resources
│   │   └── AndroidManifest.xml          # App permissions and components
│   └── build.gradle                     # App build configuration
├── build.gradle                         # Top-level build configuration
└── settings.gradle                      # Project settings
```

## Building & Installation

### Prerequisites
- Android SDK
- Java 8 or higher
- Gradle

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean
```

### Installation
1. Build the APK: `./gradlew assembleDebug`
2. Install on device: `adb install app/build/outputs/apk/debug/app-debug.apk`
3. Add widget to home screen:
   - Long-press home screen → Widgets
   - Find "Blood Donor Widget"
   - Drag to desired location

## Configuration

### Setting Blood Type
1. Open the app from app drawer
2. Select blood type from list (ordered: O+, O-, A+, A-, B+, B-, AB+, AB-)
3. Click "Save"
4. Widget automatically updates with selected blood type

### Widget Behavior
- **Automatic Updates**: Every 30 minutes (configurable in `blood_donor_widget_info.xml`)
- **Manual Updates**: Tap widget to force immediate refresh
- **Status Display**: Colored circle + blood type + update date
- **Error Handling**: Gray indicator with error message on network/image issues

## Customization

### Adjusting ROI Coordinates
Edit `getRoiCoordinates()` method in `BloodDonorWidget.java`:
```java
case "A+": return new int[]{250, 160, 340, 195}; // x1, y1, x2, y2
```

### Modifying Color Thresholds
Edit `classifyColor()` method in `BloodDonorWidget.java`:
```java
// Adjust RGB thresholds for different color detection
if (redRatio > 0.5 && red > green * 1.5 && red > blue * 1.5) {
    return Color.RED;
}
```

### Changing Update Frequency
Edit `blood_donor_widget_info.xml`:
```xml
android:updatePeriodMillis="1800000" <!-- 30 minutes in milliseconds -->
```

## Security Note

⚠️ **SSL Certificate Bypass**: This app disables SSL certificate validation to connect to servers with invalid certificates. This is for development/testing only. In production:
- Use a server with valid SSL certificate
- Or implement proper certificate pinning
- Or remove the SSL bypass code in `analyzeImageColor()` method

## Troubleshooting

### Common Issues

1. **"Widget Error" displayed**:
   - Check internet connection
   - Verify server URL is accessible
   - Ensure image format is 900×450 JPEG

2. **Wrong color detection**:
   - Adjust ROI coordinates for your specific image layout
   - Modify color thresholds in `classifyColor()` method
   - Test with sample images to calibrate

3. **Widget not updating**:
   - Check `updatePeriodMillis` value
   - Verify widget is not in battery optimization restricted mode
   - Test manual update by tapping widget

### Debugging
- Check Logcat for error messages
- Test image download with browser: `https://fnkc.ru/donor_tl2.jsp`
- Verify ROI coordinates match actual image layout

## License

This project is provided as-is for educational and demonstration purposes. Modify and use according to your needs.

## Acknowledgments

- Blood donation status image provided by `fnkc.ru`
- Unicode circle character (●) for status indicator
- Simple, dependency-free Android implementation
