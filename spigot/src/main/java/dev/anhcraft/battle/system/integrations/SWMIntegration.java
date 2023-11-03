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

package dev.anhcraft.battle.system.integrations;
/*
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;*/
import dev.anhcraft.battle.BattleComponent;
import dev.anhcraft.battle.BattlePlugin;/*
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;*/
import java.util.concurrent.CountDownLatch;

public class SWMIntegration extends BattleComponent implements ISWMIntegration {
    //private final SlimePlugin api = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

    public SWMIntegration(BattlePlugin plugin) {
        super(plugin);
    }

    @Override
    public int isReadOnly(String world) {
        /*WorldData worldData = ConfigManager.getWorldConfig().getWorlds().get(world);
        return worldData == null ? -1 : (worldData.isReadOnly() ? 1 : 0);*/
        return -1;
    }

    @Override
    public void reloadWorld(CountDownLatch countDownLatch, String world) {
        /*WorldData worldData = ConfigManager.getWorldConfig().getWorlds().get(world);
        if (worldData != null) {
            SlimeLoader loader = api.getLoader(worldData.getDataSource());
            SlimePropertyMap map = worldData.toPropertyMap();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                World w = Bukkit.getWorld(world);
                if (w == null) {
                    countDownLatch.countDown();
                    return;
                }
                w.getPlayers().forEach(c -> c.kickPlayer("The world is going to be reloaded"));
                if (Bukkit.unloadWorld(w, false)) {
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new a(loader, world, map, countDownLatch));
                } else {
                    countDownLatch.countDown();
                }
            });
        } else {
            countDownLatch.countDown();
        }*/
    }
/*
    private class a implements Runnable {
        private final SlimeLoader loader;
        private final String world;
        private final SlimePropertyMap map;
        private final CountDownLatch countDownLatch;

        public a(SlimeLoader loader, String world, SlimePropertyMap map, CountDownLatch countDownLatch) {
            this.loader = loader;
            this.world = world;
            this.map = map;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                SlimeWorld slimeWorld = api.loadWorld(loader, world, true, map);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    api.generateWorld(slimeWorld);
                    countDownLatch.countDown();
                });
            } catch (UnknownWorldException | CorruptedWorldException | NewerFormatException | IOException | WorldInUseException e) {
                e.printStackTrace();
                countDownLatch.countDown();
            }
        }
    }*/
}
