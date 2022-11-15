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

    private Economy econ;

    public VaultIntegration(){
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null){
            errorNotRegistered();
            return;
        }

        econ = rsp.getProvider();
    }

    private void errorNotRegistered(){
        Bukkit.getLogger().info(ChatColor.RED + "Could not find a registered vault service provider");
    }

    public String getSymbol(){
        return CONFIG.getString("currency-symbol");
    }

    public double getBalance(Player p){
        return econ.getBalance(p);
    }

    public void withdraw(Player p, double d){
        econ.withdrawPlayer(p, d);
    }

    public void add(Player p, double d){
        econ.depositPlayer(p, d);
    }
}
