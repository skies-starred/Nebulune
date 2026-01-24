@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.loom)
    alias(libs.plugins.ksp)
    alias(libs.plugins.fletchingTable)
}

val platforms = listOf("windows", "linux", "macos", "macos-arm64")
val mc = stonecutter.current.version
version = "${property("mod.version")}+$mc"
base.archivesName = property("mod.id").toString()

repositories {
    fun strictMaven(url: String, vararg groups: String) = maven(url) { content { groups.forEach(::includeGroupAndSubgroups) } }

    strictMaven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1", "me.djtheredstoner")
    strictMaven("https://repo.hypixel.net/repository/Hypixel", "net.hypixel")
    strictMaven("https://api.modrinth.com/maven", "maven.modrinth")
    strictMaven("https://maven.parchmentmc.org/", "org.parchmentmc")
    strictMaven("https://maven.teamresourceful.com/repository/maven-public/", "tech.thatgravyboat", "com.terraformersmc", "earth.terrarium", "com.teamresourceful", "me.owdding")
    strictMaven("https://maven.deftu.dev/snapshots", "dev.deftu")
    strictMaven("https://maven.deftu.dev/releases", "dev.deftu")
    strictMaven("https://repo.nea.moe/releases", "moe.nea")
}

fletchingTable {
    mixins.create("main", Action {
        mixin("default", "nebulune.mixins.json") {
            env("CLIENT")
        }
    })
}

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("parchment".mc(mc))
    })

    modRuntimeOnly(libs.devauth)
    modCompileOnly("entityculling".mc(mc))

    modImplementation("athen".mc(mc))
    modImplementation("modmenu".mc(mc))
    modImplementation("fabric-api".mc(mc))
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.language.kotlin)
    modImplementation(libs.hypixel.modapi)
    modImplementation(libs.hypixel.modapi.fabric)

    modImplementation(libs.classgraph)
    modImplementation(libs.autoupdate)
    modImplementation("omnicore".mc(mc))
    modImplementation("olympus".mc(mc))
    modImplementation("rlib".mc(mc))
    modImplementation(libs.lwjgl.nanovg)
    for (p in platforms) modImplementation("${libs.lwjgl.nanovg.get()}:natives-$p")

    modImplementation(libs.skyblock.api) {
        capabilities { requireCapability("tech.thatgravyboat:skyblock-api-$mc-remapped") }
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    runConfigs.named("client") {
        isIdeConfigGenerated = true
        vmArgs.addAll(
            arrayOf(
                "-Ddevauth.enabled=true",
                "-Ddevauth.account=main",
                "-XX:+AllowEnhancedClassRedefinition"
            )
        )
    }

    runConfigs.named("server") {
        isIdeConfigGenerated = false
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))

        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "id" to project.property("mod.id"),
                    "name" to project.property("mod.name"),
                    "version" to project.property("mod.version"),
                    "minecraft" to project.property("mod.mc_dep")
                )
            )
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

fun String.mc(mc: String): Provider<MinimalExternalModuleDependency> = project.extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary("$this-${mc.replace(".", "_")}").get()

fun DependencyHandler.shadow(dep: Any, config: ExternalModuleDependency.() -> Unit = {}) {
    val d = create((dep as? Provider<*>)?.get() ?: dep) as ExternalModuleDependency
    d.config()
    include(d)
    modImplementation(d)
}