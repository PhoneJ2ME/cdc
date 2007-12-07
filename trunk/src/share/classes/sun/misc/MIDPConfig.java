/*
 * 
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */

/*
 * @(#)MIDPConfig.java	1.11	06/10/30
 * This class contains all the information necessary
 * to configure a MemberFilter appropriate for MIDP2.0
 * as well as some tables we need to configure the
 * MIDPImplementationClassLoader
 */
package sun.misc;

import java.net.URL;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.util.Vector;
import java.net.MalformedURLException;

public final
class MIDPConfig{
    /* The MIDP library classloader */
    private static MIDPImplementationClassLoader midpImplCL;
    /* The midlet classloader */
    /*private static MIDletClassLoader midletCL;*/
    /* The MemberFilter */
    private static MemberFilter memberFilter;

    /* The default location of midp library zip */
    private static String defaultMidpJarPath =  
	    System.getProperty("java.home") + File.separator +
	    "lib" + File.separator + "midpclasses.zip";

    public static String MIDPVersion = "2.0";
    public static String CLDCVersion = "1.1";
    /*
     * The following data structures are for
     * managing name visibility.
     */
    static String
    systemPackages[] = {
	"java.",
	"javax.microedition."
    };

    static {
        // Create the member filter.
        memberFilter = newMemberFilter();
    }

    static String permittedSystemClasses[];
	// filled by reading a file.
	// see getPermittedClasses() below


    /*
     * Set up a MemberFilter using the classes and members
     * given in the permittedMembers structures above.
     * All MIDletClassLoaders will share the same MemberFilter
     * since using it does not change its state.
     */
    public static MemberFilter
    newMemberFilter(){
	try{
	    String filename =
		System.getProperty("java.home") + File.separator +
		"lib" + File.separator + "MIDPFilterConfig.txt";
	    MemberFilterConfig mfc = new MemberFilterConfig(filename);
	    MemberFilter mf;
	    // DEBUG System.out.println("Starting MemberFilter file parsing");
	    // DEBUG mfc.setVerbose(true);
	    mf = mfc.parseFile();
	    // DEBUG System.out.println("Done MemberFilter file parsing");
	    return mf;
	}catch(java.io.IOException e){
	    e.printStackTrace();
	    return null;
	}
    }

    private static String[]
    getPermittedClasses(){
	if (permittedSystemClasses != null){
	    return permittedSystemClasses;
	}
	BufferedReader infile;
	Vector classnames;
	int    nnames = 0;
	String filename =
		System.getProperty("java.home") + File.separator +
		"lib" + File.separator + "MIDPPermittedClasses.txt";
	try{
	    infile = new BufferedReader(new FileReader(filename));
	}catch(java.io.IOException e){
	    /*DEBUG*/ System.err.println("Could not open "+filename);
	    return null;
	}
	classnames = new Vector();
	try{
	    while(true){
		String inline = infile.readLine();
		if (inline == null)
		    break; // eof
		if (inline.length() == 0)
		    continue; // blank line
		if (inline.charAt(0) == '#')
		    continue; // comment
		classnames.add(inline);
		nnames += 1;
	    }
	    infile.close();
	}catch(java.io.IOException e){
	    /*DEBUG*/ System.err.println("Exception while reading "+filename);
	    return null;
	}
	permittedSystemClasses = new String[nnames];
	classnames.copyInto(permittedSystemClasses);
	return permittedSystemClasses;
    }

    /*
     * Set up the MIDPImplementationClassLoader.
     * All MIDlets will share the same MIDPImplementationClassLoader,
     * and thus the same class instances. This is required if we want
     * to be able to coordinate their resource use.
     * (Assuming that the implementation don't have any static fields
     * that would make this sharing a bad idea!)
     *
     * We trust these classes, so grant them all permissions.
     */
    /* NOTE: this should be removed once the MIDP code is fixed to
     * use the newMIDPImplementationClassLoader(URL[]) API.
     */
    public static MIDPImplementationClassLoader
    newMIDPImplementationClassLoader(String midpJarNames[]){
        /* The MIDPImplementationClassLoader already exist. Throw an
         * exception.
         */
        if (midpImplCL != null) {
            throw new InternalError(
                "The MIDPImplementationClassLoader is already created");
        }

	String permittedClasses[];
	PermissionCollection perms = new Permissions();
	Vector urls = new Vector();
        for (int i=0; i<midpJarNames.length; i++){
           try {
              File file = new File(midpJarNames[i]);
	      if (file.exists()) {
	         urls.add(file.toURL());
              }
	   }catch(NullPointerException e){
	   }catch(java.io.IOException e){
	      e.printStackTrace();
	   }
	}

	URL[] midpBase = (URL[]) urls.toArray(new URL[0]);
 
	if (midpBase == null || midpBase.length == 0) {
           /* Either the parameter was bad or didn't get passed in.  Use default.  */
           midpBase = new URL[1];
	   try {
	      midpBase[0] = new URL("file://".concat(defaultMidpJarPath));
           }catch(java.io.IOException e){
	      // DEBUG System.err.println("initMidpImplementation URL Creation:");
	      e.printStackTrace();
	      // END DEBUG
	      return null;
           }
        } 

	perms.add(new java.security.AllPermission());
	//DEBUG System.out.println("Constructing MIDPImplementationClassLoader with permissions "+perms);
	permittedClasses = getPermittedClasses();
	if (permittedClasses == null){
	    // there was some problem in reading the file
	    return null;
	}
         
	midpImplCL = new MIDPImplementationClassLoader(
				midpBase, permittedClasses, perms,
				null);
	return midpImplCL;

    }

    public static MIDPImplementationClassLoader
    getMIDPImplementationClassLoader() {
	    return midpImplCL;
    }

    public static MIDPImplementationClassLoader
    newMIDPImplementationClassLoader(File files[]){
        /* The MIDPImplementationClassLoader already exist. Throw an
         * exception.
         */
        if (midpImplCL != null) {
            throw new InternalError(
                "The MIDPImplementationClassLoader is already created");
        }

	String permittedClasses[];
        PermissionCollection perms = new Permissions();

        URL urls[] = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                urls[i] = files[i].toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                urls = null;
		break;
            }
        }
	if (urls == null || urls.length == 0) {
           /* Either the parameter was bad or didn't get passed in.  Use default.  */
           urls = new URL[1];
	   try {
	      urls[0] = new URL("file://".concat(defaultMidpJarPath));
           }catch(java.io.IOException e){
	      // DEBUG System.err.println("initMidpImplementation URL Creation:");
	      e.printStackTrace();
	      // END DEBUG
	      return null;
           }
        } 

	perms.add(new java.security.AllPermission());
	//DEBUG System.out.println(
        //  "Constructing MIDPImplementationClassLoader with permissions "+perms);
	permittedClasses = getPermittedClasses();
	if (permittedClasses == null){
	    // there was some problem in reading the file
	    return null;
	}
	midpImplCL = new MIDPImplementationClassLoader(
				urls, permittedClasses, perms,
				null);
	return midpImplCL;
    }

    /*
     * Set up the permissions that will be granted to MIDlet code proper.
     * Currently this is very little: only the ability to modify the properties
     * of a Thread. And this is very limited by API hiding so is not very dangerous.
     * We absolutely do not give them vmExit, which is explicitly prohibited.
     * This set of permissions is read-only and shared by all MIDletClassLoaders.
     *
     * Property access cannot be dealt with using Java permissions, as we
     * want to make properties disappear, and permissions will throw an Exception
     * to prohibit seeing the property.
     *
     * This depends on being run in the following environment:
     * security enabled, but all permissions granted to main program.
     * This is achieved but putting -Djava.security.manager on
     * the command line, and having my own .java.policy file
     * that looks like this:
     * grant codeBase "file:*" {
     *   permission java.security.AllPermission;
     * };
     */

    static PermissionCollection
    newMidletPermissions(){
	PermissionCollection mp = new Permissions();
	mp.add(new java.lang.RuntimePermission("modifyThread"));
	mp.add(new java.util.PropertyPermission("*", "read"));
	mp.setReadOnly();
	return mp;
    }

    static PermissionCollection midletPermissions = newMidletPermissions();


    /*
     * Set up a new MIDletClassLoader 
     * There should probably be one of these per MIDlet suite.
     * This would allow sharing between suite members, including data.
     */

    static String[]
    split(String path){
	int nComponents = 1;
	char separator = System.getProperty("path.separator", ":").charAt(0);
	String components[];
	int length = path.length();
	int start;
	int componentIndex;
	for (int i=0; i<length; i++){
	    if (path.charAt(i) == separator)
		nComponents += 1;
	}
	components = new String[nComponents];
	start = 0;
	componentIndex = 0;
	/* could optimize here for the common case of nComponents == 1 */
	for (int i=0; i<length; i++){
	    if (path.charAt(i) == separator){
		components[componentIndex] = path.substring(start, i);
		componentIndex += 1;
		start = i+1;
	    }
	}
	/* and the last components is delimited by end of String */
	components[componentIndex] = path.substring(start, length);

	return components;

    }

    /*
     * This version allows the caller to specify a set of permissions.
     * This is less useful than the usual version, which grants the 
     * permissions we plan on granting to MIDlets.
     *
     * The 'enableFilter' argument specifies that if the API hiding
     * filter is being enabled. If the filter is enabled, midlet
     * can only access CLDC/MIDP classes. If the filter is disabled,
     * midlet can access all classes on the bootclasspath, including
     * all the CDC classes.
     *
     * The 'auxClassLoader' is a helper classloader used when the
     * MIDletClassLoader and its parents fail to load the requested
     * class. 
     */
    private static MIDletClassLoader
    newMIDletClassLoader(
	String midpPath[], MemberFilter mf,
        PermissionCollection perms,
	MIDPImplementationClassLoader implClassLdr,
        boolean enableFilter,
        ClassLoader auxClassLoader)
    {
        if (midpImplCL == null) {
            throw new InternalError(
	        "Need to create the parent MIDPImplementationClassLoader first");
        }

	URL midJarURL[];
	int nComponents = midpPath.length;

	midJarURL = new URL[nComponents];
	try {
	    for (int i=0; i<nComponents; i++){
		midJarURL[i] = new URL("file://".concat(midpPath[i]));
	    }
	}catch(Exception e){
	    System.err.println("URL Creation:");
	    e.printStackTrace();
	    return null;
	}
	//DEBUG  System.out.println("Constructing MIDletClassLoader with permissions "+perms);
	MIDletClassLoader midletCL = new MIDletClassLoader(
                                           midJarURL, systemPackages,
					   perms, mf, implClassLdr,
                                           enableFilter, auxClassLoader);

	return midletCL;
    }

    /*
     * This version allows the caller to specify a set of permissions.
     * The parent classloader is the MIDPImplementationClassLoader.
     * The API hiding filter is enabled.
     */
    public static MIDletClassLoader
    newMIDletClassLoader(
	String midpPath[], PermissionCollection perms)
    {
        return newMIDletClassLoader(midpPath,
                                    memberFilter,
                                    perms,
                                    midpImplCL,
                                    true,
                                    null);
    }

    /*
     * Use the default midlet permission collection. The parent classloader
     * is the MIDPImplementationClassLoader. The API hiding filter is
     * enabled.
     */
    public static MIDletClassLoader
    newMIDletClassLoader(String midpPath[])
    {
	return newMIDletClassLoader(midpPath,
                                    memberFilter,
                                    midletPermissions,
                                    midpImplCL,
                                    true,
                                    null);
    }

    /*
     * The 'enableFilter' argument specifies that if the API hiding
     * filter is being enabled. If the filter is enabled, midlet
     * can only access CLDC/MIDP classes. If the filter is disabled,
     * midlet can access all classes on the bootclasspath, including
     * all the CDC classes.
     *
     * The 'auxClassLoader' is a helper classloader used when the
     * MIDletClassLoader and its parents fail to load the requested
     * class. 
     */
    public static MIDletClassLoader
    newMIDletClassLoader(String midpPath[], boolean enableFilter,
                         ClassLoader auxClassLoader)
    {
	return newMIDletClassLoader(midpPath,
                                    memberFilter,
                                    midletPermissions,
                                    midpImplCL,
                                    enableFilter,
                                    auxClassLoader);
    }

    /*
     * Get the MIDletClassLoader instance that loads the caller
     * midlet class.
     */
    public static MIDletClassLoader
    getMIDletClassLoader() {
        int i = 1; /* skip the direct caller, who must be system code */
        ClassLoader loader = null;
        Class cl = CVM.getCallerClass(i);

        while (cl != null) {
            loader = cl.getClassLoader();
            if (loader instanceof MIDletClassLoader) {
                return (MIDletClassLoader)loader;
            }
            cl = CVM.getCallerClass(++i);
	}
        return null;
    }

    /* 
     * A utility method used to load resources using the caller's
     * classloaders. This is useful when application and system
     * code are loaded by different classloaders.
     */
    public static InputStream
    getResourceAsStream(String name){
        int i = 1; /* skip tha caller, which must be system code */
        InputStream is = null;
        ClassLoader lastFailedLoader = null;
        
        /* This is a bit slow since we need to walk up the stack.
         * Because we don't know which classloader to load the resource,
         * so we have to do it the hard way. */
        while (is == null) {
            Class cl = sun.misc.CVM.getCallerClass(i);
            if (cl == null) { /* reach the top of the stack */
	        break;
	    }

            ClassLoader loader = cl.getClassLoader();
            if (i == 1 || loader != lastFailedLoader) {
                is = cl.getResourceAsStream(name);
                if (is != null) {
                    break;
		} else {
		    lastFailedLoader = loader;
		}
	    }

            i++; /* the next caller */
	}
        return is;
    }
}