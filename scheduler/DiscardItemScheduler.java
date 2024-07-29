package scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import constants.Constants;
import database.DatabaseConnection;

public class DiscardItemScheduler {

    private final ScheduledExecutorService scheduler;
    private final Connection connection;

    public DiscardItemScheduler(Connection connection) {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.connection = connection;
    }

    public void start() {
        Runnable task = this::moveItemsToDiscard;
        scheduler.scheduleAtFixedRate(task, Constants.SCHEDULER_INITIAL_DELAY, Constants.SCHEDULER_PERIOD, TimeUnit.DAYS);
    }

    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(Constants.SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                System.err.println("Scheduler did not terminate in the specified time.");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Scheduler termination interrupted: " + e.getMessage());
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void moveItemsToDiscard() {
        try (PreparedStatement moveStmt = connection.prepareStatement(Constants.MOVE_ITEMS_QUERY)) {
            int rowsAffected = moveStmt.executeUpdate();
            System.out.println("Items with bad rating moved to discard items successfully. Rows affected: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("Error moving items to discard items: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            DiscardItemScheduler discardItemScheduler = new DiscardItemScheduler(connection);
            discardItemScheduler.start();
            Runtime.getRuntime().addShutdownHook(new Thread(discardItemScheduler::stop));
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error initializing scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
