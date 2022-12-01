package com.ingreatsol.allweights;

import androidx.annotation.NonNull;

public class AllweightsData {
    public Float peso;
    public Boolean carga;
    public Float porcentaje_bateria;

    public AllweightsData() {
    }

    public AllweightsData(Float peso, Boolean carga, Float porcentaje_bateria) {
        this.peso = peso;
        this.carga = carga;
        this.porcentaje_bateria = porcentaje_bateria;
    }

    @NonNull
    @Override
    public String toString() {
        return "AllweightsData{" +
                "peso=" + peso +
                ", carga=" + carga +
                ", porcentaje_bateria=" + porcentaje_bateria +
                '}';
    }
}
