package de.lucky44.luckybounties.gui.guis;

import de.lucky44.api.luckybounties.events.EcoBountySetEvent;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.util.bounty;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GUI_EcoSetBounty {

    private Player user;
    private final Player target;


    public GUI_EcoSetBounty(Player target){
        this.target = target;
    }

    public void open(Player user){
        this.user = user;
        ItemStack item = GUIItems.EcoSetAnvilItem();

        new AnvilGUI.Builder()
                .onClose(player -> {
                    GUI_BountiesList BountiesGUI = new GUI_BountiesList(target, 0);
                    BountiesGUI.open(user);
                })
                .onComplete((player, text) -> {
                    if(text.matches("[-+]?[0-9]*\\.?[0-9]+")){
                        float amount = Float.parseFloat(text);

                        EcoBountySetEvent event = new EcoBountySetEvent(user, target, amount);
                        LuckyBounties.I.callEvent(event);
                        if(event.isCancelled())
                            return AnvilGUI.Response.close();

                        if(amount <= 0){
                            return AnvilGUI.Response.text(LANG.getText("bounty-below-zero"));
                        }

                        if(LuckyBounties.I.Vault.getBalance(user) < amount){
                            return AnvilGUI.Response.text(LANG.getText("cannot-afford")
                                    .replace("[AMOUNT]", ""+amount)
                                    .replace("[SYMBOL]", CONFIG.getString("currency-symbol")));
                        }

                        bounty toSet = new bounty(amount);
                        LuckyBounties.I.addBounty(target.getUniqueId(), toSet, user.getUniqueId());

                        if(CONFIG.getBool("bounty-set-global")){
                            Bukkit.broadcastMessage(LANG.getText("eco-bounty-set-global")
                                    .replace("[PLAYERNAME]", user.getName())
                                    .replace("[AMOUNT]", ""+amount)
                                    .replace("[SYMBOL]", CONFIG.getString("currency-symbol"))
                                    .replace("[TARGET]", target.getName()));
                        }
                        else{
                            user.sendMessage(LANG.getText("eco-bounty-set")
                                    .replace("[AMOUNT]", ""+amount)
                                    .replace("[SYMBOL]", CONFIG.getString("currency-symbol"))
                                    .replace("[TARGET]", target.getName()));
                        }

                        return AnvilGUI.Response.close();
                    }
                    else{
                        return AnvilGUI.Response.text(LANG.getText("not-a-number").replace("[INPUT]", text));
                    }
                })
                .title(LANG.getText("set-gui-title").replace("[PLAYERNAME]", target.getName()))
                .itemLeft(item.clone())
                .plugin(LuckyBounties.I)
                .open(user);
    }
}
