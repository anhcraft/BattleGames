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

package dev.anhcraft.battle.api.arena.game;

import dev.anhcraft.battle.utils.LocationUtil;
import dev.anhcraft.config.annotations.*;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class MobGroup {
    @Description("Where to spawn the mobs")
    @Validation(notNull = true)
    private String location;

    @Path("entity_type")
    @Description("Mob type")
    @Validation(notNull = true)
    private EntityType entityType;

    @Description({
            "Weight of each entity",
            "This value is used to reduce the speed of whom",
            "is carrying them. The ratio for weight:speed",
            "can be configured with option `weight_speed_scale`"
    })
    private double weight;

    @Description("Can thieves steal these mobs")
    private boolean stealable;

    @Description("How many entities should be spawned")
    private int amount = 1;

    @NotNull
    public Location getLocation() {
        return LocationUtil.fromString(location);
    }

    @NotNull
    public EntityType getEntityType() {
        return entityType;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isStealable() {
        return stealable;
    }

    public int getAmount() {
        return amount;
    }
}
