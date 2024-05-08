package com.github.lucky44x.luckybounties.abstraction.bounties;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckyutil.config.LangConfig;
import lombok.Getter;
import lombok.NonNull;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author Lucky44x
 * Simple Bounty Abstraction
 */
public abstract class Bounty {
    @Getter
    protected UUID setterID;
    @Getter
    protected final UUID targetID;

    protected OfflinePlayer setter;
    protected final OfflinePlayer target;

    @Getter
    protected final long setTime;

    protected final LuckyBounties instance;

    /**
     * IGNORE, used by auto-lang-tag-replacement
     */
    @LangConfig.LangData(langKey = "[SETTER]")
    public String langGetSetterName(){
        if(setter != null)
            return setter.getName();

        return instance.langFile.getText("system-bounty-name");
    }

    /**
     * IGNORE, used by auto-lang-tag-replacement
     */
    @LangConfig.LangData(langKey = "[TARGET]")
    public String langGetTargetName(){
        return  target.getName();
    }

    /**
     * Creates a bounty with the current time as set-time
     * @param target the target of the bounty
     * @param setter the setter of the bounty
     * @param instance the main-plugin instance
     */
    public Bounty(UUID target, UUID setter, LuckyBounties instance){
        this(target, setter, System.currentTimeMillis(), instance);
    }

    /**
     * Creates a bounty with the given time as set-time
     * @param target the target of the bounty
     * @param setter the setter of the bounty
     * @param setTime the time when the bounty was set
     * @param instance the main-plugin instance
     */
    public Bounty(UUID target, UUID setter, long setTime, LuckyBounties instance){
        this.instance = instance;
        this.setterID = setter;
        this.targetID = target;

        if(setterID == null)
            setterID = instance.getServerUUID();

        if(setterID.equals(instance.getServerUUID())){
            this.setter = null;
        }
        else {
            this.setter = Bukkit.getOfflinePlayer(setterID);
        }

        this.target = Bukkit.getOfflinePlayer(targetID);

        this.setTime = setTime;
    }

    /**
     * @return teh reward as an ItemStack
     */
    public abstract ItemStack toItem();

    /**
     * IGNORE used by automatic tag replacement via LANG-Config (LuckyUtil)
     */
    @LangConfig.LangData(langKey = "[DATE]")
    public String langGetSetDate(){
        Date dateTime = new Date(setTime);
        DateFormat df = new SimpleDateFormat(instance.configFile.getTimeFormat());

        return df.format(dateTime);
    }

    /**
     * Returns the bounty to a player (depending on implementation)
     */
    public abstract void returnBounty();

    /**
     * Makes the killer receive the bounty-reward
     * @param killer the killer
     */
    public abstract void receiveBounty(Player killer);

    /**
     * gives the bounty-reward to the given player
     * @param user the player who gets the reward
     */
    public abstract void giveReward(Player user);

    /**
     * @return the setter's UUID in string format
     */
    public String getSetterStringID() {
        if(setterID == null)
            return instance.getServerUUID().toString();

        return setterID.toString();
    }
}
