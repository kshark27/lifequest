package com.levipayne.liferpg;

import java.io.Serializable;

/**
 * Created by Levi on 4/15/2016.
 */
public class Reward implements Serializable {
    String description;
    int cost;

    public Reward(String description, int cost) {
        this.description = description;
        this.cost = cost;
    }
}
