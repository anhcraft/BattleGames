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

package dev.anhcraft.battle.api.arena.game.options;

import dev.anhcraft.battle.api.BattleSound;
import dev.anhcraft.battle.api.arena.game.MobGroup;
import dev.anhcraft.battle.api.arena.game.MobRescueObjective;
import dev.anhcraft.battle.api.arena.team.MRTeam;
import dev.anhcraft.battle.utils.LocationUtil;
import dev.anhcraft.config.annotations.*;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class MobRescueOptions extends GameOptions {
    @Path("extra_farmer_countdown_time")
    @Description("Extra countdown time for farmers")
    private long extraCountdownTimeFarmer;

    @Path("playing_spawn_points_farmer")
    @Description("The spawn points of team Farmer (in playing phase)")
    @Validation(notNull = true, silent = true)
    private List<String> playSpawnPointsFarmer = new ArrayList<>();

    @Path("playing_spawn_points_thief")
    @Description("The spawn points of team Thief (in playing phase)")
    @Validation(notNull = true, silent = true)
    private List<String> playSpawnPointsThief = new ArrayList<>();

    @Path("mob_groups")
    @Description("Configuration for mobs")
    @Validation(notNull = true)
    @Example({
            "mob_groups:",
            "  '0':",
            "    location: \"farm 15.6338 65.0 -59.4813 -95.411865 3.89998\"",
            "    entity_type: horse",
            "    weight: 800",
            "    stealable: false",
            "    amount: 1",
            "  '1':",
            "    location: \"farm 15.6295 65.0 -55.3090 -91.36191 2.8499768\"",
            "    entity_type: horse",
            "    weight: 800",
            "    stealable: false",
            "    amount: 1",
            "  '2':",
            "    location: \"farm 15.3668 65.0 -51.2762 -93.31171 1.4999669\"",
            "    entity_type: horse",
            "    weight: 800",
            "    stealable: false",
            "    amount: 1",
            "  '3':",
            "    location: \"farm 66.0071 72.0 -83.4858 -180.01172 -1.5000186\"",
            "    entity_type: cow",
            "    weight: 600",
            "    stealable: true",
            "    amount: 1"
    })
    private Map<String, MobGroup> mobGroups;

    @Path("weight_speed_ratio")
    @Description({
            "Ratio between mob weight and speed reduction",
            "Take an example, when the ratio is 4000",
            "if a player carries a cow weighting 500 (kg)",
            "his speed will be reduced by 500/4000 = 0.125",
            "Note: the default speed is 0.2"
    })
    private double weightSpeedRatio;

    @Path("gathering_region.corner_1")
    @Description("First corner of the gathering region")
    @Validation(notNull = true)
    private String gatheringRegionCorner1;

    @Path("gathering_region.corner_2")
    @Description("Second corner of the gathering region")
    @Validation(notNull = true)
    private String gatheringRegionCorner2;

    @Validation(notNull = true)
    @Description("Objectives")
    @Example({
            "objectives:",
            "  cow:",
            "    amount:",
            "      min: 1",
            "      max: 2",
            "    reward_coins: 50",
            "  pig:",
            "    amount:",
            "      min: 2",
            "      max: 5",
            "    reward_coins: 10"
    })
    private Map<EntityType, MobRescueObjective> objectives;

    @Path("sounds.on_pick_up_mob")
    @Description("Sound to be played on picking up mobs")
    private BattleSound pickUpMobSound;

    @Path("sounds.on_put_down_mob")
    @Description("Sound to be played on putting down mobs")
    private BattleSound putDownMobSound;

    @Path("sounds.extra_countdown")
    @Description("Sound during extra countdown phrase")
    private BattleSound extraCountdownSound;

    public long getExtraCountdownTimeFarmer() {
        return extraCountdownTimeFarmer;
    }

    @NotNull
    public List<Location> getPlaySpawnPoints(@NotNull MRTeam team) {
        return (team == MRTeam.FARMER ? playSpawnPointsFarmer : playSpawnPointsThief).stream().map(LocationUtil::fromString).collect(Collectors.toList());
    }

    @NotNull
    public Collection<MobGroup> getMobGroups() {
        return mobGroups.values();
    }

    public double getWeightSpeedRatio() {
        return weightSpeedRatio;
    }

    @NotNull
    public Location getGatheringRegionCorner1() {
        return LocationUtil.fromString(gatheringRegionCorner1);
    }

    @NotNull
    public Location getGatheringRegionCorner2() {
        return LocationUtil.fromString(gatheringRegionCorner2);
    }

    @NotNull
    public Map<EntityType, MobRescueObjective> getObjectives() {
        return objectives;
    }

    @Nullable
    public BattleSound getPickUpMobSound() {
        return pickUpMobSound;
    }

    @Nullable
    public BattleSound getPutDownMobSound() {
        return putDownMobSound;
    }

    @Nullable
    public BattleSound getExtraCountdownSound() {
        return extraCountdownSound;
    }
}
