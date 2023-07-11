package com.github.lucky44x.luckybounties.abstraction.integration.extension;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.integration.ConditionIntegration;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;
import com.github.lucky44x.luckybounties.config.FilterListConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO: Implement proper itemChecking: Check NBT-Tags / Something else apart from material
public abstract class FilterExtension extends ConditionIntegration {

    protected final FilterListConfig config;
    protected final List<Material> filter = new ArrayList<>();

    public FilterExtension(LuckyBounties instance, String listName){
        super(instance);
        config = new FilterListConfig(instance, listName);
        config.saveDefault();
    }

    @Override
    public void onEnable() throws IntegrationException {
        super.onEnable();
        reload();
    }

    @Override
    public void onDisable() throws IntegrationException {
        super.onDisable();
        save();
    }

    public final void reload(){
        config.reload();
        setFilter(config.readMaterials());
    }

    public final void save(){
        try {
            config.saveMaterials(filter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void addToFilter(ItemStack item){
        addToFilter(item.getType());
    }

    public final void addToFilter(Material material){
        filter.add(material);
    }

    public final void removeFromFilter(ItemStack item){
        removeFromFilter(item.getType());
    }

    public final void removeFromFilter(Material material){
        filter.remove(material);
    }

    public final void clearFilter(){
        filter.clear();
    }

    protected boolean isInFilter(ItemStack itemStack){
        return isInFilter(itemStack.getType());
    }

    protected boolean isInFilter(Material material){
        return filter.contains(material);
    }
    public final void setFilter(List<Material> materials){
        filter.clear();
        filter.addAll(materials);
    }
}
