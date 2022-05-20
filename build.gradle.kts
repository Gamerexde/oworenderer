plugins {
    kotlin("multiplatform") version "1.4.0"
}

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}

group = extra["projectGroup"] as String
version = extra["projectVersion"] as String

kotlin {
    jvm {
        tasks{
            withJava()

            register<Jar>("jvmFatJar") {
                dependsOn("jvmJar")

                manifest {
                    attributes["Main-Class"] = rootProject.extra["desktopMainClass"] as String
                }
                archiveBaseName.set("${project.name}-fat")
                from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) },
                    compilations.getByName("main").output.classesDirs,
                    compilations.getByName("main").output.resourcesDir
                )
            }
        }
    }

    sourceSets{
        val commonMain by getting {
            dependencies {
                val kgpuVersion = project.extra["kgpuVersion"]
                implementation(kotlin("stdlib-common"))
                implementation("io.github.kgpu:kgpu:$kgpuVersion")
                implementation("io.github.kgpu:kcgmath:$kgpuVersion")
                implementation("io.github.kgpu:kshader:$kgpuVersion")
            }
        }

        jvm().compilations["main"].defaultSourceSet{
            dependencies{
                implementation(kotlin("stdlib-jdk8"))
            }
        }
    }
}

tasks {
    register("runJvm", Exec::class){
        dependsOn("jvmFatJar")

        workingDir("$projectDir")
        commandLine("java", "-jar", "$buildDir/libs/${project.name}-fat-${project.version}.jar")
    }
}