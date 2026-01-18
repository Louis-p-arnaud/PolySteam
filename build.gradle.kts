plugins {
    kotlin("jvm") version "1.9.20"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

group = "com.projet.joueur"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.6.0")
    implementation("org.apache.avro:avro:1.11.3")
    implementation("io.confluent:kafka-avro-serializer:7.5.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation(kotlin("stdlib"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

avro {
    fieldVisibility.set("PRIVATE")
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDirs("src/main/kotlin", "build/generated-main-avro-java")
        }
    }
}