# My Tasks — Compose To-Do App

A native Android to-do list built with Jetpack Compose, Room, Hilt, and WorkManager.

## Features
- Pastel-card task list with custom rounded-tab shape
- Horizontal date picker with auto-filtered tasks
- Add / Edit task via Material 3 `ModalBottomSheet`
- Swipe-to-delete with `SwipeToDismissBox`
- Reminder notifications via `WorkManager`
- Coil-loaded overlapping avatars

## Build

This project is configured to build via **GitHub Actions** — you don't need
Android Studio locally. After you push to `main`, an APK is automatically
built and uploaded as a workflow artifact.

### One-time setup (before first push)

The Gradle wrapper files (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`)
are **not** in this repo yet — they need to be generated. On any computer
with Java 17 installed:

```bash
cd path/to/this/folder
curl -O https://services.gradle.org/distributions/gradle-8.7-bin.zip
unzip gradle-8.7-bin.zip
./gradle-8.7/bin/gradle wrapper --gradle-version 8.7
rm -rf gradle-8.7 gradle-8.7-bin.zip
```

This creates `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar`.
Commit all three. The workflow handles the rest.

### Run the build

After pushing the wrapper files to GitHub:

1. Open the **Actions** tab on GitHub.
2. The "Android CI Build" workflow runs automatically. (Or click **Run workflow**.)
3. When it succeeds, scroll to **Artifacts** at the bottom and download `app-debug`.
4. Unzip → install `app-debug.apk` on your phone.

## Project layout

```
TodoList/
├── .github/workflows/android_build.yml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/
        │   ├── drawable/ic_notification.xml
        │   └── values/{strings,themes}.xml
        └── java/com/example/
            ├── taskcard/TaskCard.kt
            └── todolist/
                ├── MainActivity.kt
                ├── TodoApp.kt
                ├── data/{local,repository}/...
                ├── di/DatabaseModule.kt
                ├── reminder/...
                └── ui/{calendar,components,tasks,theme}/...
```
