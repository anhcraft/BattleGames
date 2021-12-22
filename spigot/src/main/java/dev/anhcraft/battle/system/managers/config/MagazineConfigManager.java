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

import dev.anhcraft.battle.api.inventory.item.MagazineModel;
import dev.anhcraft.battle.utils.ConfigHelper;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class MagazineConfigManager extends ConfigManager {
    public final Map<String, MagazineModel> MAGAZINE_MAP = new HashMap<>();

    public MagazineConfigManager() {
        super("Magazine", "items/magazines.yml");
    }

    @Override
    public void onLoad() {
        getSettings().getKeys(false).forEach(s -> {
            MagazineModel m = new MagazineModel(s);
            ConfigurationSection cs = getSettings().getConfigurationSection(s);
            ConfigHelper.load(MagazineModel.class, cs, m);
            MAGAZINE_MAP.put(s, m);
        });
    }

    @Override
    public void onClean() {
        MAGAZINE_MAP.clear();
    }
}
