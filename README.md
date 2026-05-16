# Brewin' And Chewin'

<a href="https://www.curseforge.com/minecraft/mc-mods/brewin-and-chewin">
  <img src="http://cf.way2muchnoise.eu/full_637808_downloads.svg" alt="Curseforge Downloads">
</a>
<a href="https://discord.gg/M5AtJGPf">
  <img alt="Discord" src="https://img.shields.io/discord/855495317298741248?color=brightgreen&label=Discord">
</a>
<br>
<img alt="Brewin' And Chewin' Logo" src="https://i.imgur.com/EFkjwBq.png" width="50%">

### Overview

**Brewin' and Chewin'** is an addon mod for Farmer's Delight.

Using a keg, you can brew or ferment many new foods, including liquors, cheese, and fudge!

### Required Dependencies
- [Farmer's Delight](https://github.com/vectorwing/FarmersDelight/)

### Discord
- [Chef's Delights Discord](https://discord.gg/7PBaMYNtrg) for questions or whatever~

### Kotlin DSL
<details>

```groovy
repositories {
    maven("https://maven.greenhouse.lgbt/releases/") {
        name = "Greenhouse Maven"
    }
}

dependencies {
    // Depend on the Common project, for VanillaGradle and ModDevGradle.
    compileOnly "umpaz.brewinandchewin:BrewinAndChewin-common:${bnc_version}+${minecraft_version}"

    // Depend on the Fabric project, for Loom.
    modImplementation "umpaz.brewinandchewin:BrewinAndChewin-fabric:${bnc_version}+${minecraft_version}"

    // Depend on the NeoForge project, for ModDevGradle or NeoGradle.
    implementation "umpaz.brewinandchewin:BrewinAndChewin-neoforge:${bnc_version}+${minecraft_version}" { isTransitive = false }
}
```

</details>

### Groovy DSL
<details>

```groovy
repositories {
    maven {
        name = "Greenhouse Maven"
        url = "https://maven.greenhouse.lgbt/releases/"
    }
}

dependencies {
    // Depend on the Common project, for VanillaGradle and ModDevGradle.
    compileOnly("umpaz.brewinandchewin:BrewinAndChewin-common:${bnc_version}+${minecraft_version}")

    // Depend on the Fabric project, for Loom.
    modImplementation("umpaz.brewinandchewin:BrewinAndChewin-fabric:${bnc_version}+${minecraft_version}")

    // Depend on the NeoForge project, for ModDevGradle or NeoGradle.
    implementation("umpaz.brewinandchewin:BrewinAndChewin-neoforge:${bnc_version}+${minecraft_version}") { transitive false }
    implementation("house.greenhouse:greenhouseconfig:${Versions.GREENHOUSE_CONFIG}-neoforge")
    implementation("house.greenhouse:greenhouseconfig_toml:${Versions.GREENHOUSE_CONFIG_TOML}")
}
```
</details>