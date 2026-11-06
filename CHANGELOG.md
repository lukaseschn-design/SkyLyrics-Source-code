# Changelog

All notable changes to SkyLyrics will be documented in this file.

## [1.0] - 2026-11-06

### 🎉 Initial Release

#### Lyrics Rendering
- **3D Air Lyrics** — Floating word chunks with pop-in animations, drift, and random rotation
- **Sky Lyrics** — Full lyric lines projected onto the celestial dome at configurable distance
- **Wall Lyrics** — Lyrics projected onto block surfaces with polygon offset (no Z-fighting)
- **Screen Lyrics** — Classic on-screen subtitle display

#### Music Integration
- Automatic media detection via **Windows SMTC** (System Media Transport Controls)
- Works out of the box with **Spotify**, **YouTube**, **Apple Music**, **SoundCloud**, and any SMTC-compatible player
- Optional **Spotify Web API** connection for enhanced position accuracy
- In-game **volume control** for Spotify via Windows Core Audio (WASAPI)

#### Lyrics Sources
- **Musixmatch** — Word-synced RichSync karaoke lyrics
- **NetEase Cloud Music** — YRC word-synced karaoke lyrics
- **LRCLIB** — Timed LRC line-synced lyrics (exact + fuzzy matching)
- Automatic speed/tempo compensation for nightcore and slowed edits

#### Visual Effects
- 8 built-in neon color themes (Lilac, Black, Grey, Purple, Pink, Blue, Red, Gold)
- Fully custom color picker (main, glow 1, glow 2)
- Word-level beat pulsing synchronized to sung words
- Soft glow halos around lyrics text
- Twinkling star overlay (configurable density)
- Floating light motes drifting around the player
- Falling light streaks

#### UI & HUD
- **Dynamic Island Top Bar** — Song title, artist, cover art, and live karaoke lyrics
- **HUD Music Player** — Full music player with cover art, progress bar, and controls
- **Keyboard & CPS display** — On-screen keyboard visualization and clicks per second
- **HUD Editor** — Drag-and-drop repositioning and resizing of all HUD elements

#### Customization
- Custom `.ttf` font support (drop fonts into config folder)
- Multiple lyrics animation styles (typed, slide, arc, varied sizes)
- Adjustable lyrics scale, distance, opacity, and animation speed
- Underground mode (lyrics visible through blocks in caves)
- View lock toggle (lyrics follow camera or stay fixed)
- Profile system for saving and loading configurations

#### Compatibility
- Fully compatible with **Sodium** and **Iris** shaders
- Automatic shader detection and render mode adjustment
- Works alongside Mod Menu for easy settings access

#### Technical
- Minecraft **1.21.11** (Fabric Loader >= 0.17.0, Java 21+)
- Modern Blaze3D rendering pipeline (no deprecated OpenGL calls)
- Camera-relative coordinate system (no FP32 precision jitter)
- Fog compensation for sky-distance lyrics rendering

