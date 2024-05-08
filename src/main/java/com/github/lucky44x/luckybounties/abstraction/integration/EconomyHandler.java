package com.github.lucky44x.luckybounties.abstraction.integration;

import org.bukkit.OfflinePlayer;

/**
 * @author Lucky44x
 * extendable EconomyHandler to make it easy to add multiple ways of handling economy
 */
public interface EconomyHandler {

    String format(double value);
    void add(OfflinePlayer target, double value);
    void withdraw(OfflinePlayer target, double value);
    double getBalance(OfflinePlayer target);
}
