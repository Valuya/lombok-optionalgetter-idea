import org.jetbrains.changelog.Changelog

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.1.0"
    id("org.jetbrains.changelog") version "2.2.1"
}

group = "be.valuya"
version = providers.gradleProperty("pluginVersion").getOrElse("1.0.0")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Build against IntelliJ IDEA Community; the plugin is pure Java-PSI and loads in newer IDEs too.
        intellijIdeaCommunity("2024.2.5")
        // PsiClass / JavaPsiFacade / PsiAugmentProvider / AnnotationUtil / LightMethodBuilder live in the
        // bundled Java plugin, not the core platform.
        bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    // Pure-Kotlin plugin: no Java bytecode instrumentation (no @NotNull assertions / GUI forms) needed.
    instrumentCode = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null }   // no upper bound — works in current and future IntelliJ
        }
        // Marketplace "What's new" for this version, taken from CHANGELOG.md.
        changeNotes = provider {
            with(changelog) {
                renderItem(
                    (getOrNull(project.version.toString()) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    // JetBrains Marketplace publishing (used by publish-plugin.yml). Needs a PUBLISH_TOKEN.
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

kotlin {
    jvmToolchain(21)
}

changelog {
    version = project.version.toString()
    // CHANGELOG.md, keep-a-changelog format; the [Unreleased] section rolls into the next release.
}
