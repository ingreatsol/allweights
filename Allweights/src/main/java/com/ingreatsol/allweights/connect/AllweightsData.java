package com.ingreatsol.allweights.connect;

import androidx.annotation.NonNull;

public class AllweightsData {
    public Float weight;
    public Boolean isEnergyConnected;
    public Float bateryPercent;

    @NonNull
    @Override
    public String toString() {
        return "AllweightsData{" +
                "weight=" + weight +
                ", isEnergyConnected=" + isEnergyConnected +
                ", bateryPercent=" + bateryPercent +
                '}';
    }
}
