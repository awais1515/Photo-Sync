package com.example.filesynchor;

public interface SyncProgress {
    public void onProgressUpdate(int copiedFiles,int totalFiles);
}
