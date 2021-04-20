package com.example.filesynchor;

public class Data {

    public static final String KEY_ID = "id";
    public static final String KEY_SYNC_TIME = "sync_time";
    public static final String KEY_SYNCED_FILES = "synced_files";
    public static final String KEY_SYNCED_GB = "synced_gb";
    public static final String KEY_STATUS = "status";
    public static final String KEY_PATHS = "paths";

    private long id;
    private String syncTime;
    private int syncedFiles;
    private String syncedGB;
    private String status;
    private String paths;

    public static final String TABLE = "Data";
    public Data(String syncTime, int syncedFiles, String syncedGB, String status, String paths) {
        this.syncTime = syncTime;
        this.syncedFiles = syncedFiles;
        this.syncedGB = syncedGB;
        this.status = status;
        this.paths = paths;
    }

    public Data(){}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
        if (syncedFiles==0)
            return "";
        else return paths;
    }

    public void setPaths(String paths) {
        this.paths = paths;
    }
}
