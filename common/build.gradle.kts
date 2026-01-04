import umpaz.brewinandchewin.gradle.Properties
import umpaz.brewinandchewin.gradle.Versions

plugins {
    id("fabric-loom")
    id("conventions.common")
    id("me.modmuss50.mod-publish-plugin")
}

sourceSets {
    create("generated") {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings(loom.officialMojangMappings())

    compileOnly("io.github.llamalad7:mixinextras-common:${Versions.MIXIN_EXTRAS}")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${Versions.MIXIN_EXTRAS}")
    compileOnly("net.fabricmc:sponge-mixin:${Versions.FABRIC_MIXIN}")

    compileOnly("mezz.jei:jei-${Versions.MINECRAFT}-common-api:${Versions.JEI}")
    compileOnly("dev.emi:emi-xplat-mojmap:${Versions.EMI}:api")

    compileOnly("com.simibubi.create:create-${Versions.MINECRAFT}:${Versions.CREATE}")
    compileOnly("com.tterrag.registrate:Registrate:${Versions.REGISTRATE}")
}

configurations {
    register("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonTestResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets["main"].java.sourceDirectories.singleFile)
    add("commonResources", sourceSets["main"].resources.sourceDirectories.singleFile)
    add("commonResources", sourceSets["generated"].resources.sourceDirectories.singleFile)
    add("commonTestResources", sourceSets["test"].resources.sourceDirectories.singleFile)
}

publishMods {
    changelog = rootProject.file("CHANGELOG.md").readText()
    displayName = "v${Versions.MOD} (Minecraft ${Versions.MINECRAFT})"
    version = "${Versions.MOD}+${Versions.MINECRAFT}"
    type = STABLE

    github {
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        repository = Properties.GITHUB_REPO
        tagName = "${Versions.MOD}+${Versions.MINECRAFT}"
        commitish = Properties.GITHUB_COMMITISH

        file(project(":fabric"))
    }
}