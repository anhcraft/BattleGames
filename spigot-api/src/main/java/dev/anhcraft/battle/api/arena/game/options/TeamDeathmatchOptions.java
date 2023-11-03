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

import dev.anhcraft.battle.api.arena.team.ABTeam;
import dev.anhcraft.battle.utils.LocationUtil;
import dev.anhcraft.config.annotations.*;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class TeamDeathmatchOptions extends GameOptions {
    @Path("playing_spawn_points_a")
    @Description("The spawn points of team A (in playing phase)")
    @Validation(notNull = true, silent = true)
    private List<String> playSpawnPointsA = new ArrayList<>();

    @Path("playing_spawn_points_b")
    @Description("The spawn points of team B (in playing phase)")
    @Validation(notNull = true, silent = true)
    private List<String> playSpawnPointsB = new ArrayList<>();

    @NotNull
    public List<Location> getPlaySpawnPoints(@NotNull ABTeam team) {
        return (team == ABTeam.TEAM_A ? playSpawnPointsA : playSpawnPointsB).stream().map(LocationUtil::fromString).collect(Collectors.toList());
    }
}
