package net.kenevans.gpxinspector.utils;

/*
 * Created on Sep 6, 2010
 * By Kenneth Evans, Jr.
 */

/**
 * GpxException is a generic exception for the net.kenevans.gpxinspector
 * package.
 * 
 * @author evans
 * 
 */
public class GpxException extends Exception
{
    private static final long serialVersionUID = 1L;

    public GpxException() {
        super();
    }

    public GpxException(String s) {
        super(s);
    }

    public GpxException(String s, Throwable cause) {
        super(s, cause);
    }

    public GpxException(Throwable cause) {
        super(cause);
    }
}
