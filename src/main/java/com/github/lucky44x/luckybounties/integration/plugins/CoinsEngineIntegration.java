package com.github.lucky44x.luckybounties.integration.plugins;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.integration.ConditionPluginIntegration;
import com.github.lucky44x.luckybounties.abstraction.integration.EconomyHandler;
import com.github.lucky44x.luckybounties.bounties.types.EcoBounty;
import com.github.lucky44x.luckyutil.config.LangConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

/**
 * @author Lucky44x
 * An (untested) integration for coinsengine funcitonallity
 * (I had to redesign the whole Economy system for  this shit)
 */
public class CoinsEngineIntegration extends ConditionPluginIntegration implements EconomyHandler {

    private Currency currency;
    private boolean isActive;

    public CoinsEngineIntegration(LuckyBounties instance) {super(instance, "CoinsEngine");}

    @Override
    public void onEnable(){
        instance.getLogger().info("Loading CoinsEngineCurrency with name: " + instance.configFile.getEconomyName());
        loadCurrency(instance.configFile.getEconomyName());
    }

    public void loadCurrency(String currencyName){
        this.currency = CoinsEngineAPI.getCurrency(currencyName);
        if(!checkCurrency())
            Bukkit.getLogger().severe("[Coins-Engine Integration] Error while loading Coins-Engine currency named \"" + instance.configFile.getEconomyName() + "\": Does not exist");
    }

    /**
     * @return true when there is a loaded currency, false when there is none
     */
    public boolean checkCurrency(){
        return currency != null;
    }

    //tmp value for LANG-purposes
    @LangConfig.LangData(langKey = "[VALUE]")
    private double tmpLangValue;

    /**
     * formats the given value to the currency-format
     * @param value the currency-value
     * @return the formatted value in string-form
     */
    @Override
    public String format(double value) {
        return currency.format(value);
    }

    /**
     * Provides a way to add money to the target's balance
     * @param target the target player
     * @param value the amount to be added
     */
    @Override
    public void add(OfflinePlayer target, double value) {
        if(!checkCurrency()){
            Bukkit.getLogger().severe("[Coins-Engine Integration] could not add " + value + " to " + target.getName() + ": Currency is null");
            return;
        }

        if(target.getPlayer() == null){
            Bukkit.getLogger().severe("[Coins-Engine Integration] could not add " + value + " to " + target.getName() + ": Player is offline");
            return;
        }

        CoinsEngineAPI.addBalance(target.getPlayer(), currency, value);
    }

    /**
     * Provides a way to withdraw money from the targets balance
     * @param target the target player
     * @param value the amount to be withdrawn
     */
    @Override
    public void withdraw(OfflinePlayer target, double value) {
        if(!checkCurrency()){
            Bukkit.getLogger().severe("[Coins-Engine Integration] could not withdraw " + value + " from " + target.getName() + ": Currency is null");
            return;
        }

        if(target.getPlayer() == null){
            Bukkit.getLogger().severe("[Coins-Engine Integration] could not withdraw " + value + " from " + target.getName() + ": Player is offline");
            return;
        }

        CoinsEngineAPI.removeBalance(target.getPlayer(), currency, value);
    }

    /**
     * @param target the target player
     * @return the target's balance
     */
    @Override
    public double getBalance(OfflinePlayer target) {
        if(!checkCurrency()){
            Bukkit.getLogger().severe("[Coins-Engine Integration] could not get balance of " + target.getName() + ": Currency is null");
            return 0;
        }

        if(target.getPlayer() == null){
            Bukkit.getLogger().severe("[Coins-Engine Integration] could not get balance of " + target.getName() + ": Player is offline");
            return 0;
        }

        return CoinsEngineAPI.getBalance(target.getPlayer(), currency);
    }

    @Override
    public boolean isAllowedToSet(Bounty b, Player target, Player setter){
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
