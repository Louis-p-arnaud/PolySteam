plugins {
    kotlin("jvm") version "1.9.20"
    // Le plugin Avro permet de générer les classes à partir des fichiers .avsc
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

group = "com.polysteam.joueur"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Dépôt nécessaire pour les sérialiseurs Avro de Confluent
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation(kotlin("stdlib"))

    // Dépendances Kafka et Avro
    implementation("org.apache.avro:avro:1.11.3")
    implementation("io.confluent:kafka-avro-serializer:7.5.0")
    implementation("org.apache.kafka:kafka-clients:3.6.1")

    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.postgresql:postgresql:42.7.1")
}

// Configuration simplifiée pour Avro
avro {
    fieldVisibility.set("PRIVATE")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17" // Force la compilation en Java 17
    }
}
// Force la version de Java pour la compilation
tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

