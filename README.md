
# Focus App

Focus is a simple and professional Flutter application designed to help users stay focused by blocking distracting social media apps while studying.

## Features

- **Study Mode Toggle:** Easily enable or disable Study Mode with a clear switch on the main screen.
- **App Blocking (Android):** When Study Mode is ON, popular social media apps like WhatsApp, Instagram, and YouTube are blocked. The app uses an overlay to prevent access to these apps until Study Mode is turned OFF.
- **Permissions:** The app guides users to grant necessary permissions for usage access and overlay display, ensuring smooth operation.
- **User-Friendly UI:** The interface is clean and easy to use, making it simple to control Study Mode.

## How It Works

1. Open the Focus app.
2. Grant the required permissions when prompted (Usage Access and Overlay Permission).
3. Toggle Study Mode ON to block WhatsApp, Instagram, and YouTube.
4. When you try to open a blocked app, an overlay will appear, preventing you from using it.
5. Toggle Study Mode OFF to remove all restrictions and use your apps normally.

## Platform Support

- The blocking feature is implemented for Android devices.
- The app is built with Flutter and can run on iOS, Windows, macOS, Linux, and Web, but blocking functionality is only available on Android.

## Technologies Used

- Flutter (cross-platform UI)
- Android native code (Kotlin) for app blocking and overlay
- Platform Channels for communication between Flutter and Android

## Purpose

This app is ideal for students and anyone who wants to minimize distractions from social media while studying or working. It provides a simple way to stay focused and productive.

---
For any questions or improvements, feel free to reach out or contribute!
