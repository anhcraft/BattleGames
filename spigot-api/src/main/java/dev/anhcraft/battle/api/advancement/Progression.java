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
import dev.anhcraft.battle.utils.info.InfoHolder;
import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Path;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Configurable
public class Progression implements Comparable<Progression>, Informative {
    @Description({
            "Amount of objects need to be achieved",
            "E.g: 10 kills, 10 wins, 10 deaths, etc"
    })
    private double amount;

    @Path("reward.exp")
    @Description("Amount of exp points to reward the player")
    private long rewardExp;

    @Path("reward.money")
    @Description("Some money to reward the player")
    private double rewardMoney;

    public double getAmount() {
        return amount;
    }

    public long getRewardExp() {
        return rewardExp;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    @Override
    public void inform(@NotNull InfoHolder holder) {
        holder.inform("exp", rewardExp).inform("money", rewardMoney);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Progression that = (Progression) o;
        return amount == that.amount &&
                that.rewardExp == rewardExp &&
                Double.compare(that.rewardMoney, rewardMoney) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public int compareTo(@NotNull Progression o) {
        return Double.compare(amount, o.amount);
    }
}
