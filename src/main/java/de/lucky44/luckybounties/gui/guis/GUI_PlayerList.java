package de.lucky44.luckybounties.gui.guis;

import de.lucky44.luckybounties.gui.core.ChestGUI;
import de.lucky44.luckybounties.files.lang.LANG;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class GUI_PlayerList  extends ChestGUI {

    int page = 0;
    boolean hasNext = false;
    boolean hasLast = false;

    Player[] players;

    public GUI_PlayerList(int page, Player[] players){
        this.players = Objects.requireNonNullElseGet(players, () -> Bukkit.getOnlinePlayers().toArray(new Player[0]));

        this.page = page;

        int offset = page * 22;
        if(this.players.length - offset > 22)
            hasNext = true;

        if(page > 0)
            hasLast = true;


    }

    @Override
    public void onOpen(Player user) {

        int offset = page * 22;
        int rows = 1;

        int counter = 0;
        for(int i = 0; i < 5; i++){
            counter += i%2 == 0 ? 4 : 5;

            if(players.length - offset > counter)
                rows ++;
            else
                break;
        }

        setSize(9 * rows);
        setName(LANG.getText("main-gui-title").replace("[PAGE]", "" +page));
        construct();

        fill(GUIItems.FillerItem());


        counter = 0;
        for(int slot = 0; slot < 45; slot ++){

            if(counter + offset >= players.length)
                break;

            if(slot%2 != 0){
                set(GUIItems.getPlayerHead(players[offset + counter]),slot);
                counter++;
            }
        }

        if(hasNext)
            set(GUIItems.NextItem(), 53);
        if(hasLast)
            set(GUIItems.BackItem(), 45);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(int slot, ItemStack item) {

        if(slot % 2 != 0) {
            int offset = page * 22;
            int playerIndex = (slot-1) / 2;

            if(playerIndex  + offset < players.length){
                Player target = players[playerIndex + offset];
                GUI_BountiesList bountiesList = new GUI_BountiesList(target, 0);
                bountiesList.open(user);
            }
        }

        if(slot == 53){
            if(!hasNext)
                return;

            GUI_PlayerList list = new GUI_PlayerList(page+1, players);
            list.open(user);
        }
        else if(slot == 45){
            if(!hasLast)
                return;

            GUI_PlayerList list = new GUI_PlayerList(page-1, players);
            list.open(user);
        }
    }
}
