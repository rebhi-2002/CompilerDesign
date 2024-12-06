package projectcompiler;

import javax.swing.*; //import javax.swing.JOptionPane; //import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.attribute.*;
import java.nio.file.*;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Locale;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import projectcompiler.TokenAnalyzer.Token;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Rebhe Ibrahim
 */

import projectcompiler.FileHandler;

public class UIHandler {
    private ArrayList<File> openedFiles = new ArrayList<>(); // قائمة لتخزين الملفات المفتوحة
    private List<File> openFiles = new ArrayList<>();  // تعريف قائمة الملفات المفتوحة

    // private List<File> openedFiles = new ArrayList<>(); // قائمة لتخزين الملفات المفتوحة

    private final FileHandler fileHandler = new FileHandler();
    private final TokenAnalyzer tokenAnalyzer = new TokenAnalyzer();

    private final DefaultTableModel tableModel = new DefaultTableModel(); // نموذج الجدول
    private File lastOpenedFile; // متغير لتخزين الملف المفتوح الأخير

    // Define currentContent at the class level if not done already
    private String currentContent = ""; // private String currentContent = null;

    // Define ID counter at the class level
    private int idCounter = 1; // Starts counting from 1

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private JFrame frame;

    // private String previousContent = ""; // To store the previous content of the file
    // Map to store the content of files previously analyzed, identified by file paths
    private Map<String, String> previousContentMap = new HashMap<>();

    private Map<File, String> previousContents = new HashMap<>();
    private Map<File, FileTime> analyzedTimes = new HashMap<>();

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Lexical Analyzer"); // Multi File Open Example
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); // frame.setSize(700, 500);
        frame.setResizable(false);

        // Main panel with a (modern background color) / (minimalist design) | إنشاء اللوحة الرئيسية
        JPanel mainPanel = new JPanel(new BorderLayout());
        // new Color(230, 230, 250); خلفية فاتحة || new Color(245, 245, 245); Light gray background
        mainPanel.setBackground(new Color(240, 248, 255)); // Light blue background
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around main panel

        // Title label with modern font
        JLabel titleLabel = new JLabel("Lexical Analyzer - Token Classification", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Font 18
        titleLabel.setForeground(new Color(0, 51, 102)); // Darker blue // new Color(70, 130, 180)
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"ID", "Name", "Type", "Line No"}; // String[] columns = {"Element", "Type", "Line"};
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Type", "Line No"}, 0); //  DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30); // table.setRowHeight(25);

        // Table header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 16)); // header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(0, 102, 204)); // Blue // header.setBackground(new Color(100, 149, 237));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        // Alternate row colors for the table
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 250)); // Alternate row colors
                }
                return cell;
            }
        });

        // Scroll pane for the table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding for scroll pane
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel for buttons using GridBagLayout
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        // buttonPanel.setBackground(new Color(230, 230, 250));
        buttonPanel.setBackground(Color.WHITE); // Same as main panel

        // Create buttons: Button to open file
        JButton openButton = createButton("Open File", "open_icon.png");
        JButton analyzeButton = createButton("Analyze Tokens", "analyze_icon.png");
        JButton resetButton = createButton("Reset Table", "reset_icon.png");
        JButton saveButton = createButton("Save Results", "save_icon.png");

        // إنشاء زر إعادة التحميل
        JButton refreshButton = createButton("Refresh", "refresh_icon.png");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 16));
        refreshButton.setBackground(new Color(0, 102, 204)); // اللون الأزرق
        refreshButton.setForeground(Color.WHITE);

        // إعداد التخطيط
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // مساحة padding حول الأزرار
        gbc.fill = GridBagConstraints.HORIZONTAL; // تمديد الأزرار أفقياً
        gbc.weightx = 1.0; // توزيع متساوٍ للمساحة الأفقية
       /*
        // الصف الأول
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(openButton, gbc);
        gbc.gridx = 1;
        buttonPanel.add(analyzeButton, gbc);
        // gbc.gridx = 2;
        // buttonPanel.add(resetButton, gbc);
        // الانتقال إلى الصف الثاني
        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonPanel.add(saveButton, gbc);
        gbc.gridx = 1;
        buttonPanel.add(refreshButton, gbc);
       */
        // الصف الأول - ثلاثة أزرار
        gbc.gridy = 0;
        gbc.gridx = 0;
        buttonPanel.add(openButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(analyzeButton, gbc);

        gbc.gridx = 2;
        buttonPanel.add(refreshButton, gbc);

        // الصف الثاني - زر واحد يأخذ كامل المساحة الأفقية
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3; // يأخذ كامل عرض الثلاثة أعمدة
        buttonPanel.add(saveButton, gbc);


        // إضافة buttonPanel إلى أسفل mainPanel
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        // frame.getContentPane().add(tabbedPane, BorderLayout.CENTER); // إضافة JTabbedPane إلى الإطار
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        // Add main panel to frame
        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // IMPORTANT !!!
        // ** The previous code for adding an actionListener to the button, designed to allow opening only a single file at the bottom of the file.
    
        openFile(); // Call the openFile method
    
        openButton.addActionListener(e -> openFiles());
        analyzeButton.addActionListener(e -> analyzeFiles());
        // resetButton.addActionListener(e -> resetTable());
        saveButton.addActionListener(e -> SaveButton());
        refreshButton.addActionListener(e -> refreshContent());
    }

    // Helper method to create styled buttons with optional icons
    private JButton createButton(String text, String iconPath) {
        JButton button = new JButton(text, new ImageIcon(iconPath));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(0, 102, 204)); // Blue
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding for the button
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40)); // Set a fixed size

        // Button hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 153, 255)); // Lighter blue
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 102, 204)); // Original blue
            }
        });

        return button;
    }

    /* Start Open Button */
    // private void openFiles() {
    //     JFileChooser fileChooser = new JFileChooser("resources");
    //     fileChooser.setMultiSelectionEnabled(true); // Enable multi-selection
    //     int result = fileChooser.showOpenDialog(frame);
    //     if (result == JFileChooser.APPROVE_OPTION) {
    //         File[] files = fileChooser.getSelectedFiles(); // Get selected files
    //         System.out.println("Open dialog completed with files count: " + files.length);

    //         for (File file : files) {
    //             JOptionPane.showMessageDialog(frame, "Selected file: " + file.getAbsolutePath());
    //             System.out.println("Selected file: " + file.getAbsolutePath());
    //             System.out.println("File added: " + file.getName());

    //             // Check if file already has a tab (by title)
    //             boolean hasVisibleTab = IntStream.range(0, tabbedPane.getTabCount())
    //                     .anyMatch(i -> tabbedPane.getTitleAt(i).equals(file.getName()));
                
    //             // If file is not already opened, check its size and add it
    //             if (!hasVisibleTab) {
    //                 // Remove file from openedFiles if it is not displayed in any tab
    //                 openedFiles.removeIf(f -> f.getAbsolutePath().equals(file.getAbsolutePath())); // Clean up if file is no longer open
                    
    //                 // Check if file size is within limit
    //                 if (file.length() > FileHandler.MAX_FILE_SIZE) {
    //                     JOptionPane.showMessageDialog(frame, "File is too large to open: " + file.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
    //                     continue;  // Skip this file if it's too large
    //                 }

    //                 // Check if the file has already been opened after cleaning up
    //                 boolean isFileAlreadyOpened = openedFiles.stream()
    //                         .anyMatch(f -> f.getAbsolutePath().equals(file.getAbsolutePath()));
    //                 if (!isFileAlreadyOpened) { // Ensures the file is not already in the opened files list 
    //                     String content = fileHandler.getContents(file); // Get file content
    //                     System.out.println("Opened files after selection: " + openedFiles);
    //                     System.out.println("File added to opened files: " + file.getAbsolutePath());
    //                     System.out.println("Updated opened files list: " + openedFiles);

    //                     openedFiles.add(file); // Add to list of opened files

    //                     // Check if file content is valid and non-empty
    //                     if (content != null && !content.isEmpty()) {
    //                         List<Token> tokens = tokenAnalyzer.analyze(content); // Analyze the content

    //                         // Create a new table model for each file
    //                         DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Type", "Line No"}, 0);
    //                         JTable table = new JTable(tableModel);

    //                         // Add data to the table
    //                         idCounter = 1; // Reset ID counter
    //                         for (Token token : tokens) {
    //                             // String[] newRow = new String[4]; // 4 fields: ID, Name, Type, Line No
    //                             // newRow[0] = String.valueOf(idCounter++);  // Assign ID and increment counter
    //                             // // Assuming Token has methods to get the properties
    //                             // newRow[1] = token.getValue(); // Get the Name of the Token
    //                             // newRow[2] = token.getType(); // Get the Type of the Token
    //                             // newRow[3] = String.valueOf(token.getLineNumber()); // Get Line Number of the Token
    //                             String[] newRow = {
    //                                 String.valueOf(idCounter++),  // Assign ID and increment counter
    //                                 token.getValue(),             // Get the Name of the Token
    //                                 token.getType(),              // Get the Type of the Token
    //                                 String.valueOf(token.getLineNumber()) // Get Line Number of the Token
    //                             };
    //                             tableModel.addRow(newRow); // Add the new row to the table
    //                         }

    //                         // Add the table to a new tab
    //                         JScrollPane scrollPane = new JScrollPane(table);
    //                         tabbedPane.addTab(file.getName(), scrollPane); // Add a new tab
    //                     } else {
    //                         // Show warning if file is empty or cannot be read
    //                         JOptionPane.showMessageDialog(frame, "File is empty or couldn't be read: " + file.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
    //                     }
    //                 } else {
    //                     // Notify user if file is already open
    //                     JOptionPane.showMessageDialog(frame, "File already opened: " + file.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
    //                 }
    //             }
    //         }
    //     }
    // }

    /*
    * The problem was that if an unreadable or empty file was opened, its entry was still added to the openedFiles list. This caused issues because the analyzeFiles() method would later access the data of the previous valid file instead of correctly detecting the unreadable file as empty and moving on to analyze the next file.

    * Solution Explanation
        - To resolve this, the code was modified to only add the file to openedFiles if it has valid, readable content. This ensures that:
        1. Unreadable or empty files are skipped without leaving an entry in openedFiles.
        2. Analysis of the next file proceeds without any leftover data from a previously unreadable file, avoiding unexpected behavior.
    
    You can add a note like this in your code for clarity:
    * Note: Added a condition to only add the file to openedFiles if it has readable, non-empty content.
    * This prevents data from a previously unreadable file from interfering with the analysis of the next file.
    */

    // Method to open a single file and add it to the tabbedPane if not already opened
    private void openFile() {
        // Set the default file you want to open automatically
        File defaultFile = new File("resources/p.txt");

        // Check if the file exists
        if (!defaultFile.exists()) {
            // Show a warning if the file does not exist
            JOptionPane.showMessageDialog(frame, "File does not exist: " + defaultFile.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Log the file path (for debugging)
        System.out.println("Opening file: " + defaultFile.getAbsolutePath());

        // Check if the file is already opened (by its name) in any tab
        boolean hasVisibleTab = IntStream.range(0, tabbedPane.getTabCount())
                .anyMatch(i -> tabbedPane.getTitleAt(i).equals(defaultFile.getName()));

        // If the file is not already open
        if (hasVisibleTab) {
            // Show a warning if the file is already open
            JOptionPane.showMessageDialog(frame, "File already opened: " + defaultFile.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if file size is within the limit
        if (defaultFile.length() > FileHandler.MAX_FILE_SIZE) {
            JOptionPane.showMessageDialog(frame, "File is too large to open: " + defaultFile.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Remove the file from openedFiles list if it was previously opened
        openedFiles.removeIf(f -> f.getAbsolutePath().equals(defaultFile.getAbsolutePath()));

        // Get the content of the file
        String content = fileHandler.getContents(defaultFile);

        // If content is valid, proceed with token analysis
        if (content == null || content.isEmpty()) {
            // Show a warning if the file is empty or unreadable
            JOptionPane.showMessageDialog(frame, "File is empty or couldn't be read: " + defaultFile.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Add the file to openedFiles list since the content is valid
        openedFiles.add(defaultFile);

        // Analyze content using TokenAnalyzer
        List<Token> tokens = tokenAnalyzer.analyze(content);

        // Set up the table columns and model
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Type", "Line No"}, 0);
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14)); // Set table font
        table.setRowHeight(30); // Set row height for better visibility

        // Use AtomicInteger to safely update the ID counter inside the lambda expression
        AtomicInteger idCounter = new AtomicInteger(1);

        // Add rows to the table for each token
        tokens.forEach(token -> {
            String[] newRow = {
                String.valueOf(idCounter.getAndIncrement()),
                token.getValue(),
                token.getType(),
                String.valueOf(token.getLineNumber())
            };
            tableModel.addRow(newRow);
        });

        // Add the new tab to the tabbedPane with the file name
        JScrollPane scrollPane = new JScrollPane(table);
        tabbedPane.addTab(defaultFile.getName(), scrollPane);
    }
    private void openFiles() {
        JFileChooser fileChooser = new JFileChooser("resources");
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            System.out.println("Open dialog completed with files count: " + files.length);

            for (File file : files) {
                JOptionPane.showMessageDialog(frame, "Selected file: " + file.getAbsolutePath());
                System.out.println("Selected file: " + file.getAbsolutePath());

                boolean hasVisibleTab = IntStream.range(0, tabbedPane.getTabCount())
                    .anyMatch(i -> tabbedPane.getTitleAt(i).equals(file.getName()));

                if (!hasVisibleTab) {
                    if (file.length() > FileHandler.MAX_FILE_SIZE) {
                        JOptionPane.showMessageDialog(frame, "File is too large to open: " + file.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
                        continue;
                    }

                    openedFiles.removeIf(f -> f.getAbsolutePath().equals(file.getAbsolutePath()));
                    String content = fileHandler.getContents(file);

                    if (content != null && !content.isEmpty()) {
                        openedFiles.add(file); // Only add if content is valid
                        List<Token> tokens = tokenAnalyzer.analyze(content);

                        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Type", "Line No"}, 0);
                        JTable table = new JTable(tableModel);
                        idCounter = 1;

                        for (Token token : tokens) {
                            String[] newRow = {
                                String.valueOf(idCounter++),
                                token.getValue(),
                                token.getType(),
                                String.valueOf(token.getLineNumber())
                            };
                            tableModel.addRow(newRow);
                        }

                        JScrollPane scrollPane = new JScrollPane(table);
                        tabbedPane.addTab(file.getName(), scrollPane);
                    } else {
                        JOptionPane.showMessageDialog(frame, "File is empty or couldn't be read: " + file.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "File already opened: " + file.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }
    /* End Open Button */

    /* Start Analyze Button */

    /* Analyze Button By Old Way */
    // private void analyzeFiles() {
    //     int selectedIndex = tabbedPane.getSelectedIndex();
    //     if (selectedIndex == -1) {
    //         JOptionPane.showMessageDialog(frame, "No tab selected.", "Warning", JOptionPane.WARNING_MESSAGE);
    //         return;
    //     }
    //     File selectedFile = openedFiles.get(selectedIndex);
    //     if (selectedFile == null) {
    //         JOptionPane.showMessageDialog(frame, "File not found.", "Warning", JOptionPane.WARNING_MESSAGE);
    //         return;
    //     }
    //     // Analyze the selected file
    //     String fileContent = fileHandler.getContents(selectedFile);
    //     if (fileContent == null || fileContent.isEmpty()) {
    //         JOptionPane.showMessageDialog(frame, "The file is empty or could not be read.", "Warning", JOptionPane.WARNING_MESSAGE);
    //         return;
    //     }
    //     List<Token> tokens = tokenAnalyzer.analyze(fileContent);
    //     HashMap<String, Integer> typeCount = new HashMap<>();
    //     for (Token token : tokens) {
    //         String type = token.getType();
    //         typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
    //     }
    //     // Include the file name in the analysis results
    //     StringBuilder analysisResult = new StringBuilder("Analysis Results:\n\n");
    //         analysisResult.append("File Name: ").append(selectedFile.getName()).append("\n\n");
    //     for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
    //         analysisResult.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
    //     }
    //     analysisResult.append("\nTotal Tokens: ").append(tokens.size());
    //     JOptionPane.showMessageDialog(frame, analysisResult.toString(), "Analysis Results", JOptionPane.INFORMATION_MESSAGE);
    // }
    /* End */
    /* Analyze Button NEW WAY !!!! */
    // private void analyzeFiles() {
    //     // Get the selected tab index from the tabbed pane
    //     int selectedIndex = tabbedPane.getSelectedIndex();

    //     // If no tab is selected, show a warning message
    //     if (selectedIndex == -1) {
    //         JOptionPane.showMessageDialog(frame, "No tab selected.", "Warning", JOptionPane.WARNING_MESSAGE);
    //         return;
    //     }

    //     // Get the selected file based on the tab index
    //     File selectedFile = openedFiles.get(selectedIndex);

    //     // If the file is null (not found), show a warning message
    //     if (selectedFile == null) {
    //         JOptionPane.showMessageDialog(frame, "File not found.", "Warning", JOptionPane.WARNING_MESSAGE);
    //         return;
    //     }

    //     // Get the content of the selected file
    //     String fileContent = fileHandler.getContents(selectedFile);

    //     // If the file is empty or could not be read, show a warning message
    //     if (fileContent == null || fileContent.isEmpty()) {
    //         JOptionPane.showMessageDialog(frame, "The file is empty or could not be read.", "Warning", JOptionPane.WARNING_MESSAGE);
    //         return;
    //     }

    //     // Analyze the file content using the tokenAnalyzer
    //     List<Token> tokens = tokenAnalyzer.analyze(fileContent);

    //     // Create a map to count token types
    //     HashMap<String, Integer> typeCount = new HashMap<>();
    //     for (Token token : tokens) {
    //         String type = token.getType();
    //         // Update the count of each token type
    //         typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
    //     }

    //     // Get the total number of tokens
    //     int totalTokens = tokens.size();

    //     // Define column names for the table
    //     String[] columnNames = {"Token Type", "Count", "Percentage"};

    //     // Create a 2D array to hold the table data
    //     Object[][] data = new Object[typeCount.size()][3];
    //     int rowIndex = 0;
    //     for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
    //         String type = entry.getKey();
    //         int count = entry.getValue();
    //         // Calculate the percentage of each token type
    //         double percentage = (count / (double) totalTokens) * 100;

    //         // Populate the table data
    //         data[rowIndex][0] = type;
    //         data[rowIndex][1] = count;
    //         data[rowIndex][2] = String.format("%.2f%%", percentage);
    //         rowIndex++;
    //     }

    //     // Create a JTable to display the token analysis data
    //     JTable analysisTable = new JTable(data, columnNames);
    //     analysisTable.setFillsViewportHeight(true);
    //     analysisTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    //     analysisTable.setFont(new Font("Arial", Font.PLAIN, 14));
    //     analysisTable.setRowHeight(25);

    //     // Customize the table rows with hover effect for better interactivity
    //     analysisTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
    //         @Override
    //         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    //             Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    //             // Set alternate row colors for better readability
    //             if (row % 2 == 0) {
    //                 c.setBackground(new Color(245, 245, 245)); // Light gray for even rows
    //             } else {
    //                 c.setBackground(Color.white); // White for odd rows
    //             }
    //             return c;
    //         }
    //     });

    //     // Create a JScrollPane to make the table scrollable
    //     JScrollPane scrollPane = new JScrollPane(analysisTable);
    //     scrollPane.setPreferredSize(new Dimension(450, 350));

    //     // Create a panel to hold the table and additional information
    //     JPanel panel = new JPanel(new BorderLayout());
    //     // panel.add(scrollPane, BorderLayout.CENTER);

    //     // Create a label to show the total number of tokens
    //     // JLabel totalTokensLabel = new JLabel("<html><br>Total Tokens: " + totalTokens + "<br></html>");
    //     JLabel totalTokensLabel = new JLabel("Total Tokens: " + totalTokens);
    //     totalTokensLabel.setFont(new Font("Arial", Font.BOLD, 16));
    //     totalTokensLabel.setHorizontalAlignment(SwingConstants.CENTER);
    //     totalTokensLabel.setForeground(new Color(0, 123, 255)); // Blue color to make the label stand out
    //     // panel.add(totalTokensLabel, BorderLayout.SOUTH);

    //     // Create a JPanel to wrap the JLabel
    //     JPanel labelPanel = new JPanel();
    //     labelPanel.setBackground(Color.WHITE);  // Optional: set background color for the panel
    //     // Add red border to the panel around the label
    //     labelPanel.setBorder(new LineBorder(Color.RED, 2));  // Red border with thickness of 2px
    //     labelPanel.add(totalTokensLabel);  // Add the label to the panel
    //     // Now, add the labelPanel to your main panel or frame
    //     // panel.add(labelPanel, BorderLayout.SOUTH);

    //     // Create a new JPanel for buttons and add the save button there
    //     JPanel buttonPanel = new JPanel();
    //     buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Optional: for centering the button

    //     // Add "Save" button to the button panel
    //     JButton saveButton = new JButton("Save Analysis");
    //     // Modern button styling
    //     saveButton.setBackground(new Color(0, 123, 255));  // Blue background
    //     saveButton.setForeground(Color.WHITE);  // White text color
    //     saveButton.setFont(new Font("Arial", Font.BOLD, 16));  // Font style and size
    //     // Rounded corners with a gradient effect
    //     saveButton.setBorder(new LineBorder(new Color(0, 123, 255), 2, true));  // Rounded border
    //     saveButton.setFocusPainted(false);  // Remove focus outline
    //     // Set the size of the button
    //     saveButton.setPreferredSize(new Dimension(200, 50));  // Width: 200, Height: 50
    //     // Add hover effect (change background color on mouse hover)
    //     saveButton.addMouseListener(new MouseAdapter() {
    //         @Override
    //         public void mouseEntered(MouseEvent e) {
    //             saveButton.setBackground(new Color(0, 102, 204));  // Darker blue on hover
    //         }

    //         @Override
    //         public void mouseExited(MouseEvent e) {
    //             saveButton.setBackground(new Color(0, 123, 255));  // Reset to original blue
    //         }

    //         @Override
    //         public void mousePressed(MouseEvent e) {
    //             // Slight darker effect when clicked
    //             saveButton.setBackground(new Color(0, 90, 180));
    //         }

    //         @Override
    //         public void mouseReleased(MouseEvent e) {
    //             // Return to hover color once released
    //             saveButton.setBackground(new Color(0, 102, 204));
    //         }
    //     });

    //     saveButton.addActionListener(e -> saveFiles()); // Trigger saveFiles method when clicked
    //     buttonPanel.add(saveButton); // Add the save button to the button panel

    //     // Now, add the labelPanel to the panel in the CENTER position
    //     // And add the buttonPanel to the panel in the SOUTH position
    //     panel.setLayout(new BorderLayout());
    //     panel.add(scrollPane, BorderLayout.NORTH);
    //     panel.add(labelPanel, BorderLayout.CENTER);  // Add labelPanel at the center
    //     panel.add(buttonPanel, BorderLayout.SOUTH); // Add buttonPanel at the bottom

    //     // Customize the "OK" button for JOptionPane
    //     UIManager.put("OptionPane.okButtonText", "OK");
    //     UIManager.put("Button.background", new Color(0, 123, 255));  // Blue background
    //     UIManager.put("Button.foreground", Color.WHITE);  // White text color
    //     UIManager.put("Button.font", new Font("Arial", Font.BOLD, 16));  // Font style and size
    //     UIManager.put("Button.focus", Color.BLUE);  // Focus color
    //     // Adding custom UI for button hover and pressed effect
    //     UIManager.put("Button.rollover", true);  // Enable hover effect
    //     UIManager.put("Button.rolloverBackground", new Color(0, 102, 204)); // Dark blue on hover
    //     UIManager.put("Button.pressedBackground", new Color(0, 85, 170));  // Darker blue when pressed
    //     UIManager.put("Button.border", new LineBorder(new Color(0, 123, 255), 2, true));  // Rounded border
    //     // Customize padding for the button
    //     UIManager.put("Button.margin", new Insets(20, 30, 20, 30));  // Increase button padding
    //     // Show the results in a custom JOptionPane dialog with the customized "OK" button
    //     JOptionPane.showMessageDialog(
    //             frame,
    //             panel,
    //             "Analysis Results for " + selectedFile.getName(),
    //             JOptionPane.INFORMATION_MESSAGE
    //     );

    // }
    /* End */

    // FileAnalyzer components initialization
    private void analyzeFiles() {

        // Ensure a tab is selected
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(frame, "No tab selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        // Clear previous content and file reference
        String fileContent = ""; // لتفريغ المحتوى السابق
        File selectedFile = null; // لإعادة ضبط مرجع الملف السابق

        // Validate that the selected tab corresponds to an opened file
        if (selectedIndex >= openedFiles.size()) {
            JOptionPane.showMessageDialog(frame, "Mismatch between tabs and opened files.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

         // Get the selected file
        selectedFile = openedFiles.get(selectedIndex);
        if (selectedFile == null || selectedFile.length() > FileHandler.MAX_FILE_SIZE) {  // Add MAX_FILE_SIZE as your file size limit
            JOptionPane.showMessageDialog(frame, "The file is too large to be opened.", "Warning", JOptionPane.WARNING_MESSAGE);

            // Clear any stale data and exit
            fileContent = "";
            tableModel.setRowCount(0);  // Clear any old data from the table model
            openedFiles.set(selectedIndex, null);  // Mark this file as inaccessible
            return;
        }

        // Fetch the selected file and content | Load and analyze the file
        selectedFile = openedFiles.get(selectedIndex);
        // Proceed with the analysis only if fileContent is successfully loaded
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(frame, "The selected file is not accessible.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // // File selectedFile = openedFiles.get(selectedIndex);
        // if (selectedFile == null) {
        //     JOptionPane.showMessageDialog(frame, "File not found.", "Warning", JOptionPane.WARNING_MESSAGE);
        //     return;
        // }

        // Analyze the selected file | Fetch and analyze the content of the selected file
        fileContent = fileHandler.getContents(selectedFile);
        if (fileContent == null || fileContent.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "The file is empty or could not be read.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Proceed with token analysis
        List<Token> tokens = tokenAnalyzer.analyze(fileContent);
        if (tokens == null || tokens.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No tokens found in the file.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        HashMap<String, Integer> typeCount = new HashMap<>();
        for (Token token : tokens) {
            String type = token.getType();
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }

        // Include the file name in the analysis results
        StringBuilder analysisResult = new StringBuilder("<b>File Name: </b>").append(selectedFile.getName()).append("\n");
        analysisResult.append("<b>Analysis Results:</b>\n\n");

        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            analysisResult.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        analysisResult.append("\n<b>Total Tokens:</b> ").append(tokens.size());

        // Create table for displaying token analysis
        String[] columnNames = {"Type", "Count", "Percentage"};
        Object[][] data = new Object[typeCount.size()][3];
        int totalTokens = tokens.size();
        int row = 0;
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            double percentage = (count * 100.0) / totalTokens;

            data[row][0] = type;
            data[row][1] = count;
            data[row][2] = String.format("%.2f%%", percentage);
            row++;
        }

        // Create the token analysis table with enhanced styling
        JTable tokenTable = new JTable(data, columnNames);
        tokenTable.setRowHeight(35); // Increase row height for better visual appeal
        tokenTable.setFont(new Font("Arial", Font.PLAIN, 14)); // Modern font for better readability
        tokenTable.setGridColor(new Color(220, 220, 220)); // Light grid lines for a cleaner look
        tokenTable.setSelectionBackground(new Color(72, 133, 255)); // Blue background when selecting row
        tokenTable.setSelectionForeground(new Color(255, 255, 255)); // White text when selected
        tokenTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Resize columns dynamically

        // Adding hover effects on rows
        tokenTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tokenTable.rowAtPoint(e.getPoint());
                tokenTable.setRowSelectionInterval(row, row);
            }
        });

        // Adding hover effects on cells
        tokenTable.setIntercellSpacing(new Dimension(5, 5)); // Cell spacing for a more spacious look
        tokenTable.setBackground(new Color(255, 255, 255)); // White background for the table

        // Add horizontal and vertical scroll bars with customized style
        JScrollPane scrollPane = new JScrollPane(tokenTable);
        scrollPane.setPreferredSize(new Dimension(600, 150)); // Adjust the size of the scrollable area
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Create a blurred background panel (or a transparent panel effect)
        JPanel resultPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(54, 215, 183); // Light green gradient
                Color color2 = new Color(72, 133, 255); // Blue gradient
                GradientPaint gradient = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Create the result panel with layout | Use GridBagLayout for vertical stacking
        resultPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Create the result label with left alignment styling
        JLabel resultLabel = new JLabel("<html><div style='font-family: Arial, sans-serif; font-size: 16px; color: white; text-align: left;'>" + analysisResult.toString().replace("\n", "<br>") + "</div></html>");
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Use modern font
        resultLabel.setForeground(new Color(255, 255, 255)); // White text color for better contrast

        // Add resultLabel to the panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across the width of the grid
        resultPanel.add(resultLabel, gbc);
        
        // Add table to the panel
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        resultPanel.add(scrollPane, gbc);

        // Create button panel with Reset and Save buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for button arrangement
        GridBagConstraints gbcButtons = new GridBagConstraints();
        gbcButtons.fill = GridBagConstraints.HORIZONTAL;
        gbcButtons.insets = new Insets(5, 5, 5, 5);

        // Create Reset Button
        JButton resetButton = new JButton("Reset");
        resetButton.setBackground(new Color(255, 102, 102)); // Red color for Reset
        resetButton.setForeground(Color.WHITE);
        resetButton.setPreferredSize(new Dimension(100, 40));
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);
        buttonPanel.add(resetButton, gbcButtons);

        // Create Save Button
        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(72, 133, 255)); // Blue color for Save
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        gbcButtons.gridx = 1;
        buttonPanel.add(saveButton, gbcButtons);

        // Add buttonPanel to the resultPanel
        gbc.gridy = 2;
        resultPanel.add(buttonPanel, gbc);

        // Save functionality
        // saveButton.addActionListener(new ActionListener() {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         if (openedFiles == null || openedFiles.isEmpty()) {
        //             JOptionPane.showMessageDialog(frame, "No opened files.", "Warning", JOptionPane.WARNING_MESSAGE);
        //             return;
        //         }
                
        //         // Iterate over each opened file and save its analysis results
        //         for (File file : openedFiles) {
        //             if (file != null) {
        //                 String fileContent2 = fileHandler.getContents(file);
        //                 if (fileContent2 != null && !fileContent2.isEmpty()) {
        //                     List<Token> tokens = tokenAnalyzer.analyze(fileContent2);
        //                     HashMap<String, Integer> typeCount = new HashMap<>();
        //                     for (Token token : tokens) {
        //                         String type = token.getType();
        //                         typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        //                     }
                            
        //                     // Ask the user to select a file format and save
        //                     JFileChooser fileChooser = new JFileChooser();
        //                     fileChooser.setDialogTitle("Save Analysis for " + file.getName());
                            
        //                     // Add file filters for TXT and TSV formats
        //                     fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
        //                     fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TSV File (*.tsv)", "tsv"));
                            
        //                     int result = fileChooser.showSaveDialog(frame);
        //                     if (result == JFileChooser.APPROVE_OPTION) {
        //                         File selectedFile = fileChooser.getSelectedFile();
                                
        //                         String extension = getFileExtension(selectedFile);
                                
        //                         if (extension.isEmpty()) {
        //                             if (fileChooser.getFileFilter().getDescription().contains("Text")) {
        //                                 selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
        //                             } else if (fileChooser.getFileFilter().getDescription().contains("TSV")) {
        //                                 selectedFile = new File(selectedFile.getAbsolutePath() + ".tsv");
        //                             }
        //                         }
                                
        //                         if (selectedFile.getName().endsWith(".txt") || selectedFile.getName().endsWith(".tsv")) {
        //                             try {
        //                                 if (selectedFile.getName().endsWith(".txt")) {
        //                                     saveAnalysisResultsToText(selectedFile, typeCount, tokens.size());
        //                                 } else if (selectedFile.getName().endsWith(".tsv")) {
        //                                     saveAnalysisResultsToTSV(selectedFile, typeCount, tokens.size());
        //                                 }
                                        
        //                                 JOptionPane.showMessageDialog(frame, "The file has been saved successfully.", "File Saved", JOptionPane.INFORMATION_MESSAGE);
        //                             } catch (IOException ex) {
        //                                 JOptionPane.showMessageDialog(frame, "There was an error saving the analysis for " + file.getName(), "Error", JOptionPane.ERROR_MESSAGE);
        //                             }
        //                         } else {
        //                             JOptionPane.showMessageDialog(frame, "Please select a valid file format (TXT or TSV) before saving.", "Invalid Format", JOptionPane.WARNING_MESSAGE);
        //                         }
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // });
        // Add ActionListener to the save button
        saveButton.addActionListener(e -> saveFiles());
        JDialog dialog = new JDialog(frame, "Analysis Results", true);
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // openedFiles.clear();
                // tabbedPane.removeAll();

                // Get the index of the currently selected tab
                int selectedIndex = tabbedPane.getSelectedIndex();
                
                // التحقق من وجود تبويبات مفتوحة
                if (tabbedPane.getTabCount() == 0) {
                    JOptionPane.showMessageDialog(frame, "No tabs available.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;  // إيقاف العملية إذا لا توجد تبويبات
                }

                // Check if a tab is selected. If not, show a warning message.
                if (selectedIndex == -1) {
                    JOptionPane.showMessageDialog(frame, "No tab selected.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;  // Exit the method if no tab is selected
                }

                // التحقق من تطابق فهرس selectedIndex مع openedFiles
                if (selectedIndex >= openedFiles.size()) {
                    JOptionPane.showMessageDialog(frame, "Index out of range.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;  // إيقاف العملية إذا الفهرس غير صالح
                }

                // Clear the table data in the currently selected tab
                tableModel.setRowCount(0);  // Remove all rows from the table in the selected tab

                // Reset the result label (e.g., "Total Tokens: 0")
                resultLabel.setText("Total Tokens: 0");  // Set the label to the default value

                // إزالة الملف من القائمة المرتبطة بـ openedFiles
                openedFiles.remove(selectedIndex);  // إزالة الملف من القائمة المرتبطة بالتبويب المحدد
                // إزالة التبويب من tabbedPane
                tabbedPane.removeTabAt(selectedIndex);  // إزالة التبويب الحالي

                // // Remove the reference to the opened file from the list of opened files
                // openedFiles.set(selectedIndex, null);  // Set the selected tab's corresponding file to null in the openedFiles list

                // // Remove the reference to the opened file from the list
                // if (selectedIndex < openedFiles.size()) {
                //     openedFiles.set(selectedIndex, null);  // Set the selected tab's corresponding file to null in the openedFiles list
                // }

                // Disable the Save button since the data is reset and there's nothing to save
                saveButton.setEnabled(false);  // Disable the Save button for the currently reset tab

                // إعادة التحديد إلى التاب التالي إذا كان موجودًا، أو التاب السابق إذا تم الحذف في نهاية القائمة
                if (tabbedPane.getTabCount() > 0) {
                    int newIndex = selectedIndex >= tabbedPane.getTabCount() ? tabbedPane.getTabCount() - 1 : selectedIndex;
                    tabbedPane.setSelectedIndex(newIndex); // تعيين التاب التالي أو السابق بعد الحذف
                }
                // إغلاق نافذة التحليل (إذا كانت موجودة) بعد الضغط على Reset
                if (dialog != null && dialog.isVisible()) {
                    dialog.dispose();  // إغلاق النافذة إذا كانت مرئية
                    dialog.setVisible(false);
                }

                // Optional: If you have other buttons like Analyze, you can disable them as well
                // analyzeButton.setEnabled(false);  // Disable the Analyze button

                // If you want to return to the first tab after resetting, uncomment the following line:
                // tabbedPane.setSelectedIndex(0);  // This will select the first tab in the tabbedPane

                // // Optionally select the first tab after resetting
                // if (tabbedPane.getTabCount() > 0) {
                //     tabbedPane.setSelectedIndex(0);  // Select the first tab
                // } else {
                //     saveButton.setEnabled(false);  // Disable the Save button if no tabs are left
                // }
                // Optional: You can also add any additional logic to clear other components if necessary
                // For example, you might want to clear any other UI elements related to the file
            }
        });

        // Create the scrollable result panel
        JScrollPane scrollableResultPane = new JScrollPane(resultPanel);
        scrollableResultPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableResultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Create a dialog to show the result
        // JDialog dialog = new JDialog(frame, "Analysis Results", true);
        dialog.setSize(650, 450); // Set dialog size
        dialog.setLocationRelativeTo(frame); // Center the dialog
        dialog.add(scrollableResultPane); // Add the scrollable result panel to the dialog
        dialog.setVisible(true);
    }

    /*
    * This method (analyzeFiles) is part of another class (like FileAnalyzer), and it is being called here in this code.
    * The necessary variables such as JFrame, FileHandler, TokenAnalyzer, JTabbedPane, and the openedFiles list are initialized elsewhere.
    * After initializing these components, an instance of the FileAnalyzer class is created, and the analyzeFiles method is called to analyze the open files.
    * the FileAnalyzer class is created, and the analyzeFiles method is invoked to analyze the currently opened files.
    */
    // private void analyzeFiles() {
        //     // // Initialize the variables needed for FileAnalyzer here
        //     // JFrame frame = new JFrame(); // Assuming you have created a JFrame window | Initialize the main application frame (JFrame) where components will be displayed
        //     // FileHandler fileHandler = new FileHandler(); // If you have a FileHandler class for handling files | Initialize FileHandler to manage file-related operations, such as reading file contents
        //     // TokenAnalyzer tokenAnalyzer = new TokenAnalyzer(); // If you have a TokenAnalyzer class for analyzing tokens | Initialize TokenAnalyzer for analyzing the tokens within file contents
        //     // JTabbedPane tabbedPane = new JTabbedPane(); // Assuming you have a JTabbedPane in your interface | Initialize JTabbedPane to manage and display multiple open file tabs in the GUI
        //     // List<File> openedFiles = new ArrayList<>(); // Place the list of opened files here | List to keep track of files that are currently opened in the application

        //     // Create an instance of FileAnalyzer using the required variables | Create an instance of FileAnalyzer, passing in the components required for file analysis
        //     FileAnalyzer fileAnalyzer = new FileAnalyzer(frame, fileHandler, tokenAnalyzer, tabbedPane, openedFiles);

        //     // Call the analyzeFiles method to analyze the files | Invoke analyzeFiles to start analyzing the contents of the selected file
        //     fileAnalyzer.analyzeFiles();
    // }

    // /* ***************************************************
    // --> __Button Save In analyzeFiles__
    // Method to save files in TXT and Excel (CSV) formats
    // Save the currently selected tab at the start
    public void saveFiles() {
        System.out.println("Starting saveFiles method...");
        if (openedFiles == null || openedFiles.isEmpty()) {
            System.out.println("No opened files.");
            JOptionPane.showMessageDialog(frame, "File not found.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Save the current tab index so we can retain focus on it later
        int currentTabIndex = tabbedPane.getSelectedIndex();
        // Get the title of the currently selected tab to identify the file
        String selectedTabTitle = tabbedPane.getTitleAt(currentTabIndex);  // Get the title of the selected tab
        
        // Initialize a variable to store the current file associated with the selected tab
        File currentFile = null;
        // Iterate over all opened files and find the one associated with the selected tab
        for (File file : openedFiles) {
            // Compare file name with tab title to find the correct file
            if (file.getName().equals(selectedTabTitle)) {
                currentFile = file;
                break; // Break the loop once the correct file is found
            }
        }

        // // If no file is associated with the selected tab, show an error message
        // if (currentFile == null) {
        //     JOptionPane.showMessageDialog(frame, "No file associated with the selected tab.", "Error", JOptionPane.ERROR_MESSAGE);
        //     return;  // Exit the method if no associated file is found
        // }

        if (currentFile != null) {
            // Retrieve content of the current file
            String fileContent = fileHandler.getContents(currentFile);
            if (fileContent != null && !fileContent.isEmpty()) {
                // Perform lexical analysis of the file content
                List<Token> tokens = tokenAnalyzer.analyze(fileContent);
                HashMap<String, Integer> typeCount = new HashMap<>();
                // Count occurrences of each token type
                for (Token token : tokens) {
                    String type = token.getType();
                    typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
                }
                int totalTokens = tokens.size(); // Get total number of tokens

                // Ask the user to choose a file format (TXT or TSV) to save the analysis
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Analysis for " + currentFile.getName());  // Set the dialog title to the currentFile name
          
                // Add file filters for TXT and TSV formats to the file chooser
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TSV File (*.tsv)", "tsv"));

                // Show the file save dialog and check the user's choice
                int result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    // Get the selected file extension | Check file extension
                    String extension = getFileExtension(selectedFile);
                    System.out.println("File extension for saving: " + extension);

                    // If no extension is selected, add the extension based on the file filter
                    if (extension.isEmpty()) {
                        if (fileChooser.getFileFilter().getDescription().contains("Text")) {
                            selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
                        } else if (fileChooser.getFileFilter().getDescription().contains("TSV")) {
                            selectedFile = new File(selectedFile.getAbsolutePath() + ".tsv");
                        }
                    }

                    // Check if the file extension is either .txt or .tsv before proceeding with saving | If file extension is still empty, show a warning message
                    if (selectedFile.getName().endsWith(".txt") || selectedFile.getName().endsWith(".tsv")) {
                        try {
                            if (selectedFile.getName().endsWith(".txt")) {
                                saveAnalysisResultsToText(selectedFile, typeCount, totalTokens);
                            } else if (selectedFile.getName().endsWith(".tsv")) {
                                saveAnalysisResultsToTSV(selectedFile, typeCount, totalTokens);
                            }

                            // Show success message after saving the file
                            JOptionPane.showMessageDialog(frame, "The file has been saved successfully.", "File Saved", JOptionPane.INFORMATION_MESSAGE);

                            // Retain the current tab to ensure it remains visible | Retain the currently selected tab and refocus on it after saving
                            currentTabIndex = tabbedPane.getSelectedIndex(); // Save the currently selected tab (tab index)
                            tabbedPane.setSelectedIndex(currentTabIndex); // Refocus on the selected tab after saving

                            System.out.println("Saved file: " + selectedFile.getName());
                      
                            System.out.println("Tabs after save for " + currentFile.getName() + ":");
                            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                                System.out.println("Tab " + i + ": " + tabbedPane.getTitleAt(i));
                            }
                      
                        } catch (IOException e) {
                            // Show error message if there is an issue while saving the file
                            JOptionPane.showMessageDialog(frame, "There was an error saving the analysis for " + currentFile.getName(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        // Show error message if file extension was not selected correctly | If an invalid file extension was selected, show a warning message
                        JOptionPane.showMessageDialog(frame, "Please select a valid file format (TXT or TSV) before saving.", "Invalid Format", JOptionPane.WARNING_MESSAGE);
                    }
                }
                // Print file content for debugging purposes
                System.out.println("File content for " + currentFile.getName() + ": " + fileContent);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "No file associated with the selected tab.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        // Ensure the current tab remains visible after the save operation
        tabbedPane.setSelectedIndex(currentTabIndex);
        System.out.println("Opened files: " + openedFiles);
    }
    // Helper method to get file extension
    private String getFileExtension(File file) {
        String extension = "";
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex + 1).toLowerCase();
        }
        return extension;
    }
    // // Method to save analysis results to a text file (TXT format)
        // private void saveAnalysisResultsToText(File file, HashMap<String, Integer> typeCount, int totalTokens) throws IOException {
        //     try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        //         writer.write("Token Type,Count,Percentage\n");
        //         for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
        //             String type = entry.getKey();
        //             int count = entry.getValue();
        //             double percentage = (count / (double) totalTokens) * 100;
        //             writer.write(type + "," + count + "," + String.format(Locale.US, "%.2f%%", percentage) + "\n");
        //         }
        //     }
        // }
    // Method to save analysis results to a text file (TXT format) with a styled table
    private void saveAnalysisResultsToText(File file, HashMap<String, Integer> typeCount, int totalTokens) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Header line with table border
            writer.write("+----------------------+----------+---------------+\n");
            writer.write("| Token Type           |   Count   |  Percentage   |\n");
            writer.write("+----------------------+----------+---------------+\n");

            // Data rows with formatted alignment and borders
            for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
                String type = entry.getKey();
                int count = entry.getValue();
                double percentage = (count / (double) totalTokens) * 100;
                writer.write(String.format("| %-20s | %10d | %12s |\n", type, count, String.format(Locale.US, "%.2f%%", percentage)));
            }

            // Footer line for total tokens with centered alignment and final border
            writer.write("+----------------------+----------+---------------+\n");
            String totalTokensLine = String.format("|%-22s Total Tokens: %-10d |", "", totalTokens);
            writer.write(totalTokensLine + "\n");
            writer.write("+----------------------+----------+---------------+\n");
        }
    }
    // Method to save analysis results to a TSV file
    private void saveAnalysisResultsToTSV(File file, HashMap<String, Integer> typeCount, int totalTokens) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write the TSV header
            writer.write("Token Type\tCount\tPercentage\n");

            // Write the data
            for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
                String type = entry.getKey();
                int count = entry.getValue();
                double percentage = (count / (double) totalTokens) * 100;
                writer.write(type + "\t" + count + "\t" + String.format(Locale.US, "%.2f%%", percentage) + "\n");
            }

        }
    }
    /* End Analyze Button */

    /* Start Refresh Button */
    // دالة refreshContent لتحديث جميع الملفات المفتوحة
    // private void refreshContent() {
    //     int selectedIndex = tabbedPane.getSelectedIndex();
        
    //     if (selectedIndex != -1) {
    //         // Get the table from the currently selected tab
    //         JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(selectedIndex);
    //         JTable table = (JTable) scrollPane.getViewport().getView();
    //         DefaultTableModel model = (DefaultTableModel) table.getModel();

    //         // Clear all existing rows from the table
    //         model.setRowCount(0);

    //         // Get the file associated with the currently open tab
    //         File selectedFile = getFileFromTab(selectedIndex); // Function to get the file from the current tab
    //         if (selectedFile != null) {
    //             if (selectedFile.exists()) {
    //                 String newContent = fileHandler.getContents(selectedFile);

    //                 if (newContent != null && !newContent.isEmpty()) {
    //                     // If the content is new or modified
    //                     List<Token> tokens = tokenAnalyzer.analyze(newContent);

    //                     // Refill the table with the new data
    //                     idCounter = 1; // Reset ID counter
    //                     for (Token token : tokens) {
    //                         String[] newRow = new String[3]; // Modify based on your Token structure
    //                         newRow[0] = String.valueOf(idCounter++); // ID
    //                         newRow[1] = token.getValue(); // Token Value
    //                         newRow[2] = token.getType(); // Token Type
    //                         model.addRow(newRow); // Add new row to the table
    //                     }

    //                     JOptionPane.showMessageDialog(frame, "Content has been refreshed for file: " + selectedFile.getName(), "Refresh Successful", JOptionPane.INFORMATION_MESSAGE);
    //                 } else {
    //                     JOptionPane.showMessageDialog(frame, "File is empty or couldn't be read: " + selectedFile.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
    //                 }
    //             } else {
    //                 JOptionPane.showMessageDialog(frame, "The file associated with the current tab does not exist or may have been deleted: " + selectedFile.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
    //             }
    //         } else {
    //             JOptionPane.showMessageDialog(frame, "The file associated with the current tab is null.", "Error", JOptionPane.ERROR_MESSAGE);
    //         }
    //     } else {
    //         JOptionPane.showMessageDialog(frame, "No tab selected to refresh.", "Warning", JOptionPane.WARNING_MESSAGE);
    //     }
    // }
    private void refreshContent() {
        int selectedIndex = tabbedPane.getSelectedIndex();

        if (selectedIndex != -1) {
            // Get the table from the currently selected tab
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(selectedIndex);
            JTable table = (JTable) scrollPane.getViewport().getView();
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            // Clear all existing rows from the table
            model.setRowCount(0);

            // Get the file associated with the currently open tab
            File selectedFile = getFileFromTab(selectedIndex); // Function to get the file from the current tab
            if (selectedFile != null) {
                if (selectedFile.exists()) {
                    // Get the last modified time of the file
                    FileTime lastModifiedTime = getLastModifiedTime(selectedFile);

                    // Get the stored time of last analysis (you need to track this time)
                    FileTime lastAnalyzedTime = getLastAnalyzedTime(selectedFile); 

                    // Check if the file has been modified
                    if (lastModifiedTime.compareTo(lastAnalyzedTime) > 0) {
                        // If the file is modified, analyze the new content
                        String newContent = fileHandler.getContents(selectedFile);

                        if (newContent != null && !newContent.isEmpty()) {
                            List<Token> tokens = tokenAnalyzer.analyze(newContent);

                            // Refill the table with the new data
                            idCounter = 1; // Reset ID counter
                            for (Token token : tokens) {
                                String[] newRow = new String[3]; // Modify based on your Token structure
                                newRow[0] = String.valueOf(idCounter++); // ID
                                newRow[1] = token.getValue(); // Token Value
                                newRow[2] = token.getType(); // Token Type
                                model.addRow(newRow); // Add new row to the table
                            }

                            // Check for updates in content
                            // String updates = compareContent(previousContent, newContent);
                            String updates = compareContent(previousContentMap.get(selectedFile.getAbsolutePath()), newContent);
                        
                            // Show message indicating if there were updates and the details
                            if (!updates.isEmpty()) {
                                JOptionPane.showMessageDialog(frame, "File has been updated and content has been refreshed for: " + selectedFile.getName() +
                                        "\nLast update: " + lastModifiedTime + "\nUpdates: " + updates, "Refresh Successful", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(frame, "File has been updated, but no significant content changes detected: " + selectedFile.getName(), "Refresh Successful", JOptionPane.INFORMATION_MESSAGE);
                            }

                            // Update previousContent with the new content
                            // previousContent = newContent;
                            // Update the map with the new content for this file
                            previousContentMap.put(selectedFile.getAbsolutePath(), newContent);
                    
                        } else {
                            JOptionPane.showMessageDialog(frame, "File is empty or couldn't be read: " + selectedFile.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        // If file is not modified, notify the user
                        JOptionPane.showMessageDialog(frame, "No updates detected for file: " + selectedFile.getName() +
                                "\nLast update: " + lastModifiedTime, "No Update", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "The file associated with the current tab does not exist or may have been deleted: " + selectedFile.getName(), "Warning", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "The file associated with the current tab is null.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "No tab selected to refresh.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    // Function to compare the old and new content of the file and return the updates
    // private String compareContent(String oldContent, String newContent) {
    //     StringBuilder updates = new StringBuilder();

    //     // التحقق إذا كان oldContent فارغ أو null
    //     if (oldContent == null || oldContent.isEmpty()) {
    //         // عرض رسالة للمرة الأولى إذا لم يكن هناك محتوى سابق
    //         updates.append("=== No previous content found ===\n");
    //         updates.append("Displaying new content:\n");
    //         updates.append("==================================\n");
    //         updates.append(newContent).append("\n");
    //         updates.append("\n");
    //     } else {
    //         // عند وجود محتوى قديم، مقارنة التوكنات
    //         String[] oldTokens = oldContent.split("\\s+");
    //         String[] newTokens = newContent.split("\\s+");

    //         // عرض التوكنات الجديدة
    //         updates.append("=== New Tokens Found ===\n");
    //         boolean newTokensFound = false; // علم لتحديد إذا تم العثور على توكنات جديدة
    //         for (String newToken : newTokens) {
    //             if (!containsToken(oldTokens, newToken)) {
    //                 updates.append("New Token: ").append(newToken).append("\n");
    //                 newTokensFound = true;
    //             }
    //         }

    //         if (!newTokensFound) {
    //             updates.append("No new tokens found.\n");
    //         }

    //         updates.append("\n");

    //         // عرض التوكنات المحذوفة
    //         updates.append("=== Removed Tokens ===\n");
    //         boolean removedTokensFound = false; // علم لتحديد إذا تم العثور على توكنات محذوفة
    //         for (String oldToken : oldTokens) {
    //             if (!containsToken(newTokens, oldToken)) {
    //                 updates.append("Removed Token: ").append(oldToken).append("\n");
    //                 removedTokensFound = true;
    //             }
    //         }

    //         if (!removedTokensFound) {
    //             updates.append("No removed tokens found.\n");
    //         }

    //         updates.append("\n");
    //     }

    //     return updates.toString();
    // }
    private String compareContent(String oldContent, String newContent) {
        // Handle cases where oldContent or newContent might be null
        if (oldContent == null) {
            oldContent = "";  // or handle it differently if needed
        }
        if (newContent == null) {
            newContent = "";  // Default to empty string if new content is null
        }

        StringBuilder updates = new StringBuilder();

        String[] oldLines = oldContent.split("\\R");
        String[] newLines = newContent.split("\\R");

        // updates.append("\n==================================================\n");
        updates.append("\n\n🔍 **New Lines Found** 🔍\n");
        updates.append("==================================================\n");

        int oldIndex = 0;
        boolean newLinesFound = false;
        for (String newLine : newLines) {
            if (oldIndex < oldLines.length && oldLines[oldIndex].equals(newLine)) {
                oldIndex++;
            } else {
                updates.append("➕ **New Line**: ").append(newLine).append("\n");
                newLinesFound = true;
            }
        }
        if (!newLinesFound) {
            updates.append("🚫 **No new lines found.**\n");
        }

        // updates.append("\n==================================================\n");
        updates.append("\n\n❌ **Removed Lines** ❌\n");
        updates.append("==================================================\n");

        oldIndex = 0;
        boolean removedLinesFound = false;
        for (String oldLine : oldLines) {
            if (oldIndex < newLines.length && newLines[oldIndex].equals(oldLine)) {
                oldIndex++;
            } else {
                updates.append("➖ **Removed Line**: ").append(oldLine).append("\n");
                removedLinesFound = true;
            }
        }
        if (!removedLinesFound) {
            updates.append("🚫 **No removed lines found.**\n");
        }

        updates.append("\n\n==================================================\n");
        updates.append("🔄 **Comparison complete** 🔄\n");
        // updates.append("==================================================\n");

        return updates.toString();
    }
    // Helper function to check if a token is in the list
    private boolean containsToken(String[] tokens, String token) {
        for (String t : tokens) {
            if (t.equals(token)) {
                return true;
            }
        }
        return false;
    }
    // Helper function to format token for display (add color or styling)
    private String formatTokenForDisplay(String token) {
        // You can add formatting here, such as adding color or other symbols for better visibility
        return token;  // In this example, the token is returned as is, but you can style it based on your UI
    }
    // Helper function to format content for display (add code-style, etc.)
    private String formatContentForDisplay(String content) {
        String formattedContent = content.replaceAll("(?<=\\S)(?=[A-Z])", " ");  // Example of formatting rule (adding space before capital letters)
        return formattedContent;
    }
    // Function to get the last modified time of a file
    private FileTime getLastModifiedTime(File file) {
        try {
            Path path = file.toPath();
            return Files.getLastModifiedTime(path);  // Return the last modified time of the file
        } catch (IOException e) {
            e.printStackTrace();
            return null;  // Return null if there's an error
        }
    }
    // Function to get the last analyzed time (this can be stored in a variable or file)
    private FileTime getLastAnalyzedTime(File file) {
        // For demonstration purposes, return a static date or retrieve the stored analysis time
        // You would typically store this in a variable or metadata file for later use
        return FileTime.fromMillis(0);  // Example placeholder time
    }
    // Function to get the file from the currently selected tab
    private File getFileFromTab(int selectedIndex) {
        // Here, you can customize how to retrieve the file based on the selected tab
        // For example, use the tab title to find the corresponding file
        String tabTitle = tabbedPane.getTitleAt(selectedIndex);  // Get the title of the tab
        return openedFiles.stream()
                .filter(file -> file.getName().equals(tabTitle))  // Match the file name with the tab title
                .findFirst()
                .orElse(null);  // Return the file, or null if not found
    }
    // Method to save JTable data as CSV ?!
    /* End Refresh Button */

    /* Start SaveTable Button */
    // private void SaveButton() {
    //     if (tabbedPane.getTabCount() == 0) {
    //         JOptionPane.showMessageDialog(frame, "No file is open, nothing to save.", "Warning", JOptionPane.WARNING_MESSAGE);
    //         return;
    //     }

    //     // Get the selected tab
    //     int selectedTabIndex = tabbedPane.getSelectedIndex();
    //     JScrollPane selectedScrollPane = (JScrollPane) tabbedPane.getComponentAt(selectedTabIndex);
    //     JTable table = (JTable) selectedScrollPane.getViewport().getView();

    //     // Check if the table is empty
    //     if (table.getRowCount() == 0) {
    //         JOptionPane.showMessageDialog(frame, "The table is empty, nothing to save.", "Warning", JOptionPane.WARNING_MESSAGE);
    //         return;
    //     }

    //     JFileChooser fileChooser = new JFileChooser();
    //     fileChooser.setDialogTitle("Save File");
    //     // Adding file filters
    //     fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
    //     fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TSV Files (*.tsv)", "tsv"));
    //     fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
    //     fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
    //     fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("HTML Files (*.html)", "html"));
    //     fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("XML Files (*.xml)", "xml"));
    //     fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Markdown Files (*.md)", "md"));
    //     fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("RTF Files (*.rtf)", "rtf"));
    //     // fileChooser.setFileFilter(new FileNameExtensionFilter("All Formats", "csv", "tsv", "txt", "json", "html", "xml", "md", "rtf")); // Adding a custom "All Formats" filter
    //     fileChooser.setFileFilter(new FileNameExtensionFilter("All Formats", "all")); // Add "All Formats" filter for "All Files"
        
    //     // Setting default file name
    //     File defaultFile = new File(fileChooser.getCurrentDirectory(), "AllFormatsSave");
    //     fileChooser.setSelectedFile(defaultFile);

    //     // JTextField to display selected filter
    //     JTextField textField = new JTextField("");
    //     textField.setEditable(false);  // Make it non-editable
    //     textField.setColumns(20);  // Set the number of columns

    //     // Event listener for filter change
    //     fileChooser.addPropertyChangeListener(evt -> {
    //         if (evt.getPropertyName().equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
    //             FileFilter selectedFilter = fileChooser.getFileFilter();
    //             if (selectedFilter != null) {
    //                 String filterDescription = selectedFilter.getDescription();
    //                 if (filterDescription.equals("All Formats")) {
    //                     textField.setText("You have selected to save all formats.");
    //                 } else {
    //                     textField.setText("You have selected: " + filterDescription);
    //                 }
    //             }
    //         }
    //     });

    //     // Show file chooser dialog
    //     int result = fileChooser.showSaveDialog(frame);
    //     if (result == JFileChooser.APPROVE_OPTION) {
    //         File selectedFile = fileChooser.getSelectedFile();
    //         String filePath = selectedFile.getAbsolutePath();
    //         FileFilter selectedFilter = fileChooser.getFileFilter();

    //         // Handle "All Formats" case by using a default name if necessary
    //         if (selectedFilter != null && selectedFilter.getDescription().equals("All Formats")) {
    //             filePath = selectedFile.getParent() + File.separator + "AllFormatsSave";
    //         }
        
    //         // Ensure the file (name ends with the selected format extension) extension matches the selected filter
    //         if (selectedFilter != null && !filePath.endsWith(getFileExtension(selectedFilter))) {
    //             filePath += getFileExtension(selectedFilter);  // Add the correct extension
    //         }
    //         // Handle "All Formats" case by using a default name if necessary
    //         if (selectedFilter.getDescription().equals("All Formats")) {
    //             filePath = selectedFile.getParent() + File.separator + "AllFormatsSave";
    //         }

    //         String baseName = Paths.get(selectedFile.getName()).getFileName().toString();
    //         baseName = baseName.lastIndexOf(".") == -1 ? baseName : baseName.substring(0, baseName.lastIndexOf("."));

    //         // Check if the file name contains a dot (i.e., it has an extension)
    //         int lastDotIndex = baseName.lastIndexOf(".");
    //         if (lastDotIndex != -1) {
    //             // Keep the part before the last dot (the main extension)
    //             String mainExtension = baseName.substring(lastDotIndex);
                
    //             // Check if there's another dot before the main extension (like "m.txt.p")
    //             int secondLastDotIndex = baseName.lastIndexOf(".", lastDotIndex - 1);
    //             if (secondLastDotIndex != -1) {
    //                 // If there's a second dot, remove the last extension (like ".p") and keep the main extension
    //                 baseName = baseName.substring(0, secondLastDotIndex);
    //             }
    //         }
    //         // Now baseName will be without any unwanted additional extensions

    //         // Ensure file name doesn't contain an extension if it's "All Formats"
    //         if (selectedFilter.getDescription().equals("All Formats") && !baseName.contains("AllFormatsSave")) {
    //             baseName = "AllFormatsSave";  // Use a default name for all formats
    //         }

    //          // Update the selected file's name if "All Formats" is selected
    //         if (selectedFilter.getDescription().equals("All Formats")) {
    //             // Remove any extension from the base name and ensure it's set for the user
    //             if (!baseName.contains("AllFormatsSave")) {
    //                 baseName = "AllFormatsSave";  // Default name for "All Formats"
    //             }

    //             // Update the selected file with the new name
    //             selectedFile = new File(fileChooser.getCurrentDirectory(), baseName);
    //             fileChooser.setSelectedFile(selectedFile);  // Set the modified file name in the file chooser
    //         }

    //         // try {
    //         //     // Save the file only if it's confirmed
    //         //     if (checkFileExistence(filePath)) {
    //         //         saveFile(table.getModel(), filePath, selectedFilter);  // Save the table data
    //         //         JOptionPane.showMessageDialog(frame, "File saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
    //         //     } else {
    //         //         JOptionPane.showMessageDialog(frame, "File not saved.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
    //         //     }
    //         // } catch (IOException ex) {
    //         //     JOptionPane.showMessageDialog(frame, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    //         // }

    //         // Ensure the directory exists before saving
    //         File directory = new File(filePath).getParentFile();
    //         if (!directory.exists()) {
    //             directory.mkdirs();  // Create directory if it doesn't exist
    //         }

    //         try {
    //             if (checkFileExistence(filePath)) {
    //                 // Check if "All Formats" is selected
    //                 // if (selectedFilter.getDescription().equals("All Formats")) {
    //                 if (selectedFilter != null && selectedFilter.getDescription().equals("All Formats")) {
    //                     // // Base file name without the extension
    //                     // String baseName = selectedFile.getName().isEmpty() ? "All_Formats_Export" : selectedFile.getName();
    //                     // String baseName = selectedFile.getName().isEmpty() ? "All_Formats_Export" : selectedFile.getName().replaceFirst("[.][^.]+$", ""); // Removing any extension
                        
    //                     // Ensure file name is appropriate for saving all formats | Create a modified file path by appending '_AllFormatsSave' if necessary
    //                     if (!filePath.endsWith("AllFormatsSave")) {
    //                         filePath += "_AllFormatsSave"; // Adding suffix for multiple formats
    //                     }
    //                     saveAllFormats(table.getModel(), baseName, selectedFile.getParent()); // Save in all formats
    //                     JOptionPane.showMessageDialog(frame, "All formats have been successfully saved in the file: " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
    //                 } else {
    //                     saveFile(table.getModel(), filePath, selectedFilter);  // Save the table data
    //                     // JOptionPane.showMessageDialog(frame, "File saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
    //                     JOptionPane.showMessageDialog(frame, "Your file has been successfully saved at: " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
    //                 }
    //             } else {
    //                 // JOptionPane.showMessageDialog(frame, "File not saved.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
    //                 JOptionPane.showMessageDialog(frame, "The file save operation has been cancelled. Please try again.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
    //             }
    //         } catch (IOException ex) {
    //             // JOptionPane.showMessageDialog(frame, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    //             JOptionPane.showMessageDialog(frame, "An error occurred while saving the file. Please check the file path or permissions.\nDetails: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    //         }
    //     }
    // }
    public void SaveButton() {
        // Check if any file is open
        if (tabbedPane.getTabCount() == 0) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, "No file is open, nothing to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            });
            return;
        }

        // Get the selected tab and table
        int selectedTabIndex = tabbedPane.getSelectedIndex();
        JScrollPane selectedScrollPane = (JScrollPane) tabbedPane.getComponentAt(selectedTabIndex);
        JTable table = (JTable) selectedScrollPane.getViewport().getView();

        // Check if the table is empty
        if (table.getRowCount() == 0) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, "The table is empty, nothing to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            });
            return;
        }

        // Create a file chooser dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save File");

        // Adding file filters
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TSV Files (*.tsv)", "tsv"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("HTML Files (*.html)", "html"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("XML Files (*.xml)", "xml"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Markdown Files (*.md)", "md"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("RTF Files (*.rtf)", "rtf"));

        // Default filter for "All Formats"
        FileNameExtensionFilter allFormatsFilter = new FileNameExtensionFilter("All Formats", "all");
        fileChooser.setFileFilter(allFormatsFilter);

        // Set default file name for "All Formats"
        String defaultFileName = "_AllFormatsSave";
        fileChooser.setSelectedFile(new File(defaultFileName));

        // File filter change listener to update the file name based on the selected filter
        fileChooser.addPropertyChangeListener(evt -> {
            if (JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                FileFilter selectedFilter = fileChooser.getFileFilter();
                if (selectedFilter.equals(allFormatsFilter)) {
                    fileChooser.setSelectedFile(new File(defaultFileName));
                } else {
                    String fileExtension = getFileExtension(selectedFilter);
                    String baseName = "File";
                    fileChooser.setSelectedFile(new File(baseName + fileExtension));
                }
            }
        });

        // Show file save dialog
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path selectedFile = fileChooser.getSelectedFile().toPath();
            String filePath = selectedFile.toString();
            FileFilter selectedFilter = fileChooser.getFileFilter();

            // Set base name based on the selected filter
            String baseName = selectedFile.getFileName().toString();
            if (selectedFilter.equals(allFormatsFilter)) {
                // If "All Formats" filter is selected, allow the user to choose any format, but enforce a default name
                baseName = defaultFileName; // Default name for "All Formats" | // baseName = "_AllFormatsSave";                
            } else {
                // baseName = selectedFile.getFileName().toString().replaceFirst("[.][^.]+$", "");
                // For specific formats, ensure the selected file name has the correct extension
                String extension = getFileExtension(selectedFilter);
                if (!baseName.endsWith(extension)) {
                    baseName += extension;  // Append the correct extension if it's not already present
                }
            }

            // Now check if the file already exists
            if (Files.exists(selectedFile)) {
                int confirm = JOptionPane.showConfirmDialog(frame, "The file already exists. Do you want to overwrite it?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.NO_OPTION) {
                    // User clicked "No", don't save the file
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame, "File save operation was cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    });
                    return; // Exit early
                }
            }

            // Ensure the directory exists before saving
            Path parentDirectory = selectedFile.getParent();
            try {
                if (Files.notExists(parentDirectory)) {
                    Files.createDirectories(parentDirectory);
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Error creating directory: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
                return;
            }

            // Try saving the file
            try {
                if (selectedFilter.equals(allFormatsFilter)) {
                    // Save all formats in the specified directory
                    saveAllFormats(table.getModel(), baseName, selectedFile.getParent().toString());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame, "All formats saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    });
                } else {
                    // Save the file in the selected format
                    saveFile(table.getModel(), filePath, selectedFilter);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame, "File saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            } catch (IOException ex) {
                // Handle any errors that occur during the save process
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        } else {
            // User canceled the save operation
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, "File save operation was cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
            });
        }
    }
    /**
     * This method retrieves the file extension for a given file filter.
     * @param selectedFilter The selected file filter
     * @return The file extension associated with the filter, including the dot (.)
    */
    private String getFileExtension(FileFilter selectedFilter) {
        if (selectedFilter instanceof FileNameExtensionFilter) {
            FileNameExtensionFilter filter = (FileNameExtensionFilter) selectedFilter;
            String[] extensions = filter.getExtensions();
            if (extensions.length > 0) {
                return "." + extensions[0]; // Add dot (.) before the extension
            }
        }
        return "";
    }
    /**
     * This method saves all available formats for the selected data in a specified location.
     * @param tableModel The table model containing the data to save
     * @param baseName The base name for the saved files
     * @param parentDir The directory to save the files in
     * @throws IOException If an error occurs while saving the files
     */
    private void saveAllFormats(TableModel tableModel, String baseName, String parentDir) throws IOException {
        // Save the file in all supported formats
        saveAsCSV(tableModel, parentDir + File.separator + baseName + ".csv");
        saveAsTSV(tableModel, parentDir + File.separator + baseName + ".tsv");
        saveAsTXT(tableModel, parentDir + File.separator + baseName + ".txt");
        saveAsJSON(tableModel, parentDir + File.separator + baseName + ".json");
        saveAsHTML(tableModel, parentDir + File.separator + baseName + ".html");
        saveAsXML(tableModel, parentDir + File.separator + baseName + ".xml");
        saveAsMarkdown(tableModel, parentDir + File.separator + baseName + ".md");
        saveAsRTF(tableModel, parentDir + File.separator + baseName + ".rtf");
    }
    // // Helper method to save data in all formats
    // private void saveAllFormats(TableModel tableModel, String baseName, String directoryPath) throws IOException {
    //     saveAsCSV(tableModel, directoryPath + "/" + baseName + ".csv");
    //     saveAsTSV(tableModel, directoryPath + "/" + baseName + ".tsv");
    //     saveAsTXT(tableModel, directoryPath + "/" + baseName + ".txt");
    //     saveAsJSON(tableModel, directoryPath + "/" + baseName + ".json");
    //     saveAsHTML(tableModel, directoryPath + "/" + baseName + ".html");
    //     saveAsXML(tableModel, directoryPath + "/" + baseName + ".xml");
    //     saveAsMarkdown(tableModel, directoryPath + "/" + baseName + ".md");
    //     saveAsRTF(tableModel, directoryPath + "/" + baseName + ".rtf");
    // }
    /**
     * This method saves the table data in the selected file format.
     * @param tableModel The table model containing the data to save
     * @param filePath The path of the file to save to
     * @param selectedFilter The file filter determining the file format
     * @throws IOException If an error occurs while saving the file
    */
    public void saveFile(TableModel tableModel, String filePath, FileFilter selectedFilter) throws IOException {
        // Ensure the file extension matches the selected filter
        String fileExtension = getFileExtension(selectedFilter);

        if (filePath.endsWith(fileExtension)) {
            // // Check if the file already exists and ask for confirmation
            // if (!checkFileExistence(filePath)) {
            //     // If the user decides not to overwrite, exit the method
            //     return;
            // }

            // Call specific saving method based on file extension | Proceed with saving the file if the user confirms
            switch (fileExtension) {
                case ".csv":
                    saveAsCSV(tableModel, filePath);
                    break;
                case ".tsv":
                    saveAsTSV(tableModel, filePath);
                    break;
                case ".txt":
                    saveAsTXT(tableModel, filePath);
                    break;
                case ".json":
                    saveAsJSON(tableModel, filePath);
                    break;
                case ".html":
                    saveAsHTML(tableModel, filePath);
                    break;
                case ".xml":
                    saveAsXML(tableModel, filePath);
                    break;
                case ".md":
                    saveAsMarkdown(tableModel, filePath);
                    break;
                case ".rtf":
                    saveAsRTF(tableModel, filePath);
                    break;
                default:
                    throw new IOException("Unsupported file format");
            }
        } else {
            throw new IOException("File extension does not match the selected filter");
        }
    }
    // // Method to Get the file extension based on the selected filter description 
    // private static String getFileExtension(FileFilter filter) {
    //     // if (filter instanceof FileNameExtensionFilter) {
    //     //     FileNameExtensionFilter fileFilter = (FileNameExtensionFilter) filter;
    //     //     String[] extensions = fileFilter.getExtensions();
    //     //     return extensions.length > 0 ? "." + extensions[0] : "";
    //     // }

    //     String description = filter.getDescription();
    //     if (description.contains("CSV")) return ".csv";
    //     if (description.contains("TSV")) return ".tsv";
    //     if (description.contains("Text")) return ".txt";
    //     if (description.contains("JSON")) return ".json";
    //     if (description.contains("HTML")) return ".html";
    //     if (description.contains("XML")) return ".xml";
    //     if (description.contains("Markdown")) return ".md";
    //     if (description.contains("RTF")) return ".rtf";
    //     return "";
    // }
    /**
     * This method checks whether the file exists and is accessible for saving.
     * @param filePath The path of the file to check
     * @return true if the file exists and can be saved, false otherwise
    */
    // Method to check if a file already exists (if necessary)
    public static boolean checkFileExistence(String filePath) {
        // File file = new File(filePath);
        // return !file.exists() || JOptionPane.showConfirmDialog(frame, "The file already exists. Do you want to overwrite it?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

        File file = new File(filePath);
        return !file.exists() || (file.exists() && file.canWrite());
      /*
        File file = new File(filePath);

        // Check if the file already exists
        if (file.exists()) {
            // Show a confirmation dialog if the file exists
            int response = JOptionPane.showConfirmDialog(null, 
                "The file already exists. Do you want to overwrite it?", 
                "Confirm Save", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);

            // If the user selects "No", return false to indicate that we don't want to overwrite the file || // Return true if the user selects "Yes", false if the user selects "No"
            return response == JOptionPane.YES_OPTION;
        }

        return true;  // File doesn't exist, so it's safe to save
      */
    }
        
    /**
     * These methods save the data in the respective formats.
     * Implement saveCSV, saveTSV, saveTXT, saveJSON, saveHTML, saveXML, saveMarkdown, and saveRTF accordingly.
    */
    // ** Start saveAsCSV **
    // private static void saveAsCSV(TableModel tableModel, String filePath) throws IOException {
    //     try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
    //         // كتابة رأس الجدول
    //         for (int j = 0; j < tableModel.getColumnCount(); j++) {
    //             writer.write(tableModel.getColumnName(j));
    //             if (j < tableModel.getColumnCount() - 1) writer.write(",");
    //         }
    //         writer.newLine();

    //         // كتابة البيانات
    //         for (int i = 0; i < tableModel.getRowCount(); i++) {
    //             for (int j = 0; j < tableModel.getColumnCount(); j++) {
    //                 String value = tableModel.getValueAt(i, j).toString().replaceAll(",", "");  // إزالة الفواصل إذا كانت موجودة
    //                 writer.write(value);
    //                 if (j < tableModel.getColumnCount() - 1) writer.write(",");
    //             }
    //             writer.newLine();
    //         }
    //     }
    // }
    private static void saveAsCSV(TableModel tableModel, String filePath) throws IOException {
        //  // Check if the file exists and if the user wants to overwrite it
        //  if (!checkFileExistence(filePath)) {
        //      return;  // If the user doesn't want to overwrite, exit the method
        //  }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // كتابة رأس الجدول
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                writer.write(escapeCSV(tableModel.getColumnName(j)));  // معالجة اسم العمود
                if (j < tableModel.getColumnCount() - 1) writer.write(",");
            }
            writer.newLine();

            // كتابة البيانات
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                StringBuilder row = new StringBuilder(); // لتخزين الصف الحالي كـ CSV

                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    String value = tableModel.getValueAt(i, j) != null ? tableModel.getValueAt(i, j).toString().trim() : ""; // الحصول على القيمة مع إزالة المسافات البيضاء

                    // إذا كانت القيمة فارغة أو فقط تحتوي على مسافة بيضاء
                    if (value.isEmpty()) {
                        value = "\"\"";  // القيم الفارغة تُحيط بعلامات اقتباس
                    } else {
                        value = escapeCSV(value);  // معالجة القيم غير الفارغة
                    }

                    // إذا كانت هذه هي أول قيمة في الصف، لا نضيف فاصلة
                    if (row.length() > 0) {
                        row.append(",");  // إضافة الفاصل بين الأعمدة
                    }

                    row.append(value);  // إضافة القيمة إلى الصف
                }

                writer.write(row.toString());  // كتابة الصف
                writer.newLine();
            }
        }
    }
    // دالة لمعاملة البيانات التي تحتوي على فواصل أو علامات اقتباس في CSV
    private static String escapeCSV(String value) {
        // التحقق إذا كانت القيمة تحتوي على فواصل أو علامات اقتباس أو أسطر جديدة
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // إضافة اقتباس مزدوج حول القيمة
            value = "\"" + value.replace("\"", "\"\"") + "\"";  // استبدال علامات الاقتباس المزدوجة باقتباسين مزدوجين
        }
        return value;
    }
    // private static void saveAsCSV(TableModel tableModel, String filePath) throws IOException {
    //     try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
    //         // كتابة البيانات بتنسيق CSV
    //         for (int i = 0; i < tableModel.getRowCount(); i++) {
    //             for (int j = 0; j < tableModel.getColumnCount(); j++) {
    //                 writer.write("\"" + tableModel.getValueAt(i, j).toString() + "\"");
    //                 if (j < tableModel.getColumnCount() - 1) writer.write(",");
    //             }
    //             writer.newLine();
    //         }
    //     }
    // }
    // ** End saveAsCSV **
    private static void saveAsTSV(TableModel tableModel, String filePath) throws IOException {
        //  // Check if the file exists and if the user wants to overwrite it
        // if (!checkFileExistence(filePath)) {
        //     return;  // If the user doesn't want to overwrite, exit the method
        // }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // كتابة رأس الجدول
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                writer.write(tableModel.getColumnName(j));
                if (j < tableModel.getColumnCount() - 1) writer.write("\t");
            }
            writer.newLine();

            // كتابة البيانات
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.write(tableModel.getValueAt(i, j).toString());
                    if (j < tableModel.getColumnCount() - 1) writer.write("\t");
                }
                writer.newLine();
            }
        }
    }
    // ** Start saveAsTXT **
    // private static void saveAsTXT(TableModel tableModel, String filePath) throws IOException {
    //     //  // Check if the file exists and if the user wants to overwrite it
    //     // if (!checkFileExistence(filePath)) {
    //     //     return;  // If the user doesn't want to overwrite, exit the method
    //     // }
    //     try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
    //         // كتابة البيانات
    //         for (int i = 0; i < tableModel.getRowCount(); i++) {
    //             for (int j = 0; j < tableModel.getColumnCount(); j++) {
    //                 writer.write(tableModel.getValueAt(i, j).toString());
    //                 if (j < tableModel.getColumnCount() - 1) writer.write(" ");  // إضافة مسافة بين الأعمدة
    //             }
    //             writer.newLine();
    //         }
    //     }
    // }
    // Method to save tokens to a TXT file with additional enhancements
    public static void saveAsTXT(TableModel tableModel, String filePath) throws IOException {
        // Prepare the current date and time for the header
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write the header with the date and time
            writer.write("Token Analysis Output\n");
            writer.write("Generated on: " + currentDate + "\n");
            writer.write("=====================================\n");

            // Write column details
            writer.write(String.format("%-15s %-20s %-30s %-10s %-15s\n", 
                "Line Number", "Token Value", "Token Type", "Length", "Note"));
            writer.write("-----------------------------------------------------------\n");

            // Write token data with custom notes
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String lineNumber = tableModel.getValueAt(i, 0).toString();
                String tokenValue = tableModel.getValueAt(i, 1).toString();
                String tokenType = tableModel.getValueAt(i, 2).toString();
                String length = tableModel.getValueAt(i, 3).toString();
                String note = generateTokenNote(tokenType, tokenValue);  // Add notes based on token type

                // Format columns for neatness
                writer.write(String.format("%-15s %-20s %-30s %-10s %-15s\n", 
                    lineNumber, tokenValue, tokenType, length, note));
            }
            // Add a separator and end of report
            writer.write("\n=====================================\n");
            writer.write("End of Token Analysis\n");
        }
    }
    // Method to generate custom notes based on token type
    private static String generateTokenNote(String tokenType, String tokenValue) {
        switch (tokenType) {
            case "keyword":
                return "Reserved word";
            case "Operator":
                return "Mathematical operator";
            case "ident":
                return "User-defined identifier";
            case "Num Const":
                return "Numeric constant";
            case "Decimal":
                return "Floating point number";
            case "string":
                return "String literal";
            case "Unknown":
                return "Unrecognized symbol";
            default:
                return "No additional information";
        }
    }
    // ** End saveAsTXT **
    private static void saveAsJSON(TableModel tableModel, String filePath) throws IOException {
        //  // Check if the file exists and if the user wants to overwrite it
        // if (!checkFileExistence(filePath)) {
        //     return;  // If the user doesn't want to overwrite, exit the method
        // }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("{\n\"data\": [\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.write("  {\n");
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    String columnName = tableModel.getColumnName(j);
                    String value = tableModel.getValueAt(i, j).toString();
                    writer.write("    \"" + columnName + "\": \"" + value + "\"");
                    if (j < tableModel.getColumnCount() - 1) writer.write(",\n");
                }
                writer.write("\n  }");
                if (i < tableModel.getRowCount() - 1) writer.write(",");
                writer.write("\n");
            }

            writer.write("\n]\n}");
        }
    }
    private static void saveAsHTML(TableModel tableModel, String filePath) throws IOException {
        //  // Check if the file exists and if the user wants to overwrite it
        // if (!checkFileExistence(filePath)) {
        //     return;  // If the user doesn't want to overwrite, exit the method
        // }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("<html><body><table border='1'>\n");

            // كتابة رأس الجدول
            writer.write("<tr>");
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                writer.write("<th>" + tableModel.getColumnName(j) + "</th>");
            }
            writer.write("</tr>\n");

            // كتابة البيانات
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.write("<tr>");
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.write("<td>" + tableModel.getValueAt(i, j).toString() + "</td>");
                }
                writer.write("</tr>\n");
            }

            writer.write("</table></body></html>");
        }
    }
    private static void saveAsXML(TableModel tableModel, String filePath) throws IOException {
        //  // Check if the file exists and if the user wants to overwrite it
        // if (!checkFileExistence(filePath)) {
        //     return;  // If the user doesn't want to overwrite, exit the method
        // }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<table>\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.write("  <row>\n");
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.write("    <" + tableModel.getColumnName(j) + ">" + tableModel.getValueAt(i, j).toString() + "</" + tableModel.getColumnName(j) + ">\n");
                }
                writer.write("  </row>\n");
            }

            writer.write("</table>");
        }
    }
    private static void saveAsMarkdown(TableModel tableModel, String filePath) throws IOException {
        //  // Check if the file exists and if the user wants to overwrite it
        // if (!checkFileExistence(filePath)) {
        //     return;  // If the user doesn't want to overwrite, exit the method
        // }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // كتابة رأس الجدول
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                writer.write("| " + tableModel.getColumnName(j) + " ");
            }
            writer.write("|\n");

            // إضافة خط للفصل
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                writer.write("|---");
            }
            writer.write("|\n");

            // كتابة البيانات
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.write("| " + tableModel.getValueAt(i, j).toString() + " ");
                }
                writer.write("|\n");
            }
        }
    }
    private static void saveAsRTF(TableModel tableModel, String filePath) throws IOException {
        //  // Check if the file exists and if the user wants to overwrite it
        // if (!checkFileExistence(filePath)) {
        //     return;  // If the user doesn't want to overwrite, exit the method
        // }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("{\\rtf1\\ansi\\deff0\n");
            writer.write("{\\b ");
            
            // كتابة رأس الجدول
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                writer.write(tableModel.getColumnName(j) + "\\tab ");
            }
            writer.write("}\\par\n");

            // كتابة البيانات
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.write(tableModel.getValueAt(i, j).toString() + "\\tab ");
                }
                writer.write("\\par\n");
            }
            writer.write("}");
        }
    }
    /* End SaveTable Button */
}
/*
    // Open file button action listener || Action listener for open file button
    // Action listener to open file and analyze tokens || لفتح الملف وتحليل العناصر
    openButton.addActionListener(e -> {
        JFileChooser fileChooser = new JFileChooser("resources");
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            lastOpenedFile = fileChooser.getSelectedFile(); // تعيين الملف المفتوح
            String content = fileHandler.getContents(lastOpenedFile); // استخدام lastOpenedFile
            if (content != null && !content.isEmpty()) {
                List<TokenAnalyzer.Token> tokens = tokenAnalyzer.analyze(content);
                // Clear table and add new rows || مسح الجدول وإضافة النتائج الجديدة
                tableModel.setRowCount(0); // مسح الجدول
                idCounter = 1; // Reset ID counter here
                for (TokenAnalyzer.Token token : tokens) {
                    // Create a new row with ID and token values
                    String[] newRow = new String[4]; // 4 columns: ID, value, type, and line number
                    newRow[0] = String.valueOf(idCounter++);          // Set ID and increment counter
                    newRow[1] = token.getValue();                     // Get token value
                    newRow[2] = token.getType();                      // Get token type
                    newRow[3] = String.valueOf(token.getLineNumber());// Get line number as string
                    tableModel.addRow(newRow); // Add new row with analyzed token data       
                }
            } else {
                JOptionPane.showMessageDialog(frame, "File is empty or couldn't be read.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    });

    // Analyze button action listener || Action listener for analyze button
    analyzeButton.addActionListener(e -> {
        int totalTokens = tableModel.getRowCount();
        if (totalTokens == 0) {
            JOptionPane.showMessageDialog(frame, "No tokens to analyze.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HashMap<String, Integer> typeCount = new HashMap<>();
        for (int i = 0; i < totalTokens; i++) {
            // String type = (String) tableModel.getValueAt(i, 1); // Run This Line was Before Add Id Column. // Assuming Type is in the second column
            String type = (String) tableModel.getValueAt(i, 2); // Assuming Type is now in the third column (index 2)
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }

        StringBuilder analysisResult = new StringBuilder("Token Analysis:\n\n");
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            analysisResult.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        analysisResult.append("\nTotal Tokens: ").append(totalTokens);

        JOptionPane.showMessageDialog(frame, analysisResult.toString(), "Analysis Results",
                JOptionPane.INFORMATION_MESSAGE);
    });

    // Reset button action listener: Action listener for reset button | Reset button action listener
    resetButton.addActionListener(e -> {
        // Check if there are any rows in the table
        if (tableModel.getRowCount() > 0) {
            // Reset the ID counter
            idCounter = 1;
            tableModel.setRowCount(0); // Clear the table display
            tokenAnalyzer.clearTokens(); // Clear the stored tokens
            JOptionPane.showMessageDialog(frame, "Tokens have been cleared.", "Reset Successful", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Inform the user that there is no data to reset
            JOptionPane.showMessageDialog(frame, "No data to clear.", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    });

    // Save button action listener | Action listener for save button
    saveButton.addActionListener(e -> {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "No results to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show file save dialog with filter options for specific extensions
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");

        // Add file filters for each type
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("TSV Files (*.tsv)", "tsv"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files (*.json)", "json"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("HTML Files (*.html)", "html"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("XML Files (*.xml)", "xml"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Markdown Files (*.md)", "md"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("RTF Files (*.rtf)", "rtf")); // Add RTF filter
        // fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Word Files (*.docx)", "docx"));

        // Set default filter (e.g., TSV)
        fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[3]);

        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            // تحقق من أن نوع الملف المختار ليس فارغًا أو غير محدد
            String fileFilterDescription = fileChooser.getFileFilter().getDescription();

            // Automatically add the correct extension if the user doesn't add one
            if (fileChooser.getFileFilter().getDescription().contains("TSV") && !filePath.endsWith(".tsv")) {
                filePath += ".tsv";
            } else if (fileChooser.getFileFilter().getDescription().contains("CSV") && !filePath.endsWith(".csv")) {
                filePath += ".csv";
            } else if (fileChooser.getFileFilter().getDescription().contains("Text") && !filePath.endsWith(".txt")) {
                filePath += ".txt";
            } else if (fileChooser.getFileFilter().getDescription().contains("JSON") && !filePath.endsWith(".json")) {
                filePath += ".json";
            } else if (fileChooser.getFileFilter().getDescription().contains("HTML") && !filePath.endsWith(".html")) {
                filePath += ".html";
            } else if (fileChooser.getFileFilter().getDescription().contains("XML") && !filePath.endsWith(".xml")) {
                filePath += ".xml";
            } else if (fileChooser.getFileFilter().getDescription().contains("Markdown") && !filePath.endsWith(".md")) {
                filePath += ".md";
            } else if (fileChooser.getFileFilter().getDescription().contains("RTF") && !filePath.endsWith(".rtf")) {
                filePath += ".rtf";
            }

            try {
                switch (fileChooser.getFileFilter().getDescription()) {
                    case "CSV Files (*.csv)":
                        try (FileWriter writer = new FileWriter(filePath)) {
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                    writer.write(tableModel.getValueAt(i, j).toString());
                                    if (j < tableModel.getColumnCount() - 1) {
                                        writer.write(","); // Comma delimiter for CSV
                                    }
                                }
                                writer.write("\n"); // New line after each row
                            }
                        }
                        break;

                    case "TSV Files (*.tsv)":
                        try (FileWriter writer = new FileWriter(filePath)) {
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                    writer.write(tableModel.getValueAt(i, j).toString());
                                    if (j < tableModel.getColumnCount() - 1) {
                                        writer.write("\t"); // Tab delimiter for TSV
                                    }
                                }
                                writer.write("\n"); // New line after each row
                            }
                        }
                        break;

                    case "Text Files (*.txt)":
                        try (FileWriter writer = new FileWriter(filePath)) {
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                    writer.write(tableModel.getValueAt(i, j).toString());
                                    if (j < tableModel.getColumnCount() - 1) {
                                        writer.write(" "); // Space delimiter for TXT
                                    }
                                }
                                writer.write("\n"); // New line after each row
                            }
                        }
                        break;

                    case "JSON Files (*.json)":
                        try (FileWriter writer = new FileWriter(filePath)) {
                            writer.write("[\n");
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                writer.write("  {\n");
                                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                    writer.write("    \"" + tableModel.getColumnName(j) + "\": \"" + tableModel.getValueAt(i, j).toString() + "\"");
                                    writer.write(j < tableModel.getColumnCount() - 1 ? ",\n" : "\n");
                                }
                                writer.write(i < tableModel.getRowCount() - 1 ? "  },\n" : "  }\n");
                            }
                            writer.write("]\n");
                        }
                        break;

                    case "HTML Files (*.html)":
                        try (FileWriter writer = new FileWriter(filePath)) {
                            writer.write("<html><body><table border='1'>");
                            writer.write("<tr>");
                            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                writer.write("<th>" + tableModel.getColumnName(j) + "</th>");
                            }
                            writer.write("</tr>");
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                writer.write("<tr>");
                                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                    writer.write("<td>" + tableModel.getValueAt(i, j).toString() + "</td>");
                                }
                                writer.write("</tr>");
                            }
                            writer.write("</table></body></html>");
                        }
                        break;

                    case "XML Files (*.xml)":
                        try (FileWriter writer = new FileWriter(filePath)) {
                            writer.write("<table>\n");
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                writer.write("  <row>\n");
                                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                    writer.write("    <" + tableModel.getColumnName(j) + ">" + tableModel.getValueAt(i, j).toString() + "</" + tableModel.getColumnName(j) + ">\n");
                                }
                                writer.write("  </row>\n");
                            }
                            writer.write("</table>");
                        }
                        break;

                    case "Markdown Files (*.md)":
                        try (FileWriter writer = new FileWriter(filePath)) {
                            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                writer.write("| " + tableModel.getColumnName(j) + " ");
                            }
                            writer.write("|\n");
                            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                writer.write("|---");
                            }
                            writer.write("|\n");
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                    writer.write("| " + tableModel.getValueAt(i, j).toString() + " ");
                                }
                                writer.write("|\n");
                            }
                        }
                        break;

                    case "RTF Files (*.rtf)":
                        try (FileWriter writer = new FileWriter(filePath)) {
                            // Write RTF header
                            writer.write("{\\rtf1\\ansi\\deff0\n");
                            // Write table headers in bold
                            writer.write("{\\b ");
                            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                writer.write(tableModel.getColumnName(j) + "\\tab ");
                            }
                            writer.write("}\\par\n");
                            // Write table rows
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                    writer.write(tableModel.getValueAt(i, j).toString() + "\\tab ");
                                }
                                writer.write("\\par\n"); // New line after each row
                            }
                            // End RTF content
                            writer.write("}");
                        }
                        break;
                    default:
                        throw new IOException("Unsupported file format");
                }
                JOptionPane.showMessageDialog(frame, "Results saved to " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });

    // Update the action listener for the refresh button
    refreshButton.addActionListener(e -> {
        // Check that lastOpenedFile is not null and exists
        if (lastOpenedFile != null && lastOpenedFile.exists()) {
            // Reload file content | Attempt to reload the file newContents
            String newContent = fileHandler.getContents(lastOpenedFile);

            // Check if new content was loaded successfully | Verify newContent is loaded
            if (newContent != null && !newContent.isEmpty()) {
                // Check if currentContent is not initialized yet
                if (currentContent == null) {
                    currentContent = newContent; // Set current content for the first time
                    JOptionPane.showMessageDialog(frame, "File has been loaded successfully for the first time.", "Load Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // If old content matches new content
                    if (newContent.equals(currentContent)) {
                        JOptionPane.showMessageDialog(frame, "No updates found in the file.", "Information", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // Update current content with new content
                        currentContent = newContent; // Update current content with new content

                        // Clear previous tokens in the analyzer
                        tokenAnalyzer.clearTokens(); // Ensure clearTokens works as expected

                        // Analyze new content
                        List<Token> tokens = tokenAnalyzer.analyze(newContent);

                        // Clear the table model to avoid duplicates and reset ID counter
                        tableModel.setRowCount(0); // Clear table: This removes all existing rows from the table
                        idCounter = 1; // Reset ID counter here

                        // Populate the table with the new tokens
                        for (Token token : tokens) {
                            // Create a new row with ID and token values
                            String[] newRow = new String[3]; // Update length based on your token structure
                            newRow[0] = String.valueOf(idCounter++); // Set ID and increment counter
                            newRow[1] = token.getValue(); // Set the token value
                            newRow[2] = token.getType(); // Set the token type
                            tableModel.addRow(newRow); // Add new row with analyzed tokens
                        }

                        // Inform the user of successful refresh
                        JOptionPane.showMessageDialog(frame, "File has been refreshed successfully.", "Refresh Successful", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "File is empty or couldn't be read.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "No file has been opened yet.", "Warning", JOptionPane.WARNING_MESSAGE);
        }

        // Debugging information
        System.out.println("Last opened file: " + lastOpenedFile);
        System.out.println("File exists: " + (lastOpenedFile != null && lastOpenedFile.exists()));
        String currentContent = fileHandler.getContents(lastOpenedFile);
        System.out.println("File content: " + currentContent);
    });
*/