/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package projectcompiler;

/**
 *
 * @author Rebhe Ibrahim
 */

import javax.swing.*; // import javax.swing.table.DefaultTableModel; // import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import projectcompiler.TokenAnalyzer.Token;

public class FileAnalyzer {
    private final JFrame frame;
    private final FileHandler fileHandler;
    private final TokenAnalyzer tokenAnalyzer;
    private final JTabbedPane tabbedPane;
    private final List<File> openedFiles;

    // Constructor to initialize necessary components
    public FileAnalyzer(JFrame frame, FileHandler fileHandler, TokenAnalyzer tokenAnalyzer, JTabbedPane tabbedPane, List<File> openedFiles) {
        this.frame = frame;
        this.fileHandler = fileHandler;
        this.tokenAnalyzer = tokenAnalyzer;
        this.tabbedPane = tabbedPane;
        this.openedFiles = openedFiles;
    }

    // Method to analyze the selected file
    public void analyzeFiles() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(frame, "No tab selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File selectedFile = openedFiles.get(selectedIndex);
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(frame, "File not found.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Analyze the selected file
        String fileContent = fileHandler.getContents(selectedFile);
        if (fileContent == null || fileContent.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "The file is empty or could not be read.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Token> tokens = tokenAnalyzer.analyze(fileContent);
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

        // Use GridBagLayout for vertical stacking
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

        // Create button panel for Reset and Save buttons
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

        // Add ActionListener to the save button
        saveButton.addActionListener(e -> {
            // UIHandler uiHandler = new UIHandler();  // Create instance of UIHandler
            // uiHandler.saveFiles();  // Call the method via the instance
            saveFiles();
        });

        // Create the scrollable result panel
        JScrollPane scrollableResultPane = new JScrollPane(resultPanel);
        scrollableResultPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableResultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Create a dialog to show the result
        JDialog dialog = new JDialog(frame, "Analysis Results", true);
        dialog.setSize(650, 450); // Set dialog size
        dialog.setLocationRelativeTo(frame); // Center the dialog
        dialog.add(scrollableResultPane); // Add the scrollable result panel to the dialog
        dialog.setVisible(true);
    }

    private void saveFiles() {
        System.out.println("Starting saveFiles method...");
    }
    
}
