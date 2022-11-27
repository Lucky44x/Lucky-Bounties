package de.lucky44.luckybounties.gui.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class GUIManager implements Listener {

    public static GUIManager instance;

    private final Map<Player, GUI> guis = new HashMap<>();

    public GUIManager(Plugin pluginInstance){

        if(instance != null)
            return;

        instance = this;

        Bukkit.getServer().getPluginManager().registerEvents(this, pluginInstance);
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
        //Bukkit.getLogger().info("[LuckyGUI] registered GUI");
    }

    public void close(Player p){

        if(!guis.containsKey(p))
            return;

        guis.get(p).onClose();
        guis.remove(p);
        //Bukkit.getLogger().info("[LuckyGUI] de-registered GUI");
    }

    @EventHandler
    public void interactHandler(InventoryClickEvent e){
        Player user = (Player)e.getWhoClicked();
        GUI toSend = guis.get(user);
        if(toSend == null){
            //pluginInstance.getLogger().info("No GUI registered for player " + e.getWhoClicked().getName());
            return;
        }

        if(e.getClickedInventory() == null){
            //pluginInstance.getLogger().info("Clicked Inventory does not exist");
            return;
        }

        if(toSend.inv.getType() != e.getClickedInventory().getType()){
            //pluginInstance.getLogger().info("Clicked Inventory (" + e.getClickedInventory().getType() + ") is not registered as GUI (" + toSend.inv.getType() + ")");
            return;
        }

        int slot = e.getSlot();
        ItemStack item = null;
        if(slot > 0 && slot < e.getClickedInventory().getSize()){
            item = e.getClickedInventory().getItem(slot);
        }

        toSend.onClick(slot, item);

        //pluginInstance.getLogger().info(e.getClickedInventory().getType().toString() + " : " + slot);

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