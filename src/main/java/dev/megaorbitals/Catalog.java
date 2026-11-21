package dev.megaorbitals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default tier and menu category for every built-in cannon. Each cannon's yml can override both
 * ("tier:" and "category:"). Cannons not listed here default to rare / other.
 * Generated and addon cannons set their own defaults with {@link #define} (see Cannon#catalog).
 */
public final class Catalog {

    private Catalog() {}

    public static final List<String> TIERS = List.of("common", "rare", "epic", "legendary");
    private static final List<String> CATEGORY_LIST = new java.util.concurrent.CopyOnWriteArrayList<>(List.of("explosives", "beams",
            "weapons", "space", "weather", "terrain", "mobs", "control", "curses", "buffs", "utility", "minigames", "fun", "other"));
    /** Menu categories in tab order (read-only view; addons add theirs with {@link #addCategory}). */
    public static final List<String> CATEGORIES = java.util.Collections.unmodifiableList(CATEGORY_LIST);

    private static final Map<String, String> TIER = new HashMap<>();
    private static final Map<String, String> CATEGORY = new HashMap<>();

    private static void tier(String t, String ids) { for (String id : ids.split(" ")) TIER.put(id, t); }
    private static void cat(String c, String ids) { for (String id : ids.split(" ")) CATEGORY.put(id, c); }

    static {
        tier("legendary", "meganuke omega deathstar antimatter blackhole dragonsummon witherstorm ioncannon supernova voidgate "
                + "sandworm halocollapse orbitalhammer meteor wardens doomclock exile banishend guardianangel titanform");
        tier("epic", "drill bombingrun clusterbomb railgun laser sweeper prism sonicboom whitehole gravitywell zerog asteroidbelt "
                + "homingmissiles satellite emp tsunami volcano earthquake tornado thor blizzard sinkhole upheaval terraformer collapse "
                + "chainreaction fuelair gunship droneswarm teslatower thermite bunkerbuster lasercutter freezeray flamethrower kraken raid "
                + "undeadarmy zombieapocalypse doppelganger plague curse vampire glitch inverter petrify netherrift endrift sculk skyjail "
                + "bury portaltrap banishnether stasis rewind medusa markedfordeath deadmansswitch giant giantmaker evolution "
                + "shrinkingzone kingofthehill aegis healingbeacon hollowearth glassify bodyswap");
        tier("common", "fireworks celebration partydrop skymessage snowglobe rottenrain disco aurora rainbow xpshower flare bridge "
                + "watchtower tunnelbore respawnbeacon beammeup lootvacuum tripwire recon cavalry pinball launchpad scramble shuffle "
                + "pumpkincurse polymorph silence starvation potionroulette fishrain batswarm chickenjockeys explodingsheep eggbombs "
                + "llamabarrage snowballfight bouncepads icerink tarpit quicksand thornfield witherroses overgrowth desertify deforest "
                + "prospector drain minersblessing speedzone cloakfield smokescreen decoys flashbang hotfloor redlightgreenlight hedgemaze "
                + "spleefarena duelarena treasurehunt capturenet beastmaster golemguard wolfpack beeswarm shrinkray tinyform moongravity "
                + "heavyweight femboyify bubble yank catapult centrifuge magnetize repulsor tether");

        // 1.4.0
        tier("legendary", "kaiju defencegrid spacestation upsidedown");
        tier("epic", "mechpilot structurecopier mirrormatch wavesurvival echostrike orbitalelevator deflector capturetheflag");
        tier("rare", "mimicchests boobytrap bombdefusal linkedportals asteroidmine bountyhunter");
        tier("common", "lootpinata parkourtower");
        cat("explosives", "boobytrap");
        cat("weapons", "echostrike mechpilot");
        cat("space", "spacestation asteroidmine");
        cat("terrain", "upsidedown");
        cat("mobs", "kaiju");
        cat("curses", "bountyhunter");
        cat("buffs", "deflector");
        cat("utility", "linkedportals orbitalelevator structurecopier defencegrid");
        cat("minigames", "bombdefusal parkourtower wavesurvival capturetheflag mirrormatch");
        cat("fun", "mimicchests lootpinata");

        cat("explosives", "classic meganuke drill bombingrun clusterbomb bouncingbetty minefield doomclock railgun bombardment "
                + "satellite homingmissiles chainreaction domino aftershock fuelair halocollapse mortar shrapnel bunkerbuster tnttrain "
                + "crystalfield detonatemobs explodingsheep eggbombs ioncannon groundpound orbitalhammer gunship gravitybomb oilslick");
        cat("beams", "laser sweeper deathstar prism sonicboom lasergrid lighthouse sunstrike lasercutter freezeray flamethrower "
                + "leafblower vacuumcannon droneswarm teslatower turrets");
        cat("weapons", "anvilrain arrowstorm tridentbarrage shulkerswarm evokerfangs swordrain sniper boomerang orbitingblades "
                + "diamondrain rocketride executioner bloodpact dragonbreath poisongas dragonstrafe thermite");
        cat("space", "supernova antimatter blackhole whitehole gravitywell zerog meteor asteroidbelt starfall comet aurora eclipse "
                + "moongravity voidgate");
        cat("weather", "tsunami volcano earthquake tornado lightningstorm chainlightning thor blizzard hailstorm sandstorm firenado "
                + "waterspout whirlpool avalanche acidrain fractallightning weathercontrol dawn magmageyser windburst firestorm napalm "
                + "lavapour shockwave");
        cat("terrain", "sinkhole terraformer flatten forest netherrift endrift sculk sporebomb upheaval glacier geode lavalake "
                + "desertify overgrowth witherroses hellfloor fracture sandworm hollowearth inverter prospector deforest petrify glassify "
                + "drain termites ruinify locusts collapse glitch rainbow shatter midas thornfield floorislava lavamoat quicksand tarpit "
                + "icerink bouncepads crystalspikes geomancer spiketrap spikeline boulders wreckingball crusher");
        cat("mobs", "zombieapocalypse creeperswarm witherstorm wardens undeadarmy raid beeswarm wolfpack golemguard phantoms "
                + "enderinvasion kraken doppelganger slimerain infestation ghastfleet breezesquad zoglinstampede llamabarrage "
                + "snowballfight vexswarm snipers spidernest dragonsummon giant illusionercoven piglinraid drownedlegion frozenlegion "
                + "bogambush creakingwoods mobtower chickenjockeys batswarm fishrain phantomlegion witchcoven mitosis evolution creepify "
                + "frenzy aggro giantmaker");
        cat("control", "iceprison bury glassjar scramble exile skydrop launchpad stasis rewind entangle medusa hypnosis sirensong "
                + "magnetize repulsor pinball catapult centrifuge tether electricfence forcefield bubble skyjail abduction portaltrap "
                + "banishend banishnether trapdoor sandtomb cryoshatter yank summon switcheroo bodyswap firewall");
        cat("curses", "plague disarm shuffle blindside curse vampire xpdrain pumpkincurse polymorph markedfordeath inventorylock "
                + "nobuildzone silence starvation brittlebones kindling deadmansswitch hotpotato potionroulette shrinkray heavyweight "
                + "femboyify emp flashbang");
        cat("buffs", "healingbeacon aegis titanform tinyform volatilefists thornsaura bloodthirst guardianangel astralprojection "
                + "personalorbit cloakfield speedzone minersblessing warbanner smokescreen decoys");
        cat("utility", "lootvacuum tripwire supplydrop respawnbeacon beammeup flare bridge watchtower tunnelbore bunker greatwall "
                + "recon capturenet beastmaster cavalry treasurehunt droppod xpshower purge");
        cat("minigames", "kingofthehill duelarena shrinkingzone spleefarena redlightgreenlight hotfloor hedgemaze");
        cat("fun", "disco fireworks celebration partydrop skymessage snowglobe rottenrain roulette omega");
    }

    /** Lets generated cannons (see {@link Combo}) set their own default tier and category. Never overrides a listed one. */
    public static void define(String id, String tier, String category) {
        TIER.putIfAbsent(id, tier);
        CATEGORY.putIfAbsent(id, category);
    }

    /** Adds a menu category (tab) if it doesn't exist yet. New ones go just before "other". */
    public static void addCategory(String category) {
        if (category == null || category.isBlank()) return;
        String c = category.toLowerCase(java.util.Locale.ROOT).trim();
        synchronized (CATEGORY_LIST) {
            if (CATEGORY_LIST.contains(c)) return;
            int other = CATEGORY_LIST.indexOf("other");
            CATEGORY_LIST.add(other < 0 ? CATEGORY_LIST.size() : other, c);
        }
    }

    public static String tier(String id) { return TIER.getOrDefault(id, "rare"); }
    public static String category(String id) { return CATEGORY.getOrDefault(id, "other"); }

    public static String tierColor(String tier) {
        return switch (tier) {
            case "common" -> "&f";
            case "epic" -> "&5&l";
            case "legendary" -> "&6&l";
            default -> "&9";
        };
    }

    public static String pretty(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
