package de.lucky44.luckybounties.gui.core;

import de.lucky44.luckybounties.files.DebugLog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class GUIManager implements Listener {

    public static GUIManager instance;

    private final Map<Player, GUI> guis = new HashMap<>();

    public GUIManager(Plugin pluginInstance){
        DebugLog.info("[GUIMANAGER] Initializing GUIManager Instance (Singleton Structure) from plugin: LuckyBounties");

        if(instance != null)
            return;

        instance = this;

        Bukkit.getServer().getPluginManager().registerEvents(this, pluginInstance);
        DebugLog.info("[GUIMANAGER] registered eventSystem for GUIManager");
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Player p = (Player)e.getPlayer();
        InventoryView v = e.getView();

        if(v.getType() == InventoryType.ANVIL)
            return;

        if(guis.containsKey(p)){
            if(guis.get(p).v == v){
                close(p);
            }
        }
    }

    public void registerGUI(GUI toRegister, Player p){

        /*
            if(guis.containsKey(p)){
            close(p);
            }
            */

        guis.put(p, toRegister);
        DebugLog.info("[GUIMANAGER] registered GUI for " + p.getName());
    }

    public void close(Player p){

        DebugLog.info("[GUIMANAGER] Trying to close GUI for " + p.getName());
        if(!guis.containsKey(p)){
            DebugLog.error("[GUIMANAGER] Trying to close GUI for " + p.getName() + " failed: No GUI is registered for this player");
            return;
        }

        guis.get(p).onClose();
        guis.remove(p);
        DebugLog.info("[GUIMANAGER] de-registered GUI for " + p.getName());
    }

    @EventHandler
    public void interactHandler(InventoryClickEvent e){
        Player user = (Player)e.getWhoClicked();
        GUI toSend = guis.get(user);
        if(toSend == null){
            DebugLog.warn("[GUIMANAGER] No GUI registered for player " + e.getWhoClicked().getName());
            return;
        }

        if(e.getClickedInventory() == null){
            DebugLog.warn("[GUIMANAGER] Clicked Inventory does not exist");
            return;
        }

        if(toSend.inv.getType() != e.getClickedInventory().getType()){
            DebugLog.warn("[GUIMANAGER] Clicked Inventory (" + e.getClickedInventory().getType() + ") is not registered as GUI (" + toSend.inv.getType() + ")");
            return;
        }

        int slot = e.getSlot();
        ItemStack item = null;
        if(slot > 0 && slot < e.getClickedInventory().getSize()){
            item = e.getClickedInventory().getItem(slot);
        }

        toSend.onClick(slot, item);

        DebugLog.info("[GUIMANAGER] Click event in " + user.getName() + "'s  inventory: " + e.getClickedInventory().getType().toString() + " : " + slot);

        if(!(toSend instanceof ChestGUI toSendChestGUI)){
            e.setResult(Event.Result.DENY);
            e.setCancelled(true);
            return;
        }

        if(!toSendChestGUI.moveSlots.contains(slot)){
            e.setResult(Event.Result.DENY);
            e.setCancelled(true);
        }
    }
}