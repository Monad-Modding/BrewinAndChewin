import org.gradle.api.file.DuplicatesStrategy

plugins {
    id("net.fabricmc.fabric-loom") version "1.17.21"
    id("maven-publish")
}

val modVersion = project.property("mod_version") as String
val mavenGroup = project.property("maven_group") as String
val archivesBaseName = project.property("archives_base_name") as String
val minecraftVersion = project.property("minecraft_version") as String
val loaderVersion = project.property("loader_version") as String
val fabricVersion = project.property("fabric_version") as String
val fdrfVersion = project.property("fdrf_version") as String

base.archivesName.set(archivesBaseName)
version = modVersion
group = mavenGroup

sourceSets {
    main {
        java {
            setSrcDirs(listOf("common/src/main/java", "fabric/src/main/java"))
            exclude("**/integration/**")
            exclude("**/CreatePotionPouringRecipe.java")
            exclude("**/BnCCreateDelegate.java")
            exclude("**/JEITransferKegRecipeServerboundPacket.java")
            exclude("**/EMIFillFermentingRecipeServerboundPacket.java")
            exclude("**/EMIFillPouringRecipeServerboundPacket.java")
            exclude("**/KegPlaceRecipe.java")
            exclude("**/KegStackedContents.java")
            exclude("**/KegStackedContentsRecipePickerMixin.java")
            exclude("**/ServerPlaceRecipeAccessor.java")
            exclude("**/StackedContentsRecipePickerAccessor.java")
            exclude("**/RecipeBookCategoriesMixin.java")
            exclude("**/ClientRecipeBookMixin.java")
            exclude("**/BnCRecipeBookCategories.java")
            exclude("**/CoasterWrappedModel.java")
            exclude("**/IntoxicationAppleSkinCompatFabric.java")
        }
        resources {
            setSrcDirs(listOf(
                "common/src/main/resources",
                "common/src/generated/resources",
                "fabric/src/main/resources"
            ))
            exclude("**/neoforge/**", "**/*.cfg", ".cache")
        }
    }
}

loom {
    val ct = file("fabric/src/main/resources/brewinandchewin.classtweaker")
    if (ct.exists()) {
        accessWidenerPath.set(ct)
    }
}

fabricApi {
    configureTests {
        createSourceSet = true
        modId = "brewinandchewin-gametest"
        enableGameTests = true
        enableClientGameTests = false
        eula = true
    }
}

repositories {
    mavenLocal()
    maven("https://maven.greenhouseteam.dev/releases/") {
        name = "Greenhouse"
    }
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
    }
    maven("https://www.cursemaven.com") {
        name = "Curse Forge"
    }
    maven("https://maven.terraformersmc.com/") {
        name = "TerraformersMC"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$loaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")

    implementation("vectorwing:FarmersDelight:$fdrfVersion") {
        exclude(group = "net.fabricmc")
    }
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<AbstractCopyTask>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "2000"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}
