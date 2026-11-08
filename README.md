# SkyLyrics

SkyLyrics is a client-side Fabric mod for Minecraft 1.21.11 that brings real-time, synchronized song lyrics from your desktop media player straight into your game world.

Whether you're listening to Spotify, watching a music video on YouTube, or playing local tracks through your media player, SkyLyrics displays live lyrics floating around you in 3D ("Air" mode), projected across the sky dome ("Sky" mode), or pinned to a clean HUD widget.

> **Note:** Media playback is detected locally via Windows System Media Transport Controls (SMTC). This means SkyLyrics runs on Windows 10/11 and requires an active player (Spotify desktop app, browser tab playing audio, or media player) in the background. No account linking or logins needed.

---

## Features

- **3D Air & Celestial Sky Lyrics:** Lyrics render directly in the game world—either hovering in 3D around your player or stretched across the night sky.
- **Real-Time Karaoke Sync:** Synchronized line-by-line and word-by-word timing with smooth animations and beat pulsing.
- **Local Media Detection:** Automatically hooks into Windows SMTC to read track names, artists, and playback state without API keys or account log-ins.
- **In-Game Customization:** Over 20 built-in color presets (Aurora, Sunset, Glacier, Matrix, Vaporwave, etc.), custom RGB palettes, text glow, sizing, and distance controls.
- **Custom Font Support:** Ships with 20+ open-source typefaces (Inter, Poppins, Bebas Neue, Quicksand, Fredoka, etc.) and lets you drop your own `.ttf` fonts into the config folder.
- **Minimalist HUD & Island Player:** An optional top-bar pill showing current song progress, artist, and playback status.
- **Sodium & Iris Compatible:** Built with a lightweight rendering pipeline that runs smoothly alongside Sodium and Iris shader packs.

---

## Controls

All keybinds can be rebound in `Options -> Controls -> SkyLyrics` or directly in the in-game settings dashboard:

| Key | Action |
| :--- | :--- |
| **K** | Open the SkyLyrics Settings Dashboard |
| **J** | Open the HUD Music Player |
| **O** | Toggle lyrics visibility on / off |
| **Down Arrow** | Play / Pause |
| **Right Arrow** | Next track |
| **Left Arrow** | Previous track |
| **[** / **]** | Adjust sync offset (-100 ms / +100 ms) |

---

## Server & Multiplayer Safety

SkyLyrics is **strictly client-side**. You can safely join any vanilla, Fabric, Paper, or Spigot multiplayer server without installing anything server-side.

- **No unfair advantages:** The mod only displays cosmetic text and HUD elements.
- **Underground lyrics:** The cave/underground mode simply adjusts text contrast against dark blocks—it does not light up caves, reveal ores, or highlight hidden players.

---

## Privacy & Network

- **Local detection:** Track info is read entirely on your local machine via Windows SMTC.
- **Lyrics queries:** When a new song starts, SkyLyrics sends a public, unauthenticated query (artist name and track title only) to open lyrics APIs ([LRCLIB](https://lrclib.net/) and NetEase) over HTTPS.
- **Zero tracking:** No personal identifiers, accounts, credentials, or telemetry data are ever collected, stored, or sent.

---

## Credits & Attributions

SkyLyrics incorporates ideas, code patterns, and libraries from the community:

- **[Studious Keybinds](https://modrinth.com/mod/studious-keybinds)** by *StudiousGoose* — Interactive keycap keyboard editor in the controls menu.
- **LyricsFlow** — Architectural foundation for 3D world lyrics and celestial dome projection.
- **LumiClient / Lotus** — Inspiration for the frosted-glass UI styling.
- **Libraries:** [NanoVG](https://github.com/memononen/nanovg) (Mikko Mononen, zlib), [LWJGL 3](https://www.lwjgl.org/) (BSD-3-Clause), [JNA](https://github.com/java-native-access/jna) (LGPL 2.1 / Apache 2.0).

### Bundled Fonts

All fonts bundled within this mod are open-source and distributed under their respective licenses:

| Font | Designer / Foundry | License |
| :--- | :--- | :--- |
| **Inter** | Rasmus Andersson | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Bebas Neue** | Ryoichi Tsunekawa (Dharma Type) | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Caveat Brush** | Pablo Impallari | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Comfortaa** | Johan Aakerlund | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Courgette** | Karolina Lach | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Damion** | Vernon Adams | [SIL OFL 1.1](https://openfontlicense.org/) |
| **DejaVu Sans** | DejaVu Fonts Team & Bitstream | [Bitstream Vera](https://dejavu-fonts.github.io/License.html) |
| **Fredoka** | Ben Nathan | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Grand Hotel** | Brian J. Bonislawsky & Jim Lyles | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Kaushan Script** | Pablo Impallari | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Lobster** | Pablo Impallari | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Marck Script** | Denis Masharov & Marck Fogel | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Norican** | Vernon Adams | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Outfit** | Rodrigo Fuenzalida | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Pacifico** | Vernon Adams | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Poppins** | Ninad Kale & Jonny Pinhorn | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Quicksand** | Andrew Paglinawan | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Righteous** | Brian J. Bonislawsky | [SIL OFL 1.1](https://openfontlicense.org/) |
| **SkyLyrics Script** | Pablo Impallari & Igino Marini | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Syne** | Lucas Descroix | [SIL OFL 1.1](https://openfontlicense.org/) |
| **Yellowtail** | Brian J. Bonislawsky | [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0) |
| **Yesteryear** | Brian J. Bonislawsky | [SIL OFL 1.1](https://openfontlicense.org/) |

---

## Links & Author

- **Author:** Maximus (Discord: `i.am.maximus`)
- **Source Code:** [GitHub Repository](https://github.com/lukaseschn-design/SkyLyrics-Source-code)
- **Bug Tracker:** [Report an Issue](https://github.com/lukaseschn-design/SkyLyrics-Source-code/issues)
