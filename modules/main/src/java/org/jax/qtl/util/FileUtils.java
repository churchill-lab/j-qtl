/*
 * Copyright (c) 2009 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://research.jax.org/faculty/churchill).
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jax.qtl.util;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * <p>Title: QTL data analysis</p>
 *
 * <p>Description: </p>
 *
 * <p>Company: The Jackson Laboratory</p>
 *
 * @author Hao Wu
 * @version 1.0
 */
@SuppressWarnings("all")
public class FileUtils {

    /**
     * CSV format
     */
    public final static String csv = "csv";

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    /**
     * Add extension to a file
     * @param f
     * @param ext
     * @return
     */
    public static File addExtension(File f, String ext) {
      String s = f.getPath();
      String newfilename;
      newfilename = s + "." + ext;
      return new File(newfilename);
    }

    /**
     * replace the extension of a file by a given string
     */
    public static File replaceExtension(File f, String ext) {
      String s = f.getPath();
      String newfilename;
      int i = s.lastIndexOf('.');
      newfilename = s.substring(0, i) + "." + ext;
      return new File(newfilename);
    }

    /**
     * Turn a string line to the array of small tokens according to the divider string.
     * @param line String
     * @param divider String
     * @return String[]
     */
    public static String[] readInLine(String line, String divider) {
        StringTokenizer st = new StringTokenizer(line, divider, true);
        Vector elementsHolder = new Vector();
        // ,,a,b,
        String lastToken = divider;
        for (int i = 0; st.hasMoreTokens(); i++) {
            String currentToken = st.nextToken().trim();

            if (!currentToken.equals(divider))
                elementsHolder.add(currentToken);
            else {
                if (lastToken.equals(divider)) {
                    elementsHolder.add("");
                }
            }
            lastToken = currentToken;
        }

        int numElements = elementsHolder.size();
        String[] elements = new String[numElements];
        for (int i=0; i<numElements; i++) {
            elements[i] = (String)elementsHolder.elementAt(i);
        }
        return elements;
    }
}
