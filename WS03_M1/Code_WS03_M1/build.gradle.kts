plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20171018")
    implementation("org.postgresql:postgresql:42.2.8")
    implementation("org.hibernate:hibernate-core:5.5.7.Final")
}

tasks.test {
    useJUnitPlatform()
}