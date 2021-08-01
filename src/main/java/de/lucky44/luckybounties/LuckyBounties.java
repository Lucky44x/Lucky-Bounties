package de.lucky44.luckybounties;

import de.lucky44.luckybounties.system.commandManager;
import de.lucky44.luckybounties.system.eventManager;
import de.lucky44.luckybounties.util.bounty;
import de.lucky44.luckybounties.util.fileManager;
import de.lucky44.luckybounties.util.permissionType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LuckyBounties extends JavaPlugin {

    //Other
    public static LuckyBounties instance;

    public FileConfiguration config;

    public permissionType Clear = permissionType.OP;
    public permissionType remove = permissionType.OP;

    public String messageSing = "";
    public String messageMulti = "";

    //Bounties
    public static ArrayList<bounty> bounties = new ArrayList<>();

    //Items for GUI panels
    public static ItemStack gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    public static ItemStack green = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
    public static ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    public static ItemStack set = new ItemStack(Material.AMETHYST_SHARD);
    public static ItemStack clear = new ItemStack(Material.FEATHER);

    @Override
    public void onEnable(){
        getLogger().info(ChatColor.GREEN + "Enabling plugin");

        if(instance == null)
            instance = this;

        this.saveDefaultConfig();
        config = this.getConfig();

        messageSing = config.getString("KillMessageSing");
        messageMulti = config.getString("KillMessageMult");

        String s = config.getString("bountyRemove");
        remove = convertToPermission(s);
        s = config.getString("bountyClear");
        Clear = convertToPermission(s);

        try {
            fileManager.LoadBounties();
        } catch (IOException e) {
            getLogger().info(ChatColor.RED + "Loading bounties failed");
        }

        getLogger().info(ChatColor.GREEN + "Loaded bounties");

        //Loading all the system stuff
        commandManager cM = new commandManager();
        Objects.requireNonNull(getCommand("bounties")).setExecutor(cM);

        getServer().getPluginManager().registerEvents(new eventManager(), this);

        //Setting ItemStacks for the GUI
        ItemMeta meta = gray.getItemMeta();
        assert meta != null;
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName("-");
        gray.setItemMeta(meta);

        meta = green.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "CONFIRM");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        green.setItemMeta(meta);

        meta = red.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.RED + ChatColor.BOLD.toString() + "CANCEL");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        red.setItemMeta(meta);

        meta = set.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "SET BOUNTY");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        set.setItemMeta(meta);

        meta = clear.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.YELLOW + "Clear bounties");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        clear.setItemMeta(meta);
    }

    @Override
    public void onDisable(){
        getLogger().info(ChatColor.GREEN + "Disabling plugin");

        try {
            fileManager.SaveBounties(bounties.toArray(new bounty[bounties.size()]));
        } catch (IOException e) {
            getLogger().info(ChatColor.RED + "Saving bounties failed");
        }

        getLogger().info(ChatColor.GREEN + "Bounties saved");
    }

    public static void clearBounties(String UUID){
        List<bounty> bs = new ArrayList<>(bounties);

        for(bounty b : bs){
            if(b.UUID.equals(UUID)){
                bounties.remove(b);
            }
        }
    }

    public static List<bounty> getBounties(String UUID){
        List<bounty> bs = new ArrayList<>();

        for(bounty b : bounties){
            if(b.UUID.equals(UUID)){
                bs.add(b);
            }
        }

        return bs;
    }

    permissionType convertToPermission(String s){

        permissionType ret = permissionType.OP;

        switch(s.toUpperCase()){
            case("BOTH"):
                ret = permissionType.BOTH;
                break;
            case("LB"):
                ret = permissionType.LB;
                break;
            default:
                ret = permissionType.OP;
        }

        return ret;
    }
}
