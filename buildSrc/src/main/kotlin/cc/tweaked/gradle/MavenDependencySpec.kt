// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package cc.tweaked.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.publish.maven.MavenPublication

/**
 * A dependency in a POM file.
 */
private data class MavenDependency(val groupId: String?, val artifactId: String?) {
    constructor(project: Project, dep: Dependency) : this(
        dep.group,
        when (dep) {
            is ProjectDependency -> project.project(dep.path).extensions.getByType(BasePluginExtension::class.java).archivesName.get()
            else -> dep.name
        },
    )
}

/**
 * Remove dependencies in a POM file based on a list of dependencies
 *
 * While this approach is very ugly, it's the easiest way to handle it!
 */
internal fun excludeMavenDependencies(project: Project, publication: MavenPublication, excluded: Provider<out List<Dependency>>) {
    val excludedSpecs = excluded.map { xs -> xs.map { MavenDependency(project, it) } }

    publication.pom.withXml {
        val dependencies = XmlUtil.findChild(asNode(), "dependencies") ?: return@withXml
        dependencies.children().map { it as groovy.util.Node }.forEach {
            val dep = MavenDependency(
                groupId = XmlUtil.findChild(it, "groupId")?.text(),
                artifactId = XmlUtil.findChild(it, "artifactId")?.text(),
            )

            if (excludedSpecs.get().contains(dep)) it.parent().remove(it)
        }
    }
}
