plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "net.varoxcraft.addons"
version = providers.environmentVariable("VERSION").getOrElse("1.0.0")

labyMod {
    defaultPackageName = "net.varoxcraft.addons.market"

    minecraft {
        registerVersion(versions.toTypedArray()) {
            runs {
                getByName("client") {
                    // Setze devLogin auf true, wenn du dich im Entwicklungsclient anmelden möchtest.
                    // devLogin = true
                }
            }
        }
    }

    addonInfo {
        namespace = "varox_market"
        displayName = "Varox Markt"
        author = "Varox Markt Contributors"
        description = "Zeigt aktuelle VaroxCraft-Marktdaten und Preisverläufe direkt in LabyMod an."
        minecraftVersion = "*"
        version = rootProject.version.toString()
    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")

    group = rootProject.group
    version = rootProject.version

    extensions.findByType(JavaPluginExtension::class.java)?.apply {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}