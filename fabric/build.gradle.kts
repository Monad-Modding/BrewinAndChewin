import umpaz.brewinandchewin.gradle.Properties
import umpaz.brewinandchewin.gradle.Versions
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

    modApi("house.greenhouse:greenhouseconfig:${Versions.GREENHOUSE_CONFIG}-fabric")
    include("house.greenhouse:greenhouseconfig:${Versions.GREENHOUSE_CONFIG}-fabric")
    api("house.greenhouse:greenhouseconfig_toml:${Versions.GREENHOUSE_CONFIG_TOML}")
    include("house.greenhouse:greenhouseconfig_toml:${Versions.GREENHOUSE_CONFIG_TOML}")

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

    // Create Fabric is not updating to 1.21.1.
//    modCompileOnly("com.simibubi.create:create-fabric-1.20.1:${Versions.CREATE_FABRIC}") { isTransitive = false}
//    modCompileOnly("com.tterrag.registrate_fabric:Registrate:${Versions.REGISTRATE_FABRIC}") { isTransitive = false}
//    modCompileOnly("io.github.fabricators_of_create.Porting-Lib:lazy_registration:${Versions.PORTING_LIB}") { isTransitive = false }

    modCompileOnly("maven.modrinth:styled-chat:${Versions.STYLED_CHAT}")
    modCompileOnly("eu.pb4:placeholder-api:${Versions.PB4_PLACEHOLDER_API}")

    modLocalRuntime("maven.modrinth:styled-chat:${Versions.STYLED_CHAT}")
    modLocalRuntime("eu.pb4:predicate-api:${Versions.PB4_PREDICATE_API}")
    modLocalRuntime("eu.pb4:placeholder-api:${Versions.PB4_PLACEHOLDER_API}")
    modLocalRuntime("me.lucko:fabric-permissions-api:${Versions.PERMISSIONS_API}")
    modLocalRuntime("eu.pb4:player-data-api:${Versions.PB4_PLAYER_DATA_API}")
}

loom {
    val ct = file("src/main/resources/${Properties.MOD_ID}.classtweaker");
    if (ct.exists())
        accessWidenerPath.set(ct)
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