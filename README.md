# SkyLyrics

Client-side Fabric mod for Minecraft 1.21.11 that displays synchronized song lyrics in real-time from Spotify, YouTube, and Windows media players.

Lyrics can be displayed floating in 3D around the player ("Air" mode), projected across the sky dome ("Sky" mode), or pinned to the HUD.

---

## Features

- **3D Air & Sky Lyrics:** Render synchronized lyrics directly in the game world—either floating dynamically in 3D around you or mapped across the celestial sky dome.
- **Media Detection:** Reads currently playing track title, artist, and playback progress locally using Windows System Media Transport Controls (SMTC). No account login or credentials required.
- **Synchronized Karaoke Display:** Fetches synchronized word-by-word and line-by-line lyrics from public providers (LRCLIB, NetEase).
- **Customizable Appearance:** Choose from over 20 color presets or configure custom RGB palettes, glowing outlines, text shadow, scale, distance, and opacity.
- **Custom Fonts:** Bundled with open-source typefaces (Inter, Poppins, Quicksand, Bebas Neue, Fredoka, etc.) and allows loading custom `.ttf` fonts from the config folder.
- **HUD Music Player & Dynamic Island:** Minimalist top-bar pill showing playback status, title, artist, and mini waveform visualizer.
- **Atmospheric Sky Effects:** Optional animated aurora borealis curtains, stars, and music-reactive particles.
- **Optimization & Compatibility:** Lightweight rendering pipeline compatible with Sodium, Iris shaders, and Lithium. Includes built-in FPS optimization toggles.

---

## Screenshots

| Air Lyrics (3D Floating) | 3D Music Dashboard | Celestial Aurora Sky |
| :---: | :---: | :---: |
| ![Air Lyrics](docs/screenshots/air_lyrics.png) | ![3D Dashboard](docs/screenshots/3d_menu.png) | ![Aurora Sky](docs/screenshots/aurora_visuals.png) |

---

## Controls

Default keybindings (all configurable in Options -> Controls or in the in-game dashboard):

| Key | Function |
| :--- | :--- |
| **K** | Open SkyLyrics Settings Dashboard |
| **J** | Open HUD Music Player |
| **O** | Toggle Lyrics Visibility On / Off |
| **Down Arrow** | Play / Pause media playback |
| **Right Arrow** | Next track |
| **Left Arrow** | Previous track |
| **[** | Nudge lyrics sync -100 ms |
| **]** | Nudge lyrics sync +100 ms |

---

## Installation

1. Install **Fabric Loader** (version 0.17.0 or newer) for Minecraft **1.21.11**.
2. Install **Fabric API** for Minecraft 1.21.11.
3. Download the latest `SkyLyrics-v1.0+1.21.11.jar` from the [Releases](https://github.com/lukaseschn-design/SkyLyrics-Source-code/releases) page.
4. Place the `.jar` file into your `.minecraft/mods` folder.
5. Launch Minecraft and start any song on Spotify, YouTube, or your desktop media player.

### Requirements

- Minecraft: `1.21.11`
- Fabric Loader: `>= 0.17.0`
- Fabric API: `0.141.6+1.21.11`
- Java Runtime: `Java 21+`
- Operating System: Windows 10 / 11 (required for local SMTC media playback detection)

---

## Building from Source

SkyLyrics is built with Fabric Loom and Gradle:

```bash
git clone https://github.com/lukaseschn-design/SkyLyrics-Source-code.git
cd SkyLyrics-Source-code
./gradlew build
```

The compiled mod JAR will be located in `build/libs/`.

---

## Credits & Attributions

### Mod Inspiration & Concepts
- **Studious Keybinds** by *StudiousGoose* — Interactive keycap keyboard editor module in the controls menu.
- **LyricsFlow** — Architectural concept for 3D world lyrics and celestial dome projection.
- **LumiClient / Lotus** — Design inspiration for frosted-glass UI styling.

### Bundled Fonts
All fonts bundled within this mod are distributed in full compliance with their respective open-source licenses:
- **Inter** (Rasmus Andersson) — SIL Open Font License 1.1
- **Bebas Neue** (Dharma Type) — SIL Open Font License 1.1
- **Caveat Brush** (Pablo Impallari) — SIL Open Font License 1.1
- **Comfortaa** (Johan Aakerlund) — SIL Open Font License 1.1
- **Courgette** (Karolina Lach) — SIL Open Font License 1.1
- **Damion** (Vernon Adams) — SIL Open Font License 1.1
- **DejaVu Sans** (DejaVu Fonts Team) — Bitstream Vera License
- **Fredoka** (Ben Nathan) — SIL Open Font License 1.1
- **Grand Hotel** (Brian J. Bonislawsky & Jim Lyles) — SIL Open Font License 1.1
- **Kaushan Script** (Pablo Impallari) — SIL Open Font License 1.1
- **Lobster** (Pablo Impallari) — SIL Open Font License 1.1
- **Marck Script** (Denis Masharov & Marck Fogel) — SIL Open Font License 1.1
- **Norican** (Vernon Adams) — SIL Open Font License 1.1
- **Outfit** (Rodrigo Fuenzalida) — SIL Open Font License 1.1
- **Pacifico** (Vernon Adams) — SIL Open Font License 1.1
- **Poppins** (Indian Type Foundry) — SIL Open Font License 1.1
- **Quicksand** (Andrew Paglinawan) — SIL Open Font License 1.1
- **Righteous** (Brian J. Bonislawsky) — SIL Open Font License 1.1
- **SkyLyrics Script** (Pablo Impallari & Igino Marini) — SIL Open Font License 1.1
- **Syne** (Lucas Descroix) — SIL Open Font License 1.1
- **Yellowtail** (Brian J. Bonislawsky) — Apache License 2.0
- **Yesteryear** (Brian J. Bonislawsky) — SIL Open Font License 1.1

### Libraries
- **NanoVG** (Mikko Mononen) — zlib License
- **LWJGL 3** (LWJGL Team) — BSD-3-Clause License
- **JNA** (Java Native Access) — LGPL 2.1 / Apache 2.0
- **FabricMC** — Apache 2.0

### Lyrics APIs
- **LRCLIB** by *tranxuanthang* — Community-driven synchronized lyrics database.
- **NetEase Cloud Music** — Public lyrics catalog.

---

## License

All Rights Reserved (ARR). Free to download and use for personal gameplay, singleplayer, multiplayer servers, and video content.
