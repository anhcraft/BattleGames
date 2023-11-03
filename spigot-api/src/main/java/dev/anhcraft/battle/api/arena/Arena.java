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
package dev.anhcraft.battle.api.arena;

import dev.anhcraft.battle.ApiProvider;
import dev.anhcraft.battle.api.BattleApi;
import dev.anhcraft.battle.api.Rollback;
import dev.anhcraft.battle.api.arena.game.GamePlayer;
import dev.anhcraft.battle.api.arena.game.Mode;
import dev.anhcraft.battle.api.arena.game.options.GameOptions;
import dev.anhcraft.battle.api.effect.firework.BattleFirework;
import dev.anhcraft.battle.api.stats.natives.DeathStat;
import dev.anhcraft.battle.api.stats.natives.HeadshotStat;
import dev.anhcraft.battle.api.stats.natives.KillStat;
import dev.anhcraft.battle.impl.Informative;
import dev.anhcraft.battle.utils.PositionPair;
import dev.anhcraft.battle.utils.PreparedItem;
import dev.anhcraft.battle.utils.State;
import dev.anhcraft.battle.utils.info.InfoHolder;
import dev.anhcraft.config.ConfigDeserializer;
import dev.anhcraft.config.annotations.*;
import dev.anhcraft.config.schema.ConfigSchema;
import dev.anhcraft.config.struct.ConfigSection;
import dev.anhcraft.config.struct.SimpleForm;
import dev.anhcraft.jvmkit.utils.Condition;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class Arena implements Informative {
    @Exclude
    private final String id;

    @Description("The name of this arena")
    @Validation(notNull = true, silent = true)
    private String name;

    @Description("The game mode")
    @Validation(notNull = true)
    private String mode;

    @Description("The icon of this arena")
    @Validation(notNull = true)
    private PreparedItem icon;

    @Path("game_options")
    @Description(
            {
                    "Game mode settings",
                    "The settings are different per game-mode"
            }
    )
    @Validation(notNull = true)
    @Consistent
    private GameOptions gameOptions;

    @Path("max_time")
    @Description("The maximum playing time")
    private long maxTime;

    @Path("max_players")
    @Description("The maximum number of players")
    private int maxPlayers;

    @Path("allow_late_joins")
    @Description("Able to join a game even it has started")
    private boolean allowLateJoins;

    @Path("final_exp_formula")
    @Description({
            "The formula for calculating the final exp",
            "- a: number of headshots",
            "- b: number of kills",
            "- c: number of deaths",
            "- d: <b>1</b> if won or <b>0</b> if lost"
    })
    @Validation(notNull = true)
    private String finalExpCalculator;

    @Path("final_money_formula")
    @Description({
            "The formula for calculating the final money",
            "- a: number of headshots",
            "- b: number of kills",
            "- c: number of deaths",
            "- d: <b>1</b> if won or <b>0</b> if lost"
    })
    @Validation(notNull = true)
    private String finalMoneyCalculator;

    @Path("end_commands.winners")
    @Description({
            "Commands to be executed by the console at the end",
            "Placeholders can be used within the command with",
            "values parsed from each <b>winner</b>"
    })
    private List<String> endCommandWinners;

    @Path("end_commands.losers")
    @Description({
            "Commands to be executed by the console at the end",
            "Placeholders can be used within the command with",
            "values parsed from each <b>loser</b>"
    })
    private List<String> endCommandLosers;

    @Path("render_gui_on_death")
    @Description({
            "Re-renders the GUI on death",
            "This option can prevent players from reusing old items"
    })
    private boolean renderGuiOnDeath = true;

    @Path("bungeecord.enabled")
    @Description({
            "Enable the Bungeecord support for this arena",
            "<b>Note: You must enable the Bungeecord support for",
            "the whole plugin first (see general.yml)</b>"
    })
    private boolean bungeeSupport;

    @Path("bungeecord.remote_servers")
    @Description({
            "List of remote servers",
            "Remote servers are places where the game happens"
    })
    @Validation(notNull = true, silent = true)
    private List<String> remoteServers = new ArrayList<>();

    @Path("end_firework")
    @Description("The firework to be spawned when the game ends")
    private BattleFirework endFirework;

    @Path("end_delay")
    @Description("The delay time before the game actually ends")
    private long endDelay = 60;

    @Path("result_broadcast.won")
    @Description("The message to be sent to the winners")
    private List<String> wonReport;

    @Path("result_broadcast.lost")
    @Description("The message to be sent to the losers")
    private List<String> lostReport;

    @Description("Rollback settings")
    private Rollback rollback;

    @Path("empty_regions")
    @Consistent
    @Example({
            "empty_regions:",
            "  '1':",
            "    corner_1: lighthouse 23 118 -20 56 30",
            "    corner_2: lighthouse -10 143 15 -138 20"
    })
    private List<PositionPair> emptyRegions;

    @Path("max_deaths")
    @Description({
            "Maximum deaths allowed per player",
            "For example, to make one-life only, set this to 0",
            "Or to allow one chance of resurrection, set this to 1",
            "This option does not work with BedWars",
            "To disable, set to -1 (default)"
    })
    private int maxDeaths = -1;

    @Path("kill_stats_by_monsters")
    @Description({
            "Kill stats will be counted for monsters"
    })
    private boolean killStatsByMonsters;

    @Path("disable_death_message")
    private boolean disableDeathMessage;

    @Path("message_on_join")
    private List<String> messageOnJoin;

    public Arena(@NotNull String id) {
        Condition.notNull(id, "Id must be non-null");
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Mode getMode() {
        Mode m = Mode.get(mode);
        if (m == null) {
            throw new UnsupportedOperationException("Mode is not present");
        }
        return m;
    }

    @NotNull
    public PreparedItem getIcon() {
        return icon;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isAllowLateJoins() {
        return allowLateJoins;
    }

    private Expression finalMoneyExpression;
    private Expression finalExpExpression;

    public double calculateFinalMoney(@NotNull GamePlayer player) {
        Condition.notNull(player, "Player must be non-null");
        if(finalMoneyExpression == null) {
            finalMoneyExpression = new ExpressionBuilder(finalMoneyCalculator).variables("a", "b", "c", "d").build();
        }
        return finalMoneyExpression
                .setVariable("a", player.getStats().of(HeadshotStat.class).get())
                .setVariable("b", player.getStats().of(KillStat.class).get())
                .setVariable("c", player.getStats().of(DeathStat.class).get())
                .setVariable("d", player.isWinner() ? 1 : 0)
                .evaluate();
    }

    public long calculateFinalExp(@NotNull GamePlayer player) {
        Condition.notNull(player, "Player must be non-null");
        if(finalExpExpression == null) {
            finalExpExpression = new ExpressionBuilder(finalExpCalculator).variables("a", "b", "c", "d").build();
        }
        return (long) finalExpExpression
                .setVariable("a", player.getStats().of(HeadshotStat.class).get())
                .setVariable("b", player.getStats().of(KillStat.class).get())
                .setVariable("c", player.getStats().of(DeathStat.class).get())
                .setVariable("d", player.isWinner() ? 1 : 0)
                .evaluate();
    }

    @NotNull
    public GameOptions getGameOptions() {
        return gameOptions;
    }

    @Nullable
    public List<String> getEndCommandWinners() {
        return endCommandWinners;
    }

    @Nullable
    public List<String> getEndCommandLosers() {
        return endCommandLosers;
    }

    public boolean isRenderGuiOnDeath() {
        return renderGuiOnDeath;
    }

    @NotNull
    public List<String> getRemoteServers() {
        return remoteServers;
    }

    public boolean hasBungeecordSupport() {
        return bungeeSupport;
    }

    @Nullable
    public BattleFirework getEndFirework() {
        return endFirework;
    }

    public long getEndDelay() {
        return endDelay;
    }

    @Nullable
    public Rollback getRollback() {
        return rollback;
    }

    @Nullable
    public List<String> getWonReport() {
        return wonReport;
    }

    @Nullable
    public List<String> getLostReport() {
        return lostReport;
    }

    @Nullable
    public List<PositionPair> getEmptyRegions() {
        return emptyRegions;
    }

    public int getMaxDeaths() {
        return maxDeaths;
    }

    public boolean isKillStatsByMonsters() {
        return killStatsByMonsters;
    }

    public boolean isDeathMessageDisabled() {
        return disableDeathMessage;
    }

    @Nullable
    public List<String> getMessageOnJoin() {
        return messageOnJoin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arena arena = (Arena) o;
        return id.equals(arena.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void inform(@NotNull InfoHolder holder) {
        InfoHolder modeInfo = new InfoHolder("mode_");
        getMode().inform(modeInfo);
        holder.inform("id", id)
                .inform("name", name)
                .inform("max_time", maxTime)
                .inform("max_players", maxPlayers)
                .inform("end_delay", endDelay)
                .inform("bungeecord", State.ENABLED.inCaseOf(bungeeSupport))
                .link(modeInfo);
    }

    @PostHandler
    private void handle(ConfigDeserializer deserializer, ConfigSchema schema, ConfigSection section){
        if (bungeeSupport && !ApiProvider.consume().hasBungeecordSupport()) {
            BattleApi.getInstance().getLogger().warning(String.format("Looks like you have enabled Bungeecord support for arena `%s`. But please also enable it in general.yml as well. The option is now skipped for safe!", id));
        }
        try {
            gameOptions = deserializer.transformConfig(Objects.requireNonNull(getMode().getOptionSchema()),
                    Objects.requireNonNull(Objects.requireNonNull(section.get("game_options")).asSection()));

            SimpleForm sf = section.get("empty_regions");
            if(sf != null){
                ConfigSection cs = sf.asSection();
                if(cs != null) {
                    emptyRegions = new ArrayList<>();
                    for (String k : cs.getKeys(false)) {
                        ConfigSection v = Objects.requireNonNull(cs.get(k)).asSection();
                        if (v == null) continue;
                        SimpleForm v1 = v.get("corner_1");
                        if (v1 == null) continue;
                        SimpleForm v2 = v.get("corner_2");
                        if (v2 == null) continue;
                        emptyRegions.add(new PositionPair(
                                Objects.requireNonNull(v1.asString()),
                                Objects.requireNonNull(v2.asString())
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
