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
package dev.anhcraft.abm.system.managers;

import com.google.common.collect.Multiset;
import dev.anhcraft.abm.BattleComponent;
import dev.anhcraft.abm.BattlePlugin;
import dev.anhcraft.abm.api.BattleGameManager;
import dev.anhcraft.abm.api.BattleModeController;
import dev.anhcraft.abm.api.events.GameJoinEvent;
import dev.anhcraft.abm.api.events.GameQuitEvent;
import dev.anhcraft.abm.api.game.*;
import dev.anhcraft.abm.api.storage.data.PlayerData;
import dev.anhcraft.abm.system.QueueServer;
import dev.anhcraft.abm.system.cleaners.GameCleaner;
import dev.anhcraft.abm.system.controllers.CTFController;
import dev.anhcraft.abm.system.controllers.DeathmatchController;
import dev.anhcraft.abm.system.controllers.ModeController;
import dev.anhcraft.abm.system.controllers.TeamDeathmatchController;
import dev.anhcraft.abm.system.integrations.VaultApi;
import dev.anhcraft.abm.utils.PlaceholderUtils;
import dev.anhcraft.jvmkit.utils.Condition;
import dev.anhcraft.jvmkit.utils.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class GameManager extends BattleComponent implements BattleGameManager {
    private final Map<Arena, Game> ARENA_GAME_MAP = new ConcurrentHashMap<>();
    private final Map<UUID, LocalGame> PLAYER_GAME_MAP = new HashMap<>();
    private final Object LOCK = new Object();
    public final GameCleaner cleaner = new GameCleaner();

    public GameManager(BattlePlugin plugin) {
        super(plugin);
        initController(Mode.DEATHMATCH, new DeathmatchController(plugin));
        initController(Mode.TEAM_DEATHMATCH, new TeamDeathmatchController(plugin));
        initController(Mode.CTF, new CTFController(plugin));
    }

    private void initController(Mode mode, ModeController controller) {
        mode.setController(controller);
        Bukkit.getPluginManager().registerEvents(controller, plugin);
    }

    @Override
    @Nullable
    public GamePlayer getGamePlayer(@NotNull Player player){
        Condition.argNotNull("player", player);
        synchronized (LOCK) {
            LocalGame x = PLAYER_GAME_MAP.get(player.getUniqueId());
            return x == null ? null : x.getPlayer(player);
        }
    }

    @Override
    @Nullable
    public LocalGame getGame(@NotNull Player player){
        Condition.argNotNull("player", player);
        synchronized (LOCK) {
            return PLAYER_GAME_MAP.get(player.getUniqueId());
        }
    }

    @Override
    @Nullable
    public LocalGame getGame(@NotNull UUID playerId){
        Condition.argNotNull("playerId", playerId);
        synchronized (LOCK) {
            return PLAYER_GAME_MAP.get(playerId);
        }
    }

    @Override
    @Nullable
    public Game getGame(@NotNull Arena arena){
        Condition.argNotNull("arena", arena);
        return ARENA_GAME_MAP.get(arena);
    }

    private Game join(Player player, LocalGame localGame, BattleModeController controller) {
        GamePlayer gp = new GamePlayer(player);
        localGame.getPlayers().put(player, gp);
        PLAYER_GAME_MAP.put(player.getUniqueId(), localGame);
        controller.onJoin(player, localGame);
        Bukkit.getPluginManager().callEvent(new GameJoinEvent(localGame, gp));
        return localGame;
    }

    @Override
    @Nullable
    public Game join(@NotNull Player player, @NotNull Arena arena, boolean forceLocal){
        Condition.argNotNull("player", player);
        Condition.argNotNull("arena", arena);
        synchronized (LOCK) {
            if (PLAYER_GAME_MAP.containsKey(player.getUniqueId())) {
                plugin.chatManager.sendPlayer(player, "arena.error_already_joined");
                return null;
            }
            Game game = ARENA_GAME_MAP.get(arena);
            if(game != null){
                if (game.getPhase() == GamePhase.END || game.getPhase() == GamePhase.CLEANING) {
                    plugin.chatManager.sendPlayer(player, "arena.error_attendance_disabled");
                    return null;
                }
                if(game.getPlayerCount() == arena.getMaxPlayers()){
                    plugin.chatManager.sendPlayer(player, "arena.error_full_players");
                    return null;
                }
            } else {
                game = arena.hasBungeecordSupport() && !forceLocal ? new RemoteGame(arena) : new LocalGame(arena);
                ARENA_GAME_MAP.put(arena, game);
            }
            if(game instanceof LocalGame) {
                LocalGame localGame = (LocalGame) game;
                BattleModeController controller = localGame.getMode().getController();
                if (controller == null) {
                    plugin.chatManager.sendPlayer(player, "arena.error_mode_controller_unavailable");
                }
                else if (!controller.canJoin(player, localGame)) {
                    plugin.chatManager.sendPlayer(player, "arena.error_attendance_refused");
                }
                else {
                    return join(player, localGame, controller);
                }
            }
            else {
                plugin.queueServerTask.QUEUE.add(new QueueServer(player, arena.getRemoteServers(), arena.getId()));
            }
            return game;
        }
    }

    @Override
    @Nullable
    public Game forceJoin(@NotNull Player player, @NotNull Arena arena, boolean forceLocal) {
        Condition.argNotNull("player", player);
        Condition.argNotNull("arena", arena);
        synchronized (LOCK) {
            if (PLAYER_GAME_MAP.containsKey(player.getUniqueId())) return null;
            Game game = ARENA_GAME_MAP.get(arena);
            if(game == null){
                game = arena.hasBungeecordSupport() && !forceLocal ? new RemoteGame(arena) : new LocalGame(arena);
                ARENA_GAME_MAP.put(arena, game);
            }
            if(game instanceof LocalGame) {
                LocalGame localGame = (LocalGame) game;
                BattleModeController controller = localGame.getMode().getController();
                if (controller != null)
                    return join(player, localGame, controller);
            }
            else {
                plugin.queueServerTask.QUEUE.add(new QueueServer(player, arena.getRemoteServers(), arena.getId()));
            }
            return game;
        }
    }

    @Override
    public boolean quit(@NotNull Player player){
        Condition.argNotNull("player", player);
        synchronized (LOCK) {
            LocalGame localGame = PLAYER_GAME_MAP.get(player.getUniqueId());
            if (localGame == null) return false;
            // don't save the player data here!!!
            // plugin.getPlayerData(player);
            localGame.getMode().getController(c -> c.onQuit(player, localGame));
            GamePlayer gp = localGame.getPlayer(player);
            if(gp == null) return false;
            Bukkit.getPluginManager().callEvent(new GameQuitEvent(localGame, gp));
            localGame.getPlayers().remove(player);
            Multiset<String> servers = localGame.getDownstreamServers().keys();
            for(String s : servers) {
                if (localGame.getDownstreamServers().remove(s, player)) {
                    plugin.queueServerTask.QUEUE.add(new QueueServer(player, plugin.getLobbyServers(), null));
                    break;
                }
            }
            PLAYER_GAME_MAP.remove(player.getUniqueId());
            if(localGame.getPlayerCount() == 0) {
                localGame.setPhase(GamePhase.CLEANING);
                cleaner.doClean(localGame.getArena(), ARENA_GAME_MAP::remove);
            }
            return true;
        }
    }

    @Override
    public void destroy(@NotNull Game game){
        Condition.argNotNull("game", game);
        synchronized (LOCK) {
            if(game instanceof LocalGame) {
                ((LocalGame) game).getPlayers().forEach((player, gp) -> {
                    Bukkit.getPluginManager().callEvent(new GameQuitEvent(game, gp));
                    PLAYER_GAME_MAP.remove(player.getUniqueId());
                });
                game.setPhase(GamePhase.CLEANING);
                cleaner.doClean(game.getArena(), ARENA_GAME_MAP::remove);
            } else {
                game.setPhase(GamePhase.END);
                ARENA_GAME_MAP.remove(game.getArena());
            }
        }
    }

    private void runCmd(String s, Player player){
        s = PlaceholderUtils.formatPAPI(player, s);
        s = PlaceholderUtils.formatExpression(s);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
    }

    public void rewardAndSaveCache(LocalGame localGame) {
        localGame.getPlayers().values().forEach(gamePlayer -> {
            PlayerData playerData = plugin.getPlayerData(gamePlayer.getPlayer());
            if(playerData != null) {
                double m = Math.max(0, localGame.getArena().calculateFinalMoney(gamePlayer));
                long e = Math.max(0, localGame.getArena().calculateFinalExp(gamePlayer));
                VaultApi.getEconomyApi().depositPlayer(gamePlayer.getPlayer(), m);
                playerData.getExp().addAndGet(e);
                plugin.chatManager.sendPlayer(gamePlayer.getPlayer(), "arena.reward_message", s -> s.replace("{__money__}", MathUtil.formatRound(m)).replace("{__exp__}", Long.toString(e)));

                playerData.getKillCounter().addAndGet(gamePlayer.getKillCounter().get());
                playerData.getHeadshotCounter().addAndGet(gamePlayer.getHeadshotCounter().get());
                playerData.getAssistCounter().addAndGet(gamePlayer.getAssistCounter().get());
                playerData.getDeathCounter().addAndGet(gamePlayer.getDeathCounter().get());
                if(gamePlayer.isWinner()) {
                    playerData.getWinCounter().incrementAndGet();
                    localGame.getArena().getEndCommandWinners().forEach(s -> runCmd(s, gamePlayer.getPlayer()));
                } else {
                    playerData.getLoseCounter().incrementAndGet();
                    localGame.getArena().getEndCommandLosers().forEach(s -> runCmd(s, gamePlayer.getPlayer()));
                }
            }
        });
    }

    @NotNull
    @Override
    public Collection<Game> listGames(){
        return Collections.unmodifiableCollection(ARENA_GAME_MAP.values());
    }

    @Override
    public void listGames(@NotNull Consumer<Game> consumer) {
        Condition.argNotNull("consumer", consumer);
        ARENA_GAME_MAP.values().forEach(consumer);
    }
}
