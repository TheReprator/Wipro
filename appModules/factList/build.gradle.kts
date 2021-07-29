plugins {
    id(Libs.Plugins.androidLibrary)
    kotlin(Libs.Plugins.kotlinAndroid)
    kotlin(Libs.Plugins.kotlinKapt)
    id(Libs.Plugins.kotlinNavigation)
    id(Libs.Plugins.kaptDagger)
    id(Libs.TestDependencies.Junit5.plugin)
}

kapt {
    correctErrorTypes = true
    useBuildCache = true

    javacOptions {
        // These options are normally set automatically via the Hilt Gradle plugin, but we
        // set them manually to workaround a bug in the Kotlin 1.5.20
        option("-Adagger.fastInit=ENABLED")
        option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
    }

    arguments {
        arg("dagger.hilt.shareTestComponents", "true")
    }
}

android {
    compileSdkVersion(AndroidSdk.compile)

    defaultConfig {
        minSdkVersion(AndroidSdk.min)
        targetSdkVersion(AndroidSdk.target)

        multiDexEnabled = true

        consumerProguardFiles(
            file("proguard-rules.pro")
        )

        resConfigs(AndroidSdk.locales)
        testInstrumentationRunner = "reprator.wipro.factlist.FactListTestRunner"
    }

    buildFeatures.dataBinding = true

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        map { it.java.srcDirs("src/${it.name}/kotlin") }
    }

    buildTypes {

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                file("proguard-rules.pro")
            )
        }
    }

    packagingOptions {
        exclude("META-INF/atomicfu.kotlin_module")
        exclude("META-INF/*")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
    }

    sourceSets {
        getByName("test").java.srcDirs("src/test/kotlin/", "src/sharedTest/kotlin/")
        getByName("test").resources.srcDirs("src/sharedTest/resources/")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin/", "src/sharedTest/kotlin/")
        getByName("androidTest").resources.srcDirs("src/sharedTest/resources/")
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            when (requested.module.toString()) {
                "com.squareup.okhttp3:okhttp" -> useVersion("4.9.1")
            }
        }
    }
}

dependencies {
    implementation(project(AppModules.moduleBaseJava))
    implementation(project(AppModules.moduleBaseAndroid))
    implementation(project(AppModules.moduleNavigation))

    implementation(Libs.AndroidX.cardView)
    implementation(Libs.swiperefresh)
    implementation(Libs.AndroidX.constraintlayout)

    implementation(Libs.AndroidX.Navigation.fragmentKtx)

    implementation(Libs.AndroidX.Fragment.fragmentKtx)

    // Hilt
    implementation(Libs.DaggerHilt.hilt)
    kapt(Libs.DaggerHilt.hiltCompilerAndroid)

    /*
    *  Unit Testing
    * */
    testImplementation(Libs.TestDependencies.Junit5.jupiter)
    testRuntimeOnly(Libs.TestDependencies.Junit5.runtime)

    testImplementation(Libs.TestDependencies.AndroidXTest.truth)
    testImplementation(Libs.TestDependencies.core)
    testImplementation(Libs.OkHttp.mockWebServer)
    testImplementation(Libs.TestDependencies.Mockk.unitTest)

    testImplementation(Libs.Coroutines.coroutineTest) {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug")
    }

    /*
       UI Tests
    */
    implementation(Libs.TestDependencies.UITest.busyBee)
    debugImplementation(Libs.TestDependencies.UITest.fragmentTesting)

    androidTestImplementation(Libs.TestDependencies.UITest.fragmentRuntime)

    androidTestImplementation(Libs.DaggerHilt.hiltAndroidTest)
    kaptAndroidTest(Libs.DaggerHilt.hiltCompilerAndroid)

    androidTestImplementation(Libs.TestDependencies.AndroidXTest.junit)
    androidTestImplementation(Libs.TestDependencies.Espresso.core)
    androidTestImplementation(Libs.TestDependencies.Espresso.contrib)

    androidTestImplementation(Libs.TestDependencies.Mockk.instrumentedTest)
    androidTestImplementation(Libs.TestDependencies.UITest.dexmaker)

    androidTestImplementation(Libs.TestDependencies.UITest.kaspresso)

    androidTestImplementation(Libs.OkHttp.mockWebServer)
    androidTestImplementation(Libs.OkHttp.loggingInterceptor)

    // OkHttp Idling Resource
    androidTestImplementation(Libs.TestDependencies.UITest.okhttpIdlingResource)

    androidTestImplementation(Libs.Coroutines.coroutineTest) {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug")
    }
}
