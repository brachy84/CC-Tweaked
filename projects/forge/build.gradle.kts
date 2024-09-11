// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

import cc.tweaked.gradle.*
import net.neoforged.gradle.dsl.common.runs.run.Run

plugins {
    id("cc-tweaked.forge")
    id("cc-tweaked.gametest")
    id("cc-tweaked.mod-publishing")
}

val modVersion: String by extra

val allProjects = listOf(":core-api", ":core", ":forge-api").map { evaluationDependsOn(it) }
cct {
    inlineProject(":common")
    allProjects.forEach { externalSources(it) }
}

sourceSets {
    main {
        resources.srcDir("src/generated/resources")
    }

    testMod { runs { modIdentifier = "cctest" } }
    testFixtures { runs { modIdentifier = "cctest" } }
}

minecraft {
    accessTransformers {
        file("src/main/resources/META-INF/accesstransformer.cfg")
    }
}

runs {
    configureEach {
        systemProperty("forge.logging.markers", "REGISTRIES")
        systemProperty("forge.logging.console.level", "debug")

        cct.sourceDirectories.get().forEach {
            if (it.classes) modSources.add("computercraft", it.sourceSet)
        }

        dependencies {
            runtime(configurations["minecraftLibrary"])
        }
    }

    val client by registering {
        workingDirectory(file("run"))
    }

    val server by registering {
        workingDirectory(file("run/server"))
        programArgument("--nogui")
    }

    val data by registering {
        workingDirectory(file("run"))
        programArguments.addAll(
            "--mod", "computercraft", "--all",
            "--output", layout.buildDirectory.dir("generatedResources").getAbsolutePath(),
            "--existing", project(":common").file("src/main/resources/").absolutePath,
            "--existing", file("src/main/resources/").absolutePath,
        )
    }

    fun Run.configureForGameTest() {
        gameTest()

        systemProperty("cctest.sources", project(":common").file("src/testMod/resources/data/cctest").absolutePath)

        modSource(sourceSets.testMod.get())
        modSource(sourceSets.testFixtures.get())
        modSources.add("cctest", project(":core").sourceSets.testFixtures.get())

        jvmArgument("-ea")

        dependencies {
            runtime(configurations["testMinecraftLibrary"])
        }
    }

    val gameTestServer by registering {
        workingDirectory(file("run/testServer"))
        configureForGameTest()
    }

    val gameTestClient by registering {
        configure(runTypes.named("client"))

        workingDirectory(file("run/testClient"))
        configureForGameTest()

        systemProperties("cctest.tags", "client,common")
    }
}

configurations {
    val minecraftEmbed by registering {
        isCanBeResolved = false
        isCanBeConsumed = false
    }
    named("jarJar") { extendsFrom(minecraftEmbed.get()) }

    val minecraftLibrary by registering {
        isCanBeResolved = true
        isCanBeConsumed = false
        extendsFrom(minecraftEmbed.get())
    }
    runtimeOnly { extendsFrom(minecraftLibrary.get()) }

    val testMinecraftLibrary by registering {
        isCanBeResolved = true
        isCanBeConsumed = false
        // Prevent ending up with multiple versions of libraries on the classpath.
        shouldResolveConsistentlyWith(minecraftLibrary.get())
    }

    register("testWithIris") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }
}

dependencies {
    compileOnly(libs.jetbrainsAnnotations)
    annotationProcessorEverywhere(libs.autoService)

    clientCompileOnly(variantOf(libs.emi) { classifier("api") })
    compileOnly(libs.bundles.externalMods.forge.compile)
    runtimeOnly(libs.bundles.externalMods.forge.runtime) { cct.exclude(this) }
    compileOnly(variantOf(libs.create.forge) { classifier("slim") }) { isTransitive = false }

    implementation("net.neoforged:neoforge:${libs.versions.neoForge.get()}")

    // Depend on our other projects.
    api(commonClasses(project(":forge-api"))) { cct.exclude(this) }
    clientApi(clientClasses(project(":forge-api"))) { cct.exclude(this) }
    implementation(project(":core")) { cct.exclude(this) }

    "minecraftEmbed"(libs.cobalt)
    "minecraftEmbed"(libs.jzlib)

    // We don't jar-in-jar our additional netty dependencies (see the tasks.jarJar configuration), but still want them
    // on the legacy classpath.
    "minecraftLibrary"(libs.netty.http)
    "minecraftLibrary"(libs.netty.socks)
    "minecraftLibrary"(libs.netty.proxy)

    testFixturesApi(libs.bundles.test)
    testFixturesApi(libs.bundles.kotlin)

    testImplementation(testFixtures(project(":core")))
    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.bundles.testRuntime)

    testModImplementation(testFixtures(project(":core")))
    testModImplementation(testFixtures(project(":forge")))

    // Ensure our test fixture dependencies are on the classpath
    "testMinecraftLibrary"(libs.bundles.kotlin)
    "testMinecraftLibrary"(libs.bundles.test)

    testFixturesImplementation(testFixtures(project(":core")))

    "testWithIris"(libs.iris.forge)
    "testWithIris"(libs.sodium.forge)
}

// Compile tasks

tasks.processResources {
    inputs.property("modVersion", modVersion)
    inputs.property("neoVersion", libs.versions.neoForge.get())

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(mapOf("neoVersion" to libs.versions.neoForge.get(), "file" to mapOf("jarVersion" to modVersion)))
    }
}

tasks.jar {
    archiveClassifier.set("slim")
    duplicatesStrategy = DuplicatesStrategy.FAIL

    // Include all classes from other projects except core.
    val coreSources = project(":core").sourceSets["main"]
    for (source in cct.sourceDirectories.get()) {
        if (source.classes && source.sourceSet != coreSources) from(source.sourceSet.output)
    }

    // Include core separately, along with the relocated netty classes.
    from(zipTree(project(":core").tasks.named("shadowJar", AbstractArchiveTask::class).map { it.archiveFile })) {
        exclude("META-INF/**")
    }
}

tasks.sourcesJar {
    for (source in cct.sourceDirectories.get()) from(source.sourceSet.allSource)
}

tasks.jarJar {
    archiveClassifier.set("")
}

tasks.assemble { dependsOn("jarJar") }

// Check tasks

tasks.test {
    systemProperty("cct.test-files", layout.buildDirectory.dir("tmp/testFiles").getAbsolutePath())
}

val runGametest by tasks.registering(JavaExec::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs tests on a temporary Minecraft instance."
    dependsOn("cleanRunGametest")
    usesService(MinecraftRunnerService.get(gradle))

    copyFromTask("runGameTestServer")

    systemProperty("forge.logging.console.level", "info")
    systemProperty("cctest.gametest-report", layout.buildDirectory.dir("test-results/$name.xml").getAbsolutePath())
}
cct.jacoco(runGametest)
tasks.check { dependsOn(runGametest) }

val runGametestClient by tasks.registering(ClientJavaExec::class) {
    description = "Runs client-side gametests with no mods"
    copyFrom("runGameTestClient")
    tags("client")
}
cct.jacoco(runGametestClient)

val runGametestClientWithIris by tasks.registering(ClientJavaExec::class) {
    description = "Runs client-side gametests with Iris"
    copyFrom("runGameTestClient")

    tags("iris")
    classpath += configurations["testWithIris"]

    withComplementaryShaders()
}
cct.jacoco(runGametestClientWithIris)

tasks.register("checkClient") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs all client-only checks."
    dependsOn(runGametestClient, runGametestClientWithIris)
}

// Upload tasks

modPublishing {
    output.set(tasks.jarJar)
}

// Don't publish the slim jar
for (cfg in listOf(configurations.apiElements, configurations.runtimeElements)) {
    cfg.configure { artifacts.removeIf { it.classifier == "slim" } }
}

tasks.withType(GenerateModuleMetadata::class).configureEach { isEnabled = false }
publishing {
    publications {
        named("maven", MavenPublication::class) {
            mavenDependencies {
                cct.configureExcludes(this)
            }
        }
    }
}
