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

package dev.anhcraft.battle.api.arena.game.options;

import dev.anhcraft.battle.api.BattleSound;
import dev.anhcraft.battle.utils.LocationUtil;
import dev.anhcraft.config.annotations.*;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class GameOptions {
    @Path("min_players")
    @Description("The minimum players needed to start a game")
    private int minPlayers = 1;

    @Path("countdown_time")
    @Description("The countdown time (in ticks)")
    private long countdownTime = 300;

    @Path("spawn_protection_time")
    @Description("Protection time when a player spawns (or respawn) (in ticks)")
    private long spawnProtectionTime = 40;

    @Path("item_selection_time")
    @Description("The time for selecting Battle items (in ticks)")
    private long itemSelectTime = 200;

    @Path("respawn_waiting_time")
    @Description({
            "How long players need to wait before respawn (in ticks)",
            "Set to any number below 20 to disable"
    })
    private long respawnWaitTime = 200;

    @Path("waiting_spawn_points")
    @Description("Spawn points in waiting phase")
    @Validation(notNull = true, silent = true)
    private List<String> waitSpawnPoints = new ArrayList<>();

    @Path("sounds.countdown")
    @Description("Sound during countdown phrase")
    private BattleSound countdownSound;

    public int getMinPlayers() {
        return minPlayers;
    }

    public long getCountdownTime() {
        return countdownTime;
    }

    public long getSpawnProtectionTime() {
        return spawnProtectionTime;
    }

    public long getRespawnWaitTime() {
        return respawnWaitTime;
    }

    public long getItemSelectTime() {
        return itemSelectTime;
    }

    @NotNull
    public List<Location> getWaitSpawnPoints() {
        return waitSpawnPoints.stream().map(LocationUtil::fromString).collect(Collectors.toList());
    }

    @Nullable
    public BattleSound getCountdownSound() {
        return countdownSound;
    }
}
