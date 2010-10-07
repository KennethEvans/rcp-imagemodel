package net.kenevans.parser;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.kenevans.gpx.GpxType;
import net.kenevans.gpx.TrkType;

/*
 * Created on Aug 19, 2010
 * By Kenneth Evans, Jr.
 */

public class GPXParser
{
    private static String TEST_FILE = "C:/Users/evans/Documents/GPSLink/CM2008.gpx";
    /** This is the package specified when XJC was run. */
    private static String PACKAGE_NAME = "net.kenevans.gpx";

    /**
     * Parses a GPX file with the given name.
     * 
     * @param fileName The file name to parse.
     * @return The GpxType corresponding to the top level of the input file.
     * @throws JAXBException
     */
    public static GpxType parse(String fileName) throws JAXBException {
        return parse(new File(fileName));
    }

    /**
     * Parses a GPX file.
     * 
     * @param file The File to parse.
     * @return The GpxType corresponding to the top level of the input file.
     * @throws JAXBException
     */
    @SuppressWarnings("unchecked")
    public static GpxType parse(File file) throws JAXBException {
        GpxType gpx = null;
        JAXBContext jc = JAXBContext.newInstance(PACKAGE_NAME);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        JAXBElement<GpxType> root = (JAXBElement<GpxType>)unmarshaller
            .unmarshal(file);
        gpx = root.getValue();
        return gpx;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String fileName = TEST_FILE;
        System.out.println(fileName);
        GpxType gpx = null;
        try {
            gpx = parse(fileName);
        } catch(JAXBException ex) {
            System.out
                .println("Error creating JAXBContext: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        List<TrkType> tracks = gpx.getTrk();
        for(TrkType track : tracks) {
            System.out.println(track.getName());
        }
    }

}
