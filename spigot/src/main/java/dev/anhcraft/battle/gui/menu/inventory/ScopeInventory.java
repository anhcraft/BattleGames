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
package dev.anhcraft.battle.gui.menu.inventory;

import dev.anhcraft.battle.ApiProvider;
import dev.anhcraft.battle.api.BattleApi;
import dev.anhcraft.battle.api.gui.struct.Slot;
import dev.anhcraft.battle.api.gui.screen.View;
import dev.anhcraft.battle.api.gui.page.Pagination;
import dev.anhcraft.battle.api.gui.page.SlotChain;
import dev.anhcraft.battle.api.inventory.item.ItemType;
import dev.anhcraft.battle.api.inventory.item.ScopeModel;
import dev.anhcraft.battle.api.storage.data.PlayerData;
import dev.anhcraft.craftkit.abif.PreparedItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ScopeInventory implements Pagination {
    @Override
    public void supply(@NotNull Player player, @NotNull View view, @NotNull SlotChain chain) {
        BattleApi api = ApiProvider.consume();
        PlayerData pd = api.getPlayerData(player);
        if(pd == null) return;
        for(Map.Entry<String, Long> entry : pd.getInventory().getStorage(ItemType.SCOPE).list()){
            if(!chain.hasNext()) break;
            if(chain.shouldSkip()) continue;
            ScopeModel sm = api.getScopeModel(entry.getKey());
            if (sm == null) continue;
            PreparedItem pi = api.getItemManager().make(sm);
            if(pi == null) continue;
            Slot slot = chain.next();
            slot.setPaginationItem(sm.getSkin().transform(pi));
        }
    }
}
