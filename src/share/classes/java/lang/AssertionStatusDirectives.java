/*
 * Copyright 1990-2006 Sun Microsystems, Inc. All Rights Reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 only,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * version 2 for more details (a copy is included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 or visit www.sun.com if you need additional information or have
 * any questions.
 */

package java.lang;

/**
 * A collection of assertion status directives (such as "enable assertions
 * in package p" or "disable assertions in class c").  This class is used by
 * the JVM to communicate the assertion status directives implied by 
 * the <tt>java</tt> command line flags <tt>-enableassertions</tt>
 * (<tt>-ea</tt>) and <tt>-disableassertions</tt> (<tt>-da</tt>).
 *
 * @since  1.4
 * @author Josh Bloch
 */
class AssertionStatusDirectives {
    /**
     * The classes for which assertions are to be enabled or disabled.
     * The strings in this array are fully qualified class names (for
     * example,"com.xyz.foo.Bar").
     */
    String[] classes;

    /**
     * A parallel array to <tt>classes</tt>, indicating whether each class
     * is to have assertions enabled or disabled.  A value of <tt>true</tt>
     * for <tt>classEnabled[i]</tt> indicates that the class named by
     * <tt>classes[i]</tt> should have assertions enabled; a value of
     * <tt>false</tt> indicates that it should have classes disabled.
     * This array must have the same number of elements as <tt>classes</tt>.
     *
     * <p>In the case of conflicting directives for the same class, the
     * last directive for a given class wins.  In other words, if a string
     * <tt>s</tt> appears multiple times in the <tt>classes</tt> array
     * and <tt>i</t> is the highest integer for which
     * <tt>classes[i].equals(s)</tt>, then <tt>classEnabled[i]</tt>
     * indicates whether assertions are to be enabled in class <tt>s</tt>.
     */
    boolean[] classEnabled;

    /**
     * The package-trees for which assertions are to be enabled or disabled.
     * The strings in this array are compete or partial package names
     * (for example, "com.xyz" or "com.xyz.foo").
     */
    String[] packages;

    /**
     * A parallel array to <tt>packages</tt>, indicating whether each
     * package-tree is to have assertions enabled or disabled.  A value of
     * <tt>true</tt> for <tt>packageEnabled[i]</tt> indicates that the
     * package-tree named by <tt>packages[i]</tt> should have assertions
     * enabled; a value of <tt>false</tt> indicates that it should have
     * assertions disabled.  This array must have the same number of
     * elements as <tt>packages</tt>.
     *
     * In the case of conflicting directives for the same package-tree, the
     * last directive for a given package-tree wins.  In other words, if a
     * string <tt>s</tt> appears multiple times in the <tt>packages</tt> array
     * and <tt>i</t> is the highest integer for which
     * <tt>packages[i].equals(s)</tt>, then <tt>packageEnabled[i]</tt>
     * indicates whether assertions are to be enabled in package-tree
     * <tt>s</tt>.
     */
    boolean[] packageEnabled;

    /**
     * Whether or not assertions in non-system classes are to be enabled
     * by default.
     */
    boolean deflt;
}
