package com.biblio.gestionbiblio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "didis";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion réussie à PostgreSQL !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données !");
            e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        DatabaseConnection db = new DatabaseConnection();
        Connection conn = db.getConnection();

        if (conn != null) {
            System.out.println("Connexion établie avec succès !");
            try {
                conn.close();
                System.out.println("Connexion fermée !");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion !");
                e.printStackTrace();
            }
        } else {
            System.out.println("Impossible d’établir la connexion !");
        }
    }
}