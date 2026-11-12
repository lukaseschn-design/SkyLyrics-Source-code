# SkyLyrics

A client-side Fabric mod for Minecraft 1.21.11 that displays synced song lyrics in game. It detects whatever music is playing in the background on your PC (Spotify, browser tabs, media players) via Windows media controls (SMTC).

Lyrics can float in 3D around you in the world, appear across the night sky, or sit in a small HUD widget.

## How it works
Make sure you're playing music in Spotify (desktop app), YouTube in your browser, or another media player. The mod reads track info locally through Windows and pulls lyrics from lrclib.net / NetEase.

## Controls
- **K** - Open settings (colors, fonts, offsets)
- **J** - HUD music player
- **O** - Toggle lyrics on / off
- **Down Arrow** - Play / Pause
- **Right / Left Arrow** - Next / previous track
- **[ / ]** - Adjust timing offset (-100ms / +100ms)

Keybinds can be changed in Minecraft's standard controls menu.

## Compatibility
- 100% client-side (works on any vanilla or modded server).
- Compatible with Sodium and Iris shaders.

## Credits & Thanks
- **LyricsFlow** - 3D world lyrics and sky projection base
- **LumiClient** - Menu and UI styling
- **Studious Keybinds** - Keycap keyboard editor module
- Libraries: NanoVG, LWJGL 3, JNA
- Fonts: Inter, Bebas Neue, Caveat Brush, Comfortaa, Courgette, Damion, DejaVu Sans, Fredoka, Grand Hotel, Kaushan Script, Lobster, Marck Script, Norican, Outfit, Pacifico, Poppins, Quicksand, Righteous, Syne, Yellowtail, Yesteryear.
