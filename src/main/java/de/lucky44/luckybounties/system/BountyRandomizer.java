package de.lucky44.luckybounties.system;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class BountyRandomizer{

    public static void run() {
        Random r = new Random();
        float random = LuckyBounties.instance.minAmount + r.nextFloat() * (LuckyBounties.instance.maxAmount - LuckyBounties.instance.minAmount);

        Player set = null;

        //Hippity Hoppity your Code is now my property

        if(Bukkit.getOnlinePlayers().size() > 0){

            ArrayList<Player> allPlayers = new ArrayList<>();
            for(Player player : Bukkit.getOnlinePlayers()) {
                allPlayers.add(player);
            }
            int randInt = new Random().nextInt(allPlayers.size());
            set = allPlayers.get(randInt);

        }
        //---------------------------------------

        if(set != null){
            bounty mB = LuckyBounties.getEcoBounty(set.getUniqueId().toString());

            if(mB != null) {
                mB.moneyPayment += LuckyBounties.instance.eco_amount;
            }
            else {
                bounty b = new bounty(set.getUniqueId().toString(), random);
                LuckyBounties.bounties.add(b);
            }
        }

        long randTime = LuckyBounties.instance.maxDelay;
        if(LuckyBounties.instance.minDelay != -1){
            randTime = LuckyBounties.instance.minDelay + r.nextLong() * (LuckyBounties.instance.maxDelay - LuckyBounties.instance.minDelay);
        }

        Bukkit.getLogger().info("Bounty was set: " + random + " New randTime: " + (randTime / 20) + "s");

        final long time = randTime;

        Bukkit.getScheduler().runTask(LuckyBounties.instance, () ->{
            Bukkit.getScheduler().runTaskLaterAsynchronously(LuckyBounties.instance, BountyRandomizer::run, time);
        });
    }
}
