# 🎮 DupersUnited Public Addon

A feature-rich [Meteor Client](https://meteorclient.com/) addon focused on duplication exploits, server crashes, and
utility modules for Minecraft.

> **⚠️ Disclaimer:** This addon is for educational purposes only. Use responsibly and only on servers where you have
> permission.

> **⚠️ Disclaimer:** This README is AI-Generated as per request from the DupersUnited Skidders™®

---

## 📦 Installation

1. Install [Meteor Client](https://meteorclient.com/) for your Minecraft version
2. Download the latest release from [This Site](https://dupers.wtf/public-addon)
3. Place the `.jar` file in your `mods` folder
4. Launch Minecraft with Meteor Client

---

## ✨ Features

### 🔄 Duplication Modules

| Module               | Description                                                                                                                                              | Compatibility          |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------|
| **Bundle Dupe Plus** | Advanced bundle duplication with multiple kick/lag methods. Works on Paper and Spigot servers. Visit [dupedb.net](https://dupedb.net) for settings info. | Paper, Spigot          |
| **Book Dupe**        | Dupes your inventory using writable books. Reconnect before duping to save your inventory.                                                               | Paper < 1.21.1         |
| **Shulker Dupe**     | Classic shulker duplication exploit with GUI buttons.                                                                                                    | Vanilla 1.19 and below |
| **Trident Dupe**     | Automatic trident/bow duplication. Credit to Killet, Laztec & Ionar.                                                                                     | Vanilla < 1.19         |
| **Trade Dupe**       | Adds buttons for AxTrade and LF dupe to the Sign GUI.                                                                                                    | Patched                |

### 💥 Crash Modules

| Module           | Description                                                                        |
|------------------|------------------------------------------------------------------------------------|
| **Bundle Crash** | Crash servers (1.21.4) using bundle slot manipulation. Hold a bundle and activate! |

### ⚡ Exploit Modules

| Module             | Description                                                                                   |
|--------------------|-----------------------------------------------------------------------------------------------|
| **Attribute Swap** | Swaps hotbar slots on attack to preserve item durability. Optional shield-breaking with axes. |
| **Packet Delay**   | Queue and delay specific C2S packets. Sends all queued packets when disabled.                 |

### 🛠️ Utility Modules

| Module                      | Description                                                 |
|-----------------------------|-------------------------------------------------------------|
| **GUI Macros**              | Enables macro usage in GUIs (containers, inventories, etc.) |
| **Get NBT GUI**             | Retrieve NBT data from GUI slots                            |
| **ForEach Player Settings** | Configure settings for the `.foreachplayer` command         |

---

## 💬 Commands

| Command                                | Aliases            | Description                                                                         |
|----------------------------------------|--------------------|-------------------------------------------------------------------------------------|
| `.clickslot <slot> <button> <action>`  | `.cs`, `.cslot`    | Click a slot with specified action type                                             |
| `.wait <ms> <command>`                 | `.sleep`, `.delay` | Execute a command after a delay                                                     |
| `.repeat <times> <command>`            | -                  | Repeat a command X times. Use `%INDEX%` placeholder                                 |
| `.repeat-delay <ms> <times> <command>` | -                  | Repeat a command with delay between each. Use `%INDEX%` placeholder                 |
| `.foreachplayer <command>`             | -                  | Run a command for each player. Placeholders: `%PLAYER%`, `%PLAYER_UUID%`, `%INDEX%` |

### Command Examples

```
.repeat 10 /say Hello %INDEX%
.wait 5000 /home
.foreachplayer /msg %PLAYER% Hello!
.repeat-delay 1000 5 /tpa %PLAYER%
```

---

## 🔧 Technical Details

### Mixins Included

- `AbstractSignEditScreenMixin` - Sign GUI modifications
- `BookUpdateC2SPacketMixin` - Book packet manipulation
- `HandledScreenScreenMixin` - Container GUI enhancements
- `MacroMixin` - GUI macro support
- `ShulkerBoxScreenMixin` - Shulker dupe buttons

### Utility Classes

- `PacketUtils` - Comprehensive packet sending utilities with queueing system
- `HotbarScreenutils` - Hotbar slot ID conversion
- `RayCastUtils` - Raycast utilities
- `MsTimer` / `TickTimer` - Timing utilities for delayed actions

---

## 📜 Credits

- **YAYLOLDEV** - Main developer
- **Nummernuts & numberz** - Bundle dupe
- **Killet, Laztec & Ionar** - Trident dupe inspiration
- **KhaoDoesDev** - EnumArgumentType
- **AntiGravity (Google)** – Cute Catgirl AI that generated this README.md

---

## 📄 License

This project is open source. Feel free to contribute!

---

<div align="center">

**Made with 💜 by DupersUnited**

[GitHub](https://github.com/YAYLOLDEV/du-addon-public) • [dupedb.net](https://dupedb.net) • [dupers.wtf](https://dupers.wtf)

</div>
