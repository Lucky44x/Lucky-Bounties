package com.github.lucky44x.luckybounties.user;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Lucky44x
 * The UserStats class for keeping track of user-based statistics
 */
@Getter @Setter
public class UserStats {
    private int bountiesSet = 0;
    private int bountiesReceived = 0;
    private int bountiesTaken = 0;

    public static UserStats EMPTY = new UserStats(0,0,0);

    public UserStats(int bountiesSet, int bountiesReceived, int bountiesTaken){
        this.bountiesReceived = bountiesReceived;
        this.bountiesSet = bountiesSet;
        this.bountiesTaken = bountiesTaken;
    }
}
