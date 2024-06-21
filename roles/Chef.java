package roles;

import java.io.*;
import java.sql.*;
import java.util.*;

import clientServer.RecommendationEngine;

public class Chef implements Role {
    private BufferedReader in;
    private PrintWriter out;
    private Connection connection;

    public Chef(Connection connection, BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
        this.connection = connection;
    }

    @Override
    public void showOptions() throws IOException {
        out.println("Chef actions: 1. View Recommendations 2. Select Tommorow Menu 3. Generate Report Type exit to logout");
    }

    @Override
    public void handleAction(String action) throws IOException, SQLException {
        switch (action) {
            case "1":
                viewRecommendations();
                break;
            case "2":
                tommorrowMenu();
                break;
            case "3":
                generateReport();
                break;
            case "exit":
                out.println("Exiting...");
                break;
            default:
                out.println("Invalid action.");
        }
    }

    private void viewRecommendations() throws SQLException {
    	RecommendationEngine recommendationEngine = new RecommendationEngine(connection, out);
        recommendationEngine.viewRecommendations();
    }
    
    private void tommorrowMenu() throws IOException, SQLException {
    	out.println("tommorrow menu...");
    }

    private void generateReport() throws IOException, SQLException {
        out.println("Generating report...");
    }
}
