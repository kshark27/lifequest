package com.levipayne.liferpg;

import java.io.Serializable;

/**
 * Created by Levi on 4/15/2016.
 */
public class Reward implements Serializable {

    public String description;
    public int cost;

    public Reward(String description, int cost) {
        this.description = description;
        this.cost = cost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
