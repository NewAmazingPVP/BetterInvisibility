<div align="center">

# BetterInvisibility v4.0.0

Make invisibility actually invisible. Cleanly hides armor and held items from other players at the packet layer.

[![SpigotMC](https://img.shields.io/badge/SpigotMC-Resource-orange)](https://www.spigotmc.org/resources/betterinvisibility.110044/)
![Platforms](https://img.shields.io/badge/Platforms-Spigot%20%7C%20Paper%20%7C%20Purpur%20%7C%20Folia-5A67D8)
![MC](https://img.shields.io/badge/Minecraft-1.13%E2%86%92Latest-2EA043)
![Java](https://img.shields.io/badge/Java-8%2B-1F6FEB)
![License](https://img.shields.io/badge/License-MIT-0E8A16)

</div>

> TL;DR: Drop it in. When a player gets the vanilla invisibility effect, their armor and hands disappear for everyone else. No client mods, no ProtocolLib.

---

## Table of Contents

- [Highlights](#highlights)
- [Compatibility](#compatibility)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [How It Works](#how-it-works)
- [Troubleshooting](#troubleshooting)
- [Building From Source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)

---

## Highlights

- Packet-level equipment hiding via PacketEvents
- Optional: strip invisibility potion particles
- Folia-friendly (no unsafe async Bukkit calls; region-safe approach)
- Works across 1.13+ without per-version hacks
- Tiny footprint; zero commands, zero permissions

---

## Compatibility

- Server platforms: Spigot, Paper, Purpur, Folia
- Minecraft: 1.13 → latest
- Java: 8+

> Note: This is a Spigot/Paper-side plugin. It is not for proxies like Velocity/BungeeCord.

---

## Requirements

- Java 8 or newer
- Internet not required at runtime

PacketEvents is bundled (shaded) with the plugin. You do not need ProtocolLib.

---

## Installation

1. Download the latest jar from Spigot:
   https://www.spigotmc.org/resources/betterinvisibility.110044/
2. Place it in your server's `plugins/` folder.
3. Start/restart the server to generate `config.yml`.
4. Adjust `plugins/BetterInvisibility/config.yml` if desired.

---

## Quick Start

Out of the box, when a player gains invisibility, everyone else stops seeing their armor and held items. When it wears off, gear reappears.

No commands. No permissions. Done.

---

## Configuration

`plugins/BetterInvisibility/config.yml`

```yaml
# Better Invisibility

hide:
  helmet: true
  chestplate: true
  leggings: true
  boots: true
  mainhand: true
  offhand: true
  potionParticles: false
```

- `helmet/chestplate/leggings/boots`: hide armor pieces while invisible
- `mainhand/offhand`: hide held items while invisible
- `potionParticles`: if true, re-applies invisibility without particles

Changes apply immediately on next invisibility effect change for that player.

---

## How It Works

- Listens for invisibility effect add/remove events
- Intercepts outgoing equipment packets for invisible players and replaces items with air for other viewers
- Optionally re-applies the effect to suppress particles
- Sends a clean equipment refresh when invisibility ends

This keeps gameplay intact (damage/knockback unchanged) and needs no client mods.

---

## Troubleshooting

- Still seeing armor? Make sure the player actually has the vanilla invisibility effect active.
- Seeing particles when you don't want them? Set `hide.potionParticles: true`.
- Mixed-version packs causing issues? Keep to standard Spigot/Paper/Purpur/Folia on 1.13+.

If you hit an edge case, open an issue with steps to reproduce and your `config.yml`.

---

## Building From Source

Requires Maven and Java 8+.

```bash
mvn -DskipTests package
```

The shaded jar is produced in `target/`.

---

## Contributing

Bug reports and small PRs welcome. Keep changes focused and include a brief test plan.

Spigot page: https://www.spigotmc.org/resources/betterinvisibility.110044/

---

## License

MIT — see `LICENSE`.
