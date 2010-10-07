package net.kenevans.gpxinspector.kml;

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
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Style;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class KmlUtils
{
    /** Determines if icons are shown at the start of a track */
    private static final boolean USE_ICONS = true;
    /** The icon scale, Use 1 for normal and 0 for label only (no icon) */
    private static final double ICON_SCALE = 1;
    /**
     * The URL for the home icon. Using white will allow the mask to be more
     * effective. (The default icon is Yellow = #ff00ffff).
     */
    private static final String HOME_ICON_URL = "http://maps.google.com/mapfiles/kml/pushpin/wht-pushpin.png";
    /**
     * The URL for the waypoint icon. Using white will allow the mask to be more
     * effective. (The default icon is Yellow = #ff00ffff).
     */
    private static final String WAYPOINT_ICON_URL = "http://maps.google.com/mapfiles/kml/paddle/wht-circle.png";
    /** The home icon color, actually a mask to & the image with. */
    private static final String HOME_ICON_COLOR = "ff0077ff";
    /** The waypoint icon color, actually a mask to & the image with. */
    private static final String WAYPOINT_ICON_COLOR = "ffffcc66";
    /** The track width. */
    private static final double TRACK_WIDTH = 2.0;
    private static final String HOME_ICON_ID = "HomeIcon";
    private static final String WAYPOINT_ICON_ID = "WaypointIcon";

    /** Specifies the transparency of the lines. */
    private static final String alpha = "ff";
    /** Set of line colors. it will cycle through these. */
    private static final String[] colors = {alpha + "0000ff", alpha + "00ff00",
        alpha + "ff0000", alpha + "ffff00", alpha + "ff00ff", alpha + "00ffff",
        alpha + "0077ff", alpha + "ff0077"};
    /** Number of line colors. */
    private static final int NCOLORS = colors.length;

    /**
     * @param args
     * @throws IOException
     */
    public static void createKml(GpxFileSetModel fileSetModel,
        final KmlOptions kmlOptions) throws IOException {
        // Generate the KML
        final Kml kml = KmlFactory.createKml();
        
        // Create the Document for this file
        Document document = kml.createAndSetDocument().withName("GPX Inspector")
            .withOpen(true);
        
        // Make the Styles for this Document
        Style style;
        IconStyle iconStyle;
        // Colors
        for(String color : colors) {
            style = document.createAndAddStyle().withId(color);
            style.createAndSetLineStyle().withColor(color)
                .withWidth(TRACK_WIDTH);
            iconStyle = style.createAndSetIconStyle().withColor(color)
                .withColorMode(ColorMode.NORMAL).withScale(ICON_SCALE);
            iconStyle.createAndSetIcon().withHref(HOME_ICON_URL);
        }
        // Home icon
        style = document.createAndAddStyle().withId(HOME_ICON_ID);
        iconStyle = style.createAndSetIconStyle().withColor(HOME_ICON_COLOR)
            .withColorMode(ColorMode.NORMAL).withScale(ICON_SCALE);
        iconStyle.createAndSetIcon().withHref(HOME_ICON_URL);
        // Waypoint icon
        style = document.createAndAddStyle().withId(WAYPOINT_ICON_ID);
        iconStyle = style.createAndSetIconStyle()
            .withColor(WAYPOINT_ICON_COLOR).withColorMode(ColorMode.NORMAL)
            .withScale(ICON_SCALE);
        iconStyle.createAndSetIcon().withHref(WAYPOINT_ICON_URL);

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
            boolean first = USE_ICONS ? true : false;
            for(GpxTrackModel trackModel : trackModels) {
                if(!trackModel.getChecked()) {
                    continue;
                }
                System.out.println(trackModel.getLabel());
                // Make a Placemark with MultiGeometry
                placemark = folder.createAndAddPlacemark()
                    .withName(trackModel.getLabel() + " Track")
                    .withStyleUrl("#" + colors[nTrack % NCOLORS]);
                // Need MultiGeometry to handle non-connected segments
                mg = placemark.createAndSetMultiGeometry();
                trackSegments = trackModel.getTrack().getTrkseg();
                first = USE_ICONS ? true : false;
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
                                .withStyleUrl("#" + colors[nTrack % NCOLORS])
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

}
