# Varox Markt für LabyMod 4

**Varox Markt** zeigt öffentlich verfügbare Daten der VaroxCraft-Item-Börse direkt im Minecraft-Client an. Das Addon lädt die Marktantwort im Hintergrund, hält sie lokal im Arbeitsspeicher vor und aktualisiert sie höchstens einmal pro Minute. Es überträgt weder Minecraft-Kontodaten noch andere Spielerdaten.

| Eigenschaft | Stand |
|---|---|
| Addon-Version | 1.0.0 |
| Addon-Kennung | `varox_market` |
| Datenquelle | `https://varoxcraft.net/markt/data` |
| Build-Prüfung | Erfolgreich mit Java 21 und Minecraft 1.21.11 |
| Auslieferungsdatei | `build/libs/varox-market-release.jar` |

## Funktionen

Das Addon registriert ein frei positionierbares HUD-Widget. Dieses zeigt die drei größten positiven oder negativen Preisbewegungen mit aktuellem Kaufpreis. Im LabyMod-Widget-Editor kann das Widget aktiviert und an eine beliebige Bildschirmposition verschoben werden. LabyMod-HUD-Widgets sind ausdrücklich für solche anpassbaren Inhalte vorgesehen.[1]

Der Chatbefehl liefert eine kompakte Item-Detailansicht. Der angezeigte Verlauf wird aus den Verlaufspunkten des Feldes `spark` gezeichnet. Weil der öffentliche Endpunkt keine Zeitachse der Einzelpunkte dokumentiert, ist die Sparkline eine **relative Verlaufsgrafik** ohne absolute Zeitbeschriftung.

| Befehl | Funktion |
|---|---|
| `/varoxmarkt <Itemname>` | Zeigt Kaufpreis, Verkaufspreis, Trend und Sparkline eines Items. |
| `/varoxmarkt top` | Listet die fünf stärksten Marktbewegungen. |
| `/varoxmarkt refresh` | Startet sofort eine Hintergrundaktualisierung. |
| `/vmarkt <Itemname>` | Kurzform des Hauptbefehls. |

Beispiele sind `/varoxmarkt Coal Block`, `/varoxmarkt Bone Meal` und `/varoxmarkt top`.

## Installation

Die Datei **`build/libs/varox-market-release.jar`** ist das installierbare Produktionsartefakt. Kopiere ausschließlich diese Release-Datei in den Addon-Ordner deiner LabyMod-4-Installation und starte LabyMod anschließend neu. LabyMod weist darauf hin, dass nur ein JAR mit dem Suffix `-release.jar` für den Produktionsclient geeignet ist.[2]

| Betriebssystem | Addon-Ordner |
|---|---|
| Windows | `%APPDATA%\\.minecraft\\labymod-neo\\addons` |
| Linux | `~/.minecraft/labymod-neo/addons` |
| macOS | `~/Library/Application Support/minecraft/labymod-neo/addons` |

Nach dem Neustart erscheint **Varox Markt** in den LabyMod-Einstellungen. Aktiviere danach im Widget-Editor das Widget **Varox Markt**, falls es noch nicht sichtbar ist.

## Eigenen Build erstellen

Für eigene Änderungen wird Java 21 benötigt. Das Projekt basiert auf dem offiziellen LabyMod-4-Addon-Template. Die Grundstruktur verwendet eine mit `@AddonMain` markierte `LabyAddon`-Hauptklasse; LabyMod erzeugt daraus die Addon-Metadaten.[3]

```bash
./gradlew build
./gradlew createReleaseJar
```

Der Release-Build liegt anschließend im Verzeichnis `build/libs/`. Für eine schnelle Prüfung gegen Minecraft 1.21.11 wurde während der Entwicklung folgende Variante erfolgreich ausgeführt:

```bash
./gradlew build createReleaseJar -Pnet.labymod.minecraft-versions=1.21.11
```

## Technische Hinweise

Das Addon fragt ausschließlich die öffentliche Marktantwort von VaroxCraft ab. Wenn die Quelle vorübergehend nicht erreichbar ist, behält es die zuletzt erfolgreich geladenen Daten im Speicher und zeigt im HUD einen Fehlerstatus an. Netzwerkaufrufe laufen asynchron, damit sie weder Rendering noch Chat-Eingaben blockieren.

> Dieses Projekt ist ein unabhängiges Client-Addon und nicht als offizielles VaroxCraft-Produkt ausgewiesen. Vor einer öffentlichen Veröffentlichung sollten Name, Autor und Markenverwendung mit dem Serverteam abgestimmt werden.

## Quellen

[1] [LabyMod 4 Developer Portal: HUD Widgets](https://dev.labymod.net/pages/addon/features/hud-widgets/)

[2] [LabyMod 4 Developer Portal: Test in the Production Environment](https://dev.labymod.net/pages/addon/publishing/testing/)

[3] [LabyMod 4 Developer Portal: Your First LabyMod 4 Addon](https://dev.labymod.net/pages/addon/setup/setup/)
