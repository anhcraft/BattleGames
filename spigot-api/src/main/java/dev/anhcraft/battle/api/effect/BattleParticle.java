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
package dev.anhcraft.battle.api.effect;

import dev.anhcraft.config.annotations.*;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class BattleParticle {
    @Description("The type of particle")
    @Validation(notNull = true, silent = true)
    private Particle type = Particle.CLOUD;

    @Description("The number of particles")
    private int count = 1;

    @Path("offset_x")
    @Description({
            "The maximum random offset on the X axis",
            "Or the Red value in RGB (with colored dust particle)"
    })
    private double offsetX;

    @Path("offset_y")
    @Description({
            "The maximum random offset on the Y axis",
            "Or the Green value in RGB (with colored dust particle)"
    })
    private double offsetY;

    @Path("offset_z")
    @Description({
            "The maximum random offset on the Z axis",
            "Or the Blue value in RGB (with colored dust particle)"
    })
    private double offsetZ;

    @Description({
            "Particle speed"
    })
    private double speed;

    @NotNull
    public Particle getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public double getSpeed() {
        return speed;
    }

    public void spawn(@NotNull Location location) {
        World w = location.getWorld();
        if(w == null) return;
        w.spawnParticle(type, location, count, offsetX, offsetY, offsetZ, speed, null, true);
    }

    public void spawn(@NotNull World w, double x, double y, double z) {
        w.spawnParticle(type, x, y, z, count, offsetX, offsetY, offsetZ, speed, null, true);
    }
}
