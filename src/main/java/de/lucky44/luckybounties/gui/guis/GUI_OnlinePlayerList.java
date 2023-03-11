package de.lucky44.luckybounties.gui.guis;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.gui.core.ChestGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GUI_OnlinePlayerList extends ChestGUI {

    int page = 0;
    int pageRows = 0;
    boolean hasNext = false;
    boolean hasLast = false;

    Player[] players;

    public GUI_OnlinePlayerList(int page, Player[] players, Player viewer){

        if(players == null)
            players = LuckyBounties.I.getVisiblePlayers(viewer);

        this.players = players;

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

        if((hasNext || hasLast) && rows < 6)
            rows+=1;

        setSize(9 * rows);
        pageRows = rows;
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
            set(GUIItems.NextItem(), rows*9-1);
        if(hasLast)
            set(GUIItems.BackItem(), (rows-1)*9);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(int slot, ItemStack item) {

        if(slot == pageRows*9-1 && hasNext){
            GUI_OnlinePlayerList list = new GUI_OnlinePlayerList(page+1, players, user);
            list.open(user);
        }

        if(slot == (pageRows-1)*9 && hasLast){
            GUI_OnlinePlayerList list = new GUI_OnlinePlayerList(page-1, players, user);
            list.open(user);
        }

        if(slot > ((pageRows-1)*9)-1 && (hasNext || hasLast))
            return;

        if(slot % 2 != 0) {
            int offset = page * 22;
            int playerIndex = (slot-1) / 2;

            if(playerIndex  + offset < players.length){
                Player target = players[playerIndex + offset];
                GUI_BountiesList bountiesList = new GUI_BountiesList(target, 0);
                bountiesList.open(user);
            }
        }
    }
}
