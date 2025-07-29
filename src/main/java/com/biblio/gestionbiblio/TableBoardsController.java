package com.biblio.gestionbiblio;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;

import java.sql.Connection;

public class TableBoardsController {

    @FXML private Label TotalMembres;
    @FXML private Label TotalLivDispo;
    @FXML private Label TotalPretEnC;
    @FXML
    private BarChart<String, Number> loansChart;

    @FXML
    void initialize(){
        int total = getTotalMembres();
        TotalMembres.setText(String.valueOf(total));
        int totalLivDispo = getTotalLivDispo();
        TotalLivDispo.setText(String.valueOf(totalLivDispo));
        int totalPretEnC = getTotalPretEnC();
        TotalPretEnC.setText(String.valueOf(totalPretEnC));
        afficherEmpruntsParMois();
    }

    private int getTotalPretEnC(){
        int totalPretEnC = 0;
        try{
            Connection conn = DatabaseConnection.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT COUNT (*) FROM preter");
            if (rs.next()){
                totalPretEnC = rs.getInt(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return totalPretEnC;
    }

    private int getTotalLivDispo() {
        int totalLivDispo = 0;
        try{
            Connection conn = DatabaseConnection.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT COUNT (*) FROM livre");
            if (rs.next()){
                totalLivDispo = rs.getInt(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return totalLivDispo;
    }

    private int getTotalMembres(){
        int total = 0;
        try{
            Connection conn = DatabaseConnection.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM MEMBRE");
            if(rs.next()){
                total = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    private void afficherEmpruntsParMois() {
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Emprunts");

        try {
            Connection conn = DatabaseConnection.getConnection();
            var stmt = conn.createStatement();

            String query = """
            SELECT TO_CHAR(datepret, 'Month') AS mois,
                   COUNT(*) AS total
            FROM preter
            GROUP BY TO_CHAR(datepret, 'Month'), EXTRACT(MONTH FROM datepret)
            ORDER BY EXTRACT(MONTH FROM datepret)
        """;

            var rs = stmt.executeQuery(query);

            while (rs.next()) {
                String mois = rs.getString("mois").trim(); // Supprime les espaces inutiles
                int total = rs.getInt("total");
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(mois, total));
            }

            loansChart.getData().clear();
            loansChart.getData().add(series);

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
