package clientServer;

import java.io.*;

public class AdminService {
	private final BufferedReader userInput;
    private final BufferedReader in;
    private final PrintWriter out;
    public AdminService(BufferedReader userInput, BufferedReader in, PrintWriter out) {
        this.userInput = userInput;
        this.in = in;
        this.out = out;
    }
	public void handleCommands() throws IOException {
	        while (true) {
	            showMenuOptions(); 

	            String input = userInput.readLine().trim();
	            out.println(input);

	            switch (input) {
	                case "1":
	                    handleAddMenuItem();
	                    break;
	                case "2":
	                    handleUpdateMenuItem();
	                    break;
	                case "3":
	                    handleDeleteMenuItem();
	                    break;
	                case "4":
	                    handleShowMenu();
	                    break;
	                case "exit":
	                    out.println("Exiting...");
	                    break;
	                default:
	                    System.out.println("Invalid command");
	            }
	        }
	    }

	    private void showMenuOptions() {
	        System.out.println("1. ADD_MENU_ITEM");
	        System.out.println("2. UPDATE_MENU_ITEM");
	        System.out.println("3. DELETE_MENU_ITEM");
	        System.out.println("4. SHOW_MENU");
	        System.out.println("Type EXIT to logout");
	        System.out.print("Enter your choice: ");
	    }

	    private void handleAddMenuItem() throws IOException {
	        System.out.print("Enter name: ");
	        String name = userInput.readLine();
	        out.println(name);

	        System.out.print("Enter price: ");
	        String price = userInput.readLine();
	        out.println(price);
	        

	        System.out.print("Enter Meal Type (1-3): ");
	        String mealType = userInput.readLine();
	        out.println(mealType);
	        
	        System.out.print("Enter availability status (0-1): ");
	        String AvailabilityStatus = userInput.readLine();
	        out.println(AvailabilityStatus);

	        String serverResponse = in.readLine();
	        System.out.println("=> " + serverResponse);
	    }

	    private void handleUpdateMenuItem() throws IOException {
	        System.out.print("Enter id: ");
	        String id = userInput.readLine();
	        out.println(id);

	        System.out.print("Enter price: ");
	        String price = userInput.readLine();
	        out.println(price);
	        
	        System.out.print("Enter availability status (0-1): ");
	        String AvailabilityStatus = userInput.readLine();
	        out.println(AvailabilityStatus);

	        String serverResponse = in.readLine();
	        System.out.println("=> " + serverResponse);
	    }

	    private void handleDeleteMenuItem() throws IOException {
	        System.out.print("Enter id: ");
	        String id = userInput.readLine();
	        out.println(id);

	        String serverResponse = in.readLine();
	        System.out.println("=> " + serverResponse);
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
