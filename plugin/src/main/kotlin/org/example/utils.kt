package org.example

import org.gradle.api.Project
import org.gradle.api.attributes.*
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import java.io.File
import java.security.MessageDigest

data class NativeVariant(val os: OS, val arch: Arch,
                         val lib: File,
                         val classifier: String = "natives-$os-$arch")

fun Project.addRuntimeVariantsFor(vararg nativeVariants: NativeVariant) = addRuntimeVariantsFor(nativeVariants.toList())

fun Project.addRuntimeVariantsFor(nativeVariants: List<NativeVariant>) {
    // Add a different runtime variant for each platform
    val javaComponent = components.findByName("java") as AdhocComponentWithVariants
    nativeVariants.forEach { variantDefinition ->
        // Creation of the native jars
        val nativeJar = tasks.create<Jar>(variantDefinition.classifier + "Jar") {
            archiveClassifier = variantDefinition.classifier
            from(variantDefinition.lib)
            val md = MessageDigest.getInstance("SHA-1")
            md.update(variantDefinition.lib.readBytes())
            manifest.attributes["sha1"] = md.digest().joinToString("") { "%02x".format(it) }
        }

        val nativeRuntimeElements = configurations.consumable(variantDefinition.classifier + "RuntimeElements") {
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME)) // this is also by default
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY)) // this is also by default
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR)) // this is also by default
                attribute(Attribute.of(OS::class.java), variantDefinition.os)
                attribute(Attribute.of(Arch::class.java), variantDefinition.arch)
            }
            outgoing {
                artifact(tasks.named("jar"))
                artifact(nativeJar)
            }
            extendsFrom(configurations["runtimeElements"])
        }
        javaComponent.addVariantsFromConfiguration(nativeRuntimeElements.get()) {}
    }

    // don't publish the default runtime without native jar
    javaComponent.withVariantsFromConfiguration(configurations["runtimeElements"]) {
        skip()
    }
}