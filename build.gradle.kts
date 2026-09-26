plugins {
    id("java")
}

group = "tw.sac"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.129-stable")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

val configuredPluginsDirectory = providers.gradleProperty("serverBridge.pluginsDirectory")
val serverPluginsDirectory = configuredPluginsDirectory.orElse(
    layout.buildDirectory.dir("undeployed").map { it.asFile.absolutePath }
)

val deployPlugin by tasks.registering(Copy::class) {
    group = "deployment"
    description = "Copies the latest ServerBridge jar into the configured Paper server."
    dependsOn(tasks.jar)
    from(tasks.jar.flatMap { it.archiveFile })
    into(serverPluginsDirectory)
    onlyIf("serverBridge.pluginsDirectory is configured") {
        configuredPluginsDirectory.isPresent
    }
}

tasks.build {
    finalizedBy(deployPlugin)
}

tasks.test {
    useJUnitPlatform()
}
