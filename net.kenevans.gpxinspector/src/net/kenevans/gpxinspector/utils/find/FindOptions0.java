package net.kenevans.gpxinspector.utils.find;

import net.kenevans.gpxinspector.utils.GpsUtils;
import net.kenevans.gpxinspector.utils.GpxException;

/*
 * Created on Sep 6, 2010
 * By Kenneth Evans, Jr.
 */

public class FindOptions0
{
    public static enum Units {
        UNSPECIFIED("unspecified", 0), FEET("ft", GpsUtils.FT2MI), MILES("mi",
            1), METERS("m", GpsUtils.M2MI), KILOMETERS("km", GpsUtils.KM2MI);
        private final String name;
        private final double factor;

        Units(String name, double factor) {
            this.name = name;
            this.factor = factor;
        }

        public String getName() {
            return name;
        }

        public double radiusInMiles(double radius) {
            return factor * radius;
        }
    };

    /**
     * A list of the possible (specified) Units. Used for looping over the
     * types. Perhaps there is a better way.
     */
    private static final Units[] unitTypes = {Units.FEET, Units.MILES,
        Units.METERS, Units.KILOMETERS};

    public static enum InputType {
        DIRNAME, GPXFILESET, GPXFILE,
    };

    public static final double DEFAULT_DELTA = 1;
    private double deltaLat = DEFAULT_DELTA;
    private double deltaLon = DEFAULT_DELTA;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private double latMin = Double.NaN;
    private double latMax = Double.NaN;
    private double lonMin = Double.NaN;
    private double lonMax = Double.NaN;
    private double radius = 1.;
    private Units units = Units.MILES;
    private InputType inputType = InputType.DIRNAME;
    private String dirName = null;
    /**
     * The input value of deltaLat. The values of inputDeltaLat, inputDeltaLon,
     * and inputUnits are consistent with each other, are read only, and are
     * only set by convertAndSetDelta(). The values of deltaLat and deltaLon are
     * always in degrees.
     */
    private double inputDeltaLat = DEFAULT_DELTA;
    /**
     * The input value of deltaLat. The values of inputDeltaLat, inputDeltaLon,
     * and inputUnits are consistent with each other, are read only, and are
     * only set by convertAndSetDelta(). The values of deltaLat and deltaLon are
     * always in degrees.
     */
    private double inputDeltaLon = DEFAULT_DELTA;
    /**
     * The input value of units. The values of inputDeltaLat, inputDeltaLon, and
     * inputUnits are consistent with each other, are read only, and are only
     * set by convertAndSetDelta(). The values of deltaLat and deltaLon are
     * always in degrees.
     */
    private Units inputUnits = Units.UNSPECIFIED;

    // private boolean ok = true;
    // private boolean dirSpecified = false;
    // private boolean latSpecified = false;
    // private boolean lonSpecified = false;
    // private boolean latMinSpecified = false;
    // private boolean latMaxSpecified = false;
    // private boolean lonMinSpecified = false;
    // private boolean lonMaxSpecified = false;

    /**
     * Converts the given values of deltaLat and deltaLon in the given
     * inputUnits to degrees. Sets the values of inputDeltaLat, inputDeltaLon,
     * and inputUnits. Converts deltaLat and deltaLon using the great-circle
     * distance corresponding to the input values if the units are not DEGREES.
     * 
     * @param deltaLat
     * @param deltaLon
     * @param inputUnits
     * @throws GpxException
     */
    public void convertAndSetDelta(double deltaLat, double deltaLon, Units units)
        throws GpxException {
        this.inputDeltaLat = deltaLat;
        this.inputDeltaLon = deltaLon;
        this.inputUnits = units;
        double scale = 1;
        if(units == Units.FEET) {
            scale = 1;
        } else if(units == Units.MILES) {
            scale = 5280;
        } else {
            throw new GpxException("Not implemented: " + units.getName());
        }
        // Iterate
        for(int coord = 0; coord < 2; coord++) {
            String name = null;
            double goal = 0;
            if(coord == 0) {
                goal = deltaLat;
                name = "deltaLat";
            } else {
                goal = deltaLon;
                name = "deltaLon";
            }
            double delta0 = 0;
            double delta1 = DEFAULT_DELTA;
            double res0 = 0;
            double res1 = 0;
            int nLimit = 1000;
            double eps = 1.e-8;
            boolean found = false;
            for(int i = 0; i < nLimit; i++) {
                if(coord == 0) {
                    res1 = scale
                        * GpsUtils.greatCircleDistance(latitude - delta1 / 2,
                            longitude, latitude + delta1 / 2, longitude);
                } else {
                    res1 = scale
                        * GpsUtils.greatCircleDistance(latitude, longitude
                            - delta1 / 2, latitude, longitude + delta1 / 2);
                }
                if(res1 == 0) {
                    throw new GpxException("Cannot find " + name
                        + " for inputUnits=\"" + units.toString() + "\"");
                }
                double test = (res1 - goal) / goal;
                if(Math.abs(test) < eps) {
                    found = true;
                    break;
                }
                // Calculate new delta
                double slope = (delta1 - delta0) / (res1 - res0);
                delta0 = delta1;
                res0 = res1;
                delta1 = delta1 + slope * (goal - res1);
            }
            if(!found) {
                throw new GpxException("Cannot find " + name
                    + " for inputUnits=\"" + units.toString() + "\" after "
                    + nLimit + " iterations");
            }
            if(coord == 0) {
                deltaLat = delta1;
            } else {
                deltaLon = delta1;
            }
        }
    }

    /**
     * Calculates the latitude and longitude limits. Calls convertDelta which
     * may change deltaLon and deltaLat.
     * 
     * @return The value of limits as {latMin, latMax, lonMin, lonMax}.
     * @throws GpxException
     */
    public double[] getLimits() throws GpxException {
        double[] limits = null;
        if(!isLatSpecified()) {
            latitude = .5 * (latMax + latMin);
        }
        if(!isLonSpecified()) {
            longitude = .5 * (lonMax + lonMin);
        }
        limits = new double[4];
        if(isLatMinSpecified()) {
            limits[0] = latMin;
        } else {
            limits[0] = latitude - deltaLat;
        }
        if(isLatMaxSpecified()) {
            limits[1] = latMax;
        } else {
            limits[1] = latitude + deltaLat;
        }
        if(isLonMinSpecified()) {
            limits[2] = lonMin;
        } else {
            limits[2] = longitude - deltaLon;
        }
        if(isLonMaxSpecified()) {
            limits[3] = lonMax;
        } else {
            limits[3] = longitude + deltaLon;
        }
        return limits;
    }

    /**
     * @return The value of ok.
     */
    public boolean isOk() {
        if(!isLatSpecified() && !(isLatMinSpecified() && isLatMaxSpecified())) {
            return false;
        }
        if(!isLonSpecified() && !(isLonMinSpecified() && isLonMaxSpecified())) {
            return false;
        }
        if(deltaLat <= 0) {
            return false;
        }
        if(deltaLon <= 0) {
            return false;
        }
        return true;
    }

    /**
     * @return The value of dirSpecified.
     */
    public boolean isDirSpecified() {
        return dirName != null;
    }

    /**
     * @return The value of latSpecified.
     */
    public boolean isLatSpecified() {
        return latitude != Double.NaN;
    }

    /**
     * @return The value of lonSpecified.
     */
    public boolean isLonSpecified() {
        return longitude != Double.NaN;
    }

    /**
     * @return The value of latMinSpecified.
     */
    public boolean isLatMinSpecified() {
        return latMin != Double.NaN;
    }

    /**
     * @return The value of latMaxSpecified.
     */
    public boolean isLatMaxSpecified() {
        return latMax != Double.NaN;
    }

    /**
     * @return The value of lonMinSpecified.
     */
    public boolean isLonMinSpecified() {
        return lonMin != Double.NaN;
    }

    /**
     * @return The value of lonMaxSpecified.
     */
    public boolean isLonMaxSpecified() {
        return lonMax != Double.NaN;
    }

    /**
     * @return The value of deltaLat.
     */
    public double getDeltaLat() {
        return deltaLat;
    }

    /**
     * @param deltaLat The new value for deltaLat.
     */
    public void setDeltaLat(double deltaLat) {
        this.deltaLat = deltaLat;
    }

    /**
     * @return The value of deltaLon.
     */
    public double getDeltaLon() {
        return deltaLon;
    }

    /**
     * @param deltaLon The new value for deltaLon.
     */
    public void setDeltaLon(double deltaLon) {
        this.deltaLon = deltaLon;
    }

    /**
     * @return The value of latitude.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude The new value for latitude.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return The value of longitude.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude The new value for longitude.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return The value of latMin.
     */
    public double getLatMin() {
        return latMin;
    }

    /**
     * @param latMin The new value for latMin.
     */
    public void setLatMin(double latMin) {
        this.latMin = latMin;
    }

    /**
     * @return The value of latMax.
     */
    public double getLatMax() {
        return latMax;
    }

    /**
     * @param latMax The new value for latMax.
     */
    public void setLatMax(double latMax) {
        this.latMax = latMax;
    }

    /**
     * @return The value of lonMin.
     */
    public double getLonMin() {
        return lonMin;
    }

    /**
     * @param lonMin The new value for lonMin.
     */
    public void setLonMin(double lonMin) {
        this.lonMin = lonMin;
    }

    /**
     * @return The value of lonMax.
     */
    public double getLonMax() {
        return lonMax;
    }

    /**
     * @param lonMax The new value for lonMax.
     */
    public void setLonMax(double lonMax) {
        this.lonMax = lonMax;
    }

    /**
     * @return The value of inputType.
     */
    public InputType getInputType() {
        return inputType;
    }

    /**
     * @param inputType The new value for inputType.
     */
    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    /**
     * @return The value of dirName.
     */
    public String getDirName() {
        return dirName;
    }

    /**
     * @param dirName The new value for dirName.
     */
    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    /**
     * @return The value of inputUnits.
     */
    public Units getInputUnits() {
        return inputUnits;
    }

    /**
     * @return The value of inputDeltaLat.
     */
    public double getInputDeltaLat() {
        return inputDeltaLat;
    }

    /**
     * @return The value of inputDeltaLon.
     */
    public double getInputDeltaLon() {
        return inputDeltaLon;
    }

    /**
     * @return The value of radius.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @param radius The new value for radius.
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * @return The value of units.
     */
    public Units getUnits() {
        return units;
    }

    /**
     * @param units The new value for units.
     */
    public void setUnits(String unitsString) {
        units = Units.UNSPECIFIED;
        for(Units unit : unitTypes) {
            if(unitsString.equalsIgnoreCase(unit.getName())) {
                units = unit;
                return;
            }
        }
    }

    /**
     * @param units The new value for units.
     */
    public void setUnits(Units units) {
        this.units = units;
    }

}
