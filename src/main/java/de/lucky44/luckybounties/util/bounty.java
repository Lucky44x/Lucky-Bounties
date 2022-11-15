package de.lucky44.luckybounties.util;

import org.bukkit.inventory.ItemStack;

public class bounty {
    public float moneyPayment = -1;
    public ItemStack payment;

    public bounty(float payment){
        this.moneyPayment = payment;
    }

    public bounty(ItemStack payment){
        this.moneyPayment = -1;
        this.payment = payment;
    }
}
