package com.netfilestorage.common;

import java.util.TreeMap;

public class RefreshServerStorageMessage extends AbstractMessage {

    private TreeMap<String, Long> findFiles;

    public RefreshServerStorageMessage(TreeMap<String, Long> findFiles) {
        this.findFiles = findFiles;
    }

    public TreeMap<String, Long> getFindFiles() {
        return findFiles;
    }
}
