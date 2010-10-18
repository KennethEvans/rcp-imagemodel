/*******************************************************************************
 * Copyright © 2007, UChicago Argonne, LLC
 * 
 * All Rights Reserved
 * 
 * X-Ray Analysis Software (XRAYS)
 * 
 * OPEN SOURCE LICENSE
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. Software changes,
 * modifications, or derivative works, should be noted with comments and the
 * author and organization’s name.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. Neither the names of UChicago Argonne, LLC or the Department of Energy nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * 4. The software and the end-user documentation included with the
 * redistribution, if any, must include the following acknowledgment:
 * 
 * "This product includes software produced by UChicago Argonne, LLC under
 * Contract No. DE-AC02-06CH11357 with the Department of Energy."
 * 
 * ***************************************************************************
 * 
 * DISCLAIMER
 * 
 * THE SOFTWARE IS SUPPLIED "AS IS" WITHOUT WARRANTY OF ANY KIND.
 * 
 * NEITHER THE UNITED STATES GOVERNMENT, NOR THE UNITED STATES DEPARTMENT OF
 * ENERGY, NOR UCHICAGO ARGONNE, LLC, NOR ANY OF THEIR EMPLOYEES, MAKES ANY
 * WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY LEGAL LIABILITY OR
 * RESPONSIBILITY FOR THE ACCURACY, COMPLETENESS, OR USEFULNESS OF ANY
 * INFORMATION, DATA, APPARATUS, PRODUCT, OR PROCESS DISCLOSED, OR REPRESENTS
 * THAT ITS USE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS.
 * 
 ******************************************************************************/

/*
 * Program to make a rainbow color scheme
 * Created on Apr 7, 2006
 * By Kenneth Evans, Jr.
 */

package net.kenevans.gpxinspector.utils;

import java.awt.Color;

/**
 * RainbowColorScheme
 * Provides an array of colors in a rainbow scheme
 * @author Kenneth Evans, Jr.
 */
public class RainbowColorScheme
{
  private final static int NCOLORS = 256;
  private int nColors = NCOLORS;
  private Color[] colors = null;
  
  /**
   * RainbowColorScheme default constructor (256 colors).
   */
  public RainbowColorScheme() {
  }
  
  /**
   * RainbowColorScheme constructor.
   * @param nColors The number of colors in the scheme.
   */
  public RainbowColorScheme(int nColors) {
    this.nColors = nColors;
  }
  
  /**
   * Defines the Color array.
   */
  public Color[] defineColors() {
    if(colors != null) return colors;
    
    colors = new Color[nColors];
    // Make a rainbow palette
    for(int i = 0; i < nColors; i++) {
      colors[i] = defineColor(i, nColors);
    }
    return colors;
  }
  
  /**
   * Gets a color corresponding to a given number of colors.
   * @param index Index of the color [0, nColors - 1].
   * @param nColors The total number of colors.
   * @return The Color corresponding to the index.
   */
  public static Color defineColor(int index, int nColors) {
    Color color = null;
    double nGroups = 5, nMembers = 45, nTotal = nGroups * nMembers;
    double high = 1.000, medium = .375;

    double h = (double)index / (double)nColors;
    double hx = h * nTotal;
    double deltax = (high - medium) / nMembers;
    double r, g, b;
    int gh = (int)Math.floor(hx / nMembers);
    int ih = (int)Math.floor(hx);
    switch(gh) {
    case 0:
      r = medium;
      g = medium + (ih - gh * nMembers) * deltax;
      b = high;
      break;
    case 1:
      r = medium;
      g = high;
      b = high - (ih - gh * nMembers) * deltax;
      break;
    case 2:
      r = medium + (ih - gh * nMembers) * deltax;
      g = high;
      b = medium;
      break;
    case 3:
      r = high;
      g = high - (ih - gh * nMembers) * deltax;
      b = medium;
      break;
    case 4:
      r = high;
      g = medium;
      b = medium + (ih - gh * nMembers) * deltax;
      break;
    default:
      r = high;
      g = medium;
      b = high;
      break;
    }
    int red = (int)(r * 255 + .5);
    if(red > 255) red = 255;
    int green = (int)(g * 255 + .5);
    if(green > 255) green = 255;
    int blue = (int)(b * 255 + .5);
    if(blue > 255) blue = 255;
    color = new Color(red, green, blue);
    return color;
  }
  
  /**
   * Calculates the integer value of the Color.
   * @param color
   * @return  256*256*red + 256*green + blue.
   */
  public static int toColorInt(Color color) {
    int colorInt = 65536 * color.getRed() + 256 * color.getGreen()
    + color.getBlue();
    return colorInt;
  }
  
  /**
   * Calculates the string value of the Color.
   * 
   * @param color
   * @return "rrr,ggg,bbb".
   */
  public static String toColorString(Color color) {
    String string = color.getRed() + "," + color.getGreen() + ","
      + color.getBlue();
    return string;
  }
  
  /**
   * Calculates the color corresponding to a fraction of the default number
   * of colors (256).
   * 
   * @param fract A fraction in the range [0,1] inclusive.
   * @return The Color with index closest to the fraction times the maximun
   *         color index (255).
   */
  public static Color getColor(double fract) {
    return getColor(fract, NCOLORS);
  }
  
  /**
   * Returns the color corresponding to a fraction of the number of colors
   * from the stored color array.  Calculates the array if it has not
   * previously been calculated.
   * 
   * @param fract A fraction in the range [0,1] inclusive.
   * @param nColors The total number of colors.
   * @return The Color with index closest to the fraction times the maximun
   *         color index (nColors - 1).
   */
  public  Color getStoredColor(double fract) {
    if(colors == null) defineColors();
    if(colors == null) return null;
    int index = (int)Math.round((nColors - 1) * fract);
    if(index < 0) index = 0;
    if(index > nColors) index = nColors;
    return colors[index];
  }
  
  /**
   * Calculates the color corresponding to a fraction of the number of colors.
   * 
   * @param fract A fraction in the range [0,1] inclusive.
   * @param nColors The total number of colors.
   * @return The Color with index closest to the fraction times the maximun
   *         color index (nColors - 1).
   */
  public static Color getColor(double fract, int nColors) {
    int index = (int)Math.round((nColors - 1) * fract);
    if(index < 0) index = 0;
    if(index > nColors) index = nColors;
    return defineColor(index, nColors);
  }
  
  /**
   * @return The colors array.
   */
  public Color[] getColors() {
    return colors;
  }

  /**
   * @return Returns the default number of colors in the color array (256).
   */
  public static int getNColorsDefault() {
    return NCOLORS;
  }

  /**
   * @return Returns the number of colors in the color array.
   */
  public int getNColors() {
    return nColors;
  }

}
