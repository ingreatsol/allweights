package com.ingreatsol.allweights.connect;

public interface AllweightsConnectCallback {
    void onAllweightsDataChange(AllweightsData data);
    void onConnectionStatusChange(ConnectionStatus status);
}
