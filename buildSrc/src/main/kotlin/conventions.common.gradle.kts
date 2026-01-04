import umpaz.brewinandchewin.gradle.Properties
import umpaz.brewinandchewin.gradle.Versions

plugins {
    base
    `java-library`
    idea
    `maven-publish`
}

base.archivesName.set("${Properties.ARCHIVES_NAME}-${project.name}")
group = Properties.GROUP
version = "${Versions.MOD}+${Versions.MINECRAFT}"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(Versions.JAVA))
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    // https://docs.gradle.org/current/userguide/declaring_repositories.html#declaring_content_exclusively_found_in_one_repository
    exclusiveContent {
        forRepository {
            maven("https://repo.spongepowered.org/repository/maven-public") {
                name = "Sponge"
            }
        }
        filter { includeGroupAndSubgroups("org.spongepowered") }
    }
    exclusiveContent {
        forRepositories(
            maven("https://maven.parchmentmc.org/") {
                name = "ParchmentMC"
            },
            maven("https://maven.neoforged.net/releases") {
                name = "NeoForge"
            }
        )
        filter { includeGroup("org.parchmentmc.data") }
    }
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    maven("https://maven.ryanliptak.com/") {
        name = "Ryanliptak"
    }
    maven("https://maven.blamejared.com/") {
        name = "Jared's maven"
    }
    maven("https://maven.terraformersmc.com/") {
        name = "TerraformersMC"
    }
    maven("https://maven.greenhouse.lgbt/releases/") {
        name = "Greenhouse"
    }
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven") {
                name = "Modrinth"
            }
        }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.createmod.net") // Create, Ponder, Flywheel
    maven("https://mvn.devos.one/snapshots") // Registrate
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // ForgeConfigAPIPort
}

dependencies {
    implementation("org.jetbrains:annotations:24.1.0")
}

tasks {
    named<Jar>("sourcesJar").configure {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${Properties.MOD_NAME}" }
        }
    }
    named<Jar>("jar").configure {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${Properties.MOD_NAME}" }
        }

        manifest {
            attributes["Specification-Title"] = Properties.MOD_NAME
            attributes["Specification-Vendor"] = Properties.MOD_AUTHORS
            attributes["Specification-Version"] = archiveVersion
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = archiveVersion
            attributes["Implementation-Vendor"] = Properties.MOD_AUTHORS
            attributes["Built-On-Minecraft"] = Versions.MINECRAFT
        }
    }

    val expandProps = mapOf(
        "mod_version" to Versions.MOD,
        "group" to project.group, //Else we target the task's group.
        "minecraft_version" to Versions.MINECRAFT,
        "fabric_api_version" to Versions.FABRIC_API,
        "fabric_loader_version" to Versions.FABRIC_LOADER,
        "fabric_minecraft_version_range" to Versions.FABRIC_MINECRAFT_RANGE,
        "fabric_loader_range" to Versions.FABRIC_LOADER_RANGE,
        "mod_name" to Properties.MOD_NAME,
        "neoforge_mod_authors" to Properties.MOD_AUTHORS.joinToString(),
        "fabric_mod_authors" to Properties.MOD_AUTHORS.joinToString(separator = "\",\n\t\t\""),
        "mod_id" to Properties.MOD_ID,
        "mod_license" to Properties.LICENSE,
        "mod_description" to Properties.DESCRIPTION,
        "java_version" to Versions.JAVA,
        "curseforge_page" to Properties.CURSEFORGE_PAGE,
        "modrinth_page" to Properties.MODRINTH_PAGE,
        "sources" to Properties.GITHUB_REPO,
        "farmers_delight_refabricated_range" to Versions.FARMERS_DELIGHT_REFABRICATED_RANGE,
        // Gradle is a bit trigger-happy...
        "HotCocoa" to "\$HotCocoa",
        "MilkBottle" to "\$MilkBottle",
        "LootParamsBuilderMixin" to "\$LootParamsBuilderMixin",
        "LootParamsMixin" to "\$LootParamsMixin",
        "TipsyCanvasSignRenderMixin" to "\$TipsyCanvasSignRenderMixin",
        "TipsySignRenderMixin" to "\$TipsySignRenderMixin"
    )

    val processResourcesTasks = listOf("processResources", "processTestResources", "processDatagenResources")

    withType<ProcessResources>().matching { processResourcesTasks.contains(it.name) }.configureEach {
        inputs.properties(expandProps)
        filesMatching(setOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
            expand(expandProps)
        }
        exclude("\\.cache")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "Greenhouse"
            url = uri("https://maven.greenhouse.lgbt/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}