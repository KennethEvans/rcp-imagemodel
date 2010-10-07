/*
 * Program to
 * Created on Apr 3, 2006
 * By Kenneth Evans, Jr.
 */

package net.kenevans.gpxinspector.utils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Utils
{
    /**
     * Generic method to get a file suing a JFileChooder
     * 
     * @param defaultPath
     * @return the File or null if aborted.
     */
    public static File getOpenFile(String defaultPath) {
        File file = null;
        JFileChooser chooser = new JFileChooser();
        if(defaultPath != null) {
            chooser.setCurrentDirectory(new File(defaultPath));
        }
        int result = chooser.showOpenDialog(null);
        if(result == JFileChooser.APPROVE_OPTION) {
            // Save the selected path for next time
            defaultPath = chooser.getSelectedFile().getParentFile().getPath();
            // Process the file
            file = chooser.getSelectedFile();
        }
        return file;
    }

    /**
     * Error message dialog
     * 
     * @param msg
     */
    public static void errMsg(String msg) {
        // Show it in a message box
        JOptionPane.showMessageDialog(null, msg, "Error",
            JOptionPane.ERROR_MESSAGE);
        System.out.println(msg);
    }

    /**
     * Exception message dialog. Displays message plus the exception and
     * exception message.
     * 
     * @param msg
     * @param ex
     */
    public static void excMsg(String msg, Exception ex) {
        msg += "\n" + "Exception: " + ex + "\n" + ex.getMessage();
        // Show it in a message box
        JOptionPane.showMessageDialog(null, msg, "Error",
            JOptionPane.ERROR_MESSAGE);
        System.out.println(msg);
    }

    /**
     * Warning message dialog
     * 
     * @param msg
     */
    public static void warnMsg(String msg) {
        // Show it in a message box
        JOptionPane.showMessageDialog(null, msg, "Warning",
            JOptionPane.WARNING_MESSAGE);
        System.out.println(msg);
    }

    /**
     * Information message dialog
     * 
     * @param msg
     */
    public static void infoMsg(String msg) {
        // Show it in a message box
        JOptionPane.showMessageDialog(null, msg, "Information",
            JOptionPane.INFORMATION_MESSAGE);
        System.out.println(msg);
    }

    /**
     * Get the extension for a file.
     * 
     * @param f
     * @return
     */
    public static String getExtension(File file) {
        String ext = null;
        String s = file.getName();
        int i = s.lastIndexOf('.');
        if(i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Check if a file exists.
     * 
     * @param fileName The path name of the file.
     * @return
     */
    public static boolean checkIfFileExists(String fileName) {
        boolean exists = false;
        if(fileName != null && !fileName.equals("")) {
            File myFile = new File(fileName);

            if(myFile.exists()) {
                exists = true;
            }
        }
        return exists;
    }

}
