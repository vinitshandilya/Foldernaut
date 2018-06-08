package com.vinit.foldernaut;

public interface FileMovementStatusFeedback {
    void notifyStatus(double percentMoved, double speedInMB);
}
