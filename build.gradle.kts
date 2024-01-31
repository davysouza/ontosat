plugins {
    id("java")
}

group = "br.usp.ime"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.sourceforge.owlapi:owlapi-distribution:5.5.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("au.csiro:elk-owlapi5:0.5.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to "br.usp.ime.Main"))
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.compileClasspath.map {
        config -> config.map {
            if (it.isDirectory)
                it
            else
                zipTree(it)
        }
    })
}