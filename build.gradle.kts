buildscript {
    extra["kotlin_plugin_id"] = "io.github.staakk.wrapwith"
}

plugins {
    kotlin("jvm") version "1.6.0" apply false
    id("com.github.gmazzo.buildconfig") version "3.0.3" apply false
}

allprojects {
    group = "io.github.staakk.wrapwith"
    version = "0.1.0"
}

subprojects {
    repositories {
        mavenCentral()
    }
}
