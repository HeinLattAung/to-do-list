// Project-root  /  build.gradle.kts
//
// Lists the plugin versions used by the whole project. Don't apply them here —
// individual modules (app/) opt-in via their own build.gradle.kts.

plugins {
    id("com.android.application")           version "8.5.2"  apply false
    id("org.jetbrains.kotlin.android")      version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
    id("com.google.devtools.ksp")           version "2.0.20-1.0.25" apply false
    id("com.google.dagger.hilt.android")    version "2.51.1" apply false
}
