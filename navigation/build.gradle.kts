plugins {
    id(Libs.Plugins.androidLibrary)
    kotlin(Libs.Plugins.kotlinAndroid)
    kotlin(Libs.Plugins.kotlinKapt)
    id(Libs.Plugins.kotlinNavigation)
}

kapt {
    correctErrorTypes = true
    useBuildCache = true
}

android {
    compileSdkVersion(AndroidSdk.compile)

    defaultConfig {
        minSdkVersion(AndroidSdk.min)

        testInstrumentationRunner = Libs.TestDependencies.testRunner

        consumerProguardFiles(
            file("proguard-rules.pro")
        )
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    sourceSets {
        map { it.java.srcDirs("src/${it.name}/kotlin") }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }

    packagingOptions {
        exclude("META-INF/*")
    }
}

dependencies {
    implementation(project(AppModules.moduleBaseAndroid))

    implementation(Libs.AndroidX.appcompat)
    implementation(Libs.AndroidX.coreKtx)

    implementation(Libs.AndroidX.Navigation.fragmentKtx)
    implementation(Libs.AndroidX.Navigation.uiKtx)
}
