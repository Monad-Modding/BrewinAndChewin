import umpaz.brewinandchewin.gradle.Properties
import umpaz.brewinandchewin.gradle.Versions
import org.apache.tools.ant.filters.LineContains
import org.gradle.jvm.tasks.Jar

plugins {
    id("conventions.loader")
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
}

neoForge {
    version = Versions.NEOFORGE
    parchment {
        minecraftVersion = Versions.PARCHMENT_MINECRAFT
        mappingsVersion = Versions.PARCHMENT
    }
    addModdingDependenciesTo(sourceSets["test"])

    val at = project(":common").file("src/main/resources/${Properties.MOD_ID}.cfg")
    if (at.exists())
        setAccessTransformers(at)
    validateAccessTransformers = true

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            systemProperty("neoforge.enabledGameTestNamespaces", Properties.MOD_ID)
        }
        create("client") {
            client()
            ideName = "NeoForge Client (:${project.name})"
            gameDirectory.set(file("runs/client"))
            sourceSet = sourceSets["test"]
            jvmArguments.set(setOf("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true"))
        }
        create("server") {
            server()
            ideName = "NeoForge Server (:${project.name})"
            gameDirectory.set(file("runs/server"))
            programArgument("--nogui")
            sourceSet = sourceSets["test"]
            jvmArguments.set(setOf("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true"))
        }
        create("data") {
            data()
            ideName = "NeoForge Datagen (:${project.name})"
            gameDirectory.set(file("runs/datagen"))
            programArguments.addAll(
                "--mod", Properties.MOD_ID,
                "--existing-mod", "farmersdelight",
                "--output", file("../common/src/generated/resources").absolutePath,
                "--all"
            )
            sourceSet = sourceSets["test"]
        }
    }

    mods {
        register(Properties.MOD_ID) {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["test"])
        }
    }
}

repositories {
    maven("https://maven.theillusivec4.top/") {
        name = "Curios"
    }
}

dependencies {
    api("house.greenhouse:greenhouseconfig:${Versions.GREENHOUSE_CONFIG}-neoforge")
    jarJar("house.greenhouse:greenhouseconfig:${Versions.GREENHOUSE_CONFIG}-neoforge")
    api("house.greenhouse:greenhouseconfig_toml:${Versions.GREENHOUSE_CONFIG_TOML}")
    jarJar("house.greenhouse:greenhouseconfig_toml:${Versions.GREENHOUSE_CONFIG_TOML}")

    implementation("maven.modrinth:farmers-delight:${Versions.FARMERS_DELIGHT}")

    runtimeOnly("maven.modrinth:no-mans-land:${Versions.NO_MANS_LAND}")

    compileOnly("mezz.jei:jei-${Versions.MINECRAFT}-neoforge-api:${Versions.JEI}")
    runtimeOnly("mezz.jei:jei-${Versions.MINECRAFT}-neoforge:${Versions.JEI}")
    compileOnly("dev.emi:emi-neoforge:${Versions.EMI}:api")
//    runtimeOnly("dev.emi:emi-neoforge:${Versions.EMI}")

    implementation("squeek.appleskin:appleskin-neoforge:${Versions.APPLESKIN}")

    implementation("com.simibubi.create:create-${Versions.MINECRAFT}:${Versions.CREATE}") { isTransitive = false }
    implementation("net.createmod.ponder:Ponder-NeoForge-${Versions.MINECRAFT}:${Versions.PONDER}") { isTransitive = false }
    compileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${Versions.MINECRAFT}:${Versions.FLYWHEEL}")
    runtimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${Versions.MINECRAFT}:${Versions.FLYWHEEL}")
    implementation("com.tterrag.registrate:Registrate:${Versions.REGISTRATE}")  { isTransitive = false }
    runtimeOnly("top.theillusivec4.curios:curios-neoforge:${Versions.CURIOS}")
}

tasks {
    named<ProcessResources>("processResources").configure {
        filesMatching("*.mixins.json") {
            filter<LineContains>("negate" to true, "contains" to setOf("refmap"))
        }
    }
}

publishMods {
    file.set(tasks.named<Jar>("jar").get().archiveFile)
    modLoaders.add("neoforge")
    changelog = rootProject.file("CHANGELOG.md").readText()
    displayName = "v${Versions.MOD} (NeoForge ${Versions.MINECRAFT})"
    version = "${Versions.MOD}+${Versions.MINECRAFT}-neoforge"
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