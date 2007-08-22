/*
 * @(#)jcov_md.c	1.5 06/10/10
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

#ifdef WINCE
#include <winbase.h>
#else
#include <sys/stat.h>
#endif

#include "jcov_md.h"

int jcov_file_exists(const char *filename)
{
#ifndef WINCE
    struct stat status;
    int sresult = stat(filename, &status);
    int result = 0;
    if (sresult == -1) {
        result = 0;
    } else {
        result = 1;
    }
    return result;
#else
    return 1;
#endif
}

#ifdef WINCE
int rename(char *old_name, char *new_name) {
    int retval = -1; /* failure */
    DeleteFile((LPCTSTR)new_name);
    if (MoveFile((LPCTSTR)new_name, (LPCTSTR)old_name)) {
	retval = 0;  /* success */
    }
    return retval;

    //    return rename(temp_file_name, filename) == -1);
}

void
*bsearch(void *key, void *base, size_t num, size_t width,
	 int ( __cdecl *compare ) ( const void *, const void *))
{
    int i;
    for (i = 0; i < num; i++, (size_t)base += width) {
	if (compare(key, base) == 0) {
	    return base;
	}
    }
    return NULL;
}

int
remove(char *old_name) {
    return !DeleteFile((LPCTSTR)old_name);
}
#endif
