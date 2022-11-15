package de.lucky44.luckybounties.util;

import de.lucky44.luckybounties.LuckyBounties;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class playerData {

    public String playerName;
    public String playerUUID;
    public int worth = 0;
    public int collected = 0;
    public int set = 0;
    public double ecoWorth = 0;

    public long lastUpdateWor = 0;
    public long lastUpdateCol = 0;


    public playerData(String name, UUID id){
        playerName = name;
        playerUUID = id.toString();
        lastUpdateWor = Calendar.getInstance().getTimeInMillis();
        lastUpdateCol = Calendar.getInstance().getTimeInMillis();

        //List<bounty> bounties = LuckyBounties.I.fetchBounties(id);
        //worth = bounties.size();

        if(LuckyBounties.I.getEcoBounty(id) != null)
            ecoWorth = LuckyBounties.I.getEcoBounty(id).moneyPayment;
    }

    public void onGetSetOn(){
        worth++;
        lastUpdateWor = Calendar.getInstance().getTimeInMillis();

        if(LuckyBounties.mostWorth == this)
            return;

        LuckyBounties.I.getHighestBountyCount();
    }

    public void onDeath(){
        worth = 0;
        ecoWorth = 0;

        if(LuckyBounties.mostWorth == this){

            LuckyBounties.I.getHighestBountyCount();
        }
    }

    public void onCollect(){
        collected ++;
        lastUpdateCol = Calendar.getInstance().getTimeInMillis();

        if(LuckyBounties.mostCollected == this)
            return;

        LuckyBounties.I.getMostCollected();
    }

    public void setBounty(){
        set ++;
    }
}
