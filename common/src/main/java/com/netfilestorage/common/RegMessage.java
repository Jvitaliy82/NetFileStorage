package com.netfilestorage.common;

public class RegMessage extends AbstractMessage {

    private String login;
    private String password;

    public RegMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
