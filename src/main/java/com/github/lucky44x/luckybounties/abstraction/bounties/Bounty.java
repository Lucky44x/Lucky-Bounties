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

public abstract class Bounty {
    @Getter
    protected UUID setterID;
    @Getter
    protected final UUID targetID;

    protected OfflinePlayer setter;
    protected final OfflinePlayer target;

    @Getter@NonNull
    protected final long setTime;

    protected final LuckyBounties instance;

    @LangConfig.LangData(langKey = "[SETTER]")
    public String langGetSetterName(){
        if(setter != null)
            return setter.getName();

        return instance.langFile.getText("system-bounty-name");
    }

    @LangConfig.LangData(langKey = "[TARGET]")
    public String langGetTargetName(){
        return  target.getName();
    }

    public Bounty(UUID target, UUID setter, LuckyBounties instance){
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

        setTime = System.currentTimeMillis();
    }

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

    public abstract ItemStack toItem();

    @LangConfig.LangData(langKey = "[DATE]")
    public String langGetSetDate(){
        Date dateTime = new Date(setTime);
        DateFormat df = new SimpleDateFormat(instance.configFile.getTimeFormat());

        return df.format(dateTime);
    }

    public abstract void returnBounty();

    public abstract void receiveBounty(Player killer);

    public abstract void giveReward(Player user);

    public String getSetterStringID() {
        if(setterID == null)
            return instance.getServerUUID().toString();

        return setterID.toString();
    }
}
