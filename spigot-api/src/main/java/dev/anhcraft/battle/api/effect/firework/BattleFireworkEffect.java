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

import dev.anhcraft.battle.api.BattleColor;
import dev.anhcraft.config.annotations.*;
import org.bukkit.FireworkEffect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class BattleFireworkEffect {
    @Description("The firework type")
    @Validation(notNull = true, silent = true)
    private FireworkEffect.Type type = FireworkEffect.Type.BALL;

    @Description("Make this firework flicker")
    private boolean flicker;

    @Description("Make this firework has a trail")
    private boolean trail;

    @Path("primary_colors")
    @Description("All primary colors")
    @Validation(notNull = true, silent = true)
    private List<BattleColor> primaryColors = new ArrayList<>();

    @Path("fade_colors")
    @Description("All fade colors")
    @Validation(notNull = true, silent = true)
    private List<BattleColor> fadeColors = new ArrayList<>();

    private FireworkEffect cached;

    @NotNull
    public FireworkEffect.Type getType() {
        return type;
    }

    public boolean isFlicker() {
        return flicker;
    }

    public boolean isTrail() {
        return trail;
    }

    @NotNull
    public List<BattleColor> getPrimaryColors() {
        return primaryColors;
    }

    @NotNull
    public List<BattleColor> getFadeColors() {
        return fadeColors;
    }

    @NotNull
    public FireworkEffect getFireworkEffect() {
        if (cached == null) {
            FireworkEffect.Builder b = FireworkEffect.builder().with(type).flicker(flicker).trail(trail);
            for (BattleColor c : primaryColors) {
                b.withColor(c.asBukkit());
            }
            for (BattleColor c : fadeColors) {
                b.withFade(c.asBukkit());
            }
            cached = b.build();
        }
        return cached;
    }
}
