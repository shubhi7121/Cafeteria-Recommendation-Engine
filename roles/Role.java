package roles;

import java.io.*;
import java.sql.*;

public interface Role {
    void showOptions() throws IOException;
    void handleAction(String action) throws IOException, SQLException;
}
