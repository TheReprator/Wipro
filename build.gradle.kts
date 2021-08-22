import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

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
        classpath(Libs.TestDependencies.Junit5.classPath)
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

apply(from = file("$rootDir/gradle/dependencyGraph.gradle"))

project.rootProject.allprojects {
    apply(plugin = "project-report")

    this.task("allDependencies", DependencyReportTask::class) {

        outputFile = file("$rootDir/reports/dependencies.txt")

        evaluationDependsOnChildren()
        this.setRenderer(AsciiDependencyReportRenderer())
    }
}

/*
Junit5 Configuration for all modules
* */
subprojects {
    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            lifecycle {
                events =
                    mutableSetOf(org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED, org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED, org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED)
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                showExceptions = true
                showCauses = true
                showStackTraces = true
                showStandardStreams = true
            }
            info.events = lifecycle.events
            info.exceptionFormat = lifecycle.exceptionFormat
        }

        val failedTests = mutableListOf<TestDescriptor>()
        val skippedTests = mutableListOf<TestDescriptor>()

        // See https://github.com/gradle/kotlin-dsl/issues/836
        addTestListener(object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {}
            override fun beforeTest(testDescriptor: TestDescriptor) {}
            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
                when (result.resultType) {
                    TestResult.ResultType.FAILURE -> failedTests.add(testDescriptor)
                    TestResult.ResultType.SKIPPED -> skippedTests.add(testDescriptor)
                    else -> Unit
                }
            }

            override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                if (suite.parent == null) { // root suite
                    logger.lifecycle("----")
                    logger.lifecycle("Test result: ${result.resultType}")
                    logger.lifecycle(
                        "Test summary: ${result.testCount} tests, " +
                                "${result.successfulTestCount} succeeded, " +
                                "${result.failedTestCount} failed, " +
                                "${result.skippedTestCount} skipped"
                    )
                    failedTests.takeIf { it.isNotEmpty() }?.prefixedSummary("\tFailed Tests")
                    skippedTests.takeIf { it.isNotEmpty() }?.prefixedSummary("\tSkipped Tests:")
                }
            }

            private infix fun List<TestDescriptor>.prefixedSummary(subject: String) {
                logger.lifecycle(subject)
                forEach { test -> logger.lifecycle("\t\t${test.displayName()}") }
            }

            private fun TestDescriptor.displayName() =
                parent?.let { "${it.name} - $name" } ?: "$name"
        })
    }
}

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
}