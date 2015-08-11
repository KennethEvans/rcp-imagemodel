package net.kenevans.imagemodel.colorset;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ColorSetUtils
{
    public static final String LS = System.getProperty("line.separator");

    public static void writeACOFile(File file, int[][]rgb)
        throws IOException, IllegalArgumentException, FileNotFoundException,
        SecurityException {
        if(file == null) {
            throw new IllegalArgumentException("writeACOFile: File is null");
        }
        if(rgb == null) {
            throw new IllegalArgumentException(
                "writeACOFile: Color array is null");
        }
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        OutputStream os = null;
        int len = rgb.length;
        byte[] reds = new byte[len];
        byte[] greens = new byte[len];
        byte[] blues = new byte[len];
        
        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);
        // Version
        dos.writeShort(1);
        // Number of colors
        dos.writeShort(len);
        // Loop over colors
        for(int i = 0; i < len; i++) {
            // DEBUG
            System.out.printf("%3d %02x %02x %02x" + LS, i, reds[i],
                greens[i], blues[i]);
            // Color space (0=RGB)
            dos.writeShort(0);
            // Red
            dos.writeShort(rgb[i][0] << 8);
            // Green
            dos.writeShort(rgb[i][1] << 8);
            // Blue
            dos.writeShort(rgb[i][2] << 8);
            // Unused
            dos.writeShort(0);
        }
        dos.close();
        // Write it to the file (This CTOR does not append)
        os = new FileOutputStream(file);
        baos.writeTo(os);
        if(os != null) os.close();
        
        // DEBUG
        System.out.println("File size=" + file.length());
    }

}
