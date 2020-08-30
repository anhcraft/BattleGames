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

package dev.anhcraft.battle.utils;

import dev.anhcraft.battle.api.BattleApi;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EntityUtil {
    public static void teleport(@NotNull Entity entity, @Nullable Location location) {
        teleport(entity, location, ok -> {
        });
    }

    public static void teleport(@NotNull Entity entity, @Nullable Location location, @NotNull Consumer<Boolean> callback) {
        if (location == null) return;
        if (location.getWorld() == null) {
            Bukkit.getLogger().warning(String.format("`%s` is missing param `world`. Recheck your config!", LocationUtil.toString(location)));
            location.setWorld(entity.getWorld());
        }
        if (BattleApi.getInstance().isPremium()) {
            PaperLib.teleportAsync(entity, location).thenAccept(callback);
        } else {
            entity.teleport(location);
            callback.accept(true);
        }
    }
}
