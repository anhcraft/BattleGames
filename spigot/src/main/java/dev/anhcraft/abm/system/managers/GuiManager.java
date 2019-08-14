package dev.anhcraft.abm.system.managers;

import com.google.common.collect.LinkedHashMultimap;
import dev.anhcraft.abif.ABIF;
import dev.anhcraft.abif.PreparedItem;
import dev.anhcraft.abm.BattlePlugin;
import dev.anhcraft.abm.api.ext.BattleComponent;
import dev.anhcraft.abm.api.ext.gui.GuiHandler;
import dev.anhcraft.abm.api.ext.gui.GuiListener;
import dev.anhcraft.abm.api.impl.gui.PaginationHandler;
import dev.anhcraft.abm.api.objects.gui.*;
import dev.anhcraft.abm.utils.PlaceholderUtils;
import dev.anhcraft.jvmkit.helpers.PaginationHelper;
import dev.anhcraft.jvmkit.lang.annotation.Label;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class GuiManager extends BattleComponent {
    private final Map<String, Gui> GUI = new HashMap<>();
    private final Map<String, GuiHandler> GUI_HANDLERS = new HashMap<>();
    private final Map<Player, PlayerGui> PLAYER_GUI = new HashMap<>();

    public GuiManager(BattlePlugin plugin) {
        super(plugin);
    }

    public void registerGui(String id, Gui gui){
        GUI.put(id, gui);
    }

    public void registerGuiHandler(String id, GuiHandler handler){
        GUI_HANDLERS.put(id, handler);

        // register slot listeners here
        Method[] methods = handler.getClass().getMethods();
        for(Method m : methods){
            m.setAccessible(true);
            if(!m.isAnnotationPresent(Label.class)) continue;
            int count = m.getParameterCount();
            String[] args = m.getAnnotation(Label.class).value();
            if(args.length == 1){
                handler.getEventListeners().put(args[0], new GuiListener<GuiReport>(GuiReport.class) {
                    @Override
                    public void call(GuiReport event) {
                        try {
                            if (count == 1) m.invoke(handler, event.getPlayer());
                            else if (count == 2) m.invoke(handler, event.getPlayer(), event.getGui());
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if(args.length == 2){
                String event = args[1];
                switch (event){
                    case "onSlot":{
                        handler.getEventListeners().put(args[0], new GuiListener<SlotReport>(SlotReport.class) {
                            @Override
                            public void call(SlotReport event) {
                                try {
                                    m.invoke(handler, event);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    }
                    case "onClickSlot":{
                        handler.getEventListeners().put(args[0], new GuiListener<SlotClickReport>(SlotClickReport.class) {
                            @Override
                            public void call(SlotClickReport event) {
                                try {
                                    m.invoke(handler, event);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    }
                    case "onCancellableSlot":{
                        handler.getEventListeners().put(args[0], new GuiListener<SlotCancelReport>(SlotCancelReport.class) {
                            @Override
                            public void call(SlotCancelReport event) {
                                try {
                                    m.invoke(handler, event);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    @NotNull
    public PlayerGui getPlayerGui(Player player){
        PlayerGui x = PLAYER_GUI.get(player);
        if(x == null) {
            PLAYER_GUI.put(player, x = new PlayerGui());
        }
        return x;
    }

    public void callEvent(Player p, int slot, boolean top) {
        PlayerGui pg = getPlayerGui(p);
        callEvent(p, top ? pg.getTopGui() : pg.getBottomGui(), slot);
    }

    public void callEvent(Player p, BattleGui bg, int slot) {
        callEvent(p, bg, slot, null);
    }

    public void callEvent(Player p, int slot, boolean top, @Nullable Event event) {
        PlayerGui pg = getPlayerGui(p);
        callEvent(p, top ? pg.getTopGui() : pg.getBottomGui(), slot, event);
    }

    public void callEvent(Player p, @Nullable BattleGui bg, int slot, @Nullable Event event) {
        if(bg == null) return;
        BattleGuiSlot[] x = bg.getSlots();
        if (slot < x.length) {
            BattleGuiSlot s = x[slot];
            if(s == null) return;
            s.getEvents().forEach(gl -> {
                if(gl.getClazz() == GuiReport.class){
                    ((GuiListener<GuiReport>) gl).call(new GuiReport(p, bg));
                }
                else if(gl.getClazz() == SlotReport.class){
                    ((GuiListener<SlotReport>) gl).call(new SlotReport(p, bg, s));
                }
                else if(gl.getClazz() == SlotClickReport.class){
                    if(event instanceof InventoryClickEvent)
                        ((GuiListener<SlotClickReport>) gl).call(
                                new SlotClickReport(p, bg, s, (InventoryClickEvent) event));
                }
                else if(gl.getClazz() == SlotCancelReport.class){
                    if(event instanceof Cancellable)
                        ((GuiListener<SlotCancelReport>) gl).call(
                                new SlotCancelReport(p, bg, s, (Cancellable) event));
                }
            });
        }
    }

    private BattleGui setupGui(Player player, PlayerGui pg, Gui gui){
        BattleGuiSlot[] slots = new BattleGuiSlot[gui.getSize()];
        for(int i = 0; i < gui.getSize(); i++){
            List<GuiListener<? extends GuiReport>> listeners = new ArrayList<>();
            // only handling on normal slots
            GuiSlot s = gui.getSlots()[i];
            if(s.isPaginationSlot()) continue;
            Collection<String> ehs = s.getEventHandlers();
            for (String eh : ehs) {
                String[] args = eh.split("::");
                if (args.length < 2) continue;

                GuiHandler guiHandler = GUI_HANDLERS.get(args[0]);
                if (guiHandler == null) continue;

                GuiListener<? extends GuiReport> listener = guiHandler.getEventListeners().get(args[1]);
                listeners.add(listener);
            }
            slots[i] = new BattleGuiSlot(i, s, listeners);
        }

        if(gui.getPagination() != null){
            GuiHandler gh = GUI_HANDLERS.get(gui.getPagination().getHandler());
            if (gh instanceof PaginationHandler) {
                LinkedHashMultimap<ItemStack, GuiListener<? extends SlotReport>> data = LinkedHashMultimap.create();
                // get data
                ((PaginationHandler) gh).pullData(gui.getPagination(), player, data);
                // slots per page
                int[] pageSlots = gui.getPagination().getSlots();
                // all pagination slots
                BattleGuiSlot[] ps = new BattleGuiSlot[data.size()];
                Set<ItemStack> keys = data.keySet();
                int i = 0; // data index
                int j = 0; // slot index on one page
                for(ItemStack item : keys){
                    int index = pageSlots[j++]; // get slot index
                    BattleGuiSlot s = slots[index];
                    if(s == null) {
                        slots[index] = (s = new BattleGuiSlot(index, new GuiSlot(null, new ArrayList<>(), true), Collections.unmodifiableCollection(data.get(item))));
                    } else {
                        s.getEvents().clear();
                        s.getEvents().addAll(data.get(item));
                    }
                    s.setCachedItem(item); // cache item
                    // put to temp pagination slots
                    ps[i++] = s;
                    if(j == pageSlots.length) j = 0; // reset if reached the maximum slot
                }
                BattleGui bg = new BattleGui(gui, pg, slots);
                bg.setPagination(new PaginationHelper<>(ps, pageSlots.length));
                return bg;
            }
        }
        return new BattleGui(gui, pg, slots);
    }

    public void setBottomInv(Player player, String name){
        PlayerGui gui = getPlayerGui(player);
        gui.setBottomGui(setupGui(player, gui, GUI.get(name)));
        renderBottomInv(player, gui);
    }

    public void renderBottomInv(Player player, PlayerGui apg){
        BattleGui bg = apg.getBottomGui();
        if(bg == null) return;
        ItemStack[] items = renderItems(player, bg);
        for(int i = 0; i < items.length; i++)
            player.getInventory().setItem(i, items[i]);
    }

    public void openTopInventory(Player player, String name){
        PlayerGui pg = getPlayerGui(player);
        BattleGui bg = setupGui(player, pg, GUI.get(name));
        pg.setTopGui(bg);
        Inventory inv;
        if(bg.getGui().getTitle() == null) inv = Bukkit.createInventory(null, bg.getGui().getSize());
        else {
            String title = PlaceholderUtils.localizeString(bg.getGui().getTitle(), plugin.getLocaleConf());
            title = PlaceholderUtils.formatPAPI(player, title);
            inv = Bukkit.createInventory(null, bg.getGui().getSize(), title);
        }
        pg.setTopInv(inv);
        renderTopInventory(player, pg);
        player.openInventory(inv);
        if(bg.getGui().getSound() != null)
            player.playSound(player.getLocation(), bg.getGui().getSound(), 2f, 1f);
    }

    public void renderTopInventory(Player player, PlayerGui apg){
        BattleGui gui = apg.getTopGui();
        if(gui == null || apg.getTopInv() == null) return;
        apg.getTopInv().setContents(renderItems(player, gui));
    }

    public void renderGui(Player player, BattleGui gui){
        if(gui.getPlayerGui().getTopGui() == gui)
            plugin.guiManager.renderTopInventory(player, gui.getPlayerGui());
        else if(gui.getPlayerGui().getBottomGui() == gui)
            plugin.guiManager.renderBottomInv(player, gui.getPlayerGui());
        else
            throw new UnsupportedOperationException();
    }

    public void destroyPlayerGui(Player player){
        PLAYER_GUI.remove(player);
    }

    private ItemStack[] renderItems(Player player, BattleGui bg){
        ItemStack[] items = new ItemStack[bg.getGui().getSize()];
        BattleGuiSlot[] bs = bg.getSlots();
        for(int i = 0; i < bs.length; i++){
            BattleGuiSlot x = bs[i];
            if(x != null && x.getSlot() != null && x.getSlot().getItemConf() != null)
                items[i] = formatStrings(ABIF.read(x.getSlot().getItemConf()), player).build();
        }
        if(bg.getPagination() != null && bg.getGui().getPagination() != null){
            BattleGuiSlot[] ps = bg.getPagination().collect(); // get all slots in current page
            int[] is = bg.getGui().getPagination().getSlots(); // get all slot indexes
            int len = Math.min(is.length, ps.length);
            for(int i = 0; i < len; i++)
                items[is[i]] = ps[i].getCachedItem();
        }
        return items;
    }

    private PreparedItem formatStrings(PreparedItem pi, Player player) {
        pi.name(PlaceholderUtils.formatPAPI(player, PlaceholderUtils.localizeString(pi.name(), plugin.getLocaleConf())));
        pi.lore(PlaceholderUtils.formatPAPI(player, PlaceholderUtils.localizeStrings(pi.lore(), plugin.getLocaleConf())));
        return pi;
    }
}
