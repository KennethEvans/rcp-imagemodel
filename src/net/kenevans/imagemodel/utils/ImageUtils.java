package net.kenevans.imagemodel.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ImageUtils
{
    public static final String LS = System.getProperty("line.separator");

    /**
     * Makes a copy of an image using BufferedImage.TYPE_INT_ARGB.
     * 
     * @param image
     * @return
     */
    public static BufferedImage copyImage(BufferedImage image) {
        return copyImage(image, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Makes a copy of an image with a specified image type.
     * 
     * @param image Source image.
     * @param type Typically BufferedImage.TYPE_INT_ARGB, etc. Use
     *            image.getType() to use the same type as the source.
     * @return The new image.
     */
    public static BufferedImage copyImage(BufferedImage image, int type) {
        // Is this the best way to copy an image
        // Note that TYPE_INT_ARGB supports transparency, RGB does not
        BufferedImage newImage = new BufferedImage(image.getWidth(),
            image.getHeight(), type);
        // Use "null" as the ImageObserver since no asynchronism is involved
        // For drawing the first time, using "this" is appropriate (Component
        // implements ImageObserver)
        Graphics2D gc = newImage.createGraphics();
        // Do this to insure transparency propagates
        gc.setComposite(AlphaComposite.Src);
        gc.drawImage(image, 0, 0, null);
        gc.dispose();
        return newImage;
    }

    /**
     * Gets the ColorModel of an Image.
     * 
     * @param image
     * @return
     */
    public static ColorModel getColorModel(Image image) {
        if(image == null) return null;
        if(image instanceof BufferedImage) {
            return ((BufferedImage)image).getColorModel();
        }
        // Grab a single pixel
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch(InterruptedException ex) {
        }
        ColorModel cm = pg.getColorModel();
        return cm;
    }

    /**
     * Creates a BufferedImage from an Image.
     * 
     * @param image
     * @return
     */
    public static BufferedImage imageToBufferedImage(Image image) {
        // If it is already a BufferedImage, then just return it
        if(image instanceof BufferedImage) return (BufferedImage)image;

        // Insure that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // See if the image has transparent pixels
        ColorModel cm = ImageUtils.getColorModel(image);
        boolean hasAlpha = cm == null ? false : cm.hasAlpha();

        // Create a BufferedImage
        BufferedImage bi = null;
        GraphicsEnvironment ge = GraphicsEnvironment
            .getLocalGraphicsEnvironment();
        try {
            int transparency = hasAlpha ? Transparency.BITMASK
                : Transparency.OPAQUE;
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            bi = gc.createCompatibleImage(image.getWidth(null),
                image.getHeight(null), transparency);
        } catch(HeadlessException ex) {
            // No screen
        }

        // If unsuccessful create a BufferedImage with the default ColorModel
        if(bi == null) {
            int type = hasAlpha ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;
            bi = new BufferedImage(image.getWidth(null), image.getHeight(null),
                type);

            // Copy the Image into the BufferedImage
            Graphics g = bi.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }
        return bi;
    }

    /**
     * Creates an unsigned short grayscale image from a data array.
     * 
     * @param width The width of the image.
     * @param height The height of the image.
     * @param data The data array, which must have at least offset + width
     *            elements.
     * @param offset The offset into the data array. Only elements offset
     *            through offset + width - 1 are to be used.
     * @return
     */
    public static BufferedImage createUnsignedShortGrayscaleImage(int width,
        int height, short data[], int offset) {
        ComponentColorModel cm = new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] {16}, false, // has
            // alpha
            false, // alpha premultipled
            Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
        ComponentSampleModel sm = new ComponentSampleModel(
            DataBuffer.TYPE_USHORT, width, height, 1, width, new int[] {0});
        DataBuffer buf = new DataBufferUShort(data, width, offset);
        WritableRaster wr = Raster.createWritableRaster(sm, buf,
            new Point(0, 0));
        return new BufferedImage(cm, wr, true, null); // no properties
    }

    // }
    /**
     * Gets a set of all basic file suffixes that are recognized by image
     * readers.
     * 
     * @return the file suffix set
     */
    public static Set<String> getReaderSuffixes() {
        TreeSet<String> readerSuffixes = new TreeSet<String>();
        String[] names = ImageIO.getReaderFormatNames();
        for(int i = 0; i < names.length; i++) {
            String name = names[i];
            Iterator<ImageReader> iter = ImageIO
                .getImageReadersByFormatName(name);
            while(iter.hasNext()) {
                ImageReader reader = iter.next();
                String[] s = reader.getOriginatingProvider().getFileSuffixes();
                readerSuffixes.addAll(Arrays.asList(s));
            }
        }
        // Remove any blanks
        readerSuffixes.remove("");
        return readerSuffixes;
    }

    /**
     * Gets a set of all basic file suffixes that are recognized by image
     * writers.
     * 
     * @return the file suffix set
     */
    public static Set<String> getWriterSuffixes() {
        TreeSet<String> writerSuffixes = new TreeSet<String>();
        String[] names = ImageIO.getWriterFormatNames();
        for(int i = 0; i < names.length; i++) {
            String name = names[i];
            Iterator<ImageReader> iter = ImageIO
                .getImageReadersByFormatName(name);
            while(iter.hasNext()) {
                ImageReader writer = iter.next();
                String[] s = writer.getOriginatingProvider().getFileSuffixes();
                writerSuffixes.addAll(Arrays.asList(s));
            }
        }
        // Remove any blanks
        writerSuffixes.remove("");
        return writerSuffixes;
    }

    /**
     * Determines if ImageIO supports the image type of this file and returns a
     * String with the status information. The String starts with "Supported" if
     * the file is supported or is a longer string including suggestions if it
     * is not.
     * 
     * @param file
     * @return a String indicating the status.
     */
    public static String getImageIOSupportedStatus(File file) {
        String info = "";
        // Check the format
        String[] formats = ImageIO.getReaderFormatNames();
        String ext = Utils.getExtension(file);
        boolean found = false;
        if(formats != null && formats.length > 0 && ext != null) {
            for(int i = 0; i < formats.length; i++) {
                String format = formats[i];
                if(ext.equals(format)) {
                    found = true;
                    break;
                }
            }
        }
        if(!found) {
            info += "Extension ." + ext + " represents an unsupported format."
                + LS;
            String extDir = System.getProperty("java.ext.dirs");
            if(extDir != null) {
                info += "JAI might not be installed.  Check the following" + LS;
                info += "directory(s) for jai_imageio.jar:" + LS;
                info += extDir;
            } else {
                String home = System.getProperty("java.home");
                info += "JAI might not be installed.  Check the following" + LS;
                info += "directory for lib/ext/jai_imageio.jar:" + LS;
                info += home;
            }
        } else {
            info += "Supported";
        }
        return info;
    }

    /**
     * Convenience function to determine if ImageIO supports the image type of
     * this file. Calls getImageIOSupportedStatus(file) and checks the String
     * returned to determine if the file is supported or not.
     * 
     * @param file
     * @return whether the file is supported or not.
     */
    public static boolean isImageIOSupported(File file) {
        String status = getImageIOSupportedStatus(file);
        boolean supported = status.startsWith("Supported");
        return supported;
    }

    public static String getSuffixInfo() {
        int itemsPerLine = 8;
        String info = "";
        boolean needsCR = true;
        if(true) {
            // These are from the routine in the Core Java book
            // They don't seem to be any more than the simple routines below
            Set<String> readerSuffixes = getReaderSuffixes();
            info += "Basic ImageIO Reader Suffixes" + LS;
            Iterator<String> iter1 = readerSuffixes.iterator();
            int i = 0;
            needsCR = true;
            while(iter1.hasNext()) {
                String suffix = iter1.next();
                if(i % itemsPerLine == 0) {
                    needsCR = false;
                    if(i > 0) info += LS;
                    info += "  " + suffix;
                } else {
                    needsCR = true;
                    info += ", " + suffix;
                }
                i++;
            }
            if(needsCR) info += LS;
        }

        if(true) {
            // These are from the routine in the Core Java book
            // They don't seem to be any more than the simple routines below
            Set<String> writerSuffixes = getWriterSuffixes();
            info += LS + "Basic ImageIO Writer Suffixes" + LS;
            Iterator<String> iter1 = writerSuffixes.iterator();
            int i = 0;
            needsCR = true;
            while(iter1.hasNext()) {
                String suffix = iter1.next();
                if(i % itemsPerLine == 0) {
                    needsCR = false;
                    if(i > 0) info += LS;
                    info += "  " + suffix;
                } else {
                    needsCR = true;
                    info += ", " + suffix;
                }
                i++;
            }
            if(needsCR) info += LS;
        }

        info += LS + "ReaderFormat Names" + LS;
        String[] names = ImageIO.getReaderFormatNames();
        needsCR = true;
        for(int i = 0; i < names.length; i++) {
            String name = names[i];
            if(i % itemsPerLine == 0) {
                needsCR = false;
                if(i > 0) info += LS;
                info += "  " + name;
            } else {
                needsCR = true;
                info += ", " + name;
            }
        }
        if(needsCR) info += LS;

        info += LS + "WriterFormat Names" + LS;
        names = ImageIO.getWriterFormatNames();
        needsCR = true;
        for(int i = 0; i < names.length; i++) {
            String name = names[i];
            if(i % itemsPerLine == 0) {
                needsCR = false;
                if(i > 0) info += LS;
                info += "  " + name;
            } else {
                needsCR = true;
                info += ", " + name;
            }
        }
        if(needsCR) info += LS;
        return info;
    }

    public static File savePanelToFile(JPanel panel, String defaultPath) {
        File file = null;
        if(panel == null) return null;

        // Get the file name
        JFileChooser chooser = new JFileChooser();
        if(defaultPath != null) {
            chooser.setCurrentDirectory(new File(defaultPath));
        }
        // chooser.addChoosableFileFilter(new GifFilter());
        int result = chooser.showSaveDialog(panel);
        if(result != JFileChooser.APPROVE_OPTION) return null;

        // Process the file
        String fileName = chooser.getSelectedFile().getPath();
        file = new File(fileName);
        if(file.exists()) {
            int selection = JOptionPane.showConfirmDialog(panel,
                "File already exists:" + LS + fileName + LS + "OK to replace?",
                "Warning", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if(selection != JOptionPane.OK_OPTION) return null;
        }

        // Save it
        boolean success = savePanel(panel, file);

        return success ? file : null;
    }

    public static boolean savePanel(JPanel panel, File file) {
        boolean retVal = false;
        if(panel == null || file == null) return false;

        // Check the format
        String[] formats = ImageIO.getWriterFormatNames();
        String ext = Utils.getExtension(file);
        if(formats != null && formats.length > 0 && ext != null) {
            for(int i = 0; i < formats.length; i++) {
                String format = formats[i];
                if(ext.equals(format)) {
                    retVal = true;
                    break;
                }
            }
        }
        if(!retVal) {
            Utils
                .errMsg("Extension [" + ext + "] represents an invalid format");
            return false;
        }

        // Get the image and write it
        try {
            BufferedImage image = getBufferedImageFromPanel(panel);
            ImageIO.write(image, ext, file);
            image.flush();
            // Check for zero length output
            if(file.length() <= 0) {
                Utils
                    .warnMsg("There may be a problem.  The saved file is empty:"
                        + LS + file.getPath());
            }
        } catch(Exception ex) {
            Utils.errMsg("Capture to file failed:" + LS + "File: " + file + LS
                + ex + LS + ex.getMessage());
            retVal = false;
        }

        return retVal;
    }

    /**
     * Saves an image to a file using a JFileChooser, specifying the defaultPath
     * and not the selectedFile.
     * 
     * @param image The image.
     * @param defaultPath The defaultPath for the JFileChooser.
     * @return The File saved or null if canceled.
     * 
     * @see #saveImageToFile(BufferedImage, String, String)
     */
    public static File saveImageToFile(BufferedImage image, String defaultPath) {
        return saveImageToFile(image, defaultPath, null);
    }

    /**
     * Saves an image to a file using a JFileChooser. If the selectedFile is not
     * null, uses JFileChooser.setSelectedFile(), else if the defaultPath is not
     * null, uses JFileChooser.setCurrentDirectory().
     * 
     * @param image The image.
     * @param defaultPath The defaultPath for the JFileChooser.
     * @param selectedFile The selectedFile for the JFileChooser.
     * @return
     * 
     * @see #saveImageToFile(BufferedImage, String)
     */
    public static File saveImageToFile(BufferedImage image, String defaultPath,
        String selectedFile) {
        File file = null;
        if(image == null) return null;

        // Get the file name
        JFileChooser chooser = new JFileChooser();
        if(selectedFile != null) {
            // Seems to do the same as setCurrentDirectory and also sets the
            // file name
            chooser.setSelectedFile(new File(selectedFile));
        } else if(defaultPath != null) {
            chooser.setCurrentDirectory(new File(defaultPath));
        }
        // chooser.addChoosableFileFilter(new GifFilter());
        int result = chooser.showSaveDialog(null);
        if(result != JFileChooser.APPROVE_OPTION) return null;

        // Process the file
        String fileName = chooser.getSelectedFile().getPath();
        file = new File(fileName);
        if(file.exists()) {
            int selection = JOptionPane.showConfirmDialog(null,
                "File already exists:" + LS + fileName + "\nOK to replace?",
                "Warning", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if(selection != JOptionPane.OK_OPTION) return null;
        }

        // Save it
        boolean success = saveImage(image, file);

        return success ? file : null;
    }

    public static boolean saveImage(BufferedImage image, File file) {
        boolean retVal = false;
        if(image == null || file == null) return false;

        // Check the format
        String[] formats = ImageIO.getWriterFormatNames();
        String ext = Utils.getExtension(file);
        if(formats != null && formats.length > 0 && ext != null) {
            for(int i = 0; i < formats.length; i++) {
                String format = formats[i];
                if(ext.equals(format)) {
                    retVal = true;
                    break;
                }
            }
        }
        if(!retVal) {
            Utils
                .errMsg("Extension [" + ext + "] represents an invalid format");
            return false;
        }

        // Get the image and write it
        try {
            // Add compression for JPEG
            if(ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
                // First convert it to TYPE_INT_RGB because otherwise it tends
                // to get interpreted as CMYK since it has 4 channels
                BufferedImage rgbImg = copyImage(image,
                    BufferedImage.TYPE_INT_RGB);
                // Get a writer that will handle compression
                Iterator<ImageWriter> iter = ImageIO
                    .getImageWritersByFormatName("jpeg");
                ImageWriter writer = (ImageWriter)iter.next();
                ImageWriteParam iwp = writer.getDefaultWriteParam();
                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                // Get the compression
                float quality = 95f;
                String res = null;
                // Allow 3 tries
                int count = 0;
                while(res == null) {
                    // Prompt for compression quality
                    res = JOptionPane.showInputDialog(
                        (count != 0 ? "Invalid value!" + LS : "")
                            + "Enter the quality (0 - 100)",
                        String.format("%.0f", quality));
                    if(res == null) {
                        return false;
                    }
                    try {
                        quality = Float.parseFloat(res);
                        if(quality < 0 || quality > 100) {
                            res = null;
                        }
                    } catch(NumberFormatException ex) {
                        res = null;
                    }
                    if(++count >= 3) {
                        Utils.warnMsg("Aborting");
                        return false;
                    }
                }
                // Set the quality (0-1)
                iwp.setCompressionQuality(.01F * quality);
                FileImageOutputStream output = new FileImageOutputStream(file);
                writer.setOutput(output);
                IIOImage iioimage = new IIOImage(rgbImg, null, null);
                writer.write(null, iioimage, iwp);
                // If don't do this other app cannot use the file
                output.close();
                writer.dispose();
            } else {
                ImageIO.write(image, ext, file);
                image.flush();
            }
            // Check for zero length output
            if(file.length() <= 0) {
                Utils
                    .warnMsg("There may be a problem.  The saved file is empty:"
                        + LS + file.getPath());
            }
        } catch(Exception ex) {
            Utils.excMsg("Save image to file failed", ex);
            retVal = false;
        }

        return retVal;
    }

    public static File savePanelToClipboard(JPanel panel) {
        File file = null;
        if(panel == null) return null;

        // Get the image
        try {
            BufferedImage image = getBufferedImageFromPanel(panel);
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            ImageSelection sel = new ImageSelection(image);
            clip.setContents(sel, null);
        } catch(Exception ex) {
            Utils.excMsg("Failed to get image", ex);
        }

        return file;
    }

    public static BufferedImage getBufferedImageFromPanel(JPanel panel) {
        BufferedImage image = null;
        image = new BufferedImage(panel.getWidth(), panel.getHeight(),
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        panel.paint(g);
        g.dispose();

        return image;
    }

    /**
     * Get an Image from a resource in a class.
     * 
     * @param cls The class.
     * @param resource Path of the resource, i.e. "images/main.gif", relative to
     *            the class.
     * @return
     */
    public static Image getImageFromClassResource(Class<?> cls, String resource) {
        if(cls == null || resource == null) return null;
        java.net.URL imgURL = cls.getResource(resource);
        if(imgURL != null) {
            return new ImageIcon(imgURL).getImage();
        } else {
            return null;
        }
    }

    /**
     * Set the icon image for a JFrame from a resource in its class. If the
     * image is null, it will set that, which should result in using the default
     * image.
     * 
     * @param frame The JFrame.
     * @param resource Path of the resource, i.e. "images/main.gif", relative to
     *            the class.
     */
    public static void setIconImageFromResource(JFrame frame, String resource) {
        Image image = null;
        if(frame != null && resource != null) {
            image = getImageFromClassResource(frame.getClass(), resource);
        }
        frame.setIconImage(image);
    }

    /**
     * Gets whether the given BufferedImage is sRGB or not.
     * 
     * @param image
     * @return
     */
    public static boolean isSRGB(BufferedImage image) {
        if(image == null) {
            return false;
        }
        ColorModel cm = image.getColorModel();
        if(cm == null) {
            return false;
        }
        ColorSpace cs = cm.getColorSpace();
        if(cs == null) {
            return false;
        }
        return cs.isCS_sRGB();
    }

    /**
     * Gets the ICC profile from the given ColorModel.
     * 
     * @param cm The given ColorModel.
     * @return The profile or null on failure.
     */
    public static ICC_Profile getICCProfile(ColorModel cm) {
        if(cm == null) {
            return null;
        }
        ICC_Profile profile = null;
        // Find if there is an embedded ICC profile
        ColorSpace cs = cm.getColorSpace();
        if(cs instanceof ICC_ColorSpace) {
            ICC_ColorSpace icccs = (ICC_ColorSpace)cs;
            profile = icccs.getProfile();
        }
        return profile;
    }

    /**
     * Gets the name of the given ICC profile from the given BufferedImage.
     * 
     * @param image
     * @return The name of the ICC profile or null on failure.
     */
    public static String getICCProfileName(BufferedImage image) {
        if(image == null) {
            return null;
        }
        ColorModel cm = image.getColorModel();
        if(cm == null) {
            return null;
        }
        return getICCProfileName(cm);
    }

    /**
     * Gets the name of the given ICC profile from the given ColorModel.
     * 
     * @param cm
     * @return The name of the ICC profile or null on failure.
     */
    public static String getICCProfileName(ColorModel cm) {
        if(cm == null) {
            return null;
        }
        ICC_Profile profile = getICCProfile(cm);
        if(profile == null) {
            return null;
        }
        return getICCProfileName(profile);
    }

    /**
     * Gets the name of the given ICC profile.
     * 
     * @param profile
     * @return The name of the ICC profile or null on failure.
     */
    public static String getICCProfileName(ICC_Profile profile) {
        if(profile == null) {
            return null;
        }
        // Get the ICC profile tag
        // It is a Structure containing invariant and localizable
        // versions of the profile name for display.
        // Bytes 0-3 are "desc". Bytes 4-7 are nulls. Bytes 8-11 are the
        // length of the ASCII invariant profile name. The ASCII invariant
        // part starts at 12 and should end with a null.
        String desc = null;
        byte[] data = profile.getData(ICC_Profile.icSigProfileDescriptionTag);
        if(data != null && data.length > 12) {
            desc = new String(data).substring(12);
            // Find any nulls
            int pos = desc.indexOf('\0');
            if(pos > -1) {
                desc = desc.substring(0, pos);
            }
        }
        return desc;
    }

    /**
     * Gets the ICC profile for the given GraphicsDevice.
     * 
     * @param gdev
     * @return The profile or null on failure.
     */
    public static ICC_Profile getMonitorProfile(GraphicsDevice gdev) {
        if(gdev == null) {
            return null;
        }
        ICC_Profile profile = null;
        try {
            GraphicsConfiguration gconf = gdev.getDefaultConfiguration();
            ColorModel cm = gconf.getColorModel();
            ColorSpace cs = cm.getColorSpace();
            if(cs instanceof ICC_ColorSpace) {
                ICC_ColorSpace icccs = (ICC_ColorSpace)cs;
                profile = icccs.getProfile();
            }
        } catch(Exception ex) {
            // TODO Is showing an error wise?
            Utils.excMsg("Getting monitor profile failed", ex);
            return null;
        }
        return profile;
    }

    /**
     * Gets the ICC profile of the default monitor.
     * 
     * @return
     */
    public static ICC_Profile getDefaultMonitorProfile() {
        ICC_Profile profile = null;
        try {
            GraphicsEnvironment genv = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
            GraphicsDevice gdev = genv.getDefaultScreenDevice();
            profile = getMonitorProfile(gdev);
        } catch(Exception ex) {
            // TODO Is showing an error wise?
            Utils.excMsg("Getting defualt monitor profile failed", ex);
            return null;
        }

        return profile;
    }

    /**
     * Gets information about the installed screen devices.
     * 
     * @return
     */
    public static String getMonitorInfo() {
        String info = "Monitor Information" + LS;
        GraphicsEnvironment genv = GraphicsEnvironment
            .getLocalGraphicsEnvironment();
        if(genv == null) {
            info += "  Cannot find local graphics environment";
            return info;
        }
        GraphicsDevice[] devices = genv.getScreenDevices();
        if(devices == null) {
            info += "  Cannot find screen devices";
            return info;
        }
        if(devices.length == 0) {
            info += "  No screen devices found";
            return info;
        }
        GraphicsConfiguration[] configurations;
        ColorModel cm;
        for(GraphicsDevice device : devices) {
            info += "  " + device.getIDstring() + LS;
            configurations = device.getConfigurations();
            for(GraphicsConfiguration config : configurations) {
                cm = config.getColorModel();
                if(cm != null) {
                    info += "    ICC Profile="
                        + ImageUtils.getICCProfileName(cm) + LS;
                } else {
                    info += "    ICC Profile=Not Found" + LS;
                }
            }
        }
        return info;
    }

    /**
     * Generates a new image from the given one. The new image has the specified
     * ICC profile.
     * 
     * @param profile
     * @param image
     * @return The new image or null on failure.
     */
    public static BufferedImage convertProfile(ICC_Profile profile,
        BufferedImage image) {
        if(profile == null || image == null) {
            return null;
        }
        ICC_ColorSpace cs = new ICC_ColorSpace(profile);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        return op.filter(image, null);
    }

}
