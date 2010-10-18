package net.kenevans.gpxinspector.kml;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import net.kenevans.gpx.TrksegType;
import net.kenevans.gpx.WptType;
import net.kenevans.gpxinspector.model.GpxFileModel;
import net.kenevans.gpxinspector.model.GpxFileSetModel;
import net.kenevans.gpxinspector.model.GpxTrackModel;
import net.kenevans.gpxinspector.model.GpxWaypointModel;
import net.kenevans.gpxinspector.preferences.IPreferenceConstants;
import net.kenevans.gpxinspector.utils.RainbowColorScheme;
import net.kenevans.gpxinspector.utils.SWTUtils;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class KmlUtils implements IPreferenceConstants
{
    // private static final String HOME_ICON_ID = "HomeIcon";
    private static final String WAYPOINT_ICON_ID = "WaypointIcon";

    /** Specifies the transparency of the lines. */
    /** Set of line colorSetColors. it will cycle through these. */
    private static final String[] colorSetColors = {"0000ff", "00ff00",
        "ff0000", "ffff00", "ff00ff", "00ffff", "0077ff", "ff0077"};
    /** Number of line colorSetColors. */
    private static String[] colors;

    /**
     * @param args
     * @throws IOException
     */
    public static void createKml(GpxFileSetModel fileSetModel,
        final KmlOptions kmlOptions) throws IOException {
        // Generate the KML
        final Kml kml = KmlFactory.createKml();

        // Create the Document for this file
        Document document = kml.createAndSetDocument()
            .withName("GPX Inspector").withOpen(true);

        // Make the Styles for this Document
        Style style;
        IconStyle iconStyle;
        // Colors
        switch(kmlOptions.getTrkColorMode()) {
        case KML_COLOR_MODE_COLOR:
            createColorColors(kmlOptions);
            break;
        case KML_COLOR_MODE_COLORSET:
            createColorSetColors(kmlOptions);
            break;
        case KML_COLOR_MODE_RAINBOW:
            createRainbowColors(kmlOptions, fileSetModel);
            break;
        }
        // Create the color styles
        int nColors = colors.length;
        for(String color : colors) {
            style = document.createAndAddStyle().withId(color);
            style.createAndSetLineStyle().withColor(color)
                .withWidth(kmlOptions.getTrkLineWidth());
            iconStyle = style.createAndSetIconStyle().withColor(color)
                .withColorMode(ColorMode.NORMAL)
                .withScale(kmlOptions.getIconScale());
            iconStyle.createAndSetIcon().withHref(kmlOptions.getTrkIconUrl());
        }

        // Waypoint icon
        style = document.createAndAddStyle().withId(WAYPOINT_ICON_ID);
        iconStyle = style.createAndSetIconStyle()
            .withColor(kmlOptions.getWptAlpha() + kmlOptions.getWptColor())
            .withColorMode(ColorMode.NORMAL)
            .withScale(kmlOptions.getIconScale());
        iconStyle.createAndSetIcon().withHref(kmlOptions.getWptIconUrl());

        // Loop over GPX files
        List<GpxTrackModel> trackModels;
        List<TrksegType> trackSegments;
        List<WptType> trackPoints;
        List<GpxWaypointModel> waypointModels;
        WptType waypoint;
        Folder folder;
        MultiGeometry mg;
        Placemark placemark;
        LineString ls = null;
        double lat, lon, alt;
        int nTrack = 0;
        String fileName;
        List<GpxFileModel> fileModels = fileSetModel.getGpxFileModels();
        for(GpxFileModel fileModel : fileModels) {
            if(!fileModel.getChecked()) {
                continue;
            }
            File file = fileModel.getFile();
            fileName = file.getPath();
            System.out.println(file.getPath());
            if(!file.exists()) {
                SWTUtils.errMsgAsync("File does not exist: " + fileName);
                continue;
            }
            // Create the Folder for this file
            folder = document.createAndAddFolder().withName(file.getName())
                .withOpen(true);

            // Loop over waypoints
            waypointModels = fileModel.getWaypointModels();
            for(GpxWaypointModel waypointModel : waypointModels) {
                if(!waypointModel.getChecked()) {
                    continue;
                }
                System.out.println(waypointModel.getLabel());
                waypoint = waypointModel.getWaypoint();
                lat = waypoint.getLat().doubleValue();
                lon = waypoint.getLon().doubleValue();
                alt = waypoint.getEle().doubleValue();
                // Make a Placemark with MultiGeometry
                placemark = folder.createAndAddPlacemark()
                    .withName(waypointModel.getLabel())
                    .withStyleUrl("#" + WAYPOINT_ICON_ID);
                placemark.createAndSetPoint().addToCoordinates(lon, lat, alt);
                // mg = placemark.createAndSetMultiGeometry();
                // point = mg.createAndAddPoint().addToCoordinates(lon, lat,
                // alt);
            }

            // Loop over tracks
            trackModels = fileModel.getTrackModels();
            boolean first = kmlOptions.getUseTrkIcon() ? true : false;
            for(GpxTrackModel trackModel : trackModels) {
                if(!trackModel.getChecked()) {
                    continue;
                }
                System.out.println(trackModel.getLabel());
                // Make a Placemark with MultiGeometry
                placemark = folder.createAndAddPlacemark()
                    .withName(trackModel.getLabel() + " Track")
                    .withStyleUrl("#" + colors[nTrack % nColors]);
                // Need MultiGeometry to handle non-connected segments
                mg = placemark.createAndSetMultiGeometry();
                trackSegments = trackModel.getTrack().getTrkseg();
                first = kmlOptions.getUseTrkIcon() ? true : false;
                for(TrksegType trackSegment : trackSegments) {
                    // Add a LineString to the MultiGeometry
                    ls = mg.createAndAddLineString().withExtrude(false)
                        .withTessellate(true);
                    trackPoints = trackSegment.getTrkpt();
                    for(WptType trackPoint : trackPoints) {
                        lat = trackPoint.getLat().doubleValue();
                        lon = trackPoint.getLon().doubleValue();
                        alt = trackPoint.getEle().doubleValue();
                        // Add coordinates to the LineString
                        ls.addToCoordinates(lon, lat, alt);
                        if(first) {
                            // Make a Placemark with an Icon at the first point
                            // on the track
                            first = false;
                            folder.createAndAddPlacemark()
                                .withName(trackModel.getLabel())
                                .withStyleUrl("#" + colors[nTrack % nColors])
                                .createAndSetPoint().addToCoordinates(lon, lat);
                        }
                    }
                }
                nTrack++;
            }
        }

        // Create the file
        final String kmlFileName = kmlOptions.getKmlFileName();
        final File outFile = new File(kmlFileName);
        if(kmlOptions.getPromptToOverwrite() && outFile.exists()) {
            Boolean res = SWTUtils.confirmMsg("File exists: "
                + outFile.getPath() + "\nOK to overwrite?");
            if(!res) {
                return;
            }
        }
        outFile.createNewFile();

        try {
            kml.marshal(outFile);
            // Send it to Google Earth
            if(kmlOptions.getSendToGoogle()) {
                System.out.println("Sending " + outFile.getPath()
                    + " to Google Earth");
                if(Desktop.isDesktopSupported()) {
                    final Desktop dt = Desktop.getDesktop();
                    if(dt.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            dt.open(outFile);
                        } catch(IOException ex) {
                            SWTUtils.excMsgAsync(
                                "Could not send to Google Earth", ex);
                        }
                    }
                } else {
                    SWTUtils.errMsgAsync("Could not send to Google Earth\n"
                        + "Desktop is not supported");
                }
            }
        } catch(FileNotFoundException ex) {
            SWTUtils.excMsgAsync("Could not marshal file: " + outFile, ex);
            ex.printStackTrace();
        } catch(Throwable t) {
            SWTUtils.excMsgAsync(
                "Error marshaling KML file: " + kmlOptions.getKmlFileName(), t);
            t.printStackTrace();
        }
    }

    private static void createColorColors(KmlOptions kmlOptions) {
        // Make a single element with the options track color
        colors = new String[1];
        colors[0] = kmlOptions.getTrkAlpha() + kmlOptions.getTrkColor();
    }

    private static void createColorSetColors(KmlOptions kmlOptions) {
        // Use the colorset colors with alpha prepended
        int nColors = colorSetColors.length;
        colors = new String[nColors];
        String alpha = kmlOptions.getTrkAlpha();
        // Insure alpha has two characters
        if(alpha.length() == 0) {
            alpha = "00";
        } else if(alpha.length() == 1) {
            alpha = "0" + alpha;
        } else if(alpha.length() > 2){
            alpha = alpha.substring(0, 2);
        }
        for(int i = 0; i < nColors; i++) {
            colors[i] = alpha + colorSetColors[i];
        }
    }

    private static void createRainbowColors(KmlOptions kmlOptions,
        GpxFileSetModel fileSetModel) {
        // Make a color for each track
        int nColors = 0;
        List<GpxFileModel> fileModels = fileSetModel.getGpxFileModels();
        for(GpxFileModel fileModel : fileModels) {
            if(!fileModel.getChecked()) {
                continue;
            }
            List<GpxTrackModel> trackModels = fileModel.getTrackModels();
            for(GpxTrackModel trackModel : trackModels) {
                if(!trackModel.getChecked()) {
                    continue;
                }
                nColors++;
            }
        }
        colors = new String[nColors];

        // Calculate the colors
        Color color;
        int red, green, blue;
        String alpha = kmlOptions.getTrkAlpha();
        // Insure alpha has two characters
        if(alpha.length() == 0) {
            alpha = "00";
        } else if(alpha.length() == 1) {
            alpha = "0" + alpha;
        } else if(alpha.length() > 2){
            alpha = alpha.substring(0, 2);
        }
        for(int i = 0; i < nColors; i++) {
            color = RainbowColorScheme.defineColor(i, nColors);
            red = color.getRed();
            green = color.getGreen();
            blue = color.getBlue();
            colors[i] = String.format("%s%02x%02x%02x", alpha, blue, green,
                red);
            System.out.println(colors[i]);
        }
        System.out.println("nColors=" + nColors);
    }

}
