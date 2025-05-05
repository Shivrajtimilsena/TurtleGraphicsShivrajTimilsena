package com.gui;
import uk.ac.leedsbeckett.oop.LBUGraphics;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*; // Import all event classes
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner; // Import Scanner

public class TurtleGraphics extends LBUGraphics {

    // Constants for command strings
    private static final String ABOUT_COMMAND = "about";
    private static final String PENUP_COMMAND = "penup";
    private static final String PENDOWN_COMMAND = "pendown";
    private static final String LEFT_COMMAND = "left";
    private static final String RIGHT_COMMAND = "right";
    private static final String MOVE_COMMAND = "move";
    private static final String REVERSE_COMMAND = "reverse";
    private static final String BLACK_COMMAND = "black";
    private static final String GREEN_COMMAND = "green";
    private static final String RED_COMMAND = "red";
    private static final String WHITE_COMMAND = "white";
    private static final String RESET_COMMAND = "reset";
    private static final String CLEAR_COMMAND = "clear";
    private static final String CENTER_COMMAND = "center";
    private static final String SAVE_IMAGE_COMMAND = "saveimage";
    private static final String LOAD_IMAGE_COMMAND = "loadimage";
    private static final String SAVE_COMMANDS_COMMAND = "savecommands";
    private static final String LOAD_COMMANDS_COMMAND = "loadcommands";
    private static final String SQUARE_COMMAND = "square";
    private static final String PENCOLOUR_COMMAND = "pencolour";
    private static final String PENWIDTH_COMMAND = "penwidth";
    private static final String TRIANGLE_COMMAND = "triangle";
    private static final String POLYGON_COMMAND = "polygon";
    private static final String SPEED_COMMAND = "speed";
    private static final String BACKGROUND_COLOR_COMMAND = "backgroundcolour";
    private static final String DRAW_STAR_COMMAND = "star";


    // Default pen settings
    private Color defaultPenColour = Color.WHITE;
    private int defaultPenWidth = 1;
    private List<String> commandHistory = new ArrayList<>();
    private boolean unsavedChanges = false;
    JFrame frame;
    private JTextArea commandPanel;

    //  Field to store the loaded background image
    private BufferedImage loadedImage = null;


    // List of commands that discard the current drawing state
    private static final List<String> DISCARDING_COMMANDS = Arrays.asList(
            CLEAR_COMMAND, RESET_COMMAND, LOAD_IMAGE_COMMAND, LOAD_COMMANDS_COMMAND
    );

    // List of commands that modify the drawing or turtle state
    private static final List<String> MODIFYING_COMMANDS = Arrays.asList(
            MOVE_COMMAND, REVERSE_COMMAND, LEFT_COMMAND, RIGHT_COMMAND,
            BLACK_COMMAND, GREEN_COMMAND, RED_COMMAND, WHITE_COMMAND, PENCOLOUR_COMMAND, PENWIDTH_COMMAND,
            SQUARE_COMMAND, TRIANGLE_COMMAND, POLYGON_COMMAND, PENUP_COMMAND, PENDOWN_COMMAND, SPEED_COMMAND,
            BACKGROUND_COLOR_COMMAND // Background color changes the state

    );

    // Flag to control the behavior of the 'about' command from the text box
    private boolean drawShivrajOnAboutCommand = false ;


    // Constructor for the TurtleGraphics class
    public TurtleGraphics() {
        // Initialize the main JFrame
        frame = new JFrame("Turtle Graphics - SHIVRAJ");
        // Change default close operation to allow our window listener to handle closing
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Add window listener to handle closing and check for unsaved changes
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if (confirmDiscardChanges()) {
                    System.exit(0); // Exit if user confirms or no unsaved changes
                }
            }
        });

        // Create the drawing canvas (LBUGraphics) and add it to the center
        JPanel drawingPanel = new JPanel(new FlowLayout());
        drawingPanel.add(this); // 'this' refers to the TurtleGraphics instance, which is a JPanel
        frame.add(drawingPanel, BorderLayout.CENTER);


        // Initialize the command panel (text area to display command history)
        commandPanel = new JTextArea(10, 20);
        commandPanel.setEditable(false); // Make the command panel read-only
        JScrollPane scrollPane = new JScrollPane(commandPanel); // Add a scroll pane to the command panel
        frame.add(scrollPane, BorderLayout.EAST); // Add the command panel to the east (right) side

        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        JMenuItem saveImageMenuItem = new JMenuItem("Save Image");
        JMenuItem loadImageMenuItem = new JMenuItem("Load Image");
        JMenuItem saveCommandsMenuItem = new JMenuItem("Save Commands");
        JMenuItem loadCommandsMenuItem = new JMenuItem("Load Commands");
        JMenuItem changeBackgroundMenuItem = new JMenuItem("Change Background Color");

        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpCommandsMenuItem = new JMenuItem("Commands");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        JMenuItem shortcutMenuItem = new JMenuItem("Shortcut Keys");


        // Add ActionListener to the Exit menu item
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 if (confirmDiscardChanges()) { // Check for unsaved changes on exit menu click
                     System.exit(0);
                 }
            }
        });

        
        // Route menu actions through processCommand for consistency and unsaved changes checks
        saveImageMenuItem.addActionListener(e -> processCommand(SAVE_IMAGE_COMMAND));
        loadImageMenuItem.addActionListener(e -> processCommand(LOAD_IMAGE_COMMAND));
        saveCommandsMenuItem.addActionListener(e -> processCommand(SAVE_COMMANDS_COMMAND));
        loadCommandsMenuItem.addActionListener(e -> processCommand(LOAD_COMMANDS_COMMAND));
        
        
     // Add ActionListener for the Help menu item (Modified to show commands)
        helpCommandsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show a dialog with the list of commands, formatted into categories
                String message = "Available Commands:\n\n" +
                        "Basic:\n" +
                        "  " + ABOUT_COMMAND + ": Displays information about the application.\n" +
                        "  " + RESET_COMMAND + ": Resets the turtle's position and attributes.\n" +
                        "  " + CLEAR_COMMAND + ": Clears the drawing canvas.\n" +
                        "\n" + // Add a newline for spacing between categories
                        "File:\n" +
                        "  " + SAVE_IMAGE_COMMAND + ": Saves the current drawing as a PNG image.\n" +
                        "  " + LOAD_IMAGE_COMMAND + ": Loads a PNG image to the drawing canvas.\n" +
                        "  " + SAVE_COMMANDS_COMMAND + ": Saves the executed commands to a text file.\n" +
                        "  " + LOAD_COMMANDS_COMMAND + ": Loads and executes commands from a text file.\n" +
                        "  " + BACKGROUND_COLOR_COMMAND + ": change the background colour of canvas.\n" +
                        "\n" + // Add a newline for spacing
                        "Drawing:\n" +
                        "  " + PENUP_COMMAND + ": Stops drawing when the turtle moves.\n" +
                        "  " + PENDOWN_COMMAND + ": Starts drawing when the turtle moves.\n" +
                        "  " + LEFT_COMMAND + " <degrees>: Turns the turtle left by the specified degrees.\n" +
                        "  " + RIGHT_COMMAND + " <degrees>: Turns the turtle right by the specified degrees.\n" +
                        "  " + MOVE_COMMAND + " <distance>: Moves the turtle forward by the specified distance.\n" +
                        "  " + REVERSE_COMMAND + " <distance>: Moves the turtle backward by the specified distance.\n" +
                        "  " + PENCOLOUR_COMMAND + " <r,g,b>: Sets the pen color using RGB values (e.g., 255,0,0 for red).\n" +
                        "  " + PENWIDTH_COMMAND + " <width>: Sets the pen thickness.\n" +
                        "  " + SPEED_COMMAND + " <value>: Sets the turtle's movement speed (0 is fastest).\n" +
                        "\n" + // Add a newline for spacing
                        "Shapes:\n" +
                        "  " + SQUARE_COMMAND + " <length>: Draws a square with the specified side length.\n" +
                        "  " + TRIANGLE_COMMAND + " <size>: Draws an equilateral triangle, or\n" +
                        "  " + TRIANGLE_COMMAND + " <side1> <side2> <side3>: Draws a triangle with specified sides.\n" +
                        "  " + POLYGON_COMMAND + " <side1> <side2> ...: Draws a polygon with the specified side lengths.\n" +
                        "  " + DRAW_STAR_COMMAND + " <length> Draws a star with the fixed length.\n" +
                         // Add other shape commands if any
                        "\n" + // Add a newline for spacing
                        "Colors:\n" +
                        "  " + BLACK_COMMAND + ": Sets the pen color to black.\n" +
                        "  " + GREEN_COMMAND + ": Sets the pen color to green.\n" +
                        "  " + RED_COMMAND + ": Sets the pen color to red.\n" +
                        "  " + WHITE_COMMAND + ": Sets the pen color to white.\n" ;

                displayInfo(message); // Use displayInfo for help message
            }
        });

        // Add ActionListener for the About menu item (under Help)
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show a dialog with application information
                String message = "Turtle Graphics Application\n" +
                        "Version: 1.0\n" +
                        "Developed by: SHIVRAJ\n" +
                        "Copyright © 2024 Shivraj Timilsena. All rights reserved.\n\n" +
                        "This application allows you to draw using simple commands. \n" +
                        "Type commands in the command line, or load them from a file.\n\n" +
                        "For Any Query:\n" +
                        "Contact Email: tshivraj23@tbc.edu.np" ;
                displayInfo(message); // Use displayInfo for about message
            }
        });
        // add the actionlisterner for Shortcut key menu item (under Help)
        shortcutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show a dialog with application information
                String message = "<html><body><pre>" +  // Start HTML and use pre to maintain spacing
                		"<u>HELP MENU SHORTCUT KEYS</u><br>" + //<u> for underline
                        "Help               : ALT + H<br>" + //<br> to break line
                        "About              : ALT + A<br>" +
                        "Commands           : ALT + C<br>" +
                        "Shortcut           : ALT + K<br><br>" +
                        "<u>FILE MENU SHORTCUT KEYS</u><br>" +
                        "File               : ALT + F<br>" +
                        "Load Image         : SHIFT + L<br>" +
                        "Save Image         : SHIFT + S<br>" +
                        "Save Commands      : ALT + S<br>" +
                        "Load Commands      : ALT + L<br>" +
                        "Change Background  : ALT + B<br>" +
                        "Exit               : ALT + E<br>" +
                        "</pre></body></html>";  //Close HTML and pre tags
                         
                                             
                displayInfo(message); // Display the formatted shortcut keys in a pop-up
            }
        });


        // ActionListener for the Change Background Color menu item now calls processCommand
        changeBackgroundMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call processCommand with the background color command constant
                // The logic for opening the color chooser is now in processCommand
                processCommand(BACKGROUND_COLOR_COMMAND);
            }
        });


        // Add menu items to the File menu
        fileMenu.add(saveImageMenuItem);
        fileMenu.add(loadImageMenuItem);
        fileMenu.add(saveCommandsMenuItem);
        fileMenu.add(loadCommandsMenuItem);
        // Add the new background color menu item
        fileMenu.add(changeBackgroundMenuItem);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        // Add the Help menu sub items to the Help menu
        helpMenu.add(helpCommandsMenuItem);
        helpMenu.add(aboutMenuItem);
        helpMenu.add(shortcutMenuItem);
        menuBar.add(helpMenu);

        // Set the menu bar to the frame
        frame.setJMenuBar(menuBar);
        
        // shortcut keys to quickly start the menu items 
        fileMenu.setMnemonic(KeyEvent.VK_F); // alt + f for file menu       
        helpMenu.setMnemonic(KeyEvent.VK_H); // alt + h for help menu
        
        // using Accelerator to set shortcut key directly. if we use setMnemonic here we have to  open file menu first then shortcut. 
        
        //lets first create keystroke. and setting accelerator.
        KeyStroke exitKey = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK); //for exit alt + E
        exitMenuItem.setAccelerator(exitKey);
        
        KeyStroke imageSaveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK);// for image save SHIFT + S
        saveImageMenuItem.setAccelerator(imageSaveKey);
        
        KeyStroke imageLoadKey = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.SHIFT_DOWN_MASK);// for image load SHIFT + L
        loadImageMenuItem .setAccelerator(imageLoadKey);
        
        KeyStroke cmdSaveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK); //for commands save CONTROL + S
        saveCommandsMenuItem.setAccelerator(cmdSaveKey);
        
        KeyStroke cmdLoadKey = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK); // for commands load CONTROL + L
        loadCommandsMenuItem.setAccelerator(cmdLoadKey);
        
        KeyStroke helpCommandsKey = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK); //for commands(help) ALT + C
        helpCommandsMenuItem.setAccelerator(helpCommandsKey);
        
        KeyStroke aboutKey = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK); // for about panel ALT + A
        aboutMenuItem.setAccelerator(aboutKey);
        
        KeyStroke shortcutKey = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_DOWN_MASK); // to view shortcut panel ALT + K
        shortcutMenuItem.setAccelerator(shortcutKey);
        
        KeyStroke backgroundChangeKey = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.ALT_DOWN_MASK); // for background change ALT + B
        changeBackgroundMenuItem.setAccelerator(backgroundChangeKey);
        
        
        

        frame.pack();
        frame.setVisible(true);
        resetTurtleState(); // Initial reset of the turtle state

    }


    // Resets the turtle's state to default values
    private void resetTurtleState() {
        reset(); // LBUGraphics reset (center, point down) 
        // *** MODIFIED: reset() now calls clear(), which handles clearing the loaded image ***
        setPenColour(defaultPenColour); // Set default pen color
        setStroke(defaultPenWidth); // Set default pen width
        drawOn(); // Pen down by default
        unsavedChanges = false; // Reset means no unsaved changes
        setTurtleSpeed(1); // Keep default speed
    }

    // Helper method to confirm discarding unsaved changes
    private boolean confirmDiscardChanges() {
        if (unsavedChanges) {
            int response = JOptionPane.showConfirmDialog(frame,
                    "You have unsaved changes.\n Do you want to discard them?",
                    "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            return response == JOptionPane.YES_OPTION;
        }
        return true; // No unsaved changes, ok to proceed
    }

    // Override displayMessage to show messages in a popup
    @Override
    public void displayMessage(String message) {
        if (message.toLowerCase().contains("error")) {
            displayError(message);
        } else {
            displayInfo(message);
        }
    }

    // Helper method to display error messages in a popup
    private void displayError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Helper method to display informational messages in a popup
    private void displayInfo(String message) {
        JOptionPane.showMessageDialog(frame, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    // Main method to process user commands
    // ProcessCommand is public for console input handling in MainClass
    @Override
    public void processCommand(String command) {
        String trimmedCommand = command.trim();
        if (trimmedCommand.isEmpty()) {
            return; // Ignore empty commands
        }

        String[] parts = trimmedCommand.split("\\s+");
        String action = parts[0].toLowerCase(); // Converts the command to lowercase

        // Check for unsaved changes before executing discarding commands
        if (DISCARDING_COMMANDS.contains(action) && unsavedChanges) {
            if (!confirmDiscardChanges()) {
                displayInfo("Action cancelled.");
                return;
            }
        }

        // If the command is reset or clear, clear history and panel *before* adding the command itself
        if (action.equals(RESET_COMMAND) || action.equals(CLEAR_COMMAND)) {
            commandHistory.clear();
            commandPanel.setText("");
        }

        // Add the command to history and update the command panel display
        // Note: We only add text-based commands to history, not internal GUI triggers like background colour
        if (!action.equals(BACKGROUND_COLOR_COMMAND)) {
             commandHistory.add(trimmedCommand);
             updateCommandPanel(trimmedCommand);
        }


        try {
            switch (action) {
                case ABOUT_COMMAND:
                    // Original logic using the flag and separate drawShivraj()
                    if (drawShivrajOnAboutCommand) {
                        drawShivraj();
                    } else {
                        about(); // Call the base class about if not drawing the name
                    }
                    // unsavedChanges is set within the drawShivraj() method now
                    break;
                case PENUP_COMMAND:
                    drawOff();
                    break;
                case PENDOWN_COMMAND:
                    drawOn();
                    break;
                case LEFT_COMMAND:
                    if (parts.length > 1) {
                        try {
                            int degrees = Integer.parseInt(parts[1]);
                            left(degrees);
                        } catch (NumberFormatException e) {
                            displayError("Error: Parameter missing.");
                        }
                    } else {
                        left(); // Default left turn (likely 90 degrees in LBUGraphics)
                    }
                    break;
                case RIGHT_COMMAND:
                    if (parts.length > 1) {
                        try {
                            int degrees = Integer.parseInt(parts[1]);
                            right(degrees);
                        } catch (NumberFormatException e) {
                            displayError("Error: Parameter missing.");
                        }
                    } else {
                        right(); // Default right turn (likely 90 degrees in LBUGraphics)
                    }
                    break;
                case MOVE_COMMAND:
                    if (parts.length > 1) {
                        try {
                            int distance = Integer.parseInt(parts[1]);
                            if (distance < 0) {
                                displayError("Error: Distance cannot be negative.");
                            } else {
                                // --- Bounds checking for move command ---
                                if (isMoveWithinBounds(distance)) {
                                     forward(distance);
                                } else {
                                    displayError("Error: Parameter is out of bound ");
                                }
                                // --- End of bounds checking ---
                            }
                        } catch (NumberFormatException e) {
                            displayError("Error: Invalid distance\n for move command.");
                        }
                    } else {
                        displayError("Error: Missing distance\n for move command.");
                    }
                    break;
                case REVERSE_COMMAND:
                    if (parts.length > 1) {
                        try {
                            int distance = Integer.parseInt(parts[1]);
                             if (distance < 0) {
                                displayError("Error: Distance cannot be negative.");
                            }
                            else {
                                // --- Bounds checking for reverse command ---
                                // Reverse is just moving forward by a negative distance
                                if (isMoveWithinBounds(-distance)) {
                                     forward(-distance);
                                } else {
                                    displayError("Error: Parameter is out of bound ");
                                }
                                // --- End of bounds checking ---
                            }
                        } catch (NumberFormatException e) {
                            displayError("Error: Invalid distance \nfor reverse command.");
                        }
                    } else { displayError("Error: Missing distance\n for reverse command."); }
                    break;
                case BLACK_COMMAND:
                    setPenColour(Color.BLACK);
                    break;
                case GREEN_COMMAND:
                    setPenColour(Color.GREEN);
                    break;
                case RED_COMMAND:
                    setPenColour(Color.RED);
                    break;
                case WHITE_COMMAND:
                    setPenColour(Color.WHITE);
                    break;
                case RESET_COMMAND:
                    resetTurtleState();
                    drawOff(); // Ensure pen is up after reset if needed, though resetTurtleState sets drawOn
                    break;
                case CLEAR_COMMAND:
                    clear(); // LBUGraphics clear
                    // Reposition turtle to a default spot, e.g., middle left
                    int canvasHeight = getHeight();
                    setxPos(0);
                    setyPos(canvasHeight / 2);
                    pointTurtle(0); // Point right after clear
                    // *** MODIFIED: clear() now also clears the loaded image ***
                    loadedImage = null;
                    unsavedChanges = false;
                    repaint(); // Request a repaint after clearing
                    break;
                case CENTER_COMMAND: // use Turtle to bring in the center of the canvas.
                	clear();
                	reset();
                	setStroke(1);
                	setPenColour(Color.RED);
                	drawOn();
                	unsavedChanges = false;
                	break;
                case SAVE_IMAGE_COMMAND:
                    saveImage();
                    break;
                case LOAD_IMAGE_COMMAND:
                    loadImage(); // This now stores the image and calls repaint
                    break;
                case SAVE_COMMANDS_COMMAND:
                    saveCommands();
                    break;
                case LOAD_COMMANDS_COMMAND:
                    // Original loadCommands without SwingWorker
                    loadCommands();
                    break;
                case SQUARE_COMMAND:
                    if (parts.length > 1) {
                        try {
                            int length = Integer.parseInt(parts[1]);
                            if (length <= 0) {
                                displayError("Error: Length must be \na positive value.");
                            } else {
                                // Note: Drawing shapes like square might still go out of bounds
                                // if the starting position is too close to the edge.
                                // More complex bounds checking would be needed for shapes.
                                drawSquare(length);
                            }
                        } catch (NumberFormatException e) {
                            displayError("Error: Invalid length \nfor square command.");
                        }
                    } else {
                        displayError("Error: Missing length \nfor square command.");
                    }
                    break;
                case PENCOLOUR_COMMAND:
                    if (parts.length > 1) {
                        String[] colours = parts[1].split(",");
                        if (colours.length == 3) {
                            try {
                                int red = Integer.parseInt(colours[0]);
                                int green = Integer.parseInt(colours[1]);
                                int blue = Integer.parseInt(colours[2]);
                                if (red >= 0 && red <= 255 && green >= 0 && green <= 255 && blue >= 0 && blue <= 255) {
                                    setPenColour(new Color(red, green, blue));
                                } else {
                                    displayError("Error: RGB values must be\n between 0 and 255.");
                                }
                            } catch (NumberFormatException e) {
                                displayError("Error: Invalid RGB values\n for pencolour command.");
                            }
                        } else {
                            displayError("Error: Incorrect parameters\n(e.g., pencolour 255,0,0).");
                        }
                    } else {
                        displayError("Error: Missing parameters\n(e.g., pencolour 255,0,0).");
                    }
                    break;
                case PENWIDTH_COMMAND:
                    if (parts.length > 1) {
                        try {
                            int width = Integer.parseInt(parts[1]);
                            if (width <= 0) {
                                displayError("Error: Pen width must \nbe a positive value.");
                            } else {
                                setStroke(width);
                            }
                        } catch (NumberFormatException e) {
                            displayError("Error: Invalid width \nfor penwidth command.");
                        }
                    } else {
                        displayError("Error: Missing width \nfor penwidth command.");
                    }
                    break;
                case TRIANGLE_COMMAND:
                    if (parts.length == 2) { // Equilateral triangle
                        try {
                            int size = Integer.parseInt(parts[1]);
                            if (size <= 0) {
                                displayError("Error: Size must be a positive value.");
                            } else {
                                // Note: Drawing shapes like triangle might still go out of bounds
                                // if the starting position is too close to the edge.
                                // More complex bounds checking would be needed for shapes.
                                drawEquilateralTriangle(size);
                            }
                        } catch (NumberFormatException e) {
                            displayError("Error: Invalid size \nfor triangle command.");
                        }
                    } else if (parts.length == 4) { // Triangle with 3 sides
                        try {
                            double side1 = Double.parseDouble(parts[1]);
                            double side2 = Double.parseDouble(parts[2]);
                            double side3 = Double.parseDouble(parts[3]);
                            if (side1 <= 0 || side2 <= 0 || side3 <= 0) {
                                displayError("Error: Triangle sides \nmust be positive values.");
                            } else if (!isValidTriangle(side1, side2, side3)) {
                                displayError("Error: Invalid triangle dimensions.");
                            } else {
                                // Note: Drawing shapes like triangle might still go out of bounds
                                // if the starting position is too close to the edge.
                                // More complex bounds checking would be needed for shapes.
                                drawTriangle(side1, side2, side3);
                            }
                        } catch (NumberFormatException e) {
                            displayError("Error: Invalid side lengths \n(e.g., triangle 30 40 50).");
                        }
                    } else {
                        displayError("Error: Incorrect number of parameters \n(e.g., triangle 100 or triangle 30 40 50).");
                    }
                    break;
                case POLYGON_COMMAND:
                    if (parts.length >= 2) {
                        List<Double> lengths = new ArrayList<>();
                         try {
                             for (int i = 1; i < parts.length; i++) {
                                 double length = Double.parseDouble(parts[i]);
                                 if (length <= 0) { // Keep this check here too for robustness
                                      displayError("Error: Polygon side lengths must be positive values.");
                                      return; // Exit if any side is invalid
                                 }
                                 lengths.add(length);
                             }
                              if (lengths.size() >= 3) {
                                 drawPolygon(lengths);
                             } else {
                                  displayError("Error: Polygon requires at least 3 side lengths.");
                             }
                         } catch (NumberFormatException e) {
                              displayError("Error: Invalid side length value for polygon command.");
                         }
                    } else {
                         displayError("Error: Missing side lengths for polygon command\n (e.g., polygon 100 100 100).");
                    }
                    break;
                 case SPEED_COMMAND:
                    if (parts.length > 1) {
                        try {
                            int speedValue = Integer.parseInt(parts[1]);
                            if (speedValue >= 0) { // Speed should be non-negative
                                setTurtleSpeed(speedValue);
                                displayInfo("Turtle speed set to " + speedValue + " (0 is fastest).");
                            } else {
                                displayError("Error: Speed value cannot be negative.");
                            }
                        } catch (NumberFormatException e) {
                            displayError("Error: Invalid speed value for speed command.");
                        }
                    } else {
                        displayError("Error: Missing speed value for speed command.");
                    }
                    break;
                // Case for the BACKGROUND_COLOR_COMMAND
                case BACKGROUND_COLOR_COMMAND:
                    // This case is triggered by the menu item.
                    // Open the color chooser dialog.
                    Color selectedColor = JColorChooser.showDialog(frame, "Background Color Panel", getBackground_Col());

                    // If a color was selected (user didn't click Cancel)
                    if (selectedColor != null) {
                        // Set the background color using the LBUGraphics method
                        setBackground_Col(selectedColor);
                        clear();
                        // Mark that the drawing state has changed
                        unsavedChanges = true;
                        displayInfo("Background color changed.");
                    } else {
                         // User cancelled the color chooser
                         displayInfo("Background color change cancelled.");
                    }
                    // Note: We don't add BACKGROUND_COLOR_COMMAND to history here as it's a GUI action,
                    // not a text command meant to be replayed from a file.
                    break;
                    
                case DRAW_STAR_COMMAND:
                	if (parts.length > 1) {
                        try {
                            int length = Integer.parseInt(parts[1]);
                            if (length <= 0) {
                                displayError("Error: Length must be \na positive value.");
                            } else {
                                
                                // if the starting position is too close to the edge.
                                // More complex bounds checking would be needed for shapes.
                            	                           	
                            	drawOn();
                                drawStar(length);//this is the main method to draw star.
                                drawOff();
                                reset();
                            }
                        } catch (NumberFormatException e) {
                            displayError("Error: Invalid length \nfor star command.");
                        }
                    } else {
                        displayError("Error: Missing length \nfor star command.");
                    }
                    break;

                default:
                    displayError("Error: Invalid command.");
            }

            // Set unsavedChanges = true for commands that modify the drawing state,
            // unless it was just explicitly set to false by a save/load/reset/clear command
            // ABOUT command (when drawing shivraj) also sets unsavedChanges internally now.
            // Note: Background color change is handled by the menu item's action listener
            // and sets unsavedChanges directly there.
            if (MODIFYING_COMMANDS.contains(action) && !DISCARDING_COMMANDS.contains(action) && !action.equals(SAVE_IMAGE_COMMAND) && !action.equals(SAVE_COMMANDS_COMMAND) && !action.equals(ABOUT_COMMAND) && !action.equals(BACKGROUND_COLOR_COMMAND)) {
                 unsavedChanges = true;
            }


        } catch (Exception e) {
            displayError("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            unsavedChanges = true; // Assume an error might leave the state inconsistent
        }
    }

    /**
     * Checks if a move of a given distance would result in the turtle being within the canvas bounds.
     * This is a predictive check before actually performing the move.
     * @param distance The distance to check (can be negative for reverse).
     * @return true if the move stays within bounds, false otherwise.
     */
    private boolean isMoveWithinBounds(int distance) {
        int currentX = getxPos();
        int currentY = getyPos();
        int direction = getDirection(); // Get current direction in degrees

        // Calculate potential new position based on current position, direction, and distance
        // Convert direction from degrees to radians for trigonometric functions
        double directionInRadians = Math.toRadians(direction);

        // Calculate the change in x and y coordinates
        // Note: In Java AWT/Swing, the y-axis typically increases downwards.
        // The turtle's direction is 0 degrees to the right, 90 down, 180 left, 270 up.
        // Standard trigonometric functions assume 0 to the right, 90 up, 180 left, 270 down.
        // We need to adjust for this. A direction of D degrees means an angle of (90 - D) or (450 - D) mod 360
        // relative to the positive y-axis (downwards). Or, relative to the positive x-axis (rightwards):
        // x_change = distance * cos(angle_from_positive_x)
        // y_change = distance * sin(angle_from_positive_x)
        // Turtle direction 0 (right) -> angle 0, cos(0)=1, sin(0)=0. Correct.
        // Turtle direction 90 (down) -> angle 90, cos(90)=0, sin(90)=1. Correct.
        // Turtle direction 180 (left) -> angle 180, cos(180)=-1, sin(180)=0. Correct.
        // Turtle direction 270 (up) -> angle 270, cos(270)=0, sin(270)=-1. Correct.
        // So, standard trig functions with the turtle's direction in degrees converted to radians should work.

        int potentialNewX = (int) Math.round(currentX + distance * Math.cos(directionInRadians));
        int potentialNewY = (int) Math.round(currentY + distance * Math.sin(directionInRadians));

        // Get the dimensions of the drawing canvas
        int canvasWidth = getWidth();
        int canvasHeight = getHeight();

        // Check if the potential new position is within the canvas bounds (0 to width-1 and 0 to height-1)
        // We might allow a small buffer around the edges depending on how the turtle graphic is drawn,
        // but for a strict check, we use the exact bounds.
        boolean withinBounds = potentialNewX >= 0 && potentialNewX < canvasWidth &&
                               potentialNewY >= 0 && potentialNewY < canvasHeight;

        return withinBounds;
    }


    // Original about() method calling super.about()
    @Override
    public void about() {
        // Call the base class's about() method for the default animation
        super.about();
        // Note: This method does not draw SHIVRAJ.
        // If you want to draw SHIVRAJ, you would call drawShivraj() instead.
        // The processCommand method handles the choice based on the flag.
    }


    // Method to execute the commands to draw "shivraj" using direct method calls
    private void drawShivraj() {
          // Corresponds to "clear"
          clear();
          int canvasHeight = getHeight();
          setxPos(0);
          setyPos(canvasHeight / 2);
          pointTurtle(0);

          // Corresponds to "right"
          right(); // Assuming 90 degrees
          drawOff();
          forward(150);
          drawOn();
          setPenColour(Color.RED);
          setStroke(10);

          // Start drawing S
          left();
          forward(70);
          left();
          forward(70);
          left();
          forward(70);
          left(55);
          forward(80);
          right(55);
          forward(70);
          right();
          forward(70);
          right();
          forward(70);
          // End drawing S, move to start of H


          drawOff();
          forward(17);
          right();
          forward(120);
          right();
          forward(87);
          drawOn();
          setPenColour(Color.WHITE);
          // Start drawing H
          forward(-175);
          forward(87);
          left();
          forward(70);
          right();
          forward(88);
          forward(-175);
          forward(87);
          // End drawing H, move to start of I

          drawOff();
          left();
          forward(40);
          right();
          forward(88);
          right();
          drawOn();
          setPenColour(Color.GREEN);

          // Start drawing I

          forward(10);
          forward(-20);
          forward(10);
          right();
          forward(175);
          right();
          forward(10);
          forward(-20);
          forward(10);
          // End drawing I, move to start of V

          drawOff();
          forward(36);
          right(80);
          setPenColour(new Color(100, 200, 140));

          // Start drawing V

          drawOn();
          forward(185);
          left(80);
          left(80);
          forward(185);

          // End drawing V, move to start of R

          drawOff();
          right(80);
          forward(40);
          right();
          setPenColour(new Color(160, 230, 10));
          // Start drawing R

          drawOn();
          forward(175);
          forward(-175);
          left();
          forward(70);
          right();
          forward(70);
          forward(10);
          right();
          forward(70);
          right(180);
          right(60);
          forward(110);

          // End drawing R, move to start of A

          drawOff();
          left(60);
          forward(40);
          forward(-40);
          forward(40);
          right();
          forward(10);
          left(180);
          right(80);
          left(80);
          right(10);
          drawOn();
          setPenColour(new Color(255, 0, 150));
          // Start drawing A

          forward(185);
          right(80);
          right(10);
          right(70);
          forward(185);
          forward(-85);
          right(10);
          right();
          forward(20);
          forward(10);
          // End drawing A, move to start of J

          drawOff();
          forward(-70);
          left();
          setPenColour(new Color(0, 255, 100));
          drawOn();

          // Start drawing J

          forward(85);
          left();
          forward(70);
          left();
          forward(185);
          right();
          forward(20);
          forward(-40);
          drawOff();
          // End drawing J
          forward(-650);
          right();
          forward(50);



          unsavedChanges = true; // The drawing was modified
    }


    // Method to draw a square
    private void drawSquare(int length) {
        int initialX = getxPos();
        int initialY = getyPos();
        int initialDirection = getDirection();


        for (int i = 0; i < 4; i++) {
        	drawOn();
            forward(length);
            right(90);
        }

        setxPos(initialX);
        setyPos(initialY);
        pointTurtle(initialDirection);
    }

    // Method to draw an equilateral triangle
    private void drawEquilateralTriangle(int size) {
        int initialX = getxPos();
        int initialY = getyPos();
        int initialDirection = getDirection();

        for (int i = 0; i < 3; i++) {
        	drawOn();
            forward(size);
            right(120);
        }

        setxPos(initialX);
        setyPos(initialY);
        pointTurtle(initialDirection);
    }

    // Method to draw a triangle with given side lengths
    private void drawTriangle(double side1, double side2, double side3) {
        int initialX = getxPos();
        int initialY = getyPos();
        int initialDirection = getDirection();

        double angleA_rad = Math.acos((side2 * side2 + side3 * side3 - side1 * side1) / (2 * side2 * side3));
        double angleB_rad = Math.acos((side1 * side1 + side3 * side3 - side2 * side2) / (2 * side1 * side3));
        double angleC_rad = Math.PI - angleA_rad - angleB_rad;

        // Convert internal angles to external turns (180 - internal angle)
        double turnC_deg = 180 - Math.toDegrees(angleC_rad);
        double turnA_deg = 180 - Math.toDegrees(angleA_rad);
        // double turnB_deg = 180 - Math.toDegrees(angleB_rad); // Not explicitly needed for the last turn to close


        drawOn();
        forward((int) Math.round(side1)); // Draw side 1
        right((int) Math.round(turnC_deg)); // Turn based on angle C
        forward((int) Math.round(side2)); // Draw side 2
        right((int) Math.round(turnA_deg)); // Turn based on angle A
        forward((int) Math.round(side3)); // Draw side 3 (should connect back to the start)


        // Restore initial state
        setxPos(initialX);
        setyPos(initialY);
        pointTurtle(initialDirection);
    }

    // Helper method to check if three side lengths can form a valid triangle
    private boolean isValidTriangle(double side1, double side2, double side3) {
        return (side1 + side2 > side3) && (side1 + side3 > side2) && (side2 + side3 > side1);
    }


    // Method to draw a polygon with given side lengths
    // Note: This implementation currently attempts to draw a regular polygon using the *first* side length
    // as the length for all sides and dividing 360 by the number of sides for the turn angle.
    // A true polygon with arbitrary side lengths requires more complex angle calculations.
    private void drawPolygon(List<Double> lengths) {
        int initialX = getxPos();
        int initialY = getyPos();
        int initialDirection = getDirection();

        int numberOfSides = lengths.size();
        if (numberOfSides < 3) {
            displayError("Error: Cannot draw a polygon \nwith less than 3 sides.");
            return;
        }

        // For a regular polygon, the external angle is 360 / number of sides
        double angleToTurn = 360.0 / numberOfSides;

        for (Double length : lengths) {
             // Keep this check here too for robustness, although processCommand also checks
            if (length <= 0) {
                 System.err.println("Unexpected error: Negative or zero \n side length passed to drawPolygon.");
                 // displayError("Error: Polygon side lengths must be positive values."); // Avoid double error message
                 return;
            }
            drawOn();
            // Note: The current logic iterates through the list for lengths, but uses a fixed turn angle.
            // This only draws a regular polygon if all lengths in the list are the same.
            forward((int) Math.round(length));
            // Corrected: Cast the double angle to int for the right() method
            right((int) Math.round(angleToTurn));
        }

        // After the loop, the turtle should theoretically be back near the start and original direction
        // for a closed polygon.
        // Restore initial state anyway for robustness.
        setxPos(initialX);
        setyPos(initialY);
        pointTurtle(initialDirection);
    }
    
    //Star draw method
    private void drawStar(int length) {


        drawOn(); // Ensure pen is down to draw

        // For a 5-pointed star, turn 144 degrees externally for each point
        int angle = 144;
        int numberOfPoints = 5;

        for (int i = 0; i < numberOfPoints; i++) {
            forward(length);
            right(angle); // Turn right by the external angle
        }
    }
        


    // Method to update the command panel text area
    private void updateCommandPanel(String command) {
        // Ensure this update happens on the EDT
        SwingUtilities.invokeLater(() -> {
            commandPanel.append(command + "\n");
            commandPanel.setCaretPosition(commandPanel.getDocument().getLength());
        });
    }

    // Method to save the current drawing as a PNG image
    private void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }

            try {
                BufferedImage image = getBufferedImage();
                ImageIO.write(image, "png", fileToSave);
                displayInfo("Image saved successfully to \n" + fileToSave.getAbsolutePath());
                unsavedChanges = false;
            } catch (IOException ex) {
                displayError("Error saving image: " + ex.getMessage());
                ex.printStackTrace();
            } catch (Exception ex) {
                 displayError("An unexpected error occurred while getting the image:\n " + ex.getMessage());
                 ex.printStackTrace();
            }
        } else {
             displayInfo("Image save cancelled.");
        }
    }

    // Method to load a PNG image onto the canvas
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showOpenDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();

            if (!confirmDiscardChanges()) {
                displayInfo("Loading image cancelled.");
                return;
            }

            try {
                BufferedImage image = ImageIO.read(fileToLoad);
                if (image != null) {
                    // *** MODIFIED: Store the loaded image in the field ***
                    loadedImage = image;
                    // Clear the current drawing state maintained by LBUGraphics (turtle path etc.)
                    super.clear();
                    // Repaint the canvas to show the new background image
                    repaint(); // Request a repaint

                    displayInfo("Image loaded successfully from \n" + fileToLoad.getAbsolutePath());
                    unsavedChanges = true; // Loading an image changes the drawing state
                } else {
                    displayError("Error: Could not read image file or \n file is not a valid image.");
                }
            } catch (IOException ex) {
                displayError("Error loading image: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            displayInfo("Image load cancelled.");
        }
    }

    // Method to save the command history to a text file
    private void saveCommands() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Commands");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                for (String command : commandHistory) {
                    // MODIFICATION: Ignore savecommands when saving
                    // Check if the command starts with the savecommands command (case-insensitive)
                    if (!command.trim().toLowerCase().startsWith(SAVE_COMMANDS_COMMAND)) {
                        writer.write(command);
                        writer.newLine();
                    }
                }
                displayInfo("Commands saved successfully to \n" + fileToSave.getAbsolutePath());
                unsavedChanges = false;
            } catch (IOException ex) {
                displayError("Error saving commands: \n" + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            displayInfo("Command save cancelled.");
        }
    }

    // Method to load and execute commands from a text file
    private void loadCommands() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Commands");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showOpenDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();

            if (!confirmDiscardChanges()) {
                displayInfo("Loading commands cancelled.");
                return;
            }

            // Clear current state and history before loading new commands
            commandHistory.clear();
            commandPanel.setText("");
            resetTurtleState(); // Reset turtle position/state

            // Original direct file loading logic
            try (BufferedReader reader = new BufferedReader(new FileReader(fileToLoad))) {
                String line;
                // Process each line (command) from the file
                while ((line = reader.readLine()) != null) {
                     // MODIFICATION: Ignore savecommands when loading
                     String trimmedLine = line.trim();
                     // Check if the line starts with the savecommands command (case-insensitive)
                    if (!trimmedLine.toLowerCase().startsWith(SAVE_COMMANDS_COMMAND)) {
                        // Use the main processCommand method to execute the loaded commands
                        // This adds them to history and updates state/drawing
                        processCommand(trimmedLine);
                    } else {
                         // Optionally display a message that the command was ignored
                         System.out.println("Ignored savecommands command in file:\n " + trimmedLine);
                    }
                }
                displayInfo("Commands loaded and executed successfully from \n" + fileToLoad.getAbsolutePath());
                // Mark as unsaved changes because loading commands changes the drawing state
                unsavedChanges = true;
            } catch (IOException ex) {
                displayError("Error loading commands: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            displayInfo("Command load cancelled.");
        }
    }

    // Method to get the current drawing as a BufferedImage
     @Override
     public BufferedImage getBufferedImage() {
         // Create a BufferedImage the size of the panel
         BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
         // Get a Graphics2D context for the image
         Graphics2D g2d = image.createGraphics();
         // Ask the panel to paint its content onto the image's graphics context
         this.paint(g2d);
         // Dispose the graphics context
         g2d.dispose();
         // Return the resulting image
         return image;
     }

     // *** ADDED: Override paintComponent to draw the loaded image first ***
    @Override
    public void paintComponent(Graphics g) {
        // Call the superclass's paintComponent first to handle background color and initial setup
        super.paintComponent(g);

        // Draw the loaded image if it exists
        if (loadedImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            // Draw the loaded image scaled to fit the panel size
            g2d.drawImage(loadedImage, 0, 0, this.getWidth(), this.getHeight(), null);
            // The superclass will then draw the turtle and its path on top of this background image.
        }

        // Note: The superclass's paintComponent will handle drawing the turtle and its path
        // after this method returns.
    }

    // *** MODIFIED: Override clear() to also clear the loaded image ***
    @Override
    public void clear() {
        super.clear();
        // Also clear the loaded image
        loadedImage = null;
        unsavedChanges = false; // Clearing means no unsaved changes
        repaint(); // Request a repaint to show the cleared state
    }

    // *** MODIFIED: resetTurtleState() now relies on clear() to remove the image ***
    // The reset() method from LBUGraphics calls clear(), so the loaded image will be cleared
    // when resetTurtleState() calls reset().
    // No direct change needed in resetTurtleState() itself for image clearing.
}
