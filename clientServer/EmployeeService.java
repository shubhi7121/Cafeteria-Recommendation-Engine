package clientServer;
import java.io.*;

public class EmployeeService {
    private final BufferedReader userInput;
    private final BufferedReader in;
    private final PrintWriter out;

    public EmployeeService(BufferedReader userInput, BufferedReader in, PrintWriter out) {
        this.userInput = userInput;
        this.in = in;
        this.out = out;
    }

    public void handleCommands() throws IOException {
        while (true) {
            displayMenu();
            String command = userInput.readLine().trim();
            out.println(command);
            if (command.equalsIgnoreCase("EXIT")) {
                System.out.println("Log Out Successfully.");
                break;
            }
            handleEmployeeCommand(command);
        }
    }

    private void displayMenu() {
        System.out.println("\nEmployee Service Menu:");
        System.out.println("1. Feedback for Today");
        System.out.println("2. Vote for Tomorrow");
        System.out.println("3. Show Menu");
        System.out.println("Type 'EXIT' to quit");
        System.out.print("Select an option: ");
    }

    private void handleEmployeeCommand(String command) throws IOException {
        try {
            int choice = Integer.parseInt(command);
            switch (choice) {
                case 1 -> handleFeedbackForToday();
                case 2 -> handleVoteForTomorrow();
                case 3 -> handleShowMenu();
                default -> System.out.println("Invalid command number.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    private void handleFeedbackForToday() throws IOException {
        System.out.print("Enter id: ");
        String id = userInput.readLine();
        out.println(id);

        System.out.print("Enter comment: ");
        String price = userInput.readLine();
        out.println(price);
        
        System.out.print("Enter rating: ");
        String rating = userInput.readLine();
        out.println(rating);
        
        String serverResponse = in.readLine();
        System.out.println("=> " + serverResponse);
    }
    
    private void handleVoteForTomorrow() throws IOException {
    	System.out.println("Fetching menu from server...");
        String serverResponse;
        while ((serverResponse = in.readLine()) != null) {
            if ("End of Menu".equalsIgnoreCase(serverResponse)) {
                break;
            }
            System.out.println(serverResponse);
        }
        System.out.print("Enter breakfast: ");
        String rating = userInput.readLine();
        out.println(rating);
    }
    
    private void handleShowMenu() throws IOException {
        System.out.println("Fetching menu from server...");
        String serverResponse;
        while ((serverResponse = in.readLine()) != null) {
            if ("End of Menu".equalsIgnoreCase(serverResponse)) {
                break;
            }
            System.out.println(serverResponse);
        }
    }
}