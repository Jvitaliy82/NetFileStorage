package com.netfilestorage.server;

import java.sql.*;

class AuthService {
    private static Connection connection;
    private static Statement stmt;

    static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void addUser(String login, String pass) {
        try {
            String query = "INSERT INTO main (login, password) VALUES (?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, login);
            ps.setInt(2, pass.hashCode());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static boolean checkPassword(String login, String password) {
        try {
            String sql = String.format("SELECT password FROM main where login = '%s'", login);
            ResultSet rs = stmt.executeQuery(sql);
            int dbPass = rs.getInt(1);
            if (dbPass == password.hashCode()) {
                System.out.println("Password checked");
                return true;
            } else {
                System.out.println("Password failed");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    static boolean checkUser(String login) {
        try {
            String s = String.format("SELECT login FROM main where login = '%s'", login);
            ResultSet res = stmt.executeQuery(s);
            if (res.next()) {
                System.out.println("User checked: " + login);
                return true;
            } else {
                System.out.println("User doesn't exist");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
