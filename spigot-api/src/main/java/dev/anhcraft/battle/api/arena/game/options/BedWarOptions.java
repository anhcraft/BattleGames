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

import dev.anhcraft.config.annotations.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class BedWarOptions extends GameOptions {
    @Description("List of teams")
    @Example({
            "teams:",
            "  '1':",
            "    name: \"&fWhite\"",
            "    color: white",
            "    spawn_points:",
            "    - lighthouse 30.39 65 -88.76 -0.45 -1.05",
            "    - lighthouse 27.80 65 -88.75 -0.45 -1.05",
            "    - lighthouse 29.36 65 -88.07 -0.45 -1.05",
            "    bed_location: lighthouse 29.53 66.56 -74.76 0 0",
            "  '2':",
            "    name: \"&bCyan\"",
            "    color: cyan",
            "    spawn_points:",
            "    - lighthouse -20.62 65 -88.37 -359.85 -0.30",
            "    - lighthouse -22.97 65 -88.38 -359.85 -0.30",
            "    - lighthouse -21.98 65 -87.81 -357.75 -0.60",
            "    bed_location: lighthouse -22.44 66.56 -76.01 0 0"
    })
    @Validation(notNull = true, silent = true)
    private final Map<String, BWTeamOptions> teams = new HashMap<>();

    @Path("team_size")
    @Description("The size of a team")
    private int teamSize = 2;

    public int getTeamSize() {
        return teamSize;
    }

    @NotNull
    public Collection<BWTeamOptions> getTeams() {
        return teams.values();
    }
}
