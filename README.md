# MegaOrbitals

A Paper plugin for Minecraft **26.1.2** with **302 overpowered orbital strike cannons**, each one a completely different weapon: nukes, black holes, tornadoes, mob armies, terrain tools, minigames, curses, buffs and more. Cannon #201, **Omega**, fires every other cannon at once.

## Install

1. Build the jar (see below) or grab it from Releases.
2. Drop `MegaOrbitals-<version>.jar` into your server's `plugins/` folder.
3. Start the server. Configs are created in `plugins/MegaOrbitals/`.

## Commands

| Command | Who | What it does |
|---|---|---|
| `/orbital <cannon>` | ops | Fires a cannon at the block you're looking at (unlimited range) |
| `/orbital menu` | ops | Paged GUI of every cannon; click to take one (shift-click for 16) |
| `/orbital list [page]` | ops | Lists cannons, 40 per page |
| `/orbital search <word>` | ops | Finds cannons by name or description |
| `/orbital info <cannon>` | ops | Shows a cannon's description and settings file |
| `/orbital reload` | ops | Reloads `config.yml` and every cannon config |
| `/orbitalgive <cannon\|all> [player] [amount]` | ops | Gives cannon items |

Anyone holding a cannon item can **right-click** to fire it at the block they're looking at.

## Permissions

| Permission | Default | Meaning |
|---|---|---|
| `megaorbitals.orbital` | op | Use `/orbital` |
| `megaorbitals.give` | op | Use `/orbitalgive` and take items from the menu |
| `megaorbitals.use` | everyone | Fire cannon items |
| `megaorbitals.immune` | nobody | Never targeted by cannons |
| `megaorbitals.bypass.cooldown` | op | Ignore cooldowns |
| `megaorbitals.cannon.<id>` | — | Needed per cannon when `cannons.per-cannon-permission` is on |

## Configuration

- **`config.yml`** controls the worlds whitelist or blacklist and the master block-damage switch. It also sets damage multiplier, spawn protection, rate limit, item and per-cannon cooldowns, item behaviour, disabled cannons, announcements, logging and every message.
- **`configs/<cannon>.yml`**: one file per cannon. It holds the name, lore and item, the warning time and colour, `break-blocks`, `hurt-shooter`, and that cannon's own `settings:`. Every option is commented.

Big block edits are spread over several ticks (`general.blocks-per-tick`) to keep lag down.

## Building

Needs **JDK 25** and **Maven**.

```
mvn clean package
```

The jar ends up in `target/`. The build also regenerates `src/main/resources/configs/*.yml` from the cannon classes.

## Adding a cannon

Create a class in `src/main/java/dev/megaorbitals/cannons/` that extends `Cannon`:

```java
public final class MyCannon extends Cannon {
    public MyCannon() {
        super("&cMy Cannon", Material.TNT, "Description line");
        option("radius", 10, "Range.");
    }

    @Override
    public void strike(Strike s) {
        s.explode(s.at(), (float) d("radius") / 2, false);
    }
}
```

It's picked up automatically. Its id is the class name in lower case (`mycannon`), and its config is generated on the next build or server start. `Strike`, `Fx` and `Blocks` provide helpers for timers, targets, particles and lag-safe block editing.
