val buildsDirectory = "${System.getenv("IDEA_WORKSPACE")}/builds"

// Project values
group = "me.grabsky"
version = "1.0-SNAPSHOT"
description = "Tweaks"

// Defining Java version
java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("io.papermc.paperweight.userdev") version "1.3.5"
}

repositories {
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    paperDevBundle("1.18.2-R0.1-SNAPSHOT")
    compileOnly(files(buildsDirectory + File.separator + "Indigo.jar"))
}

tasks {
    build {
        dependsOn(reobfJar)
        doLast {
            // Copying output file to builds directory
            copy {
                from (reobfJar)
                into(buildsDirectory)
                // Renaming output file
                rename(reobfJar.get().outputJar.asFile.get().name, rootProject.name + ".jar")
            }
        }
    }
    compileKotlin { kotlinOptions.javaParameters = true }
}