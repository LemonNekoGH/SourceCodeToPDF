import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    id("org.jetbrains.compose") version "0.4.0-build180"
}

fun getVersionCode(): Int {
    return Runtime.getRuntime()
        .exec("git rev-list --count HEAD", null, file("."))
        .inputStream
        .bufferedReader()
        .readText()
        .trim()
        .toInt()
}

group = "moe.lemonneko"
version = "1.0.${getVersionCode()}"

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    // Compose Desktop
    implementation(compose.desktop.currentOs)
    // Apache Pdfbox 用于读写PDF
    implementation("org.apache.pdfbox:pdfbox:2.0.23")
    // 用于检测文件类型
    implementation("org.apache.tika:tika-core:2.0.0-ALPHA")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "moe.lemonneko.sctp.Main"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "源码PDF生成工具"
        }
    }
}
