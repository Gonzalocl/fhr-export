plugins {
    id 'application'
}

group 'org.eu.gonzalocaparros'
version '1.0'

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.google.code.gson:gson:2.11.0'

    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.7.0'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.7.0'
}

//tasks.withType(JavaCompile) {
//    options.compilerArgs += '--enable-preview'
//}

compileJava {
    options.compilerArgs += '--enable-preview'
}

test {
    jvmArgs += '--enable-preview'
    useJUnitPlatform()
}

//tasks.withType(JavaExec) {
//    jvmArgs += '--enable-preview'
//}

application {
    mainClass = 'org.eu.gonzalocaparros.doc_dl.App'
    applicationDefaultJvmArgs += '--enable-preview'
}
