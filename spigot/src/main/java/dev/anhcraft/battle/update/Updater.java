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

package dev.anhcraft.battle.update;

import dev.anhcraft.battle.BattlePlugin;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Updater {
    private int project;
    private URL checkURL;
    private String newVersion;
    private BattlePlugin plugin;

    public Updater(BattlePlugin plugin, int projectID) {
        this.plugin = plugin;
        this.project = projectID;
        this.newVersion = plugin.getDescription().getVersion();
        try {
            this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectID);
        } catch (MalformedURLException e) {
            plugin.getLogger().info("Could not connect to Spigot, plugin disabled!");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public boolean checkForUpdates() throws Exception {
        URLConnection con = this.checkURL.openConnection();
        this.newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
        return !this.plugin.getDescription().getVersion().equalsIgnoreCase(this.newVersion);
    }
}