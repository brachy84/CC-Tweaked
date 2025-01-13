// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package cc.tweaked.gradle

import cc.tweaked.vanillaextract.configurations.Capabilities
import cc.tweaked.vanillaextract.configurations.MinecraftSetup
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType

/**
 * This sets up a separate client-only source set, and extends that and the main/common source set with additional
 * metadata, to make it easier to consume jars downstream.
 */
class MinecraftConfigurations private constructor(private val project: Project) {
    private val java = project.extensions.getByType(JavaPluginExtension::class.java)
    private val sourceSets = java.sourceSets
    private val configurations = project.configurations

    private val main = sourceSets[SourceSet.MAIN_SOURCE_SET_NAME]
    private val test = sourceSets[SourceSet.TEST_SOURCE_SET_NAME]

    /**
     * Performs the initial setup of our configurations.
     */
    private fun setup() {
        // Define a client source set.
        val client = sourceSets.maybeCreate("client")

        // Ensure the client classpaths behave the same as the main ones.
        consistentWithMain(client)

        // Set up an API configuration for clients (to ensure it's consistent with the main source set).
        val clientApi = configurations.maybeCreate(client.apiConfigurationName).apply {
            isVisible = false
            isCanBeConsumed = false
            isCanBeResolved = false
        }
        configurations.named(client.implementationConfigurationName) { extendsFrom(clientApi) }

        project.tasks.register(client.jarTaskName, Jar::class.java) {
            description = "An empty jar standing in for the client classes."
            group = BasePlugin.BUILD_GROUP
            archiveClassifier.set("client")
        }

        MinecraftSetup(project).setupOutgoingConfigurations()

        // Reset the client classpath (Loom configures it slightly differently to this) and add a main -> client
        // dependency. Here we /can/ use source set outputs as we add transitive deps by patching the classpath. Nasty,
        // but avoids accidentally pulling in Forge's obfuscated jar.
        client.compileClasspath = client.compileClasspath + main.compileClasspath
        client.runtimeClasspath = client.runtimeClasspath + main.runtimeClasspath
        project.dependencies.add(client.apiConfigurationName, main.output)

        // Also add client classes to the test classpath. We do the same nasty tricks as needed for main -> client.
        test.compileClasspath += client.compileClasspath
        test.runtimeClasspath += client.runtimeClasspath
        project.dependencies.add(test.implementationConfigurationName, client.output)

        // Configure some tasks to include our additional files.
        project.tasks.named("javadoc", Javadoc::class.java) {
            source(client.allJava)
            classpath = main.compileClasspath + main.output + client.compileClasspath + client.output
        }
        // This are already done by Fabric, but we need it for Forge and vanilla. It shouldn't conflict at all.
        project.tasks.named("jar", Jar::class.java) { from(client.output) }
        project.tasks.named("sourcesJar", Jar::class.java) { from(client.allSource) }

        setupBasic()
    }

    private fun consistentWithMain(sourceSet: SourceSet) {
        configurations.named(sourceSet.compileClasspathConfigurationName) {
            shouldResolveConsistentlyWith(configurations[main.compileClasspathConfigurationName])
        }

        configurations.named(sourceSet.runtimeClasspathConfigurationName) {
            shouldResolveConsistentlyWith(configurations[main.runtimeClasspathConfigurationName])
        }
    }

    private fun setupBasic() {
        val client = sourceSets["client"]

        project.extensions.configure(CCTweakedExtension::class.java) {
            sourceDirectories.add(SourceSetReference.internal(client))
        }

        // We can't create accurate module metadata for our additional capabilities,
        // so disable module metadata.
        project.tasks.withType(GenerateModuleMetadata::class.java).configureEach {
            isEnabled = false
        }

        // Register a task to check there are no conflicts with the core project.
        val checkDependencyConsistency =
            project.tasks.register("checkDependencyConsistency", DependencyCheck::class.java) {
                // We need to check both the main and client classpath *configurations*, as the actual configuration
                configuration.add(configurations.named(main.runtimeClasspathConfigurationName))
                configuration.add(configurations.named(client.runtimeClasspathConfigurationName))
            }
        project.tasks.named("check") { dependsOn(checkDependencyConsistency) }
    }

    /**
     * Create a new configuration that pulls in the main and client classes from the mod.
     */
    private fun createDerivedConfiguration(name: String) {
        val client = sourceSets["client"]
        val sourceSet = sourceSets.create(name)
        sourceSet.compileClasspath += main.compileClasspath + client.compileClasspath
        sourceSet.runtimeClasspath += main.runtimeClasspath + client.runtimeClasspath
        consistentWithMain(sourceSet)
        project.dependencies.add(sourceSet.implementationConfigurationName, main.output)
        project.dependencies.add(sourceSet.implementationConfigurationName, client.output)
    }

    companion object {
        const val DATAGEN = "datagen"
        const val EXAMPLES = "examples"
        const val TEST_MOD = "testMod"

        fun setupBasic(project: Project) {
            MinecraftConfigurations(project).setupBasic()
        }

        fun setup(project: Project) {
            MinecraftConfigurations(project).setup()
        }

        fun createDerivedConfiguration(project: Project, name: String) {
            MinecraftConfigurations(project).createDerivedConfiguration(name)
        }
    }
}

fun DependencyHandler.clientClasses(notation: Any): ModuleDependency =
    Capabilities.clientClasses(create(notation) as ModuleDependency)

fun DependencyHandler.commonClasses(notation: Any): ModuleDependency =
    Capabilities.commonClasses(create(notation) as ModuleDependency)
