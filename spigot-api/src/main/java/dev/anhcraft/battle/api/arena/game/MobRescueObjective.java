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

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Path;
import dev.anhcraft.config.annotations.Setting;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class MobRescueObjective {
    @Setting
    @Path("amount.min")
    @Description("Minimum amount of entities")
    private int minAmount = 1;

    @Setting
    @Path("amount.max")
    @Description("Maximum amount of entities")
    private int maxAmount = 1;

    @Setting
    @Path("reward_coins")
    @Description("Some coins to reward the player")
    private double rewardCoins;

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public double getRewardCoins() {
        return rewardCoins;
    }
}
