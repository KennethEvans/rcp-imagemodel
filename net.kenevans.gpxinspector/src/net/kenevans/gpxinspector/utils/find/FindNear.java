package net.kenevans.gpxinspector.utils.find;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.kenevans.gpx.TrksegType;
import net.kenevans.gpx.WptType;
import net.kenevans.gpxinspector.model.GpxFileModel;
import net.kenevans.gpxinspector.model.GpxFileSetModel;
import net.kenevans.gpxinspector.model.GpxTrackModel;
import net.kenevans.gpxinspector.model.GpxWaypointModel;
import net.kenevans.gpxinspector.utils.GpsUtils;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.utils.Utils;
import net.kenevans.gpxinspector.utils.find.FindNearOptions.Units;

/*
 * Created on Sep 6, 2010
 * By Kenneth Evans, Jr.
 */

public class FindNear
{
    private static final String DEFAULT_PATH = "c:\\Documents and Settings\\evans\\My Documents\\GPSLink";

    /**
     * Mode<br>
     * <br>
     * PRINT: Prints to the out and err PrintStreams.<br>
     * CHECK: Checks the waypoints and tracks in a given GpxFileSetModel.<br>
     * ADD: Finds matching files and adds the found tracks and waypoints to a
     * given GpxFileSetModel.<br>
     * 
     * @author Kenneth Evans, Jr.
     */
    public static enum Mode {
        PRINT, CHECK, ADD
    };

    private Mode mode = Mode.PRINT;

    private boolean ok = true;
    private boolean dirSpecified = false;
    private boolean latSpecified = false;
    private boolean lonSpecified = false;

    private FindNearOptions options;
    /**
     * The PrintStream used for output. May be specified by setOutStream. May be
     * null. Defaults to System.out.
     */
    private PrintStream outStream = System.out;
    /**
     * The PrintStream used for errors. May be specified by setErrStream. May be
     * null. Defaults to System.err.
     */
    private PrintStream errStream = System.err;
    /**
     * The GpxFileSetModel used with Mode.ADD.
     */
    private GpxFileSetModel gpxFileSetModel;

    /**
     * FindNear constructor that takes command-line arguments.
     * 
     * @param args Command-line arguments.
     */
    FindNear(String args[]) {
        options = new FindNearOptions();
        ok = parseCommand(args);
    }

    /**
     * FindTracks constructor
     * 
     * @param latitude
     * @param longitude
     * @param deltaLat
     * @param deltaLon
     */
    public FindNear(double latitude, double longitude, double radius,
        Units units) {
        options = new FindNearOptions();
        options.setLatitude(latitude);
        options.setLongitude(longitude);
        options.setRadius(radius);
        options.setUnits(units);
    }

    /**
     * FindTracks constructor
     * 
     * @param latitude
     * @param longitude
     */
    public FindNear(double latitude, double longitude) {
        options = new FindNearOptions();
        options.setLatitude(latitude);
        options.setLongitude(longitude);
    }

    /**
     * @return Returns the ok.
     */
    public boolean isOk() {
        return ok;
    }

    /**
     * Searches the directory and its subdirectories with the given options to
     * find tracks with waypoints and trackpoints inside the limits. Is probably
     * only useful for Mode.ADD since setting the gpxFileSetModel is only useful
     * for that mode.
     * 
     * @param gpxFileSetModel The gpxFileSetModel to use.
     * @param options The options to use.
     * @param mode The mode to use.
     */
    public void find(GpxFileSetModel gpxFileSetModel, FindNearOptions options,
        Mode mode) {
        // TODO Print a warning
        if(mode != Mode.ADD) {
            SWTUtils
                .warnMsgAsync("find(GpxFileSetModel, FindNearOptions, Mode)"
                    + " is intended for Mode.ADD");
        }
        this.gpxFileSetModel = gpxFileSetModel;
        this.options = options;
        this.mode = mode;
        find();
    }

    /**
     * Searches the directory and its subdirectories with the given options to
     * find tracks with waypoints and trackpoints inside the limits.
     * 
     * @param options
     */
    public void find(FindNearOptions options) {
        this.options = options;
        find();
    }

    /**
     * Searches the default directory and subdirectories to find tracks with a
     * trackpoint inside the limits.
     */
    public void find() {
        File dir = new File(options.getDirName());
        find(dir);
    }

    /**
     * Searches the given directory and subdirectories to find waypoints and
     * tracks with a waypoint or trackpoint inside the limits. Uses the given
     * options.
     * 
     * @param dir
     * @param options
     */
    public void find(File dir, FindNearOptions options) {
        this.options = options;
        find(dir);
    }

    /**
     * Searches the given directory and subdirectories to find waypoints and
     * tracks with a waypoint or trackpoint inside the limits.
     * 
     * @param dir
     */
    public void find(File dir) {
        if(dir == null) {
            Utils.errMsg("Invalid directory");
            System.exit(1);
        }
        if(!dir.isDirectory()) {
            Utils.errMsg("Not a directory");
            System.exit(1);
        }

        File dirList[] = dir.listFiles();
        int len = dirList.length;

        int nFiles = 0;
        int nDirs = 0;
        for(int i = 0; i < len; i++) {
            File file = dirList[i];
            if(file.isDirectory()) {
                nDirs++;
                find(file);
            } else {
                nFiles++;
                process(file);
            }
        }
    }

    /**
     * Processes a single .gpsl file. Checks the extension first, then calls
     * readAndProcess(File file, double[] limits).
     * 
     * @param file
     */
    private void process(File file) {
        if(!file.exists()) return;
        String ext = Utils.getExtension(file);
        if(ext == null) return;
        if(options.getDoGpx() && ext.toLowerCase().equals("gpx")) {
            try {
                readAndProcessGpxFile(file);
            } catch(Exception ex) {
                Utils.excMsg("Eror parsing GPX file", ex);
            }
        } else if(options.getDoGpsl() && ext.toLowerCase().equals("gpsl")) {
            try {
                readAndProcessGpslFile(file);
            } catch(Exception ex) {
                Utils.excMsg("Eror parsing GPSL file", ex);
            }
        }
    }

    /**
     * Reads and processes a single .gpx file.
     * 
     * @param file
     */
    /**
     * @param file The file.
     */
    public void readAndProcessGpxFile(File file) {
        // Create a new GpxFileModel with no parent
        GpxFileModel fileModel = null;
        try {
            // Create with a null parent
            fileModel = new GpxFileModel(null, file);
        } catch(JAXBException ex) {
            if(errStream == null) {
                Utils.excMsg("Error parsing " + file.getPath(), ex);
            } else {
                errStream.println("Error parsing " + file.getPath());
                errStream.println(ex.getMessage());
            }
            return;
        }
        if(fileModel == null) {
            Utils.errMsg("readAndProcessGpxFile: fileModel is null");
            return;
        }
        processGpxFileModel(fileModel);
    }

    /**
     * Process a single GpxFileModel using the given options and the given mode.
     * 
     * @see #mode
     * 
     * @param fileModel The GpxFileModel.
     * @param mode The mode to use
     * @param options The options to use.
     */
    public void processGpxFileModel(GpxFileModel fileModel, Mode mode,
        FindNearOptions options) {
        this.options = options;
        this.mode = mode;
        processGpxFileModel(fileModel);
    }

    /**
     * Process a single GpxFileModel. Uses the current mode and options.
     * 
     * @param fileModel The GpxFileModel.
     */
    public void processGpxFileModel(GpxFileModel fileModel) {
        boolean fileNamePrinted = false;
        boolean wptPrinted = false;
        boolean trkPrinted = false;
        // The radius in miles
        double radius = options.getUnits().radiusInMiles(options.getRadius());
        // Note these lists always exit, but they may be empty
        List<GpxWaypointModel> waypointModels = fileModel.getWaypointModels();
        List<GpxTrackModel> trackModels = fileModel.getTrackModels();
        ;
        List<TrksegType> trackSegments;
        List<WptType> trackPoints;
        WptType waypoint;
        double lat0 = options.getLatitude();
        double lon0 = options.getLongitude();
        double lat, lon;
        boolean foundWaypoints = false;
        boolean foundTracks = false;
        boolean found = false;

        // Loop over waypoints
        if(options.getDoWpt()) {
            foundWaypoints = false;
            for(GpxWaypointModel waypointModel : waypointModels) {
                found = false;
                waypoint = waypointModel.getWaypoint();
                lat = waypoint.getLat().doubleValue();
                lon = waypoint.getLon().doubleValue();
                if(GpsUtils.greatCircleDistance(lat0, lon0, lat, lon) <= radius) {
                    found = true;
                    foundWaypoints = true;
                }
                // Handle mode
                if(mode == Mode.PRINT) {
                    if(found) {
                        if(!fileNamePrinted) {
                            if(outStream != null) {
                                outStream.println(fileModel.getFile()
                                    .getAbsolutePath());
                            }
                            fileNamePrinted = true;
                        }
                        if(!wptPrinted) {
                            if(outStream != null) {
                                outStream.println(" Waypoints");
                            }
                            wptPrinted = true;
                        }
                        if(outStream != null) {
                            outStream.println("  "
                                + waypointModel.getWaypoint().getName());
                        }
                    }
                } else if(mode == Mode.CHECK) {
                    waypointModel.setChecked(found);
                } else if(mode == Mode.ADD) {
                    waypointModel.setChecked(found);
                }
            }
        }

        // Loop over tracks
        if(options.getDoTrk()) {
            for(GpxTrackModel trackModel : trackModels) {
                found = false;
                trackSegments = trackModel.getTrack().getTrkseg();
                for(TrksegType trackSegment : trackSegments) {
                    trackPoints = trackSegment.getTrkpt();
                    for(WptType trackPoint : trackPoints) {
                        lat = trackPoint.getLat().doubleValue();
                        lon = trackPoint.getLon().doubleValue();
                        if(GpsUtils.greatCircleDistance(lat0, lon0, lat, lon) <= radius) {
                            foundTracks = true;
                            found = true;
                        }
                        // Handle printing or checking
                        if(mode == Mode.PRINT) {
                            if(found) {
                                if(!fileNamePrinted) {
                                    if(outStream != null) {
                                        outStream.println(fileModel.getFile()
                                            .getAbsolutePath());
                                    }
                                    fileNamePrinted = true;
                                }
                                if(!trkPrinted) {
                                    if(outStream != null) {
                                        outStream.println(" Tracks");
                                    }
                                    trkPrinted = true;
                                }
                                if(outStream != null) {
                                    outStream.println("  "
                                        + trackModel.getTrack().getName());
                                }
                            }
                        } else if(mode == Mode.CHECK) {
                            trackModel.setChecked(found);
                        } else if(mode == Mode.ADD) {
                            trackModel.setChecked(found);
                        }
                        if(found) {
                            // Done with this track, don't need more trackpoints
                            break;
                        }
                    }
                    // Done with this track, don't need more segments
                    if(found) {
                        break;
                    }
                }
            }
        }

        // Done searching this fileModel, handle Mode.ADD.
        if(mode == Mode.ADD) {
            if(!foundWaypoints && !foundTracks) {
                // Nothing was found, so don't add anything
                return;
            }
            // Trim the waypoints
            if(waypointModels != null) {
                if(!foundWaypoints) {
                    // Clear the waypoints
                    waypointModels.clear();
                } else {
                    // Remove all unchecked waypoints. Do in two steps
                    // to avoid ConcurrentModificationException.
                    List<GpxWaypointModel> waypointModels1 = new ArrayList<GpxWaypointModel>(
                        waypointModels.size());
                    for(GpxWaypointModel model : waypointModels) {
                        if(!model.getChecked()) {
                            waypointModels1.add(model);
                        }
                    }
                    for(GpxWaypointModel model : waypointModels1) {
                        // Does dispose, fires event, etc.
                        fileModel.remove(model);
                    }
                    waypointModels1.clear();
                }
            }
            // Trim the tracks
            if(trackModels != null) {
                if(!foundTracks) {
                    // Clear the tracks
                    trackModels.clear();
                } else {
                    // Remove all unchecked tracks. Do in two steps
                    // to avoid ConcurrentModificationException.
                    List<GpxTrackModel> trackModels1 = new ArrayList<GpxTrackModel>(
                        trackModels.size());
                    for(GpxTrackModel model : trackModels) {
                        if(!model.getChecked()) {
                            trackModels1.add(model);
                        }
                    }
                    for(GpxTrackModel model : trackModels1) {
                        // Does dispose, fires event, etc.
                        fileModel.remove(model);
                    }
                    trackModels1.clear();
                }
            }
            // Add the fileModel to the gpxFileSetModel
            if(gpxFileSetModel != null) {
                // Note that add will set the parent (was constructed with a
                // null one)
                // fileModel.setParent(gpxFileSetModel);
                gpxFileSetModel.add(fileModel);
            }
        }
    }

    /**
     * Reads and processes a single .gpsl file. Only works for Mode.PRINT, which
     * is the default. Currently only does tracks.
     * 
     * @param file
     */
    // TODO Implement waypoints
    public void readAndProcessGpslFile(File file) {
        // For safety
        if(mode != Mode.PRINT) {
            return;
        }
        // The radius in miles
        double radius = options.getUnits().radiusInMiles(options.getRadius());
        String GPSLINK_ID = "!GPSLINK";
        String DELIMITER = "Delimiter";
        String GMTOFFSET = "GMTOffset";
        boolean fileNamePrinted = false;
        // boolean wptPrinted = false;
        boolean trkPrinted = false;
        double lat0 = options.getLatitude();
        double lon0 = options.getLongitude();
        long lineNum = 0;
        boolean error = false;
        String line = null;
        String[] tokens = null;
        String delimiter = "\t";
        // String name = null;
        // double fileGMTOffsetHr = 0.0;
        String trackName = null;
        boolean trkDataInProgress = false;
        // boolean trkDataAborted = false;

        double lat;
        double lon;
        // double altitude;
        // String symbol = null;
        // String time = "";
        // boolean startTrack = false;
        boolean found = false;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));

            // Read ID
            line = in.readLine();
            lineNum++;
            if(line == null) {
                Utils.errMsg("Unexpected end of file at line " + lineNum
                    + ":\n" + file.getName());
                return;
            }
            if(!line.equals(GPSLINK_ID)) {
                Utils.errMsg("Invalid GPSLink file (Bad ID) at line " + lineNum
                    + ":\n" + file.getName());
                return;
            }

            // Read timestamp
            line = in.readLine();
            lineNum++;
            if(line == null) {
                Utils.errMsg("Unexpected end of file at line " + lineNum
                    + ":\n" + file.getName());
                return;
            }

            // Delimiter
            line = in.readLine();
            lineNum++;
            if(line == null) {
                Utils.errMsg("Unexpected end of file at line " + lineNum
                    + ":\n" + file.getName());
                return;
            }
            tokens = line.split("=");
            if(tokens.length < 2 || !tokens[0].equals(DELIMITER)) {
                Utils.warnMsg("No delimiter found at line + lineNum "
                    + ", assuming TAB:\n" + file.getName());
                delimiter = "\t";
            }
            delimiter = tokens[1];
            if(!delimiter.equals(",") && !delimiter.equals("\t")) {
                Utils.warnMsg("Invalid delimiter found at line + lineNum "
                    + ", assuming TAB:\n" + file.getName());
                delimiter = "\t";
            }

            // GMTOffset
            line = in.readLine();
            lineNum++;
            if(line == null) {
                Utils.errMsg("Unexpected end of file at line " + lineNum
                    + ":\n" + file.getName());
                return;
            }
            tokens = line.split("=");
            if(tokens.length < 2 || !tokens[0].equals(GMTOFFSET)) {
                Utils.warnMsg("No " + GMTOFFSET + " found at " + lineNum
                    + ", assuming 0:\n" + file.getName());
                // fileGMTOffsetHr = 0.0;
            } else {
                // fileGMTOffsetHr = Double.valueOf(tokens[1]).doubleValue();
            }

            // Loop over the rest of the lines
            while((line = in.readLine()) != null) {
                lineNum++;

                // Skip comments
                if(line.startsWith("#")) continue;
                // Insure there at least two characters
                if(line.length() < 2) continue;
                // Only handle lines that have a type identifier
                if(!line.subSequence(1, 2).equals(delimiter)) continue;

                // Branch on type
                tokens = line.split(delimiter);
                String startChar = line.substring(0, 1);
                // Only look at tracks and trackpoints
                if(startChar.equals("H")) {
                    // Track
                    trackName = tokens[1];
                    if(trackName == null) {
                        Utils.errMsg("Line " + lineNum
                            + " Cannot create track:\n" + file.getName());
                        error = true;
                        break;
                    }
                    trkDataInProgress = true;
                    // trkDataAborted = false;
                    found = false;
                } else if(startChar.equals("T")) {
                    // TrackPoint
                    if(found) continue;
                    if(tokens.length < 6) {
                        Utils.errMsg("Line " + lineNum
                            + ": invalid trackpoint:\n" + file.getName());
                        error = true;
                        break;
                    }
                    // name = tokens[1];
                    ;
                    lat = Double.valueOf(tokens[2]).doubleValue();
                    lon = Double.valueOf(tokens[3]).doubleValue();
                    // altitude = Double.valueOf(tokens[4]).doubleValue();
                    // symbol = "";
                    // time = tokens[5];

                    if(!trkDataInProgress) {
                        Utils.errMsg("Line " + lineNum
                            + " Found trackpoint without track:\n"
                            + file.getName());
                    }

                    // Check
                    if(GpsUtils.greatCircleDistance(lat0, lon0, lat, lon) <= radius) {
                        found = true;
                        if(!fileNamePrinted) {
                            if(outStream != null) {
                                outStream.println(file.getAbsolutePath());
                            }
                            fileNamePrinted = true;
                        }
                        if(!trkPrinted) {
                            if(outStream != null) {
                                outStream.println(" Tracks");
                            }
                            trkPrinted = true;
                        }
                        if(outStream != null) {
                            outStream.println("  " + trackName);
                        }
                    }
                }
            }
            if(in != null) in.close();
            if(error) return;
        } catch(Exception ex) {
            Utils.errMsg("Error reading " + file.getName() + "\nat line "
                + lineNum + "\n" + ex + "\n" + ex.getMessage());
        }
    }

    /**
     * Finds files using the given FindNearOptions including the dirName.
     * Deletes all waypoints and tracks that do not satisfy the radius criteria,
     * leaving only the ones that do. Adds the found and trimmed files to the
     * given GpxFileSetModel.<br>
     * <br>
     * Sets the out and error PrintStreams to get the output, and resets them
     * when finished.<br>
     * <br>
     * This is an inefficient algorithm that relies on parsing the output from
     * find(File, FindNearOptions).
     * 
     * @see #find(File, FindNearOptions)
     * 
     * @param gpxFileSetModel
     * @param options
     * 
     * @return The error output from the find(File, FindNearOptions).
     */
    public String findAndAddToFileSetModel(GpxFileSetModel gpxFileSetModel,
        FindNearOptions options) {
        if(options == null) {
            SWTUtils.errMsgAsync("FindNearOptions is null");
            return null;
        }
        if(gpxFileSetModel == null) {
            SWTUtils.errMsgAsync("GpxFileSetModel is null");
            return null;
        }
        String dirName = options.getDirName();
        if(dirName == null) {
            SWTUtils.errMsgAsync("Search directory is null");
            return null;
        }
        File file = new File(dirName);
        if(!file.exists()) {
            SWTUtils
                .errMsgAsync("Search directory does not exist:\n" + dirName);
            return null;
        }
        if(!file.isDirectory()) {
            SWTUtils.errMsgAsync("Search directory is not a directory:\n"
                + dirName);
            return null;
        }

        FindNear fn = new FindNear(options.getLatitude(),
            options.getLongitude(), options.getRadius(), options.getUnits());
        if(fn == null) {
            SWTUtils.errMsgAsync("Could not create FindNear instance");
            return null;
        }
        fn.setOptions(options);
        // Don't do GPSL
        fn.getOptions().setDoGpsl(false);
        // Not necessary
        // fn.setGpxFileSetModel(gpxFileSetModel);

        // Save the PrintStreams
        PrintStream outSave = getOutStream();
        PrintStream errSave = getErrStream();

        // Find the files and get the output in the PrintStreams
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baosOut);
        fn.setOutStream(out);
        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(baosErr);
        fn.setErrStream(err);
        fn.find(file);
        out.close();
        err.close();
        // System.out.println(baosOut.toString());
        // System.err.println(baosErr.toString());

        // Parse the output
        try {
            boolean doingWaypoints = false;
            boolean doingTracks = false;
            boolean fileError = false;
            GpxFileModel fileModel = null;
            List<GpxTrackModel> trackModels = null;
            List<GpxWaypointModel> waypointModels = null;
            List<GpxTrackModel> trackModels1 = null;
            List<GpxWaypointModel> waypointModels1 = null;
            String[] outLines = baosOut.toString().split(SWTUtils.LS);
            for(String line : outLines) {
                // DEBUG
                // System.out.println(line);
                if(line.startsWith("  ")) {
                    // Is a track or waypoint
                    if(fileError) {
                        continue;
                    }
                    if(doingWaypoints) {
                        // Set this one to be checked if it isn't already (to
                        // handle duplicate names)
                        for(GpxWaypointModel model : waypointModels) {
                            if(!model.getChecked()
                                && line.equals("  "
                                    + model.getWaypoint().getName())) {
                                model.setChecked(true);
                            }
                        }
                    }
                    if(doingTracks) {
                        // Set this one to be checked if it isn't already (to
                        // handle duplicate names)
                        for(GpxTrackModel model : trackModels) {
                            if(!model.getChecked()
                                && line.equals("  "
                                    + model.getTrack().getName())) {
                                model.setChecked(true);
                            }
                        }
                    }
                } else if(line.startsWith(" Tracks")) {
                    // Start of tracks
                    if(fileError) {
                        continue;
                    }
                    doingWaypoints = false;
                    doingTracks = true;
                    // Set all tracks unchecked
                    trackModels = fileModel.getTrackModels();
                    for(GpxTrackModel model : trackModels) {
                        model.setChecked(false);
                    }
                } else if(line.startsWith(" Waypoints")) {
                    // Start of waypoints
                    if(fileError) {
                        continue;
                    }
                    doingWaypoints = true;
                    doingTracks = false;
                    // Set all waypoints unchecked
                    waypointModels = fileModel.getWaypointModels();
                    for(GpxWaypointModel model : waypointModels) {
                        model.setChecked(false);
                    }
                } else {
                    // Is a file, first finish the old one
                    if(fileModel != null) {
                        if(waypointModels != null) {
                            // Remove all unchecked waypoints. Do in two steps
                            // to avoid ConcurrentModificationException.
                            waypointModels1 = new ArrayList<GpxWaypointModel>(
                                waypointModels.size());
                            for(GpxWaypointModel model : waypointModels) {
                                if(!model.getChecked()) {
                                    waypointModels1.add(model);
                                }
                            }
                            for(GpxWaypointModel model : waypointModels1) {
                                // Does dispose, fires event, etc.
                                fileModel.remove(model);
                            }
                            waypointModels1.clear();
                            waypointModels = null;
                        }
                        if(trackModels != null) {
                            // Remove all unchecked tracks. Do in two steps to
                            // avoid ConcurrentModificationException.
                            trackModels1 = new ArrayList<GpxTrackModel>(
                                trackModels.size());
                            for(GpxTrackModel model : trackModels) {
                                if(!model.getChecked()) {
                                    trackModels1.add(model);
                                }
                            }
                            for(GpxTrackModel model : trackModels1) {
                                // Does dispose, fires event, etc.
                                fileModel.remove(model);
                            }
                            trackModels1.clear();
                            trackModels = null;
                        }
                        // Add it to the gpxFileSet
                        gpxFileSetModel.add(fileModel);
                    }
                    try {
                        fileError = false;
                        fileModel = new GpxFileModel(gpxFileSetModel, line);
                    } catch(Exception ex) {
                        SWTUtils.excMsgAsync("Error adding GpxFileModel for\n"
                            + line, ex);
                        fileError = true;
                    }
                    doingWaypoints = false;
                    doingTracks = false;
                }
            }
            // Finish the last one
            // Is a file, first finish the old one
            if(fileModel != null) {
                if(waypointModels != null) {
                    // Remove all unchecked waypoints. Do in two steps
                    // to avoid ConcurrentModificationException.
                    waypointModels1 = new ArrayList<GpxWaypointModel>(
                        waypointModels.size());
                    for(GpxWaypointModel model : waypointModels) {
                        if(!model.getChecked()) {
                            waypointModels1.add(model);
                        }
                    }
                    for(GpxWaypointModel model : waypointModels1) {
                        // Does dispose, fires event, etc.
                        fileModel.remove(model);
                    }
                    waypointModels1.clear();
                    waypointModels = null;
                }
                if(trackModels != null) {
                    // Remove all unchecked tracks. Do in two steps to
                    // avoid ConcurrentModificationException.
                    trackModels1 = new ArrayList<GpxTrackModel>(
                        trackModels.size());
                    for(GpxTrackModel model : trackModels) {
                        if(!model.getChecked()) {
                            trackModels1.add(model);
                        }
                    }
                    for(GpxTrackModel model : trackModels1) {
                        // Does dispose, fires event, etc.
                        fileModel.remove(model);
                    }
                    trackModels1.clear();
                    trackModels = null;
                }
                // Add it to the gpxFileSet
                gpxFileSetModel.add(fileModel);
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error parsing FindNear output", ex);
            ex.printStackTrace();
        } finally {
            // Restore the PrintStreams
            setOutStream(outSave);
            setErrStream(errSave);
        }
        return baosErr.toString();
    }

    /**
     * @return A String with information abut the limits.
     */
    public String info() {
        String ls = SWTUtils.LS;
        double latitude = options.getLatitude();
        double longitude = options.getLongitude();
        double radius = options.getRadius();
        Units units = options.getUnits();
        String info = "";
        info += "FindNear" + ls;
        info += "  latitude=" + latitude + " longitude=" + longitude
            + " radius=" + radius + " " + units.getName() + ls;
        return info;
    }

    /**
     * Parses the command line
     * 
     * @param args
     * @return success or failure
     */
    private boolean parseCommand(String[] args) {
        int i;
        double latitude = 0;
        double longitude = 0;
        double radius = 0;
        String unitsString = null;
        for(i = 0; i < args.length; i++) {
            if(args[i].startsWith("-")) {
                if(args[i].equals("-lat")) {
                    latitude = Double.parseDouble(args[++i]);
                    options.setLatitude(latitude);
                    latSpecified = true;
                } else if(args[i].equals("-lon")) {
                    longitude = Double.parseDouble(args[++i]);
                    options.setLongitude(longitude);
                    lonSpecified = true;
                } else if(args[i].equals("-units")) {
                    unitsString = args[++i];
                    options.setUnits(unitsString);
                } else if(args[i].equals("-radius")) {
                    radius = Double.parseDouble(args[++i]);
                    options.setRadius(radius);
                } else if(args[i].equals("--gpsl")) {
                    options.setDoGpsl(false);
                } else if(args[i].equals("--gpx")) {
                    options.setDoGpx(false);
                } else if(args[i].startsWith("-h")) {
                    usage();
                    System.exit(0);
                } else {
                    System.err.println("\n\nInvalid option: " + args[i]);
                    usage();
                    return false;
                }
            } else {
                if(!dirSpecified) {
                    options.setDirName(args[i]);
                    dirSpecified = true;
                } else {
                    System.err.println("\n\nInvalid option: " + args[i]);
                    usage();
                    return false;
                }
            }
        }
        if(options.getUnits() == Units.UNSPECIFIED) {
            if(unitsString != null) {
                Utils.errMsg("deltaUnits (" + unitsString + ") is not valid");
            } else {
                Utils.errMsg("deltaUnits is not valid");
            }
            return false;
        }
        if(!latSpecified) {
            Utils.errMsg("lat must be specified");
            return false;
        }
        if(!lonSpecified) {
            Utils.errMsg("lon must be specified");
            return false;
        }
        return true;
    }

    /**
     * Prints usage
     */
    private void usage() {
        if(outStream == null) {
            return;
        }
        outStream
            .println("\nUsage: java "
                + this.getClass().getName()
                + " [Options] directory\n"
                + "  Find tracks: Find tracks in .gpx or .gpsl files that have a trackpoint that lies\n"
                + "    within the given radius of the specified latitude and longitude.\n"
                + "  The default directory is:\n    "
                + DEFAULT_PATH
                + "\n"
                + "  Options:\n"
                + "    -lat     latitude  (Must be specified)\n"
                + "    -lon     longitude (Must be specified)\n"
                + "    -radius  radius (Default="
                + FindNearOptions.DEFAULT_RADIUS
                + ")\n"
                + "    -units   radius units (\"mi\", \"ft\", \"m\", \"km\", )  (Default=\""
                + FindNearOptions.DEFAULT_UNITS.getName() + "\")\n"
                + "    --gpsl   Omit .gpsl files\n"
                + "    --gpx    Omit .gpx files\n"
                + "    -help    This message\n" + "");
    }

    /**
     * @return The value of outStream.
     */
    public PrintStream getOutStream() {
        return outStream;
    }

    /**
     * @param outStream The new value for outStream.
     */
    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

    /**
     * @return The value of errStream.
     */
    public PrintStream getErrStream() {
        return errStream;
    }

    /**
     * @param errStream The new value for errStream.
     */
    public void setErrStream(PrintStream errStream) {
        this.errStream = errStream;
    }

    /**
     * @return The value of options.
     */
    public FindNearOptions getOptions() {
        return options;
    }

    /**
     * @param options The new value for options.
     */
    public void setOptions(FindNearOptions options) {
        this.options = options;
    }

    /**
     * @return The value of gpxFileSetModel.
     */
    public GpxFileSetModel getGpxFileSetModel() {
        return gpxFileSetModel;
    }

    /**
     * @param gpxFileSetModel The new value for gpxFileSetModel.
     */
    public void setGpxFileSetModel(GpxFileSetModel gpxFileSetModel) {
        this.gpxFileSetModel = gpxFileSetModel;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // FindNear app = new FindNear(45.986139,
        // -89.574683);
        FindNear app = new FindNear(args);
        if(!app.isOk()) {
            System.err.println("Error parsing the command line");
            System.err.println("Aborted");
            System.exit(1);
        }
        System.out.println(app.info());
        app.find();
        System.out.println();
        System.out.println("All done");
    }

}
