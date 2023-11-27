plugins {
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20230227")
    implementation("org.postgresql:postgresql:42.5.4")
    implementation("org.hibernate:hibernate-core:6.1.7.Final")
}

tasks.test {
    useJUnitPlatform()
}