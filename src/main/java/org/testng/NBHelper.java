/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.testng;

import java.io.File;

/**
 *
 * @author Lukas Jungmann
 */
final class NBHelper {

    private static final String NB_HOME = System.getProperty("netbeans.home");

    public static boolean usingNetBeans() {
        return NB_HOME != null && new File(NB_HOME).isDirectory();
    }
}
