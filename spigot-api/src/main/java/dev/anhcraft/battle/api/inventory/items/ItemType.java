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
package dev.anhcraft.battle.api.inventory.items;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum ItemType {
    AMMO(Ammo::new),
    GUN(Gun::new),
    MAGAZINE(Magazine::new),
    SCOPE(Scope::new),
    GRENADE(Grenade::new);

    Supplier<BattleItem> c;

    ItemType(Supplier<BattleItem> c){
        this.c = c;
    }

    private String localizedName = name();

    @NotNull
    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(@NotNull String localizedName) {
        Validate.notNull(localizedName, "Localized name must be non-null");
        this.localizedName = localizedName;
    }

    @NotNull
    public BattleItem make(){
        return c.get();
    }
}