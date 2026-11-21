# API reference

The classes addon developers use. For a walkthrough, start with [Making an addon](addons.md).

- [MegaOrbitalsAPI](#megaorbitalsapi) · [Events](#events) · [Cannon](#cannon) · [Strike](#strike) · [Fx](#fx) · [Blocks](#blocks)
  · [Catalog](#catalog) · [StrikeInterceptor](#strikeinterceptor) · [Arsenal and Combo](#arsenal-and-combo)

Every method must be called on the main server thread.
Text with `&` colour codes ("legacy" text) works wherever a method takes a `String legacy`, name or title.

## MegaOrbitalsAPI

`dev.megaorbitals.api.MegaOrbitalsAPI`, the entry point for other plugins.

| Method | What it does |
|---|---|
| `int register(Plugin owner, Cannon... cannons)` | Adds cannons. Skips (with a warning) any whose id is taken or invalid. Returns how many were added. |
| `int unregister(Plugin owner)` | Removes your cannons. Happens automatically when your plugin is disabled. |
| `Cannon cannon(String id)` | A cannon by id, or null. |
| `Collection<Cannon> cannons()` | Every loaded cannon. |
| `List<Cannon> cannonsOf(Plugin owner)` | Cannons added by a plugin (MegaOrbitals itself for built-in ones). |
| `Plugin ownerOf(Cannon cannon)` | Which plugin added a cannon. |
| `boolean fire(Player shooter, Cannon cannon, boolean asItem)` | Fires for a player at what they look at (or their spotter mark), with every config rule. `asItem` = item rules (item cooldown, satellites, per-cannon permission). Returns true if it fired. |
| `Strike fireAt(Cannon cannon, Location targetBlock, Player shooter)` | Fires at a block with no checks. `shooter` may be null. |
| `ItemStack item(Cannon cannon, int amount)` | The cannon item, as `/orbitalgive` makes it. |
| `String cannonIdOf(ItemStack item)` | The cannon id on an item, or null. |
| `void addCategory(String category)` | Adds a menu tab. |
| `MegaOrbitals plugin()` | The plugin itself. Throws if it isn't enabled. |
| `boolean isReady()` | True once MegaOrbitals is enabled. |
| `int API_VERSION` | Currently `1`. |

## Events

Both are in `dev.megaorbitals.api` and are cancellable.

| Event | When | Methods |
|---|---|---|
| `OrbitalFireEvent` | A player fires with an item or `/orbital`, after MegaOrbitals' checks, before anything is used up. | `getPlayer()`, `getCannon()`, `getTarget()`, `isFromItem()` |
| `OrbitalStrikeEvent` | A strike's warning is over and it is about to land. | `getStrike()`, `getCannon()`, `getLocation()`, `getShooter()` |

## Cannon

`dev.megaorbitals.Cannon`, the base class of every cannon. It is also a Bukkit `Listener`.

**Constructor helpers** (call them in your constructor):

| Method | What it does |
|---|---|
| `super(String name, Material item, String... lore)` | Display name (`&` codes), item icon, description lines. |
| `warning(int ticks, String hexColor)` | Warning length and targeting ring colour. Default: 40 ticks, `#FF2020`. |
| `noBlockDamage()` | Defaults `break-blocks` to false. |
| `catalog(String tier, String category)` | Default tier and menu category. |
| `option(String key, Object default, String comment)` | A setting under `settings:` in the yml. Use numbers, booleans, strings or lists. |

**Override:**

| Method | What it does |
|---|---|
| `void strike(Strike s)` | **Required.** What the cannon does when it lands. |
| `void warn(Strike s)` | The warning. Default: a shrinking ring and beam (`Fx.orbitalWarning`). |
| `double warningRadius()` | Size of the default warning ring. Default: the `radius` setting (clamped 4 to 40) or 8. |
| `String id()` | The id. Default: class name in lower case. |

**Read settings:** `int i(key)`, `double d(key)`, `boolean b(key)`, `String str(key)`, `List<String> list(key)`, `Material mat(key)`.

**Other getters:** `id()`, `name()`, `lore()`, `item()`, `tier()`, `category()`, `warningTicks()`, `warningColor()`, `breaksBlocks()`,
`hurtsShooter()`, `autoRestoreSeconds()`. The field `plugin` (`MegaOrbitals`) is set once the cannon is loaded.

## Strike

`dev.megaorbitals.Strike`, one firing of a cannon. It is passed to `strike` and `warn`.

**Fields:** `plugin`, `cannon`, `world`, `shooter` (may be null).

**Where:**

| Method | Returns |
|---|---|
| `at()` | The impact point: top centre of the targeted block. A fresh copy each call. |
| `at(x, y, z)` | `at()` offset by x/y/z. |
| `block()` | The targeted block's location (for firing other cannons at the same spot). |
| `surface(Location l)` | Ground level at l's x/z. |
| `randomPoint(double r)` | A random ground point within r of the target. |

**Timers** (errors are caught and logged):

| Method | What it does |
|---|---|
| `later(long ticks, Runnable r)` | Run once after a delay. |
| `every(long period, int times, IntConsumer fn)` | Run `times` times, every `period` ticks. `fn` gets the step number. **`times <= 0` runs forever.** |
| `loop(long period, IntPredicate fn)` | Run every `period` ticks until `fn` returns false. |
| `run(Runnable r)` | Run now, with block changes recorded for this strike's undo (use it in your own event handlers). |

**Targets and damage:**

| Method | What it does |
|---|---|
| `victims(Location c, double r)` | Living entities within r, except the shooter (unless `hurt-shooter`) and immune players. |
| `players(Location c, double r)` | The players among those. |
| `nearest(l, r)` / `nearestPlayer(l, r)` | The closest victim, or null. |
| `spared(Entity e)` | True for the shooter (unless `hurt-shooter`) and immune players. |
| `damage(LivingEntity e, double amount)` | Damage credited to the shooter, times `safety.damage-multiplier`. Does nothing to spared entities. |
| `allies(double r)` | Players within r of the shooter, the shooter included (for helpful cannons). |
| `tempAttribute(entity, attribute, amount, operation, ticks)` | Changes an attribute (scale, speed, gravity...) and changes it back after `ticks`. |

**World:**

| Method | What it does |
|---|---|
| `explode(Location l, float power, boolean fire)` | An explosion that respects `break-blocks`. |
| `blocks(BlockJob job)` | Queue a lag-safe block job. Skipped if `break-blocks` is off. |
| `breaks()` | Whether this cannon may change blocks right now. |
| `tnt(l, power, fuse)` | Primed TNT that falls straight down. |
| `fallingBlock(l, material, vanish)` | A falling block; `vanish` = disappears instead of landing. |
| `mob(l, Class<T> type, Consumer<T> setup)` | Spawns a tagged mob that ignores its first fall. |
| `hunt(Entity e, double r)` | Makes a mob target the nearest player victim. |
| `tag(entity)` | Marks an entity as belonging to this cannon (block-damage rules, no core drops, undo). |
| `expire(entity, ticks)` | Removes the entity later. |
| `static fallTicks(double h)` | Roughly how many ticks something takes to fall h blocks. |

**Messages and sounds:** `sound(l, key, volume, pitch)`, `soundWorld(key, volume, pitch)`, `actionBar(player, legacy)`,
`tell(legacy)` (action bar to the shooter), `text(legacy)` (a `Component`).

**Prizes:** `give(Player p, List<String> entries)` hands out `"MATERIAL:amount"` entries (what doesn't fit is dropped);
`static items(List<String>)` turns them into `ItemStack`s.

**Settings shortcuts:** `i`, `d`, `b`, `str`, `mat` read the cannon's settings.

## Fx

`dev.megaorbitals.Fx`: particle, display and title helpers. All are static.

| Method | What it does |
|---|---|
| `p(world, particle, l, count, spread, speed)` | Any particle, with sensible default data. |
| `dust(world, l, color, size, count, spread)` | Coloured dust. |
| `block(world, particle, l, material, count, spread)` | Block-textured particles (`BLOCK`, `FALLING_DUST`). |
| `ring` / `dustRing` | A horizontal circle of particles. |
| `line` / `dustLine` | A line of particles from a to b. |
| `sphere` / `dustSphere` | Particles over a sphere's surface. |
| `circle(c, r, points)` | Points on a circle (for placing things). |
| `display(l, material, scale)` | A big floating block model. Move it with `teleport`; remove it when done. |
| `display(l, material, sx, sy, sz)` | A stretched block model. |
| `itemDisplay(l, material, scale)` | A big floating item model. |
| `dir(a, b)` | Unit vector from a to b. |
| `title(player, title, subtitle, stayTicks)` | A title (`&` codes). |
| `hex("#RRGGBB")` | A `Color`. |
| `orbitalWarning(strike, ticks, color, radius)` | The default warning. |

## Blocks

`dev.megaorbitals.Blocks`: block changes that `/orbital undo` can revert, and lag-safe jobs.

| Method | What it does |
|---|---|
| `set(block, material or blockData)` | Changes a block (skips unbreakable ones like bedrock and portals), recorded for undo. |
| `put(block, material or blockData[, physics])` | Changes any block, recorded for undo. |
| `breakable(block)` | False for bedrock, portals, barriers... |
| `loaded(world, x, z)` | Whether that chunk is loaded. |
| `surface(Location l)` | Ground level at l's x/z. |
| `box(c, rx, ry, rz, (block, dx, dy, dz) -> ...)` | A job visiting every block in a box, top layer first. |
| `columns(c, r, (x, z, dist) -> cost)` | A job visiting every x/z column within r, nearest first (spreads outward). |
| `sphere(c, r, material)` / `shell(c, r, thickness, material, onlyAir)` | Fill or hollow spheres. |
| `surface(c, r, material)` | Replace the top block of every column within r. |
| `throttle(job, perTick)` | Slow a job down so it visibly spreads. |

Run jobs with `s.blocks(job)`. A `BlockJob` has `int step(int budget)` (do up to `budget` units of work, return how many you did) and `boolean done()`.

## Catalog

`dev.megaorbitals.Catalog`: tiers and menu categories.

| Member | What it is |
|---|---|
| `TIERS` | `common`, `rare`, `epic`, `legendary`. |
| `CATEGORIES` | Menu tabs in order (read-only). |
| `addCategory(String)` | Adds a tab before `other`. |
| `define(id, tier, category)` | Default tier and category for an id. `Cannon.catalog(...)` calls this for you. |
| `tier(id)` / `category(id)` | The defaults for an id. |

## StrikeInterceptor

`dev.megaorbitals.StrikeInterceptor`: a cannon that implements `boolean intercept(Strike incoming)` is asked just before every strike lands.
Return true to cancel it. This is how the Orbital Defence Grid shoots strikes down. For most addons, `OrbitalStrikeEvent` is simpler.

## Arsenal and Combo

`dev.megaorbitals.Arsenal` holds the 40 `Payload`s and 25 `Pattern`s behind the arsenal cannons.
`dev.megaorbitals.Combo` is their base class. An addon can make its own combination of an existing payload and pattern:

```java
public final class FrostMegaSpiral extends Combo {
    public FrostMegaSpiral() { super(Arsenal.Payload.FROST, Arsenal.Pattern.SPIRAL); }
    @Override public String id() { return "myaddon_frostspiral"; }
}
```

Payloads and patterns are Java enums, so addons can't add new ones. To add a new effect, write a normal cannon instead.
