/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
*/

package projectcompiler;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.datatransfer.*; // import java.awt.datatransfer.StringSelection; // import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Rebhe Ibrahim
*/

public class CopyrightWindow {

    /**
     * Displays a stylish and flexible copyright window with detailed information.
     * Allows users to close the window and open the main application window after closing the copyright window.
     */

    public void showCopyrightWindow(JFrame parentFrame) {
        // Create the JFrame for the copyright information
        JFrame copyrightFrame = new JFrame("Copyright Information");
        copyrightFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        copyrightFrame.setSize(600, 350);
        copyrightFrame.setLocationRelativeTo(parentFrame);
        copyrightFrame.setResizable(true);

        // Remove window decorations first before setting opacity
        copyrightFrame.setUndecorated(true); // Remove title bar and borders
        copyrightFrame.setOpacity(0f); // Set opacity to 0 initially

        // Main panel to hold content
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));

        // Header with a logo or company name
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 204));
        JLabel headerLabel = new JLabel("© 2024 Your Company. All Rights Reserved", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Add an image logo or company banner (replace the path with the actual image path)
        // ImageIcon logoIcon = new ImageIcon("path/to/your/logo.png");  // Update path
        String path = "C:" + File.separator + "Users" + File.separator + "rebhi" + File.separator + "Desktop" + File.separator + "img" + File.separator + "101.jpg";
        ImageIcon logoIcon = new ImageIcon(path); // ImageIcon logoIcon = new ImageIcon("C:\\Users\\rebhi\\Desktop\\img\\101.jpg");
        JLabel logoLabel = new JLabel(logoIcon); 
        logoLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(logoLabel, BorderLayout.CENTER);

        // // Create the copyright text with additional details
        // JTextArea copyrightText = new JTextArea(
        //     "\nThis software is licensed under XYZ License.\n\n" +
        //     "For more details, visit our website: \n" +
        //     "https://www.yourcompany.com/license\n\n" +
        //     "For support, contact us at support@yourcompany.com\n" +
        //     "All rights reserved © 2024 Your Company. All trademarks are the property of their respective owners."
        // );

        // Create the copyright text with additional details in JTextPane
        JTextPane copyrightText = new JTextPane();
        // copyrightText.setText(
        //     "\nThis software is licensed under XYZ License.\n\n" +
        //     "For more details, visit our website: \n" +
        //     "https://www.yourcompany.com/license\n\n" +
        //     "For support, contact us at support@yourcompany.com\n" +
        //     "All rights reserved © 2024 Your Company. All trademarks are the property of their respective owners."
        // );

        copyrightText.setContentType("text/html");
        copyrightText.setText(
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; font-size: 14px; color: #333333; line-height: 1.6; padding: 20px; background-color: #f4f4f4; }" +
            "p { margin-bottom: 15px; }" +
            "b { color: #0073e6; font-weight: bold; }" +
            "a { color: #0073e6; text-decoration: none; }" +
            "a:hover { text-decoration: underline; }" +
            "h2 { color: #004e92; font-size: 20px; font-weight: bold; text-align: center; margin-bottom: 20px; }" +
            "ul { margin-top: 0; padding-left: 20px; list-style-type: none; }" +
            "li { padding: 5px 0; }" +
            "li b { color: #004e92; }" +
            "footer { margin-top: 40px; font-size: 12px; color: #666666; text-align: center; }" +
            "</style>" +
            "</head>" +
            "<body>" +

            "<h2>Software License Agreement</h2>" +

            "<p><b>This software is provided under the terms of the XYZ License.</b></p>" +

            "<p><b>By using this software, you agree to the following terms:</b></p>" +

            "<ul>" +
            "<li><b>1. License Grant:</b> You are granted a non-exclusive, non-transferable license to use this software for personal or commercial purposes.</li>" +
            "<li><b>2. Restrictions:</b> Redistribution, modification, or reverse engineering of this software is prohibited unless explicitly allowed by the terms of the license.</li>" +
            "<li><b>3. Warranty Disclaimer:</b> This software is provided 'as is' without warranty of any kind. We are not liable for any damage, loss of data, or inconvenience caused by the use of this software.</li>" +
            "<li><b>4. Privacy:</b> Your use of this software may be subject to our <a href='https://www.yourcompany.com/privacy-policy'>Privacy Policy</a>.</li>" +
            "<li><b>5. Security Notice:</b> We implement industry-standard security measures to protect your data while using our software, but we cannot guarantee 100% security.</li>" +
            "</ul>" +

            "<p><b>For more details, visit our official website:</b><br>" +
            "<a href='https://www.yourcompany.com/license'>XYZ License</a></p><br>" +

            "<p><b>For support or inquiries, contact us at:</b><br>" +
            "<a href='mailto:support@yourcompany.com'>support@yourcompany.com</a><br>" +
            "We aim to respond to all requests within 24-48 hours.</p><br>" +

            "<p><b>All rights reserved © 2024 Your Company. Unauthorized copying, distribution, or use is prohibited.</b></p><br>" +

            "<p><b>All trademarks, logos, and brand names are the property of their respective owners.</b></p><br>" +

            "<p><b>To report issues or provide feedback, please visit our issue tracker at:</b><br>" +
            "<a href='https://www.yourcompany.com/issues'>XYZ Issue Tracker</a></p><br>" +

            "<footer>Follow us on social media: " +
            "<a href='https://twitter.com/yourcompany'>Twitter</a> | " +
            "<a href='https://facebook.com/yourcompany'>Facebook</a> | " +
            "<a href='https://linkedin.com/company/yourcompany'>LinkedIn</a>" +
            "</footer>" +

            "</body>" +
            "</html>"
        );

        // Set text alignment to center using the StyledDocument of JTextPane
        StyledDocument doc = copyrightText.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        // // Set padding using an empty border for insets (padding)
        // int topPadding = 25;
        // int leftPadding = 30;
        // int bottomPadding = 25;
        // int rightPadding = 30;
        // copyrightText.setBorder(BorderFactory.createEmptyBorder(
        //     topPadding, leftPadding, bottomPadding, rightPadding
        // ));

        // Set text properties
        copyrightText.setEditable(false);
        copyrightText.setBackground(mainPanel.getBackground());
        copyrightText.setFont(new Font("Arial", Font.PLAIN, 14));
        // copyrightText.setWrapStyleWord(true);
        // copyrightText.setLineWrap(true);

        // Add scrollable text pane to main panel | Scrollable text area
        JScrollPane textScrollPane = new JScrollPane(copyrightText);
        textScrollPane.setPreferredSize(new Dimension(600, 400));  // Set preferred size
        mainPanel.add(textScrollPane, BorderLayout.CENTER);

        // Footer panel with additional actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footerPanel.setBackground(new Color(240, 240, 240));

        // Button to copy the copyright text to clipboard
        JButton copyButton = new JButton("Copy to Clipboard");
        copyButton.setBackground(new Color(34, 193, 195));
        copyButton.setForeground(Color.WHITE);
        copyButton.setFont(new Font("Arial", Font.BOLD, 12));
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String textToCopy = copyrightText.getText();
                StringSelection stringSelection = new StringSelection(textToCopy);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                JOptionPane.showMessageDialog(copyrightFrame, "Copyright text copied to clipboard!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        footerPanel.add(copyButton);

        // Button to open the main application window after closing the copyright window
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(255, 77, 77));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyrightFrame.dispose();
                SwingUtilities.invokeLater(() -> {
                    UIHandler uiHandler = new UIHandler();
                    uiHandler.createAndShowGUI();
                });
            }
        });
        footerPanel.add(closeButton);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        copyrightFrame.add(mainPanel);

        // Window listener to ensure the main application window opens after the copyright window closes
        copyrightFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    UIHandler uiHandler = new UIHandler();
                    // uiHandler.createAndShowGUI();
                });
            }
            @Override
            public void windowClosing(WindowEvent e) {
                // Start the fade-out effect when the window is closing
                Timer closeTimer = new Timer(20, new ActionListener() {
                    private float opacity = 1f;  // Start from full opacity

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (opacity > 0f) {
                            opacity -= 0.05f;
                            opacity = Math.max(opacity, 0f);
                            copyrightFrame.setOpacity(opacity);
                        } else {
                            ((Timer) e.getSource()).stop();
                            copyrightFrame.dispose();  // Close the window after fade-out
                        }
                    }
                });
                closeTimer.start();
            }
        });

        // Show the copyright window with a fade-in effect
        Timer timer = new Timer(20, new ActionListener() {
            private float opacity = 0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (opacity < 1f) {
                    opacity += 0.05f;
                    opacity = Math.min(opacity, 1f);
                    copyrightFrame.setOpacity(opacity);
                } else {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
        
        // Display the window
        copyrightFrame.setVisible(true);     
    } 
 
}
