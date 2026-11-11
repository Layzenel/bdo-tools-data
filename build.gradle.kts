// Сборщик справочника навыков: консольная JVM-программа.
// Запуск: ./gradlew run --args="docs/skills.json" (так её зовёт .github/workflows/update-data.yml)
plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    application
}

application {
    mainClass.set("com.layzen.bdotools.tools.skillcatalog.MainKt")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation(kotlin("test"))
}

// Относительный путь из --args считается от корня репозитория
tasks.named<JavaExec>("run") {
    workingDir = rootDir
}
