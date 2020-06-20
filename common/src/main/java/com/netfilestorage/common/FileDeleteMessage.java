package com.netfilestorage.common;

public class FileDeleteMessage extends AbstractMessage {

    private String fileToDelete;

    public String getFileName() {
        return fileToDelete;
    }

    public FileDeleteMessage(String fileToDelete) {
        this.fileToDelete = fileToDelete;
    }
}