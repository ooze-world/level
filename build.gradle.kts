plugins {
    // We can remove this once migration to Kotlin is complete.
    java

    kotlin("jvm") version "1.6.10"

    // Adds support for generating .class files from ProtoBuf straight from gradle.
    id("com.google.protobuf") version "0.8.18"
}

group = "com.github.ooze"
version = "0.0.1-SNAPSHOT"
description = "level"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.TheNullicorn", "Nedit", "v2.1.0")
    implementation("com.google.protobuf", "protobuf-java", "3.19.1")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.2")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.8.2")
    testImplementation("org.junit.jupiter", "junit-jupiter-params", "5.8.2")
    testImplementation("nl.jqno.equalsverifier", "equalsverifier", "3.8")

    // Tell ProtoBuf where our definitions are.
    protobuf(files("src/main/protobuf/"))
}

// Include generated ProtoBuf classes in sources.
// This also fixes intellisense not recognizing ProtoBuf classes.
sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}