package interfaces;

import java.io.IOException;
import java.sql.SQLException;

import exceptions.MenuItemNotFoundException;
import exceptions.NotificationException;
import exceptions.ProfileUpdateException;

/**
 * Interface defining the contract for handling actions associated with different roles.
 * Implementations of this interface should provide the logic for handling specific actions.
 */
public interface Role {

    /**
     * Handles the given action.
     *
     * @param action The action to be handled.
     * @throws IOException                If an I/O error occurs.
     * @throws SQLException               If a database access error occurs.
     * @throws MenuItemNotFoundException  If the menu item is not found.
     * @throws NotificationException      If a notification error occurs.
     * @throws ProfileUpdateException 
     */
    void handleAction(String action) throws IOException, SQLException, MenuItemNotFoundException, NotificationException, ProfileUpdateException;
}
