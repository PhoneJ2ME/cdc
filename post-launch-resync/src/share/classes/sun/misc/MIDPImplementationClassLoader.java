/*
 * @(#)MIDPImplementationClassLoader.java	1.11 06/10/10
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
 *
 */
/*
 * @(#)MIDPImplementationClassLoader.java	1.4	03/08/19
 *
 * Class loader for MIDP implementation running on CDC/PP
 */
package sun.misc;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLClassLoader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;


public class MIDPImplementationClassLoader extends URLClassLoader{

    URL myBase[];
    private HashSet allowedClasses; /* the classes we can lookup in system */
    private PermissionCollection perms;
    private ClassLoader parent;
    private ClassLoader helper;

    public MIDPImplementationClassLoader(
	URL base[],
	String allowedParentClasses[],
	PermissionCollection pc,
	ClassLoader helper,
	ClassLoader parent)
    {
	super(base);
	myBase = base;
	perms = pc;
	this.helper = helper;
	this.parent = parent;
	hashAllowedParentClasses(allowedParentClasses);
    }

    private void
    hashAllowedParentClasses(String allowedParentClasses[]){
	HashSet classes = allowedClasses = new HashSet();
	for (int i = 0; i<allowedParentClasses.length; i++){
	    String classname = allowedParentClasses[i].intern();
	    classes.add(classname);
	}
    }

    protected PermissionCollection getPermissions(CodeSource cs){
	URL srcLocation = cs.getLocation();
	for (int i=0; i<myBase.length; i++){
	    if (srcLocation.equals(myBase[i])){
		return perms;
	    }
	}
	return super.getPermissions(cs);
    }


    private Class
    loadFromParent(String classname, boolean restrict)
					throws ClassNotFoundException
    {
	// make sure classname is on the list.
	if (restrict && !allowedClasses.contains(classname))
	    return null;
	return parent.loadClass(classname);
    }

    public Class
    loadClass(String classname, boolean resolve) throws ClassNotFoundException
    {
	return loadClass(classname, resolve, false);
    }

    public synchronized Class
    loadClass(String classname, boolean resolve, boolean restrict)
						throws ClassNotFoundException
    {
	Class resultClass;
	classname = classname.intern();
	/*DEBUG boolean helperClass = false; */
	resultClass = findLoadedClass(classname);
	if (resultClass == null){
	    try {
		resultClass = loadFromParent(classname, restrict);
	    }catch(Exception e){
		/*DEBUG e.printStackTrace(); */
		resultClass = null;
	    }
	}
	if (resultClass == null && helper != null){
	    try {
		resultClass = helper.loadClass(classname); // from ROM
		/*DEBUG System.out.println("MIDPImplementationClassLoader: helper found class "+classname);  */
		/*DEBUG helperClass=true; */
	    }catch(Exception e){
		/*DEBUG e.printStackTrace(); */
		resultClass = null;
	    }
	}
	if (resultClass == null){
	    try {
		resultClass = super.findClass(classname); // from URLClassLoader
	    }catch(Exception e){
		/*DEBUG e.printStackTrace(); */
		resultClass = null;
	    }
	}
	if (resultClass == null)
	    throw new ClassNotFoundException(classname);
	if (resolve){
	    /*DEBUG if(helperClass) System.out.println("resolving "+classname); */
	    resolveClass(resultClass);
	}
	/*DEBUG if(helperClass){
	 *	System.out.println("returning "+classname+"..."); 
	 *	System.out.println(resultClass); 
	 * }
	 */
	return resultClass;
    }

    /*
    InputStream getResourceAsStream(String name)
    */

}

