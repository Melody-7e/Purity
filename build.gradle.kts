plugins {
    id("java")
}

group   = "com.ri"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ws.schild:jave-all-deps:3.5.0")
    implementation("org.slf4j:slf4j-nop:2.0.17")
    implementation("org.jsoup:jsoup:1.22.2")
    implementation("org.netbeans.external:com-google-gson:RELEASE113")
    implementation("com.microsoft.playwright:playwright:1.42.0")


    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}