package com.example.filesynchor;

public class Data {
    private String syncTime;
    private int syncedFiles;
    private String syncedGB;
    private String status;
    private String paths;

    public Data(String syncTime, int syncedFiles, String syncedGB, String status, String paths) {
        this.syncTime = syncTime;
        this.syncedFiles = syncedFiles;
        this.syncedGB = syncedGB;
        this.status = status;
        this.paths = paths;
    }

    public String getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(String syncTime) {
        this.syncTime = syncTime;
    }

    public int getSyncedFiles() {
        return syncedFiles;
    }

    public void setSyncedFiles(int syncedFiles) {
        this.syncedFiles = syncedFiles;
    }

    public String getSyncedGB() {
        return syncedGB;
    }

    public void setSyncedGB(String syncedGB) {
        this.syncedGB = syncedGB;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaths() {
        return paths;
    }

    public void setPaths(String paths) {
        this.paths = paths;
    }
}
