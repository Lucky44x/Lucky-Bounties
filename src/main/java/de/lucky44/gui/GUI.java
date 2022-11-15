package de.lucky44.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GUI implements Listener {

    //Internal vars
    protected Player user;
    protected Inventory inv;

    protected InventoryView v;
    protected List<Integer> moveSlots = new ArrayList<>();

    //Styling Vars
    private String name;
    protected int size;

    //region overridable methods
    public abstract void onOpen(Player user);
    public abstract void onClose();
    public abstract void onClick(int slot, ItemStack item);
    //endregion

    //region styling methods

    public void setName(String name){
        this.name = name;
    }

    public void setSize(int size){
        this.size = size;
    }

    public void construct(){
        moveSlots = new ArrayList<>();
        inv = Bukkit.createInventory(null, size, name);
    }

    public void construct(InventoryType typeOfInventory){
        moveSlots = new ArrayList<>();
        inv = Bukkit.createInventory(null, typeOfInventory, name);
    }

    public void fill(ItemStack backgroundItem){
        if(inv == null)
            return;

        ItemStack[] fill = new ItemStack[size];
        Arrays.fill(fill, backgroundItem);
        inv.setContents(fill);
    }

    public void movable(int slot){
        if(moveSlots.contains(slot))
            moveSlots.remove(slot);
        else
            moveSlots.add(slot);
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