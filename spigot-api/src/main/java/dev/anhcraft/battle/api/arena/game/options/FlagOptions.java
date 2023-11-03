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

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class FlagOptions {
    @Description("The location of the flag")
    @Validation(notNull = true)
    private String location;

    @Path("display_name.valid")
    @Description("The name to be displayed when the flag state is valid")
    @Validation(notNull = true)
    private String validDisplayName;

    @Path("display_name.invalid")
    @Description("The name to be displayed when the flag state is invalid")
    @Validation(notNull = true)
    private String invalidDisplayName;

    @Path("display_name.neutral")
    @Description("The name to be displayed when the flag state is neutral")
    @Validation(notNull = true)
    private String neutralDisplayName;

    @Path("max_health")
    @Description("The maximum health points of the flag")
    @Validation(notNull = true)
    private int maxHealth = 10;

    @Path("start_capture_sound")
    @Description("The sound on starting the capture")
    private BattleSound startCaptureSound;

    @Path("stop_capture_sound")
    @Description("The sound on stopping the capture")
    private BattleSound stopCaptureSound;

    @NotNull
    public Location getLocation() {
        return LocationUtil.fromString(location);
    }

    @NotNull
    public String getValidDisplayName() {
        return validDisplayName;
    }

    @NotNull
    public String getInvalidDisplayName() {
        return invalidDisplayName;
    }

    @NotNull
    public String getNeutralDisplayName() {
        return neutralDisplayName;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    @Nullable
    public BattleSound getStartCaptureSound() {
        return startCaptureSound;
    }

    @Nullable
    public BattleSound getStopCaptureSound() {
        return stopCaptureSound;
    }
}
