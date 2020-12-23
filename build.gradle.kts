plugins {
    id("dev.fritz2.fritz2-gradle") version "0.8"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    js {
        browser {
            webpackTask {
                cssSupport.enabled = true
            }

            runTask {
                cssSupport.enabled = true
            }

            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }
    jvm {}

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("dev.fritz2:core:0.8")
            }
        }
    }
}