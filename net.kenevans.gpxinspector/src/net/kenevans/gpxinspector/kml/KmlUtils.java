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
import net.kenevans.gpxinspector.model.GpxRouteModel;
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
    static private boolean VERBOSE = false;
    /** Set of hard-coded line colorSetColors. it will cycle through these. */
    private static final String[] colorSetColors = {"0000ff", "00ff00",
        "ff0000", "ffff00", "ff00ff", "00ffff", "0077ff", "ff0077"};
    /** Array to hold the track colors, values depend on the mode. */
    private static String[] trkColors;
    /** Array to hold the route colors, values depend on the mode. */
    private static String[] rteColors;
    /** Array to hold the waypoint, values depend on the mode. */
    private static String[] wptColors;

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
        // Trk Colors
        switch(kmlOptions.getTrkColorMode()) {
        case KML_COLOR_MODE_COLOR:
            createTrkColorColors(kmlOptions);
            break;
        case KML_COLOR_MODE_COLORSET:
            createTrkColorSetColors(kmlOptions);
            break;
        case KML_COLOR_MODE_RAINBOW:
            createTrkRainbowColors(kmlOptions, fileSetModel);
            break;
        }
        // Rte Colors
        switch(kmlOptions.getRteColorMode()) {
        case KML_COLOR_MODE_COLOR:
            createRteColorColors(kmlOptions);
            break;
        case KML_COLOR_MODE_COLORSET:
            createRteColorSetColors(kmlOptions);
            break;
        case KML_COLOR_MODE_RAINBOW:
            createRteRainbowColors(kmlOptions, fileSetModel);
            break;
        }
        // Wpt Colors
        switch(kmlOptions.getWptColorMode()) {
        case KML_COLOR_MODE_COLOR:
            createWptColorColors(kmlOptions);
            break;
        case KML_COLOR_MODE_COLORSET:
            createWptColorSetColors(kmlOptions);
            break;
        case KML_COLOR_MODE_RAINBOW:
            createWptRainbowColors(kmlOptions, fileSetModel);
            break;
        }
        // Create the color styles
        int nTrkColors = trkColors.length;
        for(String color : trkColors) {
            style = document.createAndAddStyle().withId("trk" + color);
            style.createAndSetLineStyle().withColor(color)
                .withWidth(kmlOptions.getTrkLineWidth());
            iconStyle = style.createAndSetIconStyle().withColor(color)
                .withColorMode(ColorMode.NORMAL)
                .withScale(kmlOptions.getIconScale());
            iconStyle.createAndSetIcon().withHref(kmlOptions.getTrkIconUrl());
        }
        int nRteColors = rteColors.length;
        for(String color : rteColors) {
            style = document.createAndAddStyle().withId("rte" + color);
            style.createAndSetLineStyle().withColor(color)
                .withWidth(kmlOptions.getTrkLineWidth());
            iconStyle = style.createAndSetIconStyle().withColor(color)
                .withColorMode(ColorMode.NORMAL)
                .withScale(kmlOptions.getIconScale());
            iconStyle.createAndSetIcon().withHref(kmlOptions.getRteIconUrl());
        }
        int nWptColors = wptColors.length;
        for(String color : wptColors) {
            style = document.createAndAddStyle().withId("wpt" + color);
            style.createAndSetLineStyle().withColor(color)
                .withWidth(kmlOptions.getTrkLineWidth());
            iconStyle = style.createAndSetIconStyle().withColor(color)
                .withColorMode(ColorMode.NORMAL)
                .withScale(kmlOptions.getIconScale());
            iconStyle.createAndSetIcon().withHref(kmlOptions.getWptIconUrl());
        }

        // // Routepoint icon
        // style = document.createAndAddStyle().withId(ROUTEPOINT_ICON_ID);
        // style.createAndSetLineStyle()
        // .withColor(kmlOptions.getRteAlpha() + kmlOptions.getRteColor())
        // .withWidth(kmlOptions.getRteLineWidth());
        // iconStyle = style.createAndSetIconStyle()
        // .withColor(kmlOptions.getRteAlpha() + kmlOptions.getRteColor())
        // .withColorMode(ColorMode.NORMAL)
        // .withScale(kmlOptions.getIconScale());
        // iconStyle.createAndSetIcon().withHref(kmlOptions.getRteIconUrl());

        // Loop over GPX files
        int nTrack = 0;
        int nRoute = 0;
        int nWaypoint = 0;
        List<GpxWaypointModel> waypointModels;
        WptType waypoint;
        Folder fileFolder;
        Folder folder;
        Folder routeFolder;
        MultiGeometry mg;
        Placemark placemark;
        LineString ls = null;
        double lat, lon, alt;
        String fileName;
        List<GpxFileModel> fileModels = fileSetModel.getGpxFileModels();
        for(GpxFileModel fileModel : fileModels) {
            if(!fileModel.getChecked()) {
                continue;
            }
            File file = fileModel.getFile();
            fileName = file.getPath();
            if(VERBOSE) {
                System.out.println(file.getPath());
            }
            if(!file.exists()) {
                SWTUtils.errMsgAsync("File does not exist: " + fileName);
                continue;
            }
            // Create the Folder for this file
            fileFolder = document.createAndAddFolder().withName(file.getName())
                .withOpen(true);

            // Loop over waypoints
            waypointModels = fileModel.getWaypointModels();
            if(waypointModels.size() > 0) {
                folder = fileFolder.createAndAddFolder().withName("Waypoints")
                    .withOpen(true);
            } else {
                folder = null;
            }
            for(GpxWaypointModel waypointModel : waypointModels) {
                if(!waypointModel.getChecked()) {
                    continue;
                }
                if(VERBOSE) {
                    System.out.println(waypointModel.getLabel());
                }
                waypoint = waypointModel.getWaypoint();
                if(waypoint.getLat() != null) {
                    lat = waypoint.getLat().doubleValue();
                } else {
                    lat = 0;
                }
                if(waypoint.getLon() != null) {
                    lon = waypoint.getLon().doubleValue();
                } else {
                    lon = 0;
                }
                if(waypoint.getEle() != null) {
                    alt = waypoint.getEle().doubleValue();
                } else {
                    alt = 0;
                }
                // Make a Placemark
                placemark = folder.createAndAddPlacemark()
                    .withName(waypointModel.getLabel())
                    .withStyleUrl("#wpt" + wptColors[nWaypoint % nWptColors]);
                placemark.createAndSetPoint().addToCoordinates(lon, lat, alt);
                nWaypoint++;
            }

            // Loop over tracks
            List<GpxTrackModel> trackModels;
            List<TrksegType> trackSegments;
            List<WptType> trackPoints;
            trackModels = fileModel.getTrackModels();
            boolean first = kmlOptions.getUseTrkIcon() ? true : false;
            if(trackModels.size() > 0) {
                folder = fileFolder.createAndAddFolder().withName("Tracks")
                    .withOpen(true);
            } else {
                folder = null;
            }
            for(GpxTrackModel trackModel : trackModels) {
                if(!trackModel.getChecked()) {
                    continue;
                }
                if(VERBOSE) {
                    System.out.println(trackModel.getLabel());
                }
                // Make a Placemark with MultiGeometry
                placemark = folder.createAndAddPlacemark()
                    .withName(trackModel.getLabel() + " Lines")
                    .withStyleUrl("#trk" + trkColors[nTrack % nTrkColors]);
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
                        if(trackPoint.getLat() != null) {
                            lat = trackPoint.getLat().doubleValue();
                        } else {
                            lat = 0;
                        }
                        if(trackPoint.getLon() != null) {
                            lon = trackPoint.getLon().doubleValue();
                        } else {
                            lon = 0;
                        }
                        if(trackPoint.getEle() != null) {
                            alt = trackPoint.getEle().doubleValue();
                        } else {
                            alt = 0;
                        }
                        // Add coordinates to the LineString
                        if(first) {
                            // Make a Placemark with an Icon at the first point
                            // on the track
                            first = false;
                            folder
                                .createAndAddPlacemark()
                                .withName(trackModel.getLabel())
                                .withStyleUrl(
                                    "#trk" + trkColors[nTrack % nTrkColors])
                                .createAndSetPoint().addToCoordinates(lon, lat);
                        }
                        ls.addToCoordinates(lon, lat, alt);
                    }
                }
                nTrack++;
            }

            // Loop over routes
            List<GpxRouteModel> routeModels;
            List<WptType> routePoints;
            routeModels = fileModel.getRouteModels();
            first = kmlOptions.getUseRteIcon() ? true : false;
            if(routeModels.size() > 0) {
                folder = fileFolder.createAndAddFolder().withName("Routes")
                    .withOpen(true);
            } else {
                folder = null;
            }
            for(GpxRouteModel routeModel : routeModels) {
                if(!routeModel.getChecked()) {
                    continue;
                }
                if(VERBOSE) {
                    System.out.println(routeModel.getLabel());
                }
                routeFolder = folder.createAndAddFolder()
                    .withName(routeModel.getLabel()).withOpen(true);
                // Make a Placemark with MultiGeometry
                placemark = routeFolder.createAndAddPlacemark()
                    .withName(routeModel.getLabel() + " Lines")
                    .withStyleUrl("#rte" + rteColors[nRoute % nRteColors]);
                // Need MultiGeometry to handle non-connected segments
                mg = placemark.createAndSetMultiGeometry();
                routePoints = routeModel.getRoute().getRtept();
                first = kmlOptions.getUseTrkIcon() ? true : false;
                // Add a LineString to the MultiGeometry
                ls = mg.createAndAddLineString().withExtrude(false)
                    .withTessellate(true);
                for(WptType rtePoint : routePoints) {
                    if(rtePoint.getLat() != null) {
                        lat = rtePoint.getLat().doubleValue();
                    } else {
                        lat = 0;
                    }
                    if(rtePoint.getLon() != null) {
                        lon = rtePoint.getLon().doubleValue();
                    } else {
                        lon = 0;
                    }
                    if(rtePoint.getEle() != null) {
                        alt = rtePoint.getEle().doubleValue();
                    } else {
                        alt = 0;
                    }
                    if(first) {
                        // Make a Placemark with an Icon at the first point
                        // on the track
                        first = false;
                        routeFolder
                            .createAndAddPlacemark()
                            .withName(routeModel.getLabel())
                            .withStyleUrl(
                                "#rte" + rteColors[nRoute % nRteColors])
                            .createAndSetPoint().addToCoordinates(lon, lat);
                    }
                    // Make a Placemark
                    placemark = routeFolder.createAndAddPlacemark()
                        .withName(rtePoint.getName())
                        .withStyleUrl("#rte" + rteColors[nRoute % nRteColors]);
                    placemark.createAndSetPoint().addToCoordinates(lon, lat,
                        alt);
                    ls.addToCoordinates(lon, lat, alt);
                }
            }
            nRoute++;
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
                if(VERBOSE) {
                    System.out.println("Sending " + outFile.getPath()
                        + " to Google Earth");
                }
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

    /**
     * Create the trkColors array when the mode is KML_COLOR_MODE_COLOR.
     * 
     * @param kmlOptions
     */
    private static void createTrkColorColors(KmlOptions kmlOptions) {
        // Make a single element with the options track color
        trkColors = new String[1];
        trkColors[0] = kmlOptions.getTrkAlpha() + kmlOptions.getTrkColor();
    }

    /**
     * Create the trkColors array when the mode is KML_COLOR_MODE_COLORSET.
     * 
     * @param kmlOptions
     */
    private static void createTrkColorSetColors(KmlOptions kmlOptions) {
        // Use the hard-coded colorset colors with alpha prepended
        int nColors = colorSetColors.length;
        trkColors = new String[nColors];
        String alpha = kmlOptions.getTrkAlpha();
        // Insure alpha has two characters
        if(alpha.length() == 0) {
            alpha = "00";
        } else if(alpha.length() == 1) {
            alpha = "0" + alpha;
        } else if(alpha.length() > 2) {
            alpha = alpha.substring(0, 2);
        }
        for(int i = 0; i < nColors; i++) {
            trkColors[i] = alpha + colorSetColors[i];
        }
    }

    /**
     * Create the trkColors array when the mode is KML_COLOR_MODE_RAINBOW.
     * 
     * @param kmlOptions
     */
    private static void createTrkRainbowColors(KmlOptions kmlOptions,
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
        trkColors = new String[nColors];

        // Calculate the trkColors
        Color color;
        int red, green, blue;
        String alpha = kmlOptions.getTrkAlpha();
        // Insure alpha has two characters
        if(alpha.length() == 0) {
            alpha = "00";
        } else if(alpha.length() == 1) {
            alpha = "0" + alpha;
        } else if(alpha.length() > 2) {
            alpha = alpha.substring(0, 2);
        }
        for(int i = 0; i < nColors; i++) {
            color = RainbowColorScheme.defineColor(i, nColors);
            red = color.getRed();
            green = color.getGreen();
            blue = color.getBlue();
            trkColors[i] = String.format("%s%02x%02x%02x", alpha, blue, green,
                red);
            // DEBUG
            // System.out.println(trkColors[i]);
        }
        // DEBUG
        // System.out.println("nColors=" + nColors);
    }

    /**
     * Create the rteColors array when the mode is KML_COLOR_MODE_COLOR.
     * 
     * @param kmlOptions
     */
    private static void createRteColorColors(KmlOptions kmlOptions) {
        // Make a single element with the options route color
        rteColors = new String[1];
        rteColors[0] = kmlOptions.getRteAlpha() + kmlOptions.getRteColor();
    }

    /**
     * Create the rteColors array when the mode is KML_COLOR_MODE_COLORSET.
     * 
     * @param kmlOptions
     */
    private static void createRteColorSetColors(KmlOptions kmlOptions) {
        // Use the hard-coded colorset colors with alpha prepended
        int nColors = colorSetColors.length;
        rteColors = new String[nColors];
        String alpha = kmlOptions.getRteAlpha();
        // Insure alpha has two characters
        if(alpha.length() == 0) {
            alpha = "00";
        } else if(alpha.length() == 1) {
            alpha = "0" + alpha;
        } else if(alpha.length() > 2) {
            alpha = alpha.substring(0, 2);
        }
        for(int i = 0; i < nColors; i++) {
            rteColors[i] = alpha + colorSetColors[i];
        }
    }

    /**
     * Create the rteColors array when the mode is KML_COLOR_MODE_RAINBOW.
     * 
     * @param kmlOptions
     */
    private static void createRteRainbowColors(KmlOptions kmlOptions,
        GpxFileSetModel fileSetModel) {
        // Make a color for each route
        int nColors = 0;
        List<GpxFileModel> fileModels = fileSetModel.getGpxFileModels();
        for(GpxFileModel fileModel : fileModels) {
            if(!fileModel.getChecked()) {
                continue;
            }
            List<GpxRouteModel> routeModels = fileModel.getRouteModels();
            for(GpxRouteModel routeModel : routeModels) {
                if(!routeModel.getChecked()) {
                    continue;
                }
                nColors++;
            }
        }
        rteColors = new String[nColors];

        // Calculate the rteColors
        Color color;
        int red, green, blue;
        String alpha = kmlOptions.getRteAlpha();
        // Insure alpha has two characters
        if(alpha.length() == 0) {
            alpha = "00";
        } else if(alpha.length() == 1) {
            alpha = "0" + alpha;
        } else if(alpha.length() > 2) {
            alpha = alpha.substring(0, 2);
        }
        for(int i = 0; i < nColors; i++) {
            color = RainbowColorScheme.defineColor(i, nColors);
            red = color.getRed();
            green = color.getGreen();
            blue = color.getBlue();
            rteColors[i] = String.format("%s%02x%02x%02x", alpha, blue, green,
                red);
            // DEBUG
            // System.out.println(rteColors[i]);
        }
        // DEBUG
        // System.out.println("nColors=" + nColors);
    }

    /**
     * Create the wptColors array when the mode is KML_COLOR_MODE_COLOR.
     * 
     * @param kmlOptions
     */
    private static void createWptColorColors(KmlOptions kmlOptions) {
        // Make a single element with the options waypoint color
        wptColors = new String[1];
        wptColors[0] = kmlOptions.getWptAlpha() + kmlOptions.getWptColor();
    }

    /**
     * Create the wptColors array when the mode is KML_COLOR_MODE_COLORSET.
     * 
     * @param kmlOptions
     */
    private static void createWptColorSetColors(KmlOptions kmlOptions) {
        // Use the hard-coded colorset colors with alpha prepended
        int nColors = colorSetColors.length;
        wptColors = new String[nColors];
        String alpha = kmlOptions.getWptAlpha();
        // Insure alpha has two characters
        if(alpha.length() == 0) {
            alpha = "00";
        } else if(alpha.length() == 1) {
            alpha = "0" + alpha;
        } else if(alpha.length() > 2) {
            alpha = alpha.substring(0, 2);
        }
        for(int i = 0; i < nColors; i++) {
            wptColors[i] = alpha + colorSetColors[i];
        }
    }

    /**
     * Create the wptColors array when the mode is KML_COLOR_MODE_RAINBOW.
     * 
     * @param kmlOptions
     */
    private static void createWptRainbowColors(KmlOptions kmlOptions,
        GpxFileSetModel fileSetModel) {
        // Make a color for each waypoint
        int nColors = 0;
        List<GpxFileModel> fileModels = fileSetModel.getGpxFileModels();
        for(GpxFileModel fileModel : fileModels) {
            if(!fileModel.getChecked()) {
                continue;
            }
            List<GpxWaypointModel> waypointModels = fileModel
                .getWaypointModels();
            for(GpxWaypointModel waypointModel : waypointModels) {
                if(!waypointModel.getChecked()) {
                    continue;
                }
                nColors++;
            }
        }
        wptColors = new String[nColors];

        // Calculate the wptColors
        Color color;
        int red, green, blue;
        String alpha = kmlOptions.getWptAlpha();
        // Insure alpha has two characters
        if(alpha.length() == 0) {
            alpha = "00";
        } else if(alpha.length() == 1) {
            alpha = "0" + alpha;
        } else if(alpha.length() > 2) {
            alpha = alpha.substring(0, 2);
        }
        for(int i = 0; i < nColors; i++) {
            color = RainbowColorScheme.defineColor(i, nColors);
            red = color.getRed();
            green = color.getGreen();
            blue = color.getBlue();
            wptColors[i] = String.format("%s%02x%02x%02x", alpha, blue, green,
                red);
            // DEBUG
            // System.out.println(wptColors[i]);
        }
        // DEBUG
        // System.out.println("nColors=" + nColors);
    }

}
