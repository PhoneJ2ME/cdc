#
# Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version
# 2 only, as published by the Free Software Foundation. 
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License version 2 for more details (a copy is
# included at /legal/license.txt). 
# 
# You should have received a copy of the GNU General Public License
# version 2 along with this work; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA 
# 
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
# Clara, CA 95054 or visit www.sun.com if you need additional
# information or have any questions. 
#

# Is there a better, centralized location to put the ant tool location?
CVM_ANT 	        ?= ant
CDC_CUR_DIR		= $(shell pwd)
CDC_DIST_DIR     	= $(CDC_CUR_DIR)/$(CVM_BUILD_SUBDIR_NAME)
CVM_JUMP_BUILDDIR	= $(CDC_DIST_DIR)/jump

ifeq ($(CVM_INCLUDE_JUMP),true)
#
# JUMP defs
#
export JAVA_HOME	= $(JDK_HOME)
JUMP_ANT_OPTIONS        += -Ddist.dir=$(call POSIX2HOST,$(CVM_JUMP_BUILDDIR)) -Dcdc.dir=$(call POSIX2HOST,${CDC_DIST_DIR})
# The default JUMP component location
JUMP_DIR		?= $(CVM_TOP)/../jump/trunk
ifeq ($(wildcard $(JUMP_DIR)/build/build.xml),)
$(error JUMP_DIR must point to a JUMP directory)
endif
JUMP_OUTPUT_DIR         = $(CVM_JUMP_BUILDDIR)/lib
JUMP_SRCDIR             = $(JUMP_DIR)/src

ifeq ($(CVM_TERSEOUTPUT), false)
CVM_ANT_OPTIONS         += -v
endif
ifneq ($(CVM_DEBUG), true)
CVM_ANT_OPTIONS         += -Ddebug=false
endif

#
# JUMP_DEPENDENCIES defines what needs to be built for jump
#
JUMP_DEPENDENCIES   = $(CVM_BUILDTIME_CLASSESZIP)

ifneq ($(CVM_PRELOAD_LIB), true)
JUMP_DEPENDENCIES   += $(LIB_CLASSESJAR)
endif

JUMP_API_CLASSESZIP	= $(JUMP_OUTPUT_DIR)/jump-api.jar
JUMP_IMPL_CLASSESZIP	= $(JUMP_OUTPUT_DIR)/jump-impl.jar

JUMP_SRCDIRS           += \
	$(JUMP_SRCDIR)/share/api/native \
	$(JUMP_SRCDIR)/share/impl/isolate/native \
	$(JUMP_SRCDIR)/share/impl/os/native

# Add as necessary
#	$(JUMP_SRCDIR)/share/impl/<component>/native \
#

JUMP_INCLUDES  += \
	-I$(call POSIX2HOST,$(JUMP_SRCDIR)/share/api/native/include) \
	-I$(call POSIX2HOST,$(JUMP_SRCDIR)/share/impl/os/native/include) \

# Add as necessary
#	-I$(JUMP_SRCDIR)/share/impl/<component>/native/include \
#

#
# Any shared native code goes here.
# 
JUMP_OBJECTS            += \
	jump_os_impl.o \
	jump_messaging.o \
	jump_isolate_impl.o \

#
# Any native code for the stand-alone jump native library goes here
# 
JUMP_NATIVE_LIBRARY_OBJECTS            += \
	jump_messaging.o

JUMP_NATIVE_LIBRARY_PATHNAME = $(JUMP_OUTPUT_DIR)/$(LIB_PREFIX)jumpmesg$(LIB_POSTFIX)

#
# Make sure this shared library gets built
#
CLASSLIB_DEPS += $(JUMP_NATIVE_LIBRARY_PATHNAME)

#
# Get any platform specific dependencies of any kind.
#
-include ../$(TARGET_CPU_FAMILY)/defs_jump.mk
-include ../$(TARGET_OS)/defs_jump.mk
-include ../$(TARGET_OS)-$(TARGET_CPU_FAMILY)/defs_jump.mk
-include ../$(TARGET_OS)-$(TARGET_CPU_FAMILY)-$(TARGET_DEVICE)/defs_jump.mk

#
# Finally modify CVM variables w/ all the JUMP items
#
JUMP_NATIVE_LIB_OBJS     = $(patsubst %.o,$(CVM_OBJDIR)/%.o,$(JUMP_NATIVE_LIBRARY_OBJECTS))
CVM_CVMC_OBJECTS        += $(JUMP_NATIVE_LIB_OBJS)
CVM_OBJECTS             += $(patsubst %.o,$(CVM_OBJDIR)/%.o,$(JUMP_OBJECTS))
CVM_SRCDIRS             += $(JUMP_SRCDIRS)
CVM_INCLUDES            += $(JUMP_INCLUDES)

MIDP_CLASSESZIP_DEPS += $(JUMP_API_CLASSESZIP)

#
# In case we build any libraries that we want the cvm binary to use
#
#LINKLIBS 		+= -L$(PCSL_OUTPUT_DIR)/$(PCSL_TARGET)/lib -lpcsl_file -lpcsl_memory -lpcsl_network -lpcsl_print -lpcsl_string

# Add JUMP classes to JCC input list so they can be romized.
ifeq ($(CVM_PRELOAD_LIB), true)
CVM_JCC_INPUT		+= $(JUMP_API_CLASSESZIP)
CVM_JCC_INPUT		+= $(JUMP_IMPL_CLASSESZIP)

CVM_CNI_CLASSES +=

endif

endif

