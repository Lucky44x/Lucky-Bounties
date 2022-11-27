package de.lucky44.luckybounties.gui.core;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ChestGUI extends GUI {

    //Internal vars
    protected List<Integer> moveSlots = new ArrayList<>();

    //Styling Vars
    protected int size;

    //region styling methods
    public void setSize(int size){
        this.size = size;
    }

    public void construct(){
        moveSlots = new ArrayList<>();
        setGUIInventoryInstance(Bukkit.createInventory(null, size, name));
    }

    public void construct(InventoryType typeOfInventory){
        moveSlots = new ArrayList<>();
        setGUIInventoryInstance(Bukkit.createInventory(null, typeOfInventory, name));
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
    //endregion
}