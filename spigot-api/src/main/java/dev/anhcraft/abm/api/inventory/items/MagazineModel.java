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
package dev.anhcraft.abm.api.inventory.items;

import dev.anhcraft.abm.api.ApiProvider;
import dev.anhcraft.confighelper.ConfigSchema;
import dev.anhcraft.confighelper.annotation.Explanation;
import dev.anhcraft.confighelper.annotation.IgnoreValue;
import dev.anhcraft.confighelper.annotation.Key;
import dev.anhcraft.confighelper.annotation.Schema;
import dev.anhcraft.confighelper.impl.TwoWayMiddleware;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Schema
public class MagazineModel extends SingleSkinItem implements Attachable, TwoWayMiddleware {
    public static final ConfigSchema<MagazineModel> SCHEMA = ConfigSchema.of(MagazineModel.class);

    @Key("ammo")
    @Explanation("All ammo types can be stored in this magazine")
    @IgnoreValue(ifNull = true)
    private Map<AmmoModel, Integer> ammunition = new HashMap<>();

    public MagazineModel(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull ItemType getItemType() {
        return ItemType.MAGAZINE;
    }

    @NotNull
    public Map<AmmoModel, Integer> getAmmunition() {
        return ammunition;
    }

    @Override
    public ItemType[] getHolderTypes() {
        return new ItemType[]{
                ItemType.GUN
        };
    }

    @Override
    protected @Nullable Object readConfig(ConfigSchema.Entry entry, @Nullable Object o) {
        if(o != null && entry.getKey().equals("ammo")){
            ConfigurationSection cs = (ConfigurationSection) o;
            Map<AmmoModel, Integer> ammo = new HashMap<>();
            for(String a : cs.getKeys(false)){
                AmmoModel am = ApiProvider.consume().getAmmoModel(a);
                if(am != null) {
                    ammo.put(am, cs.getInt(a));
                }
            }
            return ammo;
        }
        return o;
    }

    @Override
    protected @Nullable Object writeConfig(ConfigSchema.Entry entry, @Nullable Object o) {
        if(o != null && entry.getKey().equals("ammo")){
            ConfigurationSection parent = new YamlConfiguration();
            Map<AmmoModel, Integer> map = (Map<AmmoModel, Integer>) o;
            for(Map.Entry<AmmoModel, Integer> x : map.entrySet()){
                parent.set(x.getKey().getId(), x.getValue());
            }
            return parent;
        }
        return o;
    }
}
