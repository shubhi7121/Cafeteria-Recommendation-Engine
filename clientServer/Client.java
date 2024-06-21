package clientServer;

import java.io.*;
import java.net.*;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                System.out.println(serverResponse);
                if (serverResponse.equals("Enter your email: ") 
                		|| serverResponse.equals("Please re-enter your email to confirm your role:")) {
                    String email = consoleInput.readLine();
                    out.println(email);
                } else if (serverResponse.startsWith("Admin actions:")) {
                	AdminService admin = new AdminService(consoleInput, in, out);
                	admin.handleCommands();
                }
                else if(serverResponse.startsWith("Chef actions:")) {
                	String action = consoleInput.readLine();
                    out.println(action);
                    if (action.equalsIgnoreCase("exit")) {  
                        break;
                    }
                }
                else if (serverResponse.startsWith("Employee actions:")) {
                	EmployeeService emp = new EmployeeService(consoleInput, in, out);
                	emp.handleCommands();
                    
                } else if (serverResponse.startsWith("Login successful")) {
                	
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}