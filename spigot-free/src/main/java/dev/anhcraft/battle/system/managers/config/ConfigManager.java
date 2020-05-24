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

package dev.anhcraft.battle.system.managers.config;

import dev.anhcraft.battle.BattleComponent;
import dev.anhcraft.battle.BattlePlugin;
import dev.anhcraft.battle.api.BattleApi;
import dev.anhcraft.battle.utils.ColorUtil;
import dev.anhcraft.jvmkit.utils.HttpUtil;
import dev.anhcraft.jvmkit.utils.IOUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class ConfigManager extends BattleComponent {
    private static final YamlConfiguration EMPTY = new YamlConfiguration();
    protected final String loggerName;
    private final String filePath;
    private final String resourcePath;
    private YamlConfiguration defaultSettings; // must be NULL here!!!
    private YamlConfiguration settings = EMPTY;
    private int reloadCount;
    private boolean compareDefault;
    private boolean preventRemote;

    public ConfigManager(@NotNull String path) {
        this(path, null);
    }

    public ConfigManager(@NotNull String filePath, @Nullable String resourcePath) {
        super((BattlePlugin) BattleApi.getInstance());
        this.loggerName = getClass().getSimpleName();
        this.filePath = filePath;
        this.resourcePath = (resourcePath == null ? filePath : resourcePath);
    }

    protected abstract void onLoad();
    protected abstract void onClean();
    @NotNull
    protected File getConfigFile() {
        return new File(plugin.configFolder, filePath);
    }

    private void loadDefaultConfig() {
        if(defaultSettings != null) return;
        try {
            String path = "config/" + resourcePath;
            InputStream in = plugin.getResource(path);
            if(in == null) {
                plugin.getLogger().warning("["+loggerName+"] Resource not found: " + path);
                return;
            }
            byte[] bytes = IOUtil.toByteArray(in, 1024);
            in.close();
            Reader reader = new StringReader(new String(bytes, StandardCharsets.UTF_8));
            defaultSettings = YamlConfiguration.loadConfiguration(reader);
            reader.close();
        } catch (IOException e) {
            plugin.getLogger().warning("["+loggerName+"] Failed to load default config!");
            e.printStackTrace();
        }
    }

    public synchronized void reloadConfig() {
        if(reloadCount > 0) {
            plugin.getLogger().info("["+loggerName+"] Cleaning cache...");
            cleanCache();
        }
        boolean matchDef = false;
        if(preventRemote || !plugin.getSystemConfig().isRemoteConfigEnabled()){
            File f = getConfigFile();
            plugin.getLogger().info("["+loggerName+"] Loading config...");
            if (f.exists()) {
                settings = YamlConfiguration.loadConfiguration(f);
            } else {
                loadDefaultConfig();
                settings = defaultSettings;
                try {
                    f.createNewFile();
                    settings.save(f);
                } catch (IOException e) {
                    plugin.getLogger().warning("["+loggerName+"] Failed to save config to " + f.getAbsolutePath());
                    e.printStackTrace();
                }
                matchDef = true; // no need to compare with default again
            }
        } else {
            String url = String.format(plugin.getSystemConfig().getRemoteConfigLink(), filePath);
            plugin.getLogger().info("["+loggerName+"] Downloading config...");
            try {
                byte[] bytes = HttpUtil.fetch(url);
                Reader reader = new StringReader(new String(bytes, StandardCharsets.UTF_8));
                settings = YamlConfiguration.loadConfiguration(reader);
                reader.close();
            } catch (IOException e) {
                plugin.getLogger().warning("["+loggerName+"] Failed to download config from " + url);
                e.printStackTrace();
                loadDefaultConfig();
                settings = defaultSettings;
                matchDef = true; // no need to compare with default again
            }
        }
        if(!matchDef && shouldCompareDefault()) {
            loadDefaultConfig();
            if(defaultSettings != null) {
                int errorCount = 0;
                for (String s : settings.getKeys(true)) {
                    if (!defaultSettings.isSet(s)) {
                        plugin.getLogger().warning("[" + loggerName + "/Validator] Redundant entry " + s);
                        errorCount++;
                    }
                }
                for (String s : defaultSettings.getKeys(true)) {
                    if (!settings.isSet(s)) {
                        settings.set(s, defaultSettings.get(s));
                        plugin.getLogger().warning("[" + loggerName + "/Validator] Missing entry " + s);
                        errorCount++;
                    }
                }
                if (errorCount == 0) {
                    plugin.getLogger().info("[" + loggerName + "/Validator] All good! The config is up-to-date!");
                } else {
                    plugin.getLogger().warning("[" + loggerName + "/Validator] " + errorCount + " problems found. Please fix them asap!");
                }
            }
        }
        ColorUtil.colorize(settings);
        onLoad();
        reloadCount++;
    }

    public synchronized void cleanCache() {
        settings = EMPTY;
        onClean();
    }

    @NotNull
    public String getFilePath() {
        return filePath;
    }

    @NotNull
    public String getResourcePath() {
        return resourcePath;
    }

    @NotNull
    public YamlConfiguration getSettings() {
        return settings;
    }

    public int getReloadCount() {
        return reloadCount;
    }

    public boolean shouldCompareDefault() {
        return compareDefault;
    }

    public void setCompareDefault(boolean compareDefault) {
        this.compareDefault = compareDefault;
    }

    public boolean isPreventRemote() {
        return preventRemote;
    }

    public void setPreventRemote(boolean preventRemote) {
        this.preventRemote = preventRemote;
    }
}
