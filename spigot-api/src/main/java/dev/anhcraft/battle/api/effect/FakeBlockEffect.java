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
import dev.anhcraft.jvmkit.utils.Condition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@Configurable
public class FakeBlockEffect {
    @Description("The material of this block")
    @Validation(notNull = true)
    private Material material;

    @Description("External block data")
    private byte data;

    @NotNull
    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }

    public void spawn(@NotNull Location location) {
        Condition.argNotNull("location", location);
        location.getWorld().getPlayers().forEach(player -> {
            player.sendBlockChange(location, material, data);
        });
    }
}
