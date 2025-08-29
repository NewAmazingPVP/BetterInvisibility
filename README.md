# BetterInvisibility

BetterInvisibility is a modern Spigot/Paper/Folia plugin that makes invisibility truly invisible by hiding a player's
armor and hand items from other players via PacketEvents — reliably across versions and server types.

Spigot Link: https://www.spigotmc.org/resources/betterinvisibility.110044/

**Highlights**

- Packet-level equipment hiding using PacketEvents (no ProtocolLib)
- Folia-compatible scheduling (region/entity safe)
- Supports 1.13+ (tested against latest APIs)
- Zero client mods, no combat/KB workarounds
- Java 8 compatible build and runtime

- Config.yml

```yaml
# Better Invisibility Configuration

hide:
  helmet: true
  chestplate: true
  leggings: true
  boots: true
  mainhand: true
  offhand: true
  potionParticles: false
```

# Planned Features

- If you have any suggestions or feature requests, please create a new issue in the project's GitHub repository.

**Installation**

- Download the latest release from the Releases page.
- Drop the jar into your server's `plugins` folder.
- Restart the server. Tweak `config.yml` as desired and `/reload`.

**Folia Support**

- The plugin detects Folia at runtime and uses region-safe entity scheduling.
- No Paper/Folia dependencies required at compile time.

# License

BetterInvisibility is released under the MIT License. See `LICENSE` for details.

—

Built automatically on each push via GitHub Actions; tagged builds publish release jars.
