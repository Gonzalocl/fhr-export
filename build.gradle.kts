plugins {
    application
}

group = "org.eu.gonzalocaparros"
version = "0.1"

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

//tasks.withType(JavaCompile) {
//    options.compilerArgs += '--enable-preview'
//}

tasks.compileJava {
    options.compilerArgs.add("--enable-preview")
}

tasks.test {
//    jvmArgs += '--enable-preview'
    useJUnitPlatform()
}

//tasks.withType(JavaExec) {
//    jvmArgs += '--enable-preview'
//}

application {
    mainClass = "org.eu.gonzalocaparros.doc_dl.App"
    applicationDefaultJvmArgs += "--enable-preview"
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = application.mainClass
    }

    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
}
