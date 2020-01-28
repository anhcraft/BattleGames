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

package dev.anhcraft.battle.api.arena.mode.options;

import dev.anhcraft.confighelper.ConfigHelper;
import dev.anhcraft.confighelper.ConfigSchema;
import dev.anhcraft.confighelper.annotation.IgnoreValue;
import dev.anhcraft.confighelper.annotation.Key;
import dev.anhcraft.confighelper.annotation.Schema;
import dev.anhcraft.confighelper.exception.InvalidValueException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Schema
public class CaptureTheFlagOptions extends TeamDeathmatchOptions {
    public static final ConfigSchema<CaptureTheFlagOptions> SCHEMA = ConfigSchema.of(CaptureTheFlagOptions.class);

    @Key("flags")
    @IgnoreValue(ifNull = true)
    private List<FlagOptions> flags = new ArrayList<>();

    @NotNull
    public List<FlagOptions> getFlags() {
        return flags;
    }

    @Nullable
    protected Object conf2schema(@Nullable Object value, ConfigSchema.Entry entry){
        if(value != null && entry.getKey().equals("flags")){
            ConfigurationSection conf = (ConfigurationSection) value;
            List<FlagOptions> flags = new ArrayList<>();
            try {
                for (String s : conf.getKeys(false)) {
                    flags.add(ConfigHelper.readConfig(conf.getConfigurationSection(s), FlagOptions.SCHEMA));
                }
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
            return flags;
        }
        return value;
    }

    @Nullable
    protected Object schema2conf(@Nullable Object value, ConfigSchema.Entry entry){
        if(value != null && entry.getKey().equals("flags")){
            YamlConfiguration conf = new YamlConfiguration();
            int i = 0;
            for (FlagOptions f : (List<FlagOptions>) value) {
                YamlConfiguration c = new YamlConfiguration();
                ConfigHelper.writeConfig(c, FlagOptions.SCHEMA, f);
                conf.set(String.valueOf(i++), c);
            }
            return conf;
        }
        return value;
    }
}