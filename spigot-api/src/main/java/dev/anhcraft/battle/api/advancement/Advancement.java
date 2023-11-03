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

package dev.anhcraft.battle.api.advancement;

import dev.anhcraft.battle.impl.Informative;
import dev.anhcraft.battle.utils.State;
import dev.anhcraft.battle.utils.info.InfoHolder;
import dev.anhcraft.config.annotations.*;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Configurable
public class Advancement implements Informative {
    @Exclude
    private final String id;

    @Description({
            "The statistic type",
            "All statistic types can be seen here: <a href=\"https://github.com/battlegames/Battle-Hub/wiki/Statistics\">https://github.com/battlegames/Battle-Hub/wiki/Statistics</a>",
            "Except \"exp\" which is not supported."
    })
    @Validation(notNull = true)
    private String type;

    @Description("The advancement's name")
    @Validation(notNull = true)
    private String name;

    @Description("A nice description for this advancement")
    private List<String> description;

    @Validation(notNull = true)
    @Description("The icon")
    private Material icon;

    @Path("inherit_progress")
    @Description({
            "Should a player's progressions be inherited from",
            "previous advancements with the same statistics type?",
            "This should be enabled as players can continue what",
            "they have achieved without doing from the beginning."
    })
    private boolean inheritProgress;

    @Description({
            "Different period of this advancements",
            "They can be known as 'levels'"
    })
    @Validation(notNull = true)
    @Example({
            "progression:",
            "  '0':",
            "    amount: 1",
            "    reward:",
            "      exp: 5",
            "      money: 8",
            "  '1':",
            "    amount: 10",
            "    reward:",
            "      exp: 60",
            "      money: 100",
            "  '2':",
            "    amount: 20",
            "    reward:",
            "      exp: 150",
            "      money: 200"
    })
    private Map<String, Progression> progression;

    public Advancement(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getType() {
        return type;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public List<String> getDescription() {
        return description;
    }

    @NotNull
    public Material getIcon() {
        return icon;
    }

    public boolean getInheritProgress() {
        return inheritProgress;
    }

    private SortedSet<Progression> progressions;

    @NotNull
    public SortedSet<Progression> getProgression() {
        return progressions;
    }

    private double maxAmount;

    public double getMaxAmount() {
        return maxAmount;
    }

    @Override
    public void inform(@NotNull InfoHolder holder) {
        holder.inform("id", id)
                .inform("type", type)
                .inform("name", name)
                .inform("description", description)
                .inform("inherit", State.ENABLED.inCaseOf(inheritProgress));
    }

    @PostHandler
    private void handle(){
        this.progressions = new TreeSet<>(Comparator.naturalOrder());
        for (Progression p : progression.values()) {
            maxAmount = Math.max(maxAmount, p.getAmount());
            this.progressions.add(p);
        }
    }
}
