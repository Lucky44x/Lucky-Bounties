package de.lucky44.luckybounties.gui.guis;

import de.lucky44.api.luckybounties.events.EcoBountySetEvent;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.DebugLog;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.timers.CooldownManager;
import de.lucky44.luckybounties.util.bounty;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class GUI_EcoSetBounty {

    private Player user;
    private final Player target;


    public GUI_EcoSetBounty(Player target){
        this.target = target;
    }

    public void open(Player user){
        this.user = user;
        ItemStack item = GUIItems.EcoSetAnvilItem();

        DebugLog.info("[ANVIL-GUI] Opening Anvil-GUI of type GUI_EcoSetBounty for " + user.getName());

        new AnvilGUI.Builder()
                .onClose(player -> {
                    //GUI_BountiesList BountiesGUI = new GUI_BountiesList(target, 0);
                    //BountiesGUI.open(user);
                })
                .onComplete((completion) -> {

                    if(target == null || Bukkit.getServer().getPlayer(target.getUniqueId()) == null){
                        return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> {
                            DebugLog.info("[ANVIL-GUI] => ERROR Forcing Close Anvil-GUI of type GUI_EcoSetBounty for " + user.getName() + " because target is no longer online");
                        }));
                    }

                    String text = completion.getText();
                    if(text.matches("[-+]?[0-9]*\\.?[0-9]+")){
                        float amount = Float.parseFloat(text);

                        float minimum = CONFIG.getFloat("minimum-amount");
                        if(amount < minimum){
                            return List.of(AnvilGUI.ResponseAction.replaceInputText(LANG.getText("bounty-too-low").replace("[MINIMUM]", LuckyBounties.I.Vault.format(minimum))));
                        }

                        EcoBountySetEvent event = new EcoBountySetEvent(user, target, amount);
                        LuckyBounties.I.callEvent(event);
                        if(event.isCancelled())
                            return List.of(AnvilGUI.ResponseAction.close());

                        if(amount <= 0){
                            return List.of(AnvilGUI.ResponseAction.replaceInputText(LANG.getText("bounty-below-zero")));
                        }

                        if(LuckyBounties.I.Vault.getBalance(user) < amount){
                            return List.of(AnvilGUI.ResponseAction.replaceInputText(LANG.getText("cannot-afford")
                                    .replace("[AMOUNT]", LuckyBounties.I.Vault.format(amount))));
                        }

                        bounty toSet = new bounty(amount);
                        LuckyBounties.I.Vault.withdraw(user, amount);
                        LuckyBounties.I.addBounty(target.getUniqueId(), toSet, user.getUniqueId());
                        CooldownManager.I.setBounty(target, user);
                        GUI_BountiesList bList = new GUI_BountiesList(target, 0);
                        return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> {
                            DebugLog.info("[ANVIL-GUI] Closing Anvil-GUI of type GUI_EcoSetBounty for " + user.getName());
                            bList.open(user);
                        }));
                    }
                    else{
                        return List.of(AnvilGUI.ResponseAction.replaceInputText(LANG.getText("not-a-number").replace("[INPUT]", text)));
                    }
                })
                .title(LANG.getText("set-gui-title").replace("[PLAYERNAME]", target.getName()))
                .itemLeft(item.clone())
                .plugin(LuckyBounties.I)
                .open(user);
    }
}
