// Top-level build file where you can add configuration options common to all sub-projects/modules.


// ADD THIS ENTIRE BLOCK
buildscript {
    repositories {google()
        mavenCentral()
    }
    dependencies {
        // This line tells Gradle where to find the code for the Safe Args plugin
        classpath(libs.navigation.safe.args.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.navigation.safeargs) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}