package com.gui; 

import javax.swing.SwingUtilities;
import java.util.Scanner;

public class MainClass {

    public static void main(String[] args) {
        // Create the turtlegraphics object in main Method.
        TurtleGraphics turtleGraphics = new TurtleGraphics();
        turtleGraphics.about();

        // Start a new thread for console input
        Thread consoleInputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Type commands and press Enter.");
            System.out.println("Type 'exit' to close the application.");

            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();

                // Exit the application if the user types 'exit'
                if (command.trim().equalsIgnoreCase("exit")) {
                    System.out.println("Exiting application.");
                    // Dispose the frame and exit the application on the EDT
                    SwingUtilities.invokeLater(() -> {
                        turtleGraphics.frame.dispose(); // Access the frame field
                        System.exit(0);
                    });
                    break; // Exit the console input loop
                }

                // Process the command on the Event Dispatch Thread (EDT)
                // All Swing GUI updates MUST happen on the EDT
                SwingUtilities.invokeLater(() -> {
                    turtleGraphics.processCommand(command);
                });
            }

            scanner.close();
        });

        // Start the console input thread
        consoleInputThread.start();

        // The main thread can now finish, the GUI and console threads will keep the application running.
    }
}
