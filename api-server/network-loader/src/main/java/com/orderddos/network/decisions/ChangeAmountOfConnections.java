package com.orderddos.network.decisions;

public class ChangeAmountOfConnections extends Decision {

    private final int change;

    public ChangeAmountOfConnections(int change) {
        this.change = change;
    }

    public int getChange() {
        return change;
    }
}
