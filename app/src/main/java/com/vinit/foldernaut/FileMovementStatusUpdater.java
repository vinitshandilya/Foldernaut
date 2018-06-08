package com.vinit.foldernaut;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class FileMovementStatusUpdater {

    private long checkInterval = 500;
    private boolean interrupted = false;

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public void monitor(final File fileordir, final long desiredSize, final FileMovementStatusFeedback feedback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    double percentageMoved;
                    double lastCheckedSize = 0;
                    do {
                        double currentSize;
                        if(fileordir.isDirectory())
                            currentSize = fileordir.exists() ? FileUtils.sizeOfDirectory(fileordir) : 0;
                        else
                            currentSize = fileordir.exists() ? fileordir.length() : 0;

                        double speed = (currentSize - lastCheckedSize) / (1024 * checkInterval);
                        lastCheckedSize = currentSize;
                        if(desiredSize!=0)
                            percentageMoved = 100 * currentSize / desiredSize;
                        else
                            percentageMoved = 0;
                        feedback.notifyStatus(percentageMoved, speed);
                        Thread.sleep(checkInterval);
                    } while (percentageMoved < 100 && !interrupted);
                } catch (Exception e) {
                    System.err.println("File/directory monitor failed. " + e.getMessage());
                }
            }
        }).start();
    }

    public void stopMonitoring() {
        this.interrupted = true;
    }
}

