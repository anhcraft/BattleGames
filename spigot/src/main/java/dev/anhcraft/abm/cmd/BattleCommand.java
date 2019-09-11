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
package dev.anhcraft.abm.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.anhcraft.abm.BattlePlugin;
import dev.anhcraft.abm.api.game.Arena;
import dev.anhcraft.abm.api.inventory.items.*;
import dev.anhcraft.abm.utils.LocationUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandAlias("abm|b|battle")
public class BattleCommand extends BaseCommand{
    private BattlePlugin plugin;

    public BattleCommand(BattlePlugin plugin){
        this.plugin = plugin;
    }

    public BattlePlugin getPlugin() {
        return plugin;
    }

    @Default
    @CatchUnknown
    @HelpCommand
    public void help(CommandSender sender){
    }

    @Subcommand("setspawn")
    @CommandPermission("abm.setspawn")
    public void setSpawn(Player player){
        Location loc = player.getLocation();
        plugin.getServerData().setSpawnPoint(loc);
        plugin.chatManager.sendPlayer(player, "server.set_spawn_success", s -> String.format(s, LocationUtil.toString(loc)));
    }

    @Subcommand("spawn")
    @CommandPermission("abm.spawn")
    public void spawn(Player player){
        player.teleport(plugin.getServerData().getSpawnPoint());
    }

    @Subcommand("arena menu")
    @CommandPermission("abm.arena.menu")
    public void arenaMenu(Player player){
        plugin.guiManager.openTopInventory(player, "arena_chooser");
    }

    @Subcommand("arena join")
    @CommandPermission("abm.arena.join")
    public void join(Player player, String arenaName){
        Optional<Arena> arena = plugin.getArena(arenaName);
        if(arena.isPresent()) {
            if(plugin.gameManager.join(player, arena.get()))
                plugin.chatManager.sendPlayer(player, "arena.join_success");
            else
                plugin.chatManager.sendPlayer(player, "arena.join_failed");
        }
        else plugin.chatManager.sendPlayer(player, "arena.not_found");
    }

    @Subcommand("arena forcejoin")
    @CommandPermission("abm.arena.forcejoin")
    public void forceJoin(Player s, Player p, String arenaName){
        Optional<Arena> arena = plugin.getArena(arenaName);
        if(arena.isPresent()) {
            if(plugin.gameManager.forceJoin(p, arena.get()))
                plugin.chatManager.sendPlayer(p, "arena.join_success");
            else
                plugin.chatManager.sendPlayer(p, "arena.join_failed");
        }
        else plugin.chatManager.sendPlayer(s, "arena.not_found");
    }

    @Subcommand("arena quit")
    @CommandPermission("abm.arena.quit")
    public void quit(Player player, @co.aikar.commands.annotation.Optional Player target){
        target = (target == null) ? player : target;
        if(plugin.gameManager.quit(target))
            plugin.chatManager.sendPlayer(player, "arena.quit_success");
        else
            plugin.chatManager.sendPlayer(player, "arena.quit_failed");
    }

    @Subcommand("tool position")
    @CommandPermission("abm.tool.position")
    public void pos(Player player){
        String s1 = String.format(plugin.chatManager.getFormattedMessage(player, "tool.position.message"), player.getWorld().getName(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        String s2 = String.format(plugin.chatManager.getFormattedMessage(player, "tool.position.location"), player.getWorld().getName(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        TextComponent c = new TextComponent(TextComponent.fromLegacyText(s1));
        c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, s2));
        player.spigot().sendMessage(c);
    }

    @Subcommand("give gun")
    @CommandPermission("abm.give.gun")
    public void giveGun(Player player, String s, @co.aikar.commands.annotation.Optional Player r){
        r = (r == null ? player : r);
        Optional<GunModel> gun = plugin.getGunModel(s);
        if(gun.isPresent()) {
            plugin.getPlayerData(r).ifPresent(playerData ->
                    playerData.getInventory().getStorage(ItemType.GUN).put(s));
            String receiver = r.getName();
            plugin.chatManager.sendPlayer(player, "items.given", str -> String.format(str, s, receiver));
        } else plugin.chatManager.sendPlayer(player, "items.not_found");
    }

    @Subcommand("give magazine")
    @CommandPermission("abm.give.magazine")
    public void giveMagazine(Player player, String s, @co.aikar.commands.annotation.Optional Player r){
        r = (r == null ? player : r);
        Optional<MagazineModel> mag = plugin.getMagazineModel(s);
        if(mag.isPresent()) {
            plugin.getPlayerData(r).ifPresent(playerData ->
                    playerData.getInventory().getStorage(ItemType.MAGAZINE).put(s));
            String receiver = r.getName();
            plugin.chatManager.sendPlayer(player, "items.given", str -> String.format(str, s, receiver));
        } else plugin.chatManager.sendPlayer(player, "items.not_found");
    }

    @Subcommand("give ammo")
    @CommandPermission("abm.give.ammo")
    public void giveAmmo(Player player, String s, @co.aikar.commands.annotation.Optional Player r){
        r = (r == null ? player : r);
        Optional<AmmoModel> ammo = plugin.getAmmoModel(s);
        if(ammo.isPresent()) {
            plugin.getPlayerData(r).ifPresent(playerData ->
                    playerData.getInventory().getStorage(ItemType.AMMO).put(s));
            String receiver = r.getName();
            plugin.chatManager.sendPlayer(player, "items.given", str -> String.format(str, s, receiver));
        } else plugin.chatManager.sendPlayer(player, "items.not_found");
    }

    @Subcommand("give scope")
    @CommandPermission("abm.give.scope")
    public void giveScope(Player player, String s, @co.aikar.commands.annotation.Optional Player r){
        r = (r == null ? player : r);
        Optional<ScopeModel> sc = plugin.getScopeModel(s);
        if(sc.isPresent()) {
            plugin.getPlayerData(r).ifPresent(playerData ->
                    playerData.getInventory().getStorage(ItemType.SCOPE).put(s));
            String receiver = r.getName();
            plugin.chatManager.sendPlayer(player, "items.given", str -> String.format(str, s, receiver));
        } else plugin.chatManager.sendPlayer(player, "items.not_found");
    }

    @Subcommand("inv")
    public void inv(Player player){
        plugin.guiManager.openTopInventory(player, "inventory_menu");
    }

    @Subcommand("kit")
    public void kit(Player player){
        plugin.guiManager.openTopInventory(player, "kit_menu");
    }
}
