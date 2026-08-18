import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    testImplementation(libs.junit)

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
// Only ONE IntelliJ Platform dependency can be active at a time (see IGP docs
// "Target Platforms"). The Database Tools plugin (com.intellij.database) is the
// same in DataGrip and IntelliJ IDEA Ultimate, so building against IDEA also
// covers DataGrip at runtime (plugin.xml already declares module dependencies
// com.intellij.modules.platform + com.intellij.database, which DataGrip has).
// To build against DataGrip instead, swap the line below with:
//     datagrip("2025.3.6")
    intellijPlatform {
        intellijIdea("2025.3.5")

        testFramework(TestFrameworkType.Platform)

        // Add plugin dependencies for compilation here:
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("com.intellij.database")
    }
}
