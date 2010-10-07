package net.kenevans.gpxinspector.utils;

/*
 * Created on Sep 6, 2010
 * By Kenneth Evans, Jr.
 */

public class GpsUtils
{
    /**
     * Nominal radius of the earth in miles. The radius actually varies from
     * 2937 to 3976 mi.
     */
    public static final double REARTH = 3956;
    /** Multiplier to convert miles to nautical miles. */
    public static final double MI2NMI = 1.852; // Exact
    /** Multiplier to convert degrees to radians. */
    public static final double DEG2RAD = Math.PI / 180.;
    /** Multiplier to convert feet to miles. */
    public static final double FT2MI = 1. / 5280.;
    /** Multiplier to convert meters to miles. */
    public static final double M2MI = 6.2137119224d - 4;
    /** Multiplier to convert kilometers to miles. */
    public static final double KM2MI = .001 * M2MI;

    /**
     * Returns great circle distance in mi. assuming a spherical earth. Uses
     * Haversine formula.
     * 
     * @param lat1 Start latitude in deg.
     * @param lon1 Start longitude in deg.
     * @param lat2 End latitude in deg.
     * @param lon2 End longitude in deg.
     * @return
     */
    public static double greatCircleDistance(double lat1, double lon1,
        double lat2, double lon2) {
        double slon, slat, a, c, d;

        // Convert to radians
        lat1 *= DEG2RAD;
        lon1 *= DEG2RAD;
        lat2 *= DEG2RAD;
        lon2 *= DEG2RAD;

        // Haversine formula
        slon = Math.sin((lon2 - lon1) / 2.);
        slat = Math.sin((lat2 - lat1) / 2.);
        a = slat * slat + Math.cos(lat1) * Math.cos(lat2) * slon * slon;
        c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        d = REARTH * c;

        return (d);
    }

}
