#
# @(#)rules_jump.mk	1.3 06/10/25
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

ifeq ($(CVM_INCLUDE_JUMP),true)

# print our configuration
printconfig::
	@echo "JUMP_DIR           = $(JUMP_DIR)"

.PHONY: jumptargets force_jump_build
jumptargets: force_jump_build

$(CVM_BUILD_DEFS_MK)::
	$(AT) echo updating $@ [from rules_jump.mk]
	$(AT) echo "# JUMP specific exports" >> $@
	$(AT) echo "JUMP_API_CLASSESZIP = $(JUMP_API_CLASSESZIP)" >> $@
	$(AT) echo "" >> $@

#
# For now we are forcing a jump build because we can't deduce dependencies
# that force it to be rebuilt. But list $(JUMP_DEPENDENCIES) anyway just to
# make things explicit
#
force_jump_build: $(JUMP_DEPENDENCIES)
	@echo "====> start building jump api's and implementation"
	$(AT)(cd $(JUMP_DIR); $(CVM_ANT) $(CVM_ANT_OPTIONS) $(JUMP_ANT_OPTIONS) -f build/build.xml all)
	$(AT)cp $(JUMP_API_CLASSESZIP) \
                $(JUMP_IMPL_CLASSESZIP) \
                $(JUMP_SHARED_BOOTCLASSESZIP) \
                $(JUMP_EXECUTIVE_BOOTCLASSESZIP) \
                $(CVM_LIBDIR)
	@echo  "<==== done building jump api's and implementation"

.PHONY: javadoc-api
javadoc-api:
	@echo "====> start building Javadoc for jump APIs"
	$(AT)(cd $(JUMP_DIR); $(CVM_ANT) $(CVM_ANT_OPTIONS) $(JUMP_ANT_OPTIONS) -f build/build.xml javadoc-api)
	@echo "<==== end building Javadoc for jump APIs"

$(JUMP_NATIVE_LIBRARY_PATHNAME) :: $(JUMP_NATIVE_LIB_OBJS)
	@echo "Linking $@"
	$(SO_LINK_CMD)
	$(AT)cp $@ $(CVM_LIBDIR)

#
# JUMP unit testing
#
# NOTE: due to quirks of Ant 1.6.x JUnit3.8.1 jar should be added into ant libs
#

BUILD_UNITTEST_ANT_OPTIONS := $(CVM_ANT_OPTIONS) $(JUMP_ANT_OPTIONS) -Djunit3.8.1.jar=$(JUNIT_JAR)
RUN_UNITTEST_ANT_OPTIONS := $(BUILD_UNITTEST_ANT_OPTIONS) -lib $(JUNIT_JAR)

# Provide a default value to JUNIT_JAR if it's not set
JUNIT_JAR ?= /usr/share/ant/lib/junit.jar

# Quick check of JUNIT_JAR validity
define check_JUNIT_JAR
	$(AT)(jar tf $(JUNIT_JAR) &> /dev/null || (echo "JUNIT_JAR appears to be invalid or missing: [$(JUNIT_JAR)]" ; exit -1))
endef

build-unittests::
	@echo "====> start building jump unit-tests"
	$(check_JUNIT_JAR)
	$(AT)(cd $(JUMP_DIR); $(CVM_ANT) $(BUILD_UNITTEST_ANT_OPTIONS) -f build/build.xml only-build-unittests)
	@echo "<==== end building jump unit-tests"

run-unittests::
	@echo "====> start running jump unit-tests"
	$(check_JUNIT_JAR)
	$(AT)(cd $(JUMP_DIR); $(CVM_ANT) $(RUN_UNITTEST_ANT_OPTIONS) -f build/build.xml only-run-unittests)
	@echo "<==== end running jump unit-tests"

source_bundle::
	$(AT)(cd $(JUMP_DIR); $(CVM_ANT) $(CVM_ANT_OPTIONS) $(JUMP_ANT_OPTIONS) -f build/build-src-bundle.xml source_bundle)

endif
