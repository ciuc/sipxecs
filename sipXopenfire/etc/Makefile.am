include $(top_srcdir)/config/utility.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(proc_DATA:=.in) \
	$(etc_DATA:=.in)

ofcdir = @SIPX_JAVADIR@/sipXopenfire/conf
dist_ofc_DATA = \
	openfire.xml \
	web.xml.enable_openfire_admin_console \
	web.xml.disable_openfire_admin_console

# need to copy some over jars from sipxcommons for the provider plugin
# NOTE: not verified this is still nec. after the ant port because these 
# jars are also specific in the openfire startup
ofldir = @SIPX_JAVADIR@/sipXopenfire/lib
ofl_DATA = $(call JavaDep, @SIPX_JAVADIR@/sipXcommons, $(provider_PKGS))

dbdir = @SIPX_CONFDIR@/database
dist_db_DATA = \
	sipxopenfire-initdb.sql

procdir = $(SIPX_DATADIR)/process.d
proc_DATA = \
	sipxopenfire-process.xml

cfgdir = @SIPX_CONFDIR@/openfire
dist_cfg_DATA = \
	sipxopenfire-prompts.properties \
	openfire.properties \
	openfire.vm \
	sipxopenfire.vm \
	openfire.xml 

etcdir = @SIPX_CONFDIR@
etc_DATA = \
	sipxopenfire-database.xml

$(etc_DATA) $(proc_DATA) : % : %.in Makefile
	@$(call SearchAndReplace, $<, $@, OPENFIRE_HOME)

CLEANFILES = $(proc_DATA) $(etc_DATA)
