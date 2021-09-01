package de.lucky44.luckybounties.util;

import org.bukkit.inventory.ItemStack;

public class bounty {
    public float moneyPayment;
    public int type = 0;
    public item payment = null;
    public String UUID = "NAN";

    public bounty(String UUID, ItemStack payment) {
        this.UUID = UUID;
        this.payment = new item(payment);
    }

    public bounty(String UUID, float payment) {
        this.UUID = UUID;
        this.moneyPayment = payment;
        this.type = 1;
    }

}
