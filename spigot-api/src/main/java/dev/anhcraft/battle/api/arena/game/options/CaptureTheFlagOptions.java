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
public class CaptureTheFlagOptions extends TeamDeathmatchOptions {
    @Description("All flags in the arena")
    @Validation(notNull = true, silent = true)
    @Example({
            "flags:",
            "  '1':",
            "    location: italy -309.1571590469714 42.0 54.99857630906323 228.0001 7.3499637",
            "    display_name:",
            "      valid: \"&a&l<flag_team> | &f&l<flag_health> &c&l❤\"",
            "      invalid: \"&7&l<flag_team> | &f&l<flag_health>/<flag_max_health> &c&l❤\"",
            "      neutral: \"&7Neutral\"",
            "    max_health: 5",
            "  '2':",
            "    location: italy -304.39875875777005 42.5 84.5322748252515 216.60065 1.4999988",
            "    display_name:",
            "      valid: \"&a&l<flag_team> | &f&l<flag_health> &c&l❤\"",
            "      invalid: \"&7&l<flag_team> | &f&l<flag_health>/<flag_max_health> &c&l❤\"",
            "      neutral: \"&7Neutral\"",
            "    max_health: 10"
    })
    private Map<String, FlagOptions> flags = new HashMap<>();

    @NotNull
    public Collection<FlagOptions> getFlags() {
        return flags.values();
    }
}
