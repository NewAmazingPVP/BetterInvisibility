# BetterInvisibility
BetterInvisibility is a plugin for Spigot improves Invisibility in Minecraft by many ways by using protocolLib! Plugin hides player's armor and items completely when they are invisible by intercepting packets.

Spigot Link: …………..

# Features
- Compatible with Minecraft versions 1.13 and higher, including the latest versions and most likely future versions as well.

- Config.yml 
```yaml 
# Better Invisibility Configuration

hide:
  helment: true
  chestplate: true
  leggings: true
  boots: true
  mainhand: true
  offhand: true
  potionParticles: false

# Currently every time invisible players get hit, they will appear visible for a split second for the hit animation but
# there is a workaround but this will make the invisible player have same knockback from every attack (custom knockback)
enable_workaround: false
```

# Planned Features
- If you have any suggestions or feature requests, please create a new issue in the project's GitHub repository.

# Installation
- Download the latest release of the plugin from the releases page and also download latest protocolLib version.
- Copy the downloaded jars to the plugins directory of your Minecraft server.
- After installation, BetterInvisiblity will change and handle invisiblity in your spigot server


# License
AutoViaUpdater is released under the MIT License. See the LICENSE file for more information.
