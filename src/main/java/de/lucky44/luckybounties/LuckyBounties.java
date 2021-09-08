package de.lucky44.luckybounties;

import de.lucky44.luckybounties.bstats.Metrics;
import de.lucky44.luckybounties.system.BountyRandomizer;
import de.lucky44.luckybounties.system.commandManager;
import de.lucky44.luckybounties.system.eventManager;
import de.lucky44.luckybounties.util.bounty;
import de.lucky44.luckybounties.util.fileManager;
import de.lucky44.luckybounties.util.permissionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LuckyBounties extends JavaPlugin {

    //TODO: Offload the whole bounty set procedure into a function for ease of access

    //Ey, you, yeah you, I see you're in my code. I'm gonna let you in on a little secret:
    //You know how you can see why this code is made by me? Exactly ... THE FUCKING COMMENTS

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


    //New custom Stuf IDK HELP MEEMEMEMMEMEMEEEEE
    public ConsoleCommandSender console;
    public static ItemStack moneyz = new ItemStack(Material.GOLD_NUGGET);
    public boolean useItems = true;

    //Custom messages
    public String setPlayerMessage = "";
    public String setConsoleMessage = "";
    public boolean useMessages = false;

    //Economy
    public boolean economy = false;
    public boolean allowDebt = false;
    public float eco_amount = 100;
    public String economy_name = "$";
    public String eco_set = "";
    public String eco_get = "";

    //Random stuff
    public boolean rand_bounty = false;
    public long maxDelay, minDelay;
    public float maxAmount, minAmount;

    //Event bounties
    public String killBounty = "false";


    //OH MY FUCKING GOD, WHY TF DID I CODE THIS LIKE IT IS. THIS IS A FUCKING NIGHTMARE

    @Override
    public void onEnable(){

        //Enable bStats
        int pluginId = 12684;
        Metrics metrics = new Metrics(this, pluginId);

        getLogger().info(ChatColor.GREEN + "Enabling plugin");
        console = Bukkit.getConsoleSender();

        if(instance == null)
            instance = this;

        loadConfig(null);

        try {
            fileManager.LoadBounties();
        } catch (IOException e) {
            getLogger().info(ChatColor.RED + "Loading bounties failed");
        }

        getLogger().info(ChatColor.GREEN + "Loaded bounties");

        //Loading all the system stuff
        commandManager cM = new commandManager();
        Objects.requireNonNull(getCommand("bounties")).setExecutor(cM);
        getCommand("bounties").setTabCompleter(cM);

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

        if(rand_bounty)
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, BountyRandomizer::run, maxDelay);
    }

    public void loadConfig(Player p){
        this.saveDefaultConfig();

        config = this.getConfig();

        messageSing = config.getString("KillMessageSing");
        messageMulti = config.getString("KillMessageMult");

        eco_set = config.getString("eco-command-on-set");
        eco_get = config.getString("eco-command-on-receive");

        String s = config.getString("bountyRemove");
        remove = convertToPermission(s);
        s = config.getString("bountyClear");
        Clear = convertToPermission(s);

        //WOHOOO MORE LOADING SHIT
        moneyz = new ItemStack(Material.getMaterial(config.getString("economy-item").toUpperCase()));
        useItems = config.getBoolean("use-items");
        economy = config.getBoolean("use-economy");
        rand_bounty = economy && config.getBoolean("use-random");
        useMessages = config.getBoolean("use-setMessages");

        //Get max and min delay for random function
        String constraints = config.getString("interval");

        String[] parts = constraints.split("-");
        long Ticks1 = 0, Ticks2 = parts.length > 1 ? 0 : -1;
        String[] times = parts[0].split(":");
        for(String time : times){
            String[] units = time.split("_");
            long base = Long.parseLong(units[0]);
            long multiplier = 0;
            switch(units[1]){
                case("h"):
                    multiplier = 72000;
                    break;
                case("m"):
                    multiplier = 1200;
                    break;
                case("s"):
                    multiplier = 20;
                    break;
            }

            Ticks1 += base * multiplier;
        }

        if(parts.length > 1) {

            times = parts[1].split(":");
            for(String time : times){
                String[] units = time.split("_");
                long base = Long.parseLong(units[0]);
                long multiplier = 0;
                switch(units[1]){
                    case("h"):
                        multiplier = 72000;
                        break;
                    case("m"):
                        multiplier = 1200;
                        break;
                    case("s"):
                        multiplier = 20;
                        break;
                }

                Ticks2 += base * multiplier;
            }

        }

        maxDelay = Math.max(Ticks1,Ticks2);
        minDelay = Math.min(Ticks1,Ticks2);

        //Get max and min constraints for eco amount
        String amount = config.getString("amount");

        parts = amount.split("-");

        float f1 = Float.parseFloat(parts[0]);
        float f2 = -1;

        if(parts.length > 1){
            f2 = Float.parseFloat(parts[1]);
        }

        maxAmount = Math.max(f1,f2);
        minAmount = Math.min(f1,f2);

        if(f2 == -1){
            minAmount = maxAmount;
        }

        allowDebt = config.getBoolean("allow-debt");

        setPlayerMessage = config.getString("BountySetMessage-player");
        setConsoleMessage = config.getString("BountySetMessage-console");

        killBounty = config.getString("kill-bounty");
        killBounty = killBounty == null || killBounty == "" ? "false" : killBounty;

        getLogger().info(ChatColor.GREEN + "config loaded!");

        if(p != null){
            p.sendMessage(ChatColor.GREEN + "config reloaded!");
        }
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

    public static void doShit(Player p, float amount, int type){
        String command = type == 0 ? instance.eco_set : instance.eco_get;

        String playerName = "";

        playerName = p.getName();

        command = command.replace("{player}",playerName);
        command = command.replace("{amount}",Float.toString(amount));

        Bukkit.dispatchCommand(LuckyBounties.instance.console, command);
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

    public static bounty getEcoBounty(String UUID) {
        for(bounty b : bounties){
            if(b.UUID.equals(UUID) && b.type == 1){
                return b;
            }
        }

        return null;
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
