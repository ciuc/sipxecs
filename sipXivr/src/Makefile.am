include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(ivr_SRC) \
	$(ivr_RESOURCES)

noinst_DATA = javac-ivr

jardir = @SIPX_JAVADIR@/sipXivr
JAR_FILE = sipXivr.jar
jar_DATA = $(JAR_FILE)

ivr_SRC = $(shell cd $(srcdir); find org -type f -name '*.java')
ivr_RESOURCES = $(shell cd $(srcdir); find . \( -name '*.png' -o -name '*.properties' \) )

ivr_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons,$(ivr_PKGS)) \
	@SIPX_JAVADIR@/sipXopenfire/plugins/sipx-openfire-presence-plugin.jar

$(JAR_FILE) : javac-ivr $(ivr_RESOURCES)
	jar cf $@ \
	  $(call JarInclude,$(JAVAROOT),.) \
	  $(call JarInclude,$(srcdir),$(ivr_RESOURCES))
