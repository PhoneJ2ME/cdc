/*
 * jvmti hprof
 *
 * Copyright  2006-2007 Sun Microsystems, Inc. All Rights Reserved.	 
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

#ifndef HPROF_BLOCKS_H
#define HPROF_BLOCKS_H

typedef struct BlockHeader {
    struct BlockHeader *next;
    int                 bytes_left;
    int                 next_pos;
} BlockHeader;

typedef struct Blocks {
    BlockHeader *first_block;   /* Pointer to first BlockHeader */
    BlockHeader *current_block; /* Pointer to current BlockHeader */
    int          alignment;     /* Data alignment, 1, 2, 4, 8, 16 */
    int          elem_size;     /* Size in bytes, ==1 means variable sizes */
    int          population;    /* Number of elements to allow for per Block */
} Blocks;

Blocks * blocks_init(int alignment, int elem_size, int population);
void *   blocks_alloc(Blocks *blocks, int nbytes);
void     blocks_term(Blocks *blocks);

#endif
