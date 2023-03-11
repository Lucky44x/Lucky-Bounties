package de.lucky44.luckybounties.integrations.vault;

import de.lucky44.luckybounties.files.DebugLog;
import de.lucky44.luckybounties.files.config.CONFIG;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration {

    public static VaultIntegration I;

    private Economy econ = null;

    public VaultIntegration(){
        checkEconomy();
    }

    private void errorNotRegistered(){
        DebugLog.error("[VAULT-INT] Could not find a vault-service-provider");
        Bukkit.getLogger().warning("Could not find a registered vault service provider");
    }

    private boolean checkEconomy(){
        if(econ != null)
            return true;

        DebugLog.info("[VAULT-INT] Trying to connect to Vault service-provider");
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null){
            errorNotRegistered();
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public String format(double num){
        if(checkEconomy())
            return econ.format(num);
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
