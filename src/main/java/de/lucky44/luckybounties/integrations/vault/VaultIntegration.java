package de.lucky44.luckybounties.integrations.vault;

import de.lucky44.luckybounties.files.config.CONFIG;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration {

    public static VaultIntegration I;

    private Economy econ = null;

    public VaultIntegration(){
        checkEconomy();
    }

    private void errorNotRegistered(){
        Bukkit.getLogger().info(ChatColor.RED + "Could not find a registered vault service provider");
    }

    private boolean checkEconomy(){
        if(econ != null)
            return true;

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null){
            errorNotRegistered();
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public String getSymbol(){
        if(checkEconomy())
            return CONFIG.getString("currency-symbol");
        return "";
    }

    public double getBalance(Player p){
        if(checkEconomy())
            return econ.getBalance(p);
        return 0;
    }

    public void withdraw(Player p, double d){
        if(checkEconomy())
            econ.withdrawPlayer(p, d);
    }

    public void add(Player p, double d){
        if(checkEconomy())
            econ.depositPlayer(p, d);
    }
}
