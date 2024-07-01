package clientServer;

import java.sql.*;
import java.util.concurrent.*;

import databaseConnection.DatabaseConnection;

public class DiscardItemScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Connection connection;

    private static final String MOVE_ITEMS_QUERY =
            "INSERT INTO discard_items (menu_item_id, discarded_date) " +
            "SELECT menu_item_id, CURDATE() " +
            "FROM meal_item " +
            "WHERE rating < 2 AND sentiment_score < 2 ";

    public DiscardItemScheduler(Connection connection) {
        this.connection = connection;
    }

    public void startScheduler() {
        Runnable task = this::moveItemsToDiscard;
        scheduler.scheduleAtFixedRate(task, 0, 30, TimeUnit.DAYS);
    }

    public void stopScheduler() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(30, TimeUnit.SECONDS); // Wait for tasks to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void moveItemsToDiscard() {
        try {
            try (PreparedStatement moveStmt = connection.prepareStatement(MOVE_ITEMS_QUERY)) {
                int rowsAffected = moveStmt.executeUpdate();
                System.out.println("Items with bad rating moved to discard items successfully. Rows affected: " + rowsAffected);
            }
        } catch (SQLException e) {
            System.err.println("Error moving items to discard items: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            DiscardItemScheduler scheduler = new DiscardItemScheduler(connection);
            scheduler.startScheduler();
            Runtime.getRuntime().addShutdownHook(new Thread(scheduler::stopScheduler));
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
