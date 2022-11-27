package de.lucky44.api.luckybounties.util;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BountyData {
    private final ItemStack payment;
    private final UUID target;
    private final bounty originalData;

    public BountyData(UUID target, bounty b){
        this.payment = LuckyBounties.I.cleanBountyItem(b);
        this.target = target;
        this.originalData = b;
    }

    public UUID target(){
        return target;
    }

    public ItemStack payment(){
        return payment;
    }

     public bounty getOriginalBounty(){
        return originalData;
    }
}
