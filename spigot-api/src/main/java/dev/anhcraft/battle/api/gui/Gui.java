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
package dev.anhcraft.battle.api.gui;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.anhcraft.battle.api.BattleSound;
import dev.anhcraft.battle.api.gui.struct.Component;
import dev.anhcraft.battle.impl.Informative;
import dev.anhcraft.battle.utils.info.InfoHolder;
import dev.anhcraft.config.annotations.*;
import dev.anhcraft.jvmkit.utils.Condition;
import dev.anhcraft.jvmkit.utils.MathUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("FieldMayBeFinal")
@Configurable
public class Gui implements Informative {
    private final Map<Integer, Component> S2C = new HashMap<>();
    private final Multimap<String, Component> P2C = HashMultimap.create();
    private final String id;
    private int size;

    @Description("A nice title for the GUI")
    @Validation(notNull = true)
    private String title;

    @Description("List of components")
    @Validation(notNull = true, silent = true)
    @Example({
            "components:",
            "  '0':",
            "    positions: 1 -> 9; 1 -> 3",
            "    pagination: advancements",
            "    item:",
            "      material: gray_stained_glass_pane",
            "      name: '&f'",
            "    functions:",
            "      on_click:",
            "        - Battle:CancelEvent()",
            "  '1':",
            "    positions:",
            "      - 1 -> 5; 4",
            "      - 8; 4",
            "    item:",
            "      material: gray_stained_glass_pane",
            "      name: '&f'",
            "    functions:",
            "      on_click:",
            "        - Battle:CancelEvent()"
    })
    private Map<String, Component> components = new HashMap<>();

    @Description("Sound to play on opening")
    private BattleSound sound;

    public Gui(@NotNull String id) {
        Condition.argNotNull("id", id);
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    @NotNull
    public Collection<Component> getComponents() {
        return components.values();
    }

    @Nullable
    public BattleSound getSound() {
        return sound;
    }

    @Nullable
    public Component getComponentAt(int pos) {
        return S2C.get(pos);
    }

    @NotNull
    public Collection<Component> getComponentOf(@NotNull String pagination) {
        return P2C.get(pagination);
    }

    @NotNull
    public Collection<String> getAllPagination() {
        return P2C.keys();
    }

    @Override
    public void inform(@NotNull InfoHolder holder) {
        holder.inform("id", id).inform("size", size);
    }

    @PostHandler
    private void handle(){
        int highestSlot = 0;
        for (Component c : components.values()) {
            for (Integer i : c.getSlots()) {
                Component prev = S2C.put(i, c);
                // if this slot exists in previous component, we will remove it
                if (prev != null) {
                    prev.getSlots().remove(i);
                }
            }
            if (c.getPagination() != null) {
                if (!P2C.put(c.getPagination(), c)) {
                    //Bukkit.getLogger().warning("Pagination should not be duplicated! `" + c.getPagination() + "` in component: " + c.getId());
                }
            }
            int hs = Collections.max(c.getSlots());
            if (hs > highestSlot) highestSlot = hs;
        }
        size = MathUtil.nextMultiple(highestSlot, 9);
        if (size > 54) {
            Bukkit.getLogger().warning("The inventory size is out of bound: " + size);
            size = 54;
        }
    }
}
