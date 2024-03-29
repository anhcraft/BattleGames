/*
 *
 *     Battle Minigame.
 *     Copyright (c) 2019 by anhcraft.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package dev.anhcraft.battle.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.anhcraft.battle.BattlePlugin;
import dev.anhcraft.battle.api.Booster;
import dev.anhcraft.battle.api.Perk;
import dev.anhcraft.battle.api.arena.Arena;
import dev.anhcraft.battle.api.arena.game.Game;
import dev.anhcraft.battle.api.arena.game.LocalGame;
import dev.anhcraft.battle.api.gui.NativeGui;
import dev.anhcraft.battle.api.inventory.item.*;
import dev.anhcraft.battle.api.stats.natives.*;
import dev.anhcraft.battle.api.storage.data.PlayerData;
import dev.anhcraft.battle.system.ResourcePack;
import dev.anhcraft.battle.system.debugger.BattleDebugger;
import dev.anhcraft.battle.utils.*;
import dev.anhcraft.battle.utils.info.InfoHolder;
import dev.anhcraft.battle.utils.info.InfoReplacer;
import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.configdoc.ConfigDocGenerator;
import dev.anhcraft.jvmkit.utils.ReflectionUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.net.URLClassLoader;
import java.util.*;

@CommandAlias("b|bg|battle|battlegames")
public class MainCommand extends BaseCommand {
    private final BattlePlugin plugin;

    public MainCommand(BattlePlugin plugin) {
        this.plugin = plugin;
    }

    @CatchUnknown
    @HelpCommand
    public void help(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("info")
    @CommandPermission("battle.info")
    @Description("Show plugin information")
    public void info(CommandSender sender) {
        ChatUtil.formatColorCodes(Arrays.asList(
                "&e&lBattleGames Minigame &7&lv" + plugin.getDescription().getVersion(),
                "&d◈ Author: &f" + String.join(", ", plugin.getDescription().getAuthors()),
                "&d◈ Discord: &fhttps://discord.gg/5s75WtfTR2",
                "&d◈ Spigot: &fhttps://spigotmc.org/resources/69463"
        )).forEach(sender::sendMessage);
    }

    @Subcommand("configdoc")
    @CommandPermission("battle.configdoc")
    @Description("Generate config documentation")
    public void configdoc() {
        JavaPluginLoader jpl = (JavaPluginLoader) plugin.getPluginLoader();
        List<URLClassLoader> loaders = (List<URLClassLoader>) ReflectionUtil.getDeclaredField(JavaPluginLoader.class, jpl, "loaders");
        TreeMap<String, Class<?>> map = new TreeMap<>();
        ConfigDocGenerator gen = new ConfigDocGenerator();
        for (URLClassLoader loader : loaders) {
            PluginDescriptionFile desc = (PluginDescriptionFile) ReflectionUtil.getDeclaredField(loader.getClass(), loader, "description");
            if(!desc.getName().equals("Battle") && !desc.getDepend().contains("Battle")) continue;
            Map<String, Class<?>> classMap = (Map<String, Class<?>>) ReflectionUtil.getDeclaredField(loader.getClass(), loader, "classes");
            for (Class<?> clazz : classMap.values()) {
                if (clazz.isAnnotationPresent(Configurable.class)) {
                    if(map.containsKey(clazz.getSimpleName())) {
                        map.put(clazz.getSimpleName() + clazz.getName(), clazz);
                    } else {
                        map.put(clazz.getSimpleName(), clazz);
                    }
                }
            }
        }
        for(Class<?> clazz : map.values()){
            gen.withSchemaOf(clazz);
        }
        gen.addJavadoc("dev.anhcraft.battle.*", "https://battlegames.github.io/jd/").generate(new File(plugin.configFolder, "docs"));
    }

    @Subcommand("setspawn")
    @CommandPermission("battle.setspawn")
    @Description("Set the server spawn")
    public void setSpawn(Player player) {
        Location loc = player.getLocation();
        plugin.getServerData().setSpawnPoint(loc);
        plugin.chatManager.sendPlayer(player, "server.set_spawn_success", new InfoHolder("").inform("location", LocationUtil.toString(loc)).compile());
    }

    @Subcommand("spawn")
    @CommandPermission("battle.spawn")
    @Description("Teleport to the spawn")
    public void spawn(Player player) {
        EntityUtil.teleport(player, plugin.getServerData().getSpawnPoint());
    }

    @Subcommand("open")
    @CommandPermission("battle.open")
    @CommandCompletion("@gui")
    @Description("Open a GUI for you or someone")
    public void openGui(Player player, String name, @Optional OnlinePlayer target) {
        plugin.guiManager.openTopGui(target == null ? player : target.getPlayer(), name);
    }

    @Subcommand("game list")
    @CommandPermission("battle.game.list")
    @Description("List all games are happening now")
    public void listGames(CommandSender sender) {
        Collection<Game> q = plugin.arenaManager.listGames();
        if (q.isEmpty()) {
            plugin.chatManager.send(sender, "game.list_header_none");
            return;
        }
        plugin.chatManager.send(sender, "game.list_header", new InfoHolder("").inform("size", q.size()).compile());
        for (Game game : q) {
            InfoHolder holder = new InfoHolder("game_");
            game.inform(holder);
            plugin.chatManager.send(sender, "game.list_section", holder.compile());
        }
    }

    @Subcommand("game destroy")
    @CommandPermission("battle.game.destroy")
    @CommandCompletion("@arena")
    @Description("Destroy a game (without ending)")
    public void destroyGame(CommandSender sender, String arena) {
        Arena a = plugin.getArena(arena);
        if (a != null) {
            Game g = plugin.arenaManager.getGame(a);
            if (g != null) {
                plugin.arenaManager.destroy(g);
                plugin.chatManager.send(sender, "game.destroy_success");
            } else
                plugin.chatManager.send(sender, "game.inactive_arena");
        } else
            plugin.chatManager.send(sender, "arena.not_found");
    }

    @Subcommand("game end")
    @CommandPermission("battle.game.end")
    @CommandCompletion("@arena")
    @Description("End a game")
    public void endGame(CommandSender sender, String arena) {
        Arena a = plugin.getArena(arena);
        if (a != null) {
            Game g = plugin.arenaManager.getGame(a);
            if (g != null) {
                if (g instanceof LocalGame) {
                    ((LocalGame) g).end();
                } else {
                    plugin.arenaManager.destroy(g);
                }
                plugin.chatManager.send(sender, "game.end_success");
            } else
                plugin.chatManager.send(sender, "game.inactive_arena");
        } else
            plugin.chatManager.send(sender, "arena.not_found");
    }

    @Subcommand("arena menu")
    @CommandPermission("battle.arena.menu")
    @Description("Open the arena menu")
    public void arenaMenu(Player player) {
        plugin.guiManager.openTopGui(player, NativeGui.ARENA_CHOOSER);
    }

    @Subcommand("arena join")
    @CommandPermission("battle.arena.join")
    @CommandCompletion("@arena")
    @Description("Force you or someone to join an arena")
    public void join(Player player, String arena, @Optional OnlinePlayer target) {
        Player t = (target == null) ? player : target.getPlayer();
        Arena a = plugin.getArena(arena);
        if (a != null) {
            if (plugin.arenaManager.join(t, a) != null) {
                plugin.chatManager.sendPlayer(player, "arena.join_success", new InfoHolder("").inform("target", t.getName()).compile());
            } else {
                plugin.chatManager.sendPlayer(player, "arena.join_failed", new InfoHolder("").inform("target", t.getName()).compile());
            }
        } else plugin.chatManager.sendPlayer(player, "arena.not_found");
    }

    @Subcommand("arena quit")
    @CommandPermission("battle.arena.quit")
    @CommandCompletion("@players")
    @Description("Force you or someone to quit the current arena")
    public void quit(Player player, @Optional OnlinePlayer target) {
        Player t = (target == null) ? player : target.getPlayer();
        if (plugin.arenaManager.quit(t)) {
            plugin.chatManager.sendPlayer(player, "arena.quit_success", new InfoHolder("").inform("target", t.getName()).compile());
        } else {
            plugin.chatManager.sendPlayer(player, "arena.quit_failed", new InfoHolder("").inform("target", t.getName()).compile());
        }
    }

    @Subcommand("tool position")
    @CommandPermission("battle.tool.position")
    @Description("Get your current position")
    public void pos(Player player) {
        Location loc = player.getLocation();
        InfoReplacer replacer = new InfoHolder("")
                .inform("location", LocationUtil.toString(loc))
                .compile();
        String s1 = Objects.requireNonNull(plugin.getLocalizedMessage("tool.position.message"));
        String s2 = Objects.requireNonNull(plugin.getLocalizedMessage("tool.position.location"));
        TextComponent c = new TextComponent(TextComponent.fromLegacyText(replacer.replace(s1)));
        c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, replacer.replace(s2)));
        player.spigot().sendMessage(c);
    }

    @Subcommand("tool exp2lv")
    @CommandPermission("battle.tool.exp.to.level")
    @Description("Convert X exp points to levels")
    public void exp2lv(CommandSender sender, long exp) {
        int lv = plugin.calculateLevel(exp);
        plugin.chatManager.send(sender, "tool.exp2lv", new InfoHolder("").inform("exp", exp).inform("level", lv).compile());
    }

    @Subcommand("tool lv2exp")
    @CommandPermission("battle.tool.level.to.exp")
    @Description("Convert X levels to exp points")
    public void lv2exp(CommandSender sender, int lv) {
        long exp = plugin.calculateExp(lv);
        plugin.chatManager.send(sender, "tool.lv2exp", new InfoHolder("").inform("exp", exp).inform("level", lv).compile());
    }

    @Subcommand("tool spawn")
    @CommandPermission("battle.tool.spawn")
    @CommandCompletion("@entityTypes")
    @Description("Spawn mass of mobs at your location")
    public void spawn(Player player, EntityType entityType, int amount, @Optional Double health) {
        Location loc = player.getLocation();
        for (int i = 0; i < amount; i++) {
            Entity entity = player.getWorld().spawnEntity(loc, entityType);
            if (health != null && entity instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) entity;
                le.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
                le.setHealth(Math.max(health, 1));
            }
        }
    }

    @Subcommand("give exp")
    @CommandPermission("battle.give.exp")
    @Description("Give someone exp points")
    public void giveExp(CommandSender sender, long exp, Player player) {
        PlayerData playerData = plugin.getPlayerData(player);
        if (playerData != null) {
            playerData.getStats().of(ExpStat.class).increase(player, exp);
        } else {
            plugin.chatManager.sendPlayer(player, "player_data.not_found");
        }
    }

    @Subcommand("give gun")
    @CommandPermission("battle.give.gun")
    @CommandCompletion("@gun @players")
    @Description("Give you or someone a gun")
    public void giveGun(Player player, String id, @Optional OnlinePlayer targetPlayer) {
        Player target = (targetPlayer == null ? player : targetPlayer.getPlayer());
        GunModel gun = plugin.getGunModel(id);
        if (gun != null) {
            PlayerData playerData = plugin.getPlayerData(target);
            if (playerData != null) {
                playerData.getBackpack().getStorage(ItemType.GUN).put(id);
                String receiver = target.getName();
                plugin.chatManager.sendPlayer(player, "items.given", new InfoHolder("").inform("id", id).inform("receiver", receiver).compile());
            } else {
                plugin.chatManager.sendPlayer(player, "player_data.not_found");
            }
        } else plugin.chatManager.sendPlayer(player, "items.not_found");
    }

    @Subcommand("give magazine")
    @CommandPermission("battle.give.magazine")
    @CommandCompletion("@magazine @players")
    @Description("Give you or someone a magazine")
    public void giveMagazine(Player player, String id, @Optional OnlinePlayer targetPlayer) {
        Player target = (targetPlayer == null ? player : targetPlayer.getPlayer());
        MagazineModel mag = plugin.getMagazineModel(id);
        if (mag != null) {
            PlayerData playerData = plugin.getPlayerData(target);
            if (playerData != null) {
                playerData.getBackpack().getStorage(ItemType.MAGAZINE).put(id);
                String receiver = target.getName();
                plugin.chatManager.sendPlayer(player, "items.given", new InfoHolder("").inform("id", id).inform("receiver", receiver).compile());
            } else {
                plugin.chatManager.sendPlayer(player, "player_data.not_found");
            }
        } else plugin.chatManager.sendPlayer(player, "items.not_found");
    }

    @Subcommand("give ammo")
    @CommandPermission("battle.give.ammo")
    @CommandCompletion("@ammo @players")
    @Description("Give you or someone ammo")
    public void giveAmmo(Player player, String id, @Optional OnlinePlayer targetPlayer) {
        Player target = (targetPlayer == null ? player : targetPlayer.getPlayer());
        AmmoModel ammo = plugin.getAmmoModel(id);
        if (ammo != null) {
            PlayerData playerData = plugin.getPlayerData(target);
            if (playerData != null) {
                playerData.getBackpack().getStorage(ItemType.AMMO).put(id);
                String receiver = target.getName();
                plugin.chatManager.sendPlayer(player, "items.given", new InfoHolder("").inform("id", id).inform("receiver", receiver).compile());
            } else {
                plugin.chatManager.sendPlayer(player, "player_data.not_found");
            }
        } else plugin.chatManager.sendPlayer(player, "items.not_found");
    }

    @Subcommand("give scope")
    @CommandPermission("battle.give.scope")
    @CommandCompletion("@scope @players")
    @Description("Give you or someone a scope")
    public void giveScope(Player player, String id, @Optional OnlinePlayer targetPlayer) {
        Player target = (targetPlayer == null ? player : targetPlayer.getPlayer());
        ScopeModel sc = plugin.getScopeModel(id);
        if (sc != null) {
            PlayerData playerData = plugin.getPlayerData(target);
            if (playerData != null) {
                playerData.getBackpack().getStorage(ItemType.SCOPE).put(id);
                String receiver = target.getName();
                plugin.chatManager.sendPlayer(player, "items.given", new InfoHolder("").inform("id", id).inform("receiver", receiver).compile());
            } else {
                plugin.chatManager.sendPlayer(player, "player_data.not_found");
            }
        } else plugin.chatManager.sendPlayer(player, "items.not_found");
    }

    @Subcommand("give grenade")
    @CommandPermission("battle.give.grenade")
    @CommandCompletion("@grenade @players")
    @Description("Give you or someone a grenade")
    public void giveGrenade(Player player, String id, @Optional OnlinePlayer targetPlayer) {
        Player target = (targetPlayer == null ? player : targetPlayer.getPlayer());
        GrenadeModel gm = plugin.getGrenadeModel(id);
        if (gm != null) {
            PlayerData playerData = plugin.getPlayerData(target);
            if (playerData != null) {
                playerData.getBackpack().getStorage(ItemType.GRENADE).put(id);
                String receiver = target.getName();
                plugin.chatManager.sendPlayer(player, "items.given", new InfoHolder("").inform("id", id).inform("receiver", receiver).compile());
            } else {
                plugin.chatManager.sendPlayer(player, "player_data.not_found");
            }
        } else plugin.chatManager.sendPlayer(player, "items.not_found");
    }

    @Subcommand("give perk")
    @CommandPermission("battle.give.perk")
    @CommandCompletion("@perk @players")
    @Description("Give you or someone a perk")
    public void givePerk(Player player, String id, @Optional OnlinePlayer targetPlayer) {
        Player target = (targetPlayer == null ? player : targetPlayer.getPlayer());
        Perk perk = plugin.getPerk(id);
        if (perk != null) {
            perk.give(target);
            String receiver = target.getName();
            plugin.chatManager.sendPlayer(player, "perks.given", new InfoHolder("").inform("id", id).inform("receiver", receiver).compile());
        } else plugin.chatManager.sendPlayer(player, "perks.not_found");
    }

    @Subcommand("give booster")
    @CommandPermission("battle.give.booster")
    @CommandCompletion("@booster @players")
    @Description("Give you or someone a booster")
    public void giveBooster(Player player, String id, @Optional OnlinePlayer targetPlayer) {
        Player target = (targetPlayer == null ? player : targetPlayer.getPlayer());
        Booster b = plugin.getBooster(id);
        if (b != null) {
            PlayerData playerData = plugin.getPlayerData(target);
            if (playerData != null) {
                playerData.getBoosters().putIfAbsent(id, System.currentTimeMillis());
                String receiver = target.getName();
                plugin.chatManager.sendPlayer(player, "booster.given", new InfoHolder("").inform("id", id).inform("receiver", receiver).compile());
            } else {
                plugin.chatManager.sendPlayer(player, "player_data.not_found");
            }
        } else plugin.chatManager.sendPlayer(player, "booster.not_found");
    }

    @Subcommand("booster")
    @Description("Open the booster GUI")
    public void booster(Player player) {
        plugin.guiManager.openTopGui(player, NativeGui.BOOSTER_MENU);
    }

    @Subcommand("market")
    @Description("Open the market")
    public void market(Player player) {
        plugin.guiManager.openTopGui(player, NativeGui.MARKET_CATEGORY_MENU);
    }

    @Subcommand("kit")
    @Description("Open the kit menu")
    public void kit(Player player) {
        plugin.guiManager.openTopGui(player, NativeGui.KIT_MENU);
    }

    @Subcommand("stats")
    @Description("Open the statistics menu")
    public void stats(Player player) {
        plugin.guiManager.openTopGui(player, NativeGui.STATISTICS);
    }

    @Subcommand("bp")
    @Description("Open your backpack")
    public void openBackpack(Player player) {
        plugin.guiManager.openTopGui(player, NativeGui.PLAYER_BP);
    }

    @Subcommand("clear bp")
    @CommandPermission("battle.clear.bp")
    @Description("Clear your or someone's backpack")
    public void clearBackpack(Player player, @Optional OfflinePlayer target) {
        target = (target == null ? player : target);
        PlayerData pd = plugin.getPlayerData(target);
        if (pd == null) plugin.chatManager.sendPlayer(player, "player_data.not_found");
        else {
            pd.getBackpack().clear();
            plugin.chatManager.sendPlayer(player, "bp.cleared", new InfoHolder("").inform("target", Objects.requireNonNull(target.getName())).compile());
        }
    }

    @Subcommand("clear progression")
    @CommandPermission("battle.clear.progression")
    @Description("Clear your or someone's progression")
    public void clearProgression(Player player, @Optional OfflinePlayer target) {
        target = (target == null ? player : target);
        PlayerData pd = plugin.getPlayerData(target);
        if (pd == null) plugin.chatManager.sendPlayer(player, "player_data.not_found");
        else {
            pd.clearProgression();
            plugin.chatManager.sendPlayer(player, "progression.cleared");
        }
    }

    @Subcommand("gun reload")
    @CommandPermission("battle.gun.reload")
    @Description("Reload your gun instantly")
    public void reloadGun(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isEmpty(item)) {
            plugin.chatManager.sendPlayer(player, "items.is_null");
            return;
        }
        BattleItem<?> bi = plugin.itemManager.read(item);
        if (bi instanceof Gun) {
            Gun g = (Gun) bi;
            g.getMagazine().resetAmmo();
            player.getInventory().setItemInMainHand(plugin.gunManager.createGun(g, false));
            plugin.chatManager.sendPlayer(player, "gun.ammo_reloaded");
        } else {
            plugin.chatManager.sendPlayer(player, "items.not_gun");
        }
    }

    @Subcommand("adjust stats win")
    @CommandPermission("battle.adjust.stats")
    @Description("Adjust someone's win count")
    public void adjustWins(CommandSender sender, int delta, Player player) {
        Objects.requireNonNull(plugin.getPlayerData(player)).getStats().of(WinStat.class).increase(player, delta);
    }

    @Subcommand("adjust stats lose")
    @CommandPermission("battle.adjust.stats")
    @Description("Adjust someone's lose count")
    public void adjustLoses(CommandSender sender, int delta, Player player) {
        Objects.requireNonNull(plugin.getPlayerData(player)).getStats().of(LoseStat.class).increase(player, delta);
    }

    @Subcommand("adjust stats respawn")
    @CommandPermission("battle.adjust.stats")
    @Description("Adjust someone's respawn count")
    public void adjustRespawns(CommandSender sender, int delta, Player player) {
        Objects.requireNonNull(plugin.getPlayerData(player)).getStats().of(RespawnStat.class).increase(player, delta);
    }

    @Subcommand("adjust stats kill")
    @CommandPermission("battle.adjust.stats")
    @Description("Adjust someone's kill count")
    public void adjustKills(CommandSender sender, int delta, Player player) {
        Objects.requireNonNull(plugin.getPlayerData(player)).getStats().of(KillStat.class).increase(player, delta);
    }

    @Subcommand("adjust stats firstkill")
    @CommandPermission("battle.adjust.stats")
    @Description("Adjust someone's first-kill count")
    public void adjustFirstKills(CommandSender sender, int delta, Player player) {
        Objects.requireNonNull(plugin.getPlayerData(player)).getStats().of(FirstKillStat.class).increase(player, delta);
    }

    @Subcommand("adjust stats headshot")
    @CommandPermission("battle.adjust.stats")
    @Description("Adjust someone's head-shot count")
    public void adjustHeadshot(CommandSender sender, int delta, Player player) {
        Objects.requireNonNull(plugin.getPlayerData(player)).getStats().of(HeadshotStat.class).increase(player, delta);
    }

    @Subcommand("adjust stats assist")
    @CommandPermission("battle.adjust.stats")
    @Description("Adjust someone's assist count")
    public void adjustAssists(CommandSender sender, int delta, Player player) {
        Objects.requireNonNull(plugin.getPlayerData(player)).getStats().of(AssistStat.class).increase(player, delta);
    }

    @Subcommand("adjust stats death")
    @CommandPermission("battle.adjust.deaths")
    @Description("Adjust someone's deaths count")
    public void adjustDeaths(CommandSender sender, int delta, Player player) {
        Objects.requireNonNull(plugin.getPlayerData(player)).getStats().of(DeathStat.class).increase(player, delta);
    }

    @Subcommand("adjust stats stolen-mobs")
    @CommandPermission("battle.adjust.stats")
    @Description("Adjust someone's stolen mobs count")
    public void adjustStolenMobs(CommandSender sender, int delta, Player player) {
        Objects.requireNonNull(plugin.getPlayerData(player)).getStats().of(StolenMobStat.class).increase(player, delta);
    }

    @Subcommand("debug 3min")
    @CommandPermission("battle.debug")
    @Description("Make a 3-minutes debugging task")
    public void debug3(CommandSender sender) {
        if (BattleDebugger.create(s -> plugin.chatManager.send(sender, "debug.done", new InfoHolder("").inform("path", s).compile()), 20 * 60 * 3)) {
            plugin.chatManager.send(sender, "debug.created_success");
        } else {
            plugin.chatManager.send(sender, "debug.created_failure");
        }
    }

    @Subcommand("debug 5min")
    @CommandPermission("battle.debug")
    @Description("Make a 5-minutes debugging task")
    public void debug5(CommandSender sender) {
        if (BattleDebugger.create(s -> plugin.chatManager.send(sender, "debug.done", new InfoHolder("").inform("path", s).compile()), 20 * 60 * 5)) {
            plugin.chatManager.send(sender, "debug.created_success");
        } else {
            plugin.chatManager.send(sender, "debug.created_failure");
        }
    }

    @Subcommand("debug 15min")
    @CommandPermission("battle.debug")
    @Description("Make a 15-minutes debugging task")
    public void debug15(CommandSender sender) {
        if (BattleDebugger.create(s -> plugin.chatManager.send(sender, "debug.done", new InfoHolder("").inform("path", s).compile()), 20 * 60 * 15)) {
            plugin.chatManager.send(sender, "debug.created_success");
        } else {
            plugin.chatManager.send(sender, "debug.created_failure");
        }
    }

    @Subcommand("rsp refresh")
    @CommandPermission("battle.rsp.refresh")
    @Description("Refresh the resource pack")
    public void refreshRsp(CommandSender sender) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> ResourcePack.init(sender::sendMessage));
    }

    @Subcommand("rsp install")
    @CommandPermission("battle.rsp.install")
    @Description("Install the resource pack")
    public void installRsp(CommandSender sender, Player target) {
        ResourcePack.send(target);
    }

    @Subcommand("soft-reload")
    @CommandPermission("battle.reload")
    @Description("Reload the configuration without destroying playing arenas")
    public void softReload(CommandSender sender) {
        try {
            Objects.requireNonNull(plugin.getLocalizedMessages("reload.warn")).forEach(sender::sendMessage);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.reloadConfigs();
                plugin.chatManager.send(sender, "reload.done");
            }, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Subcommand("reload")
    @CommandPermission("battle.reload")
    @Description("Reload the configuration")
    public void reload(CommandSender sender) {
        try {
            Objects.requireNonNull(plugin.getLocalizedMessages("reload.warn")).forEach(sender::sendMessage);
            plugin.getArenaManager().listGames(g -> plugin.getArenaManager().destroy(g));
            // wait a bit for async tasks (e.g: rollback)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.reloadConfigs();
                plugin.chatManager.send(sender, "reload.done");
            }, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Subcommand("give medical_kit")
    @CommandPermission("battle.give.medical_kit")
    public void giveMedicalKit(CommandSender sender, int amount, @Optional OnlinePlayer target) {
        PreparedItem pi = new PreparedItem();
        pi.material(Material.STONE_SWORD);
        pi.name(ChatUtil.formatColorCodes("&f&lMedical Kit &c&l(❤)"));
        pi.damage((short) 1);
        pi.flags().add(ItemFlag.HIDE_UNBREAKABLE);
        pi.flags().add(ItemFlag.HIDE_ATTRIBUTES);
        pi.unbreakable(true);
        ItemStack itemStack = pi.build();
        for (int i = 0; i < amount; i++) {
            target.getPlayer().getInventory().addItem(itemStack.clone());
        }
    }

    @Subcommand("give adrenaline_shot")
    @CommandPermission("battle.give.adrenaline_shot")
    public void giveAdrenalineShot(CommandSender sender, int amount, @Optional OnlinePlayer target) {
        PreparedItem pi = new PreparedItem();
        pi.material(Material.STONE_SWORD);
        pi.name(ChatUtil.formatColorCodes("&4&lAdrenaline Shot"));
        pi.damage((short) 4);
        pi.flags().add(ItemFlag.HIDE_UNBREAKABLE);
        pi.flags().add(ItemFlag.HIDE_ATTRIBUTES);
        pi.unbreakable(true);
        ItemStack itemStack = pi.build();
        for (int i = 0; i < amount; i++) {
            target.getPlayer().getInventory().addItem(itemStack.clone());
        }
    }
}
