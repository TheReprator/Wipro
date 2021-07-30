import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer

plugins {
    kotlin(Libs.Plugins.kotlinJVM) version Libs.Versions.kotlin

    id(Libs.Plugins.dokka) version (Libs.Versions.dokka)
    id(Libs.Plugins.spotless) version Libs.Versions.spotless

    id(Libs.Plugins.detekt) version Libs.Versions.detekt

}

buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(Libs.androidGradlePlugin)
        classpath(Libs.Kotlin.gradlePlugin)
        classpath(Libs.AndroidX.Navigation.navigationPlugin)
        classpath(Libs.DaggerHilt.classPath)
    }
}

subprojects {

    plugins.apply(Libs.Plugins.detekt)

    plugins.apply(Libs.Plugins.dokka)
    plugins.apply(Libs.Plugins.spotless)

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")

            ktlint(Libs.Versions.ktlint)
            licenseHeaderFile("${project.rootProject.projectDir}/config/spotless/copyright.kt")
        }

        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }
    }

    tasks.named<org.jetbrains.dokka.gradle.DokkaTaskPartial>("dokkaHtmlPartial") {
        dokkaSourceSets.configureEach {
            noAndroidSdkLink.set(true)
            suppressInheritedMembers.set(true)
        }
    }

    detekt {
        config = rootProject.files("$rootDir/config/detekt/detekt.yml")
        reports {
            html {
                enabled = true
                destination = file("$rootDir/reports/detekt/detekt.html")
            }
        }
    }
}

/*Report Generation*/
tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>().configureEach {
    outputDirectory.set(file("$rootDir/reports/dokka"))
}

//apply(from = file("$rootDir/gradle/dependencyGraph.gradle"))

project.rootProject.allprojects {
    apply(plugin = "project-report")

    this.task("allDependencies", DependencyReportTask::class) {

        outputFile = file("$rootDir/reports/dependencies.txt")

        evaluationDependsOnChildren()
        this.setRenderer(AsciiDependencyReportRenderer())
    }
}