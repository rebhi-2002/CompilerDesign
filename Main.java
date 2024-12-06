/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectcompiler;

import java.io.File;
import javax.swing.SwingUtilities;

/**
 *
 * @author Rebhe Ibrahim
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        // Use invokeLater to ensure the user interface is created in the correct thread
        SwingUtilities.invokeLater(() -> {
            // Create the copyright window
            CopyrightWindow copyrightWindow = new CopyrightWindow();
            
            // Display the copyright window initially
            copyrightWindow.showCopyrightWindow(null);  // Pass null here as it's the first window
        });
        
    }

    // Dummy method to simulate file content retrieval
    private String getContents(File file) {
        // Implement logic to read the file content
        return "Content of " + file.getName(); // Placeholder content
    }

}
