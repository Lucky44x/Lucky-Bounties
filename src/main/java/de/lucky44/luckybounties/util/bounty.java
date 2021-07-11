package de.lucky44.luckybounties.util;

import org.bukkit.inventory.ItemStack;

public class bounty {
    public item payment = null;
    public String UUID = "NAN";

    public bounty(String UUID, ItemStack payment){
        this.UUID = UUID;
        this.payment = new item(payment);
    }
}
