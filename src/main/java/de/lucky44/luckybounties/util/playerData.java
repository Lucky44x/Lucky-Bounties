package de.lucky44.luckybounties.util;

import de.lucky44.luckybounties.LuckyBounties;
import jdk.internal.joptsimple.util.KeyValuePair;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class playerData {

    public String playerName;
    public String playerUUID;
    public int worth = 0;
    public int collected = 0;
    public int set = 0;

    public long lastUpdateWor = 0;
    public long lastUpdateCol = 0;


    public playerData(String name, UUID id){
        playerName = name;
        playerUUID = id.toString();
        lastUpdateWor = Calendar.getInstance().getTimeInMillis();
        lastUpdateCol = Calendar.getInstance().getTimeInMillis();
    }

    public void onGetSetOn(){
        worth++;
        lastUpdateWor = Calendar.getInstance().getTimeInMillis();

        if(LuckyBounties.mostWorth == this)
            return;

        if(LuckyBounties.mostWorth == null)
            LuckyBounties.mostWorth = this;
        else if (LuckyBounties.mostWorth.worth < worth)
            LuckyBounties.mostWorth = this;
        else if (LuckyBounties.mostWorth.worth == worth && LuckyBounties.mostWorth.lastUpdateCol < lastUpdateCol)
            LuckyBounties.mostWorth = this;
    }

    public void onDeath(){
        worth = 0;

        if(LuckyBounties.mostWorth == this){

            playerData nMW = null;
            for(playerData d : LuckyBounties.players.values()){
                if(nMW == null)
                    nMW = this;
                else if (nMW.worth < d.worth)
                    nMW = this;
                else if (nMW.worth == d.worth && nMW.lastUpdateCol < d.lastUpdateCol)
                    nMW = this;
            }

            LuckyBounties.mostWorth = nMW;
        }
    }

    public void onCollect(){
        collected ++;
        lastUpdateCol = Calendar.getInstance().getTimeInMillis();

        if(LuckyBounties.mostCol == this)
            return;

        if(LuckyBounties.mostCol == null)
            LuckyBounties.mostCol = this;
        else if (LuckyBounties.mostCol.collected < collected)
            LuckyBounties.mostCol = this;
        else if (LuckyBounties.mostCol.collected == collected && LuckyBounties.mostCol.lastUpdateCol < lastUpdateCol)
            LuckyBounties.mostCol = this;
    }

    public void setBounty(){
        set ++;
    }
}
