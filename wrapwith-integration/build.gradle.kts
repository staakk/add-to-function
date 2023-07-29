plugins {
    kotlin("multiplatform") version "1.6.0"
    id("io.github.staakk.wrapwith") version "0.1.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }

    val osName = System.getProperty("os.name")
    when {
        "Windows" in osName -> mingwX64("native")
        "Mac OS" in osName -> macosX64("native")
        else -> linuxX64("native")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.staakk.wrapwith:wrapwith-lib:0.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeMain by getting {
            dependsOn(commonMain)
        }
        val nativeTest by getting {
            dependsOn(commonTest)
        }
    }
}