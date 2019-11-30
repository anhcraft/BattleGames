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
package dev.anhcraft.battle.gui.menu.market;

import dev.anhcraft.battle.api.ApiProvider;
import dev.anhcraft.battle.api.BattleAPI;
import dev.anhcraft.battle.api.events.PlayerPurchaseEvent;
import dev.anhcraft.battle.api.game.GamePlayer;
import dev.anhcraft.battle.api.gui.struct.Slot;
import dev.anhcraft.battle.api.gui.screen.View;
import dev.anhcraft.battle.api.gui.page.Pagination;
import dev.anhcraft.battle.api.gui.page.SlotChain;
import dev.anhcraft.battle.api.market.Category;
import dev.anhcraft.battle.api.market.Market;
import dev.anhcraft.battle.api.market.Product;
import dev.anhcraft.battle.api.market.Transaction;
import dev.anhcraft.battle.api.misc.info.InfoHolder;
import dev.anhcraft.battle.api.storage.data.PlayerData;
import dev.anhcraft.battle.gui.GDataRegistry;
import dev.anhcraft.battle.system.integrations.VaultApi;
import dev.anhcraft.battle.utils.PlaceholderUtil;
import dev.anhcraft.craftkit.abif.PreparedItem;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ProductMenu implements Pagination {
    @Override
    public void supply(@NotNull Player player, @NotNull View view, @NotNull SlotChain chain) {
        BattleAPI api = ApiProvider.consume();
        Category ctg = (Category) view.getWindow().getDataContainer().remove(GDataRegistry.MARKET_CATEGORY);
        if(ctg == null) return;
        Market mk = api.getMarket();
        GamePlayer gp = api.getGameManager().getGamePlayer(player);
        for(Product p : ctg.getProducts()){
            if(!chain.hasNext()) break;
            if((p.isInGameOnly() && gp == null) || chain.shouldSkip()){
                continue;
            }

            boolean useInGamePrice = gp != null && p.getPriceIgn() >= 0;

            PreparedItem ic = p.getIcon().duplicate();
            if(mk.isSummaryProductInfoEnabled()){
                List<String> lore = gp != null && mk.getSummaryProductIgnLore() != null ? mk.getSummaryProductIgnLore() : mk.getSummaryProductLore();
                if(lore != null) {
                    InfoHolder holder = new InfoHolder("product_");
                    p.inform(holder);
                    Map<String, String> map = api.mapInfo(holder);
                    for (String s : lore) {
                        ic.lore().add(PlaceholderUtil.formatInfo(s, map));
                    }
                }
            }

            Slot slot = chain.next();
            slot.setPaginationItem(ic);
            slot.setAdditionalFunction(report -> {
                final double balance = useInGamePrice ? gp.getIgBalance().get() :
                        VaultApi.getEconomyApi().getBalance(player);
                final double price = useInGamePrice ? p.getPriceIgn() :
                        p.getPriceVault();

                if(balance < price){
                    api.getChatManager().sendPlayer(report.getPlayer(), "market.not_enough_money", s -> String.format(s, balance));
                    return;
                }
                PlayerData pd = api.getPlayerData(player);
                if(pd == null) return;

                PlayerPurchaseEvent ev = new PlayerPurchaseEvent(player, mk, ctg, p);
                Bukkit.getPluginManager().callEvent(ev);
                if(ev.isCancelled()) return;

                if(useInGamePrice){
                    gp.getIgBalance().addAndGet(-price);
                } else {
                    EconomyResponse er = VaultApi.getEconomyApi().withdrawPlayer(player, price);
                    if(!er.transactionSuccess()){
                        api.getChatManager().sendPlayer(report.getPlayer(), "market.purchase_failed");
                        return;
                    }
                }

                p.givePlayer(player, pd);
                api.getChatManager().sendPlayer(report.getPlayer(), "market.purchase_success");
                if(mk.shouldLogTransactions()){
                    pd.getTransactions().add(new Transaction(
                            player.getUniqueId(),
                            p.getId(),
                            price,
                            System.currentTimeMillis()
                    ));
                }
            });
        }
    }
}