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

package dev.anhcraft.battle.api.effect.firework;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Validation;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class BattleFirework {
    @Description("The firework's effects")
    @Validation(notNull = true, silent = true)
    private Map<String, BattleFireworkEffect> effects = new HashMap<>();

    @Description("The firework's power")
    private int power;

    @NotNull
    public Collection<BattleFireworkEffect> getEffects() {
        return effects.values();
    }

    public int getPower() {
        return power;
    }

    public void spawn(@NotNull Location location) {
        Firework fw = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.setPower(power);
        for (BattleFireworkEffect bfe : effects.values()) {
            fwm.addEffect(bfe.getFireworkEffect());
        }
        fw.setFireworkMeta(fwm);
    }
}
