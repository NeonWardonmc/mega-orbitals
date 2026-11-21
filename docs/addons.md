# Making an addon

An addon is a separate Paper plugin that adds its own cannons to MegaOrbitals, or reacts when cannons are fired.
Addon cannons work exactly like the built-in ones. Each one:

- gets its own `configs/<id>.yml`
- shows up in `/orbital`, the menu, `/orbitalgive`, `/orbital search` and core drops
- can be undone with `/orbital undo`

A complete, working addon is in [examples/example-addon](../examples/example-addon). Copying it is the quickest way to start.

Addon support was added in **MegaOrbitals 1.6.0**.

## 1. Set up the project

Your addon needs Java 25 and the Paper 26.1.2 API, plus MegaOrbitals as a `provided` dependency.
`provided` means it isn't bundled into your jar, because the server already has it.

First, put MegaOrbitals in your local Maven repository. Do one of these:

```
# either build MegaOrbitals from source:
git clone https://github.com/NeonWardonmc/mega-orbitals.git
cd mega-orbitals
mvn install

# or install a release jar you downloaded:
mvn install:install-file -Dfile=MegaOrbitals-1.6.0.jar -DgroupId=dev.megaorbitals -DartifactId=MegaOrbitals -Dversion=1.6.0 -Dpackaging=jar
```

Then, in your `pom.xml`:

```xml
<dependency>
    <groupId>dev.megaorbitals</groupId>
    <artifactId>MegaOrbitals</artifactId>
    <version>1.6.0</version>
    <scope>provided</scope>
</dependency>
```

With Gradle, put the jar in a `libs/` folder and use `compileOnly(files("libs/MegaOrbitals-1.6.0.jar"))`.

## 2. plugin.yml

Add `depend: [MegaOrbitals]` so MegaOrbitals loads first:

```yaml
name: MyOrbitalAddon
version: 1.0.0
main: com.example.myaddon.MyAddon
api-version: '26.1'
depend: [MegaOrbitals]
```

Use `softdepend: [MegaOrbitals]` instead if your plugin should also work without it.
In that case, check `getServer().getPluginManager().isPluginEnabled("MegaOrbitals")` before touching any MegaOrbitals class.

## 3. Write a cannon

A cannon extends `dev.megaorbitals.Cannon`:

```java
public final class ConfettiCannon extends Cannon {

    public ConfettiCannon() {
        super("&d&lConfetti Cannon", Material.PINK_DYE,      // name, item icon
              "Covers the target in bursts", "of colourful confetti.");  // description lines
        warning(20, "#FF80FF");     // warning: 20 ticks (1 second), pink targeting ring
        noBlockDamage();            // this cannon never changes blocks
        catalog("common", "fun");   // default tier and menu tab
        option("bursts", 20, "How many confetti bursts.");   // appears under settings: in the yml
        option("radius", 8.0, "How far the confetti spreads.");
    }

    @Override
    public void strike(Strike s) {
        s.every(2, i("bursts"), t -> {
            Location l = s.randomPoint(d("radius")).add(0, 1.5, 0);
            Fx.dust(s.world, l, Color.FUCHSIA, 1.8f, 25, 0.6);
            s.sound(l, "entity.firework_rocket.twinkle", 1.5f, 1.2f);
        });
    }
}
```

- **The constructor** sets the defaults: name, icon, description, warning, tier and category, and settings.
  Server owners can change all of these in `configs/<id>.yml`.
- **`strike(Strike s)`** runs when the warning ends. `s` knows where the strike lands (`s.at()`), who fired it (`s.shooter`),
  and has helpers for damage, timers, mobs, blocks and effects. See the [API reference](api-reference.md#strike).
- **Read settings** with `i("key")`, `d("key")`, `b("key")`, `str("key")` and `list("key")`.
  They return the server owner's value, or your default.
- **Custom warning:** override `warn(Strike s)` to replace the default ring and beam.

### The id

A cannon's id is its class name in lower case (`ConfettiCannon` → `confetticannon`). It is used in `/orbital <id>`,
in the config file name, and in permissions. If the id is already taken, your cannon is skipped and a warning is printed.
To pick your own id, override it:

```java
@Override public String id() { return "myaddon_confetti"; }
```

Ids may only contain `a-z`, `0-9`, `_` and `-`.

### Tier and category

Call `catalog(tier, category)` in the constructor.

- **Tier:** `common`, `rare`, `epic` or `legendary`. It sets which Orbital Core can give the cannon, and its satellite cost.
- **Category:** one of the built-in menu tabs (`explosives`, `beams`, `weapons`, `space`, `weather`, `terrain`, `mobs`, `control`,
  `curses`, `buffs`, `utility`, `minigames`, `fun`, `other`), or a new name, which adds a new tab.

Without `catalog(...)`, a cannon is `rare` / `other`.

## 4. Register it

In your plugin's `onEnable`:

```java
@Override
public void onEnable() {
    MegaOrbitalsAPI.register(this, new ConfettiCannon(), new PumpkinPatch());
}
```

That's all. When your plugin is disabled, MegaOrbitals removes its cannons automatically.
`register` returns how many cannons were added.

## 5. Rules for well-behaved cannons

- **Change blocks only with `Blocks.set(...)` or `Blocks.put(...)`.** Never use `block.setType(...)`.
  Otherwise `/orbital undo` can't put the blocks back.
  Changes are recorded while your strike's code runs, which covers `strike`, `s.later`, `s.every`, `s.loop` and `s.blocks` jobs.
  From one of your own event handlers, wrap block changes in `s.run(() -> ...)`.
- **Respect `break-blocks`.** `s.explode(...)` and `s.blocks(...)` already do. Check `s.breaks()` before other block changes.
- **Big block edits** (more than a few hundred blocks) should go through `s.blocks(job)`, which spreads the work over ticks.
  Use the ready-made jobs `Blocks.box`, `Blocks.columns`, `Blocks.sphere`, `Blocks.shell` and `Blocks.surface`, plus `Blocks.throttle`.
- **Hurt things with `s.damage(entity, amount)`** and find them with `s.victims(...)` / `s.players(...)`.
  These respect `hurt-shooter`, `damage-multiplier` and `megaorbitals.immune`.
- **Tag what you spawn** with `s.tag(entity)`, or use `s.mob(...)`, `s.tnt(...)` and `s.fallingBlock(...)`, which tag for you.
  Tagged explosions follow your cannon's `break-blocks` setting, and tagged mobs never drop Orbital Cores.
- **Clean up.** Remove displays and mobs when your cannon ends. `s.expire(entity, ticks)` does this for you.
- **Use the scheduler helpers** (`s.later`, `s.every`, `s.loop`). They catch errors so one broken cannon can't crash a timer.
  `s.every(period, times, ...)` runs forever when `times <= 0`, so clamp counts that come from config.
- **Everything runs on the main server thread.** Don't call MegaOrbitals from async tasks.

### Listening to events inside a cannon

Cannons are registered as event listeners (owned by your plugin), so you can put `@EventHandler` methods straight in a cannon class.
Each registered cannon is a single object, so keep per-strike state in maps. See `MimicChests` or `BombDefusal` in MegaOrbitals' source for examples.

## Events

Listen to these like any Bukkit event.

### OrbitalFireEvent

A player is about to fire a cannon with an item or `/orbital`. It is called after MegaOrbitals' own checks pass
(disabled cannon, world, permission, cooldown, range, spawn protection, satellite charge). That is before anything is used up.
Cancel it to stop the shot; no cooldown starts and no charge or item is spent.

```java
@EventHandler
public void onFire(OrbitalFireEvent e) {
    // Example: only allow cannons in regions where the player can build (pseudo-code for your protection plugin).
    if (!myProtection.canBuild(e.getPlayer(), e.getTarget())) {
        e.setCancelled(true);
        e.getPlayer().sendMessage("You can't call a strike there.");
    }
}
```

Methods: `getPlayer()`, `getCannon()`, `getTarget()` (the aimed block), `isFromItem()`.

### OrbitalStrikeEvent

A strike's warning has finished and it is about to land. Cancel it to stop the effect (the warning has already been seen).

It is called for strikes fired normally, through `MegaOrbitalsAPI.fireAt`, by Roulette, and by Omega when its `include-warnings` is on.
The Orbital Defence Grid and other [`StrikeInterceptor`](api-reference.md#strikeinterceptor) cannons are asked afterwards,
and only if the event wasn't cancelled.

Methods: `getStrike()`, `getCannon()`, `getLocation()`, `getShooter()` (may be null).

## Using MegaOrbitals from any plugin

`MegaOrbitalsAPI` also lets other plugins use the cannons. This is handy for quests, shops, minigames and so on.

```java
Cannon meteor = MegaOrbitalsAPI.cannon("meteor");

// Give a player a cannon item
player.getInventory().addItem(MegaOrbitalsAPI.item(meteor, 1));

// Fire it for a player with all config rules (cooldowns, worlds, range...), as if they used /orbital
MegaOrbitalsAPI.fire(player, meteor, false);

// Fire it at a block with no checks at all (shooter may be null)
MegaOrbitalsAPI.fireAt(meteor, someBlockLocation, null);

// Is this item a cannon?
String id = MegaOrbitalsAPI.cannonIdOf(itemStack);
```

See the [API reference](api-reference.md#megaorbitalsapi) for every method.

## Testing your addon

1. Build your addon jar (`mvn package`) and copy it, plus the MegaOrbitals jar, into a Paper 26.1.2 server's `plugins/` folder.
2. Start the server. Warnings about ids that are taken or invalid appear in the console. Turn on `general.debug` in MegaOrbitals' `config.yml` to also see one line for each cannon that was added.
3. `/orbital info <your id>` shows the cannon, and `/orbital <your id>` fires it.
4. Fire it, then `/orbital undo`, to check every block change is put back.

## Compatibility

- The API lives in `dev.megaorbitals.api` (plus the `Cannon`, `Strike`, `Fx`, `Blocks` and `Catalog` classes).
  `MegaOrbitalsAPI.API_VERSION` is increased if it changes in a way addons need to know about.
- Build against the oldest MegaOrbitals version you support. Addons need 1.6.0 or newer.
