package com.github.lucky44x.luckybounties.integration.plugins;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.integration.ConditionPluginIntegration;
import com.github.lucky44x.luckybounties.abstraction.integration.EconomyHandler;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;
import com.github.lucky44x.luckybounties.bounties.types.EcoBounty;
import com.github.lucky44x.luckyutil.config.LangConfig;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @author Lucky44x
 * EconomyHandler for Vault (for specific info on functions refer to the coinsengine handler, since I'm too lazy to rewrite all this stuff)
 */
public class VaultPluginIntegration extends ConditionPluginIntegration implements EconomyHandler {
    private Economy economy;
    public VaultPluginIntegration(LuckyBounties instance) {
        super(instance, "Vault");
    }

    @Override
    public void onEnable() throws IntegrationException {
        super.onEnable();

        checkEconomy();
    }

    //region integration
    private boolean checkEconomy(){
        if(instance.getServer().getPluginManager().getPlugin("Vault") == null){
            instance.getLogger().severe("Could not hook with Vault --> Vault is not installed");
            return false;
        }

        if(economy != null)
            return true;

        instance.getLogger().info("Trying to connect to Vault-Service-Provider");
        RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(Economy.class);

        if(rsp == null){
            instance.getLogger().severe("Could not find a registered Service-Provider for Economy");
            return false;
        }

        economy = rsp.getProvider();
        return true;
    }

    public String format(double num){
        if(checkEconomy())
            return economy.format(num);
        return String.valueOf(num);
    }

    public double getBalance(OfflinePlayer target){
        if(checkEconomy())
            return economy.getBalance(target);
        return 0;
    }

    public void add(OfflinePlayer target, double v){
        if(checkEconomy())
            economy.depositPlayer(target, v);
    }

    public void withdraw(OfflinePlayer target, double v){
        if(checkEconomy())
            economy.withdrawPlayer(target, v);
    }
    //endregion

    @Override
    public boolean isAllowedToSet(Bounty b, Player target, Player setter) {
        if(!(b instanceof EcoBounty))
            return true;

        EcoBounty bounty = (EcoBounty) b;

        if(bounty.getReward() > getBalance(setter)){
            setter.sendMessage(
                    instance.langFile.getText("eco-cannot-afford", bounty)
            );
            return false;
        }

        if(bounty.getReward() > instance.configFile.getMaximumEcoBounty() && instance.configFile.getMaximumEcoBounty() > -1){
            setter.sendMessage(
                    instance.langFile.getText("eco-too-much", this)
            );
            return false;
        }
        else if(bounty.getReward() < instance.configFile.getMinimumEcoBounty() && instance.configFile.getMinimumEcoBounty() > -1){
            setter.sendMessage(
                    instance.langFile.getText("eco-too-little", this)
            );
            return false;
        }

        return true;
    }

    @LangConfig.LangData(langKey = "[MAXBOUNTY]")
    private String getMaxBounty(){
        return format(instance.configFile.getMaximumEcoBounty());
    }

    @LangConfig.LangData(langKey = "[MINBOUNTY]")
    private String getMinBounty(){
        return format(instance.configFile.getMinimumEcoBounty());
    }
}
