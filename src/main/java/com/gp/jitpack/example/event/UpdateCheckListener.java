package com.gp.jitpack.example.event;

public interface UpdateCheckListener {
    void onNoUpdateRequired();
    void onOptionalUpdateAvailable(String version, Boolean isGracePeriod);
    void onMandatoryUpdateAvailable(String version, Boolean isGracePeriod);
}

