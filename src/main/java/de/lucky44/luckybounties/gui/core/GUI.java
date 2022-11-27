package de.lucky44.luckybounties.gui.core;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public abstract class GUI {

    protected String name;
    protected Player user;
    protected Inventory inv;
    protected InventoryView v;

    protected void setGUIInventoryInstance(Inventory I){
        inv = I;
    }

    //region overridable methods
    public abstract void onOpen(Player user);
    public abstract void onClose();
    public abstract void onClick(int slot, ItemStack item);
    //endregion

    //region Styling
    public void setName(String name){
        this.name = name;
    }

    public void set(ItemStack item, int slot){
        if(inv == null)
            return;

        inv.setItem(slot, item);
    }
    //endregion

    //region public methods
    public void open(Player user){
        this.user = user;
        onOpen(user);
        v = user.openInventory(inv);
        GUIManager.instance.registerGUI(this, user);
    }


    public void close(){
        GUIManager.instance.close(user);
        user.getOpenInventory().close();
    }
    //endregion

}
