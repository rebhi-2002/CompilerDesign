package projectcompiler;

import javax.swing.*;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.charset.Charset;

/**
 * A class to handle file reading with error handling and validation for
 * specific conditions.
 */
public class FileHandler {

    // private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // Maximum file size of 10 MB
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // Maximum file size of 10 MB

    /**
     * Reads the contents of the file as a single string.
     *
     * @param file the file to read.
     * @param encoding the character encoding to use.
     * @return the file contents as a string, or null if any error occurs.
     */
    public String getContents(File file, Charset encoding) {
        if (file == null) {
            JOptionPane.showMessageDialog(null, "No file selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (!isValidFile(file)) {
            return null;
        }

        StringBuilder contents = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), encoding)) {
            String line;
            while ((line = reader.readLine()) != null) {
                contents.append(line).append("\n");
            }
        } catch (IOException e) {
            // e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage(), "File Read Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (contents.length() == 0) {
            JOptionPane.showMessageDialog(null, "The file is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
        }

        return contents.toString();
    }

    /**
     * Reads the contents of the file as a list of lines.
     *
     * @param file the file to read.
     * @param encoding the character encoding to use.
     * @return the file contents as a list of lines, or null if any error
     * occurs.
     */
    public List<String> getContentsAsLines(File file, Charset encoding) {
        if (file == null) {
            JOptionPane.showMessageDialog(null, "No file selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (!isValidFile(file)) {
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(file.toPath(), encoding);
            if (lines.isEmpty()) {
                JOptionPane.showMessageDialog(null, "The file is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            return lines;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage(), "File Read Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * A simple wrapper method that reads the file with UTF-8 encoding.
     *
     * @param file the file to read.
     * @return the file contents as a string, or null if any error occurs.
     */
    public String getContents(File file) {
        return getContents(file, Charset.forName("UTF-8"));
    }

    /**
     * Checks if the file is valid (exists, readable, and within size limit).
     *
     * @param file the file to check.
     * @return true if the file is valid, false otherwise.
     */
    private boolean isValidFile(File file) {
        if (file == null) {
            JOptionPane.showMessageDialog(null, "File not found.", "File Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // Check if the file exists
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "File does not exist.", "File Not Found", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if the file is readable
        if (!file.canRead()) {
            JOptionPane.showMessageDialog(null, "File cannot be read due to insufficient permissions.",
                    "File Access Denied", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if the file is too large
        if (file.length() > MAX_FILE_SIZE) {
            int response = JOptionPane.showConfirmDialog(null,
                    "The file is large and may slow down processing. Do you want to continue?",
                    "Large File Warning",
                    JOptionPane.YES_NO_OPTION);
            if (response != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        // // Check for non-UTF-8 encoding
        // try {
        //     List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
        //     if (lines.isEmpty()) {
        //         JOptionPane.showMessageDialog(null, "The file is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
        //     }
        // } catch (IOException e) {
        //     JOptionPane.showMessageDialog(null, "File encoding is not supported. Please ensure the file is UTF-8.",
        //             "Unsupported Encoding", JOptionPane.ERROR_MESSAGE);
        //     return false;
        // }
        return true;
    }
}
