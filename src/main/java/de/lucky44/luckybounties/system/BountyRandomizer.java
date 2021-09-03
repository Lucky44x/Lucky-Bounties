package de.lucky44.luckybounties.system;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class BountyRandomizer{

    public static void run() {
        Random r = new Random();
        float random_num = LuckyBounties.instance.minAmount + r.nextFloat() * (LuckyBounties.instance.maxAmount - LuckyBounties.instance.minAmount);

        String tmp = Float.toString(random_num);
        String[] tmps = tmp.split("\\.");

        if(tmps.length > 1){
            tmps[1] = tmps[1].substring(0,Math.min(tmps[1].length(),2));
            tmps[0] = tmps[0] + "." + tmps[1];
        }

        random_num = Float.parseFloat(tmps[0]);

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

        final Player tmpSet = set;
        final float random = random_num;

        Bukkit.getScheduler().runTask(LuckyBounties.instance, () -> {
            if(tmpSet != null){
                bounty mB = LuckyBounties.getEcoBounty(tmpSet.getUniqueId().toString());

                if(mB != null) {
                    mB.moneyPayment += LuckyBounties.instance.eco_amount;
                }
                else {
                    bounty b = new bounty(tmpSet.getUniqueId().toString(), random);
                    LuckyBounties.bounties.add(b);
                }
            }
        });

        long randTime = LuckyBounties.instance.maxDelay;
        if(LuckyBounties.instance.minDelay != -1){
            randTime = LuckyBounties.instance.minDelay + r.nextLong() * (LuckyBounties.instance.maxDelay - LuckyBounties.instance.minDelay);
        }

        String name = "NAN";

        if(set != null) {

            name = set.getDisplayName();

            if(LuckyBounties.instance.useMessages){
                String mS = LuckyBounties.instance.setConsoleMessage.replace("{target}",set.getDisplayName()).replace("{amount}",random_num + LuckyBounties.instance.economy_name);
                Bukkit.broadcastMessage(mS);
            }
        }

        Bukkit.getLogger().info("Bounty was set on " + name + "'s head" + ": " + random_num + LuckyBounties.instance.economy_name + " New randTime: " + (randTime / 20) + "s");

        final long time = randTime;

        Bukkit.getScheduler().runTask(LuckyBounties.instance, () ->{
            Bukkit.getScheduler().runTaskLaterAsynchronously(LuckyBounties.instance, BountyRandomizer::run, time);
        });
    }
}
