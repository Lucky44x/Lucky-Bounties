package de.lucky44.luckybounties.deprecated;

import org.bukkit.inventory.ItemStack;

public class bountyOld {
    public float moneyPayment;
    public int type = 0;
    public itemOld payment = null;

    public ItemStack item;

    public String UUID = "NAN";

    public bountyOld(String UUID, ItemStack payment) {
        this.UUID = UUID;
        this.payment = new itemOld(payment);
    }

    public bountyOld(String UUID, float payment) {
        this.UUID = UUID;
        this.moneyPayment = payment;
        this.type = 1;
    }

}
