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
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class BattleEffect {
    @Description("The particle that used to make up this effect")
    private BattleParticle particle;

    @Path("block_effect")
    @Description("Fake block effect")
    private FakeBlockEffect blockEffect;

    @Description("The effect's type")
    @Validation(notNull = true)
    private EffectType type;

    @Description("Effect options")
    @Validation(notNull = true, silent = true)
    @Example({
            "options:",
            "  repeat_delay: 20",
            "  repeat_times: 5"
    })
    private Map<EffectOption, Object> options = new HashMap<>();

    @NotNull
    public Map<EffectOption, Object> getOptions() {
        return options;
    }

    @NotNull
    public EffectType getType() {
        return type;
    }

    @Nullable
    public BattleParticle getParticle() {
        return particle;
    }

    @Nullable
    public FakeBlockEffect getBlockEffect() {
        return blockEffect;
    }

    public void spawn(@NotNull Location location) {
        Condition.argNotNull("location", location);
        if (particle != null) {
            particle.spawn(location);
        }
        if (blockEffect != null) {
            blockEffect.spawn(location);
        }
    }

    public void spawn(@NotNull World world, double x, double y, double z) {
        Condition.argNotNull("world", world);
        if (particle != null) {
            particle.spawn(world, x, y, z);
        }
        if (blockEffect != null) {
            blockEffect.spawn(new Location(world, x, y, z));
        }
    }
}
