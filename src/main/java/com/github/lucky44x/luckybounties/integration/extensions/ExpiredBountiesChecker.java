package com.github.lucky44x.luckybounties.integration.extensions;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.integration.Integration;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;
import org.bukkit.Bukkit;

/**
 * @author Lucky44x
 * No idea what any of this does and I can't be bothered to think about it, or a better way to do this at that, so yeah...
 */
//ASYNC PAIN TODO: make this shit threadsafe
public class ExpiredBountiesChecker extends Integration {

    private int runnableID = -1;

    public ExpiredBountiesChecker(LuckyBounties instance) {
        super(instance);
    }

    @Override
    public void onEnable() throws IntegrationException {
        runnableID = Bukkit.getScheduler().runTaskTimerAsynchronously(
                instance,
                new ExpiredBountiesRunnable(),
                0,
                instance.configFile.toTickTime(
                        instance.configFile.getExpiredCheckPeriod()
                )
        ).getTaskId();
    }

    @Override
    public void onDisable() throws IntegrationException {
        Bukkit.getScheduler().cancelTask(runnableID);
    }

    private class ExpiredBountiesRunnable implements Runnable{

        @Override
        public void run() {
            if(!instance.configFile.isBountiesExpire())
                return;

            instance.getHandler().checkForExpiredBounties();
        }
    }
}
