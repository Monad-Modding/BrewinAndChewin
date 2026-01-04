import umpaz.brewinandchewin.gradle.Properties
import umpaz.brewinandchewin.gradle.Versions
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar

plugins {
    id("conventions.loader")
    id("fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
}

repositories {
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
    }
    maven("https://maven.greenhouse.lgbt/releases/") {
        name = "Greenhouse Maven"
    }
    maven("https://maven.greenhouse.lgbt/snapshots/") {
        name = "Greenhouse Maven (Snapshots)"
    }
    maven("https://mvn.devos.one/snapshots/") {
        name = "DevOS"
    }
    maven("https://mvn.devos.one/releases/")
    maven("https://maven.jamieswhiteshirt.com/libs-release") {
        content {
            includeGroup("com.jamieswhiteshirt")
        }
    }
    maven("https://jitpack.io/") {
        content {
            excludeGroup("io.github.fabricators_of_create")
        }
    }
    maven("https://maven.shedaniel.me/") {
        name = "Shedaniel"
    }
    exclusiveContent {
        forRepository {
            maven("https://jitpack.io")
        }
        filter {
            includeGroup("com.github.Chocohead")
        }
    }
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
    }
    maven("https://maven.nucleoid.xyz")
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")
    modLocalRuntime("com.terraformersmc:modmenu:${Versions.MOD_MENU}")

    modCompileOnly("io.github.fabricators_of_create.Porting-Lib:accessors:${Versions.PORTING_LIB}") { isTransitive = false }

    modImplementation("vectorwing:FarmersDelight:${Versions.FARMERS_DELIGHT_REFABRICATED}") {
        exclude(group = "net.fabricmc")
    }

    modCompileOnly("mezz.jei:jei-${Versions.MINECRAFT}-fabric-api:${Versions.JEI}")
    modLocalRuntime("mezz.jei:jei-${Versions.MINECRAFT}-fabric:${Versions.JEI}")
    modCompileOnly("dev.emi:emi-fabric:${Versions.EMI}:api")
    modLocalRuntime("dev.emi:emi-fabric:${Versions.EMI}")

    modCompileOnly("squeek.appleskin:appleskin-fabric:${Versions.APPLESKIN}")
    modLocalRuntime("squeek.appleskin:appleskin-fabric:${Versions.APPLESKIN}")
    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:${Versions.CLOTH_CONFIG}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modLocalRuntime("me.shedaniel.cloth:cloth-config-fabric:${Versions.CLOTH_CONFIG}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    modImplementation("com.github.Chocohead:Fabric-ASM:${Versions.FABRIC_ASM}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    modCompileOnly("com.simibubi.create:create-fabric-1.20.1:${Versions.CREATE_FABRIC}") { isTransitive = false}
    modCompileOnly("com.tterrag.registrate_fabric:Registrate:${Versions.REGISTRATE_FABRIC}") { isTransitive = false}

    modLocalRuntime("me.lucko:fabric-permissions-api:${Versions.PERMISSIONS_API}")
}

loom {
    val aw = file("src/main/resources/${Properties.MOD_ID}.accesswidener");
    if (aw.exists())
        accessWidenerPath.set(aw)
    mixin {
        defaultRefmapName.set("${Properties.MOD_ID}.refmap.json")
    }
    mods {
        register(Properties.MOD_ID) {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["test"])
        }
    }
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
        }
    }
}

tasks {
    named<ProcessResources>("processResources").configure {
        exclude("${Properties.MOD_ID}.cfg")
    }
}

publishMods {
    file.set(tasks.named<Jar>("remapJar").get().archiveFile)
    modLoaders.add("fabric")
    changelog = rootProject.file("CHANGELOG.md").readText()
    displayName = "v${Versions.MOD} (Fabric ${Versions.MINECRAFT})"
    version = "${Versions.MOD}+${Versions.MINECRAFT}-fabric"
    type = STABLE

    curseforge {
        projectId = Properties.CURSEFORGE_PROJECT_ID
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")

        minecraftVersions.add(Versions.MINECRAFT)
        javaVersions.add(JavaVersion.VERSION_21)

        clientRequired = true
        serverRequired = true
    }

    modrinth {
        projectId = Properties.MODRINTH_PROJECT_ID
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")

        minecraftVersions.add(Versions.MINECRAFT)
    }
}