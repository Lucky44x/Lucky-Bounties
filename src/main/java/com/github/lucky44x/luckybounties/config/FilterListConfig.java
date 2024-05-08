package com.github.lucky44x.luckybounties.config;

import com.github.lucky44x.luckyutil.config.ConfigFile;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lucky44x
 * ConfigFile for the FilterConfig
 */
public class FilterListConfig extends ConfigFile {
    private final String listName;

    public FilterListConfig(Plugin instance, String listName) {
        super(instance, "lists");
        this.listName = listName;
        reload();
    }

    public List<Material> readMaterials(){
        List<Material> materials = new ArrayList<>();

        if(config.get(listName) == null)
            return materials;

        for(String s : config.getStringList(listName)){
            materials.add(Material.getMaterial(s.toUpperCase()));
        }

        return materials;
    }

    public void saveMaterials(List<Material> materials) throws IOException {
        List<String> converted = new ArrayList<>();
        for(Material m : materials)
            converted.add(m.name());

        config.set(listName, converted);
        config.save(instance.getDataFolder()+ "/lists.yml");
    }

    @Override
    protected void reloadFile() {

    }
}
