package de.lucky44.luckybounties.files;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FilterList {
    private List<Material> filtered = new ArrayList<>();

    public FilterList(){

    }

    public void setFilters(List<Material> materials){
        filtered.clear();
        filtered.addAll(materials);
    }

    public boolean isFiltered(ItemStack I){
        return isMaterialFiltered(I.getType());
    }

    protected boolean isMaterialFiltered(Material m){
        return filtered.contains(m);
    }

    protected boolean isItemFiltered(ItemStack I){
        //TODO: Implement custom Item filtering
        return true;
    }
}
