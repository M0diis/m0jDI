plugins {
    id("java")
}

group = "me.m0dii"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.jar {
    exclude("me/m0dii/m0jdi/example/**")
}

tasks.test {
    useJUnitPlatform()
}