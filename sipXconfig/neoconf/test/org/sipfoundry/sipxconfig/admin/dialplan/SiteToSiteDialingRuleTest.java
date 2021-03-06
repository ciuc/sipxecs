/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.admin.dialplan;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import junit.framework.JUnit4TestAdapter;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.sipfoundry.sipxconfig.admin.ScheduledDay;
import org.sipfoundry.sipxconfig.admin.dialplan.attendant.WorkingTime;
import org.sipfoundry.sipxconfig.admin.dialplan.attendant.WorkingTime.WorkingHours;
import org.sipfoundry.sipxconfig.admin.dialplan.config.FullTransform;
import org.sipfoundry.sipxconfig.admin.dialplan.config.Transform;
import org.sipfoundry.sipxconfig.admin.forwarding.GeneralSchedule;
import org.sipfoundry.sipxconfig.admin.forwarding.Schedule;
import org.sipfoundry.sipxconfig.gateway.Gateway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * SiteToSiteDialingRuleTest
 */
public class SiteToSiteDialingRuleTest {

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(SiteToSiteDialingRuleTest.class);
      }

    private static final int PATTERN_COUNT = 10;
    private static final String[] GATEWAYS = {
        "10.2.3.4", "10.4.5.6"
    };

    private static final int[] PORTS = {
        0, 5090
    };

    private static final String[] GATEWAYADDRESSES = {
        "10.2.3.4", "10.4.5.6:5090"
    };

    private static final String[] PREFIXES = {
        null, "43"
    };

    private SiteToSiteDialingRule m_rule;
    private List m_patternsList;
    private Schedule m_schedule;

    @Before
    public void setUp() throws Exception {
        m_schedule = new GeneralSchedule();
        m_schedule.setName("Custom schedule");
        WorkingHours[] hours = new WorkingHours[1];
        WorkingTime wt = new WorkingTime();
        hours[0] = new WorkingHours();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2006, Calendar.DECEMBER, 31, 10, 12);
        hours[0].setStart(cal.getTime());
        cal.set(2006, Calendar.DECEMBER, 31, 11, 88);
        hours[0].setStop(cal.getTime());
        hours[0].setEnabled(true);
        hours[0].setDay(ScheduledDay.WEDNESDAY);
        wt.setWorkingHours(hours);
        wt.setEnabled(true);
        m_schedule.setWorkingTime(wt);
        DialPattern[] dialPatterns = new DialPattern[PATTERN_COUNT];
        for (int i = 0; i < dialPatterns.length; i++) {
            DialPattern p = new DialPattern();
            p.setPrefix("91");
            p.setDigits(i + 2);
            dialPatterns[i] = p;
        }
        m_patternsList = Arrays.asList(dialPatterns);

        m_rule = new SiteToSiteDialingRule();
        m_rule.setDialPatterns(m_patternsList);

        for (int i = 0; i < GATEWAYS.length; i++) {
            Gateway gateway = new Gateway();
            gateway.setUniqueId();
            gateway.setAddress(GATEWAYS[i]);
            gateway.setPrefix(PREFIXES[i]);
            gateway.setAddressPort(PORTS[i]);
            m_rule.addGateway(gateway);
        }

        m_rule.setEnabled(true);
        m_rule.setCallPattern(new CallPattern("999", CallDigits.VARIABLE_DIGITS));
    }

    @Test
    public void testGetPatterns() {
        String[] patterns = m_rule.getPatterns();
        assertEquals(PATTERN_COUNT, patterns.length);
        for (int i = 0; i < patterns.length; i++) {
            String p = patterns[i];
            assertTrue(p.startsWith("91"));
        }
    }

    @Test
    public void testGetTransforms() {
        Transform[] transforms = m_rule.getTransforms();
        assertEquals(GATEWAYS.length, transforms.length);
        for (int i = 0; i < GATEWAYS.length; i++) {
            assertTrue(transforms[i] instanceof FullTransform);
            FullTransform full = (FullTransform) transforms[i];
            String[] fieldParams = full.getFieldParams();
            assertEquals(1, fieldParams.length);
            assertTrue(fieldParams[0].startsWith("q="));
            assertEquals(full.getHeaderParams()[0], "expires=7200");
            assertEquals(GATEWAYADDRESSES[i], full.getHost());
            EmergencyRuleTest.assertUrlParams(full.getUrlParams());
            assertTrue(full.getUser().startsWith(StringUtils.defaultString(PREFIXES[i]) + "999"));
        }
    }

    @Test
    public void testGetTransformsWithSchedule() {
        m_rule.setSchedule(m_schedule);
        Transform[] transforms = m_rule.getTransforms();
        assertEquals(GATEWAYS.length, transforms.length);
        for (int i = 0; i < GATEWAYS.length; i++) {
            assertTrue(transforms[i] instanceof FullTransform);
            FullTransform full = (FullTransform) transforms[i];
            String[] fieldParams = full.getFieldParams();
            assertEquals(2, fieldParams.length);
            assertTrue(fieldParams[0].startsWith("q="));
            assertTrue(fieldParams[1].startsWith("sipx-ValidTime="));
            assertEquals(full.getHeaderParams()[0], "expires=7200");
            assertEquals(GATEWAYADDRESSES[i], full.getHost());
            EmergencyRuleTest.assertUrlParams(full.getUrlParams());
            assertTrue(full.getUser().startsWith(StringUtils.defaultString(PREFIXES[i]) + "999"));
        }
    }

    @Test
    public void testGetRouteHeader() {
        Gateway g = new Gateway() {
            @Override
            public String getRoute() {
                return "bongo.example.org";
            }
        };
        m_rule.setGateways(Collections.singletonList(g));
        Transform[] transforms = m_rule.getTransforms();
        assertEquals(1, transforms.length);
        FullTransform full = (FullTransform) transforms[0];
        assertEquals(2, full.getHeaderParams().length);
        assertEquals("route=bongo.example.org", full.getHeaderParams()[0]);
    }

    @Test
    public void testNoGateways() {
        SiteToSiteDialingRule rule = new SiteToSiteDialingRule();
        rule.setDialPatterns(m_patternsList);
        rule.setEnabled(true);
        rule.setCallPattern(new CallPattern("999", CallDigits.VARIABLE_DIGITS));

        String[] patterns = rule.getPatterns();
        assertEquals(PATTERN_COUNT, patterns.length);
        for (int i = 0; i < patterns.length; i++) {
            String p = patterns[i];
            assertTrue(p.startsWith("91"));
        }

        Transform[] transforms = rule.getTransforms();
        assertEquals(1, transforms.length);
        FullTransform tr = (FullTransform) transforms[0];
        assertEquals("999{vdigits}", tr.getUser());
        assertNull(tr.getFieldParams());
        assertNull(tr.getHost());
    }

    @Test
    public void testNoGatewaysWithSchedule() {
        SiteToSiteDialingRule rule = new SiteToSiteDialingRule();
        rule.setDialPatterns(m_patternsList);
        rule.setEnabled(true);
        rule.setCallPattern(new CallPattern("999", CallDigits.VARIABLE_DIGITS));
        rule.setSchedule(m_schedule);

        String[] patterns = rule.getPatterns();
        assertEquals(PATTERN_COUNT, patterns.length);
        for (int i = 0; i < patterns.length; i++) {
            String p = patterns[i];
            assertTrue(p.startsWith("91"));
        }

        Transform[] transforms = rule.getTransforms();
        assertEquals(1, transforms.length);
        FullTransform tr = (FullTransform) transforms[0];
        assertEquals("999{vdigits}", tr.getUser());
        String[] fieldParams = tr.getFieldParams();
        assertTrue(fieldParams[0].startsWith("sipx-ValidTime="));
        assertNull(tr.getHost());
    }

    @Test
    public void testGetTransformedPatternsVariable() throws Exception {
        SiteToSiteDialingRule rule = new SiteToSiteDialingRule();
        rule.setDialPatterns(m_patternsList);
        rule.setEnabled(true);
        rule.setCallPattern(new CallPattern("77", CallDigits.VARIABLE_DIGITS));

        String[] patterns = rule.getTransformedPatterns(new Gateway());
        assertEquals(PATTERN_COUNT, patterns.length);
        String suffix = "xx";
        for (int i = 0; i < patterns.length; i++) {
            assertEquals("77" + suffix, patterns[i]);
            suffix = suffix + "x";
        }
    }

    @Test
    public void testGetTransformedPatternsFixed() throws Exception {
        SiteToSiteDialingRule rule = new SiteToSiteDialingRule();
        rule.setDialPatterns(m_patternsList);
        rule.setEnabled(true);
        rule.setCallPattern(new CallPattern("77", CallDigits.FIXED_DIGITS));

        String[] patterns = rule.getTransformedPatterns(new Gateway());
        assertEquals(PATTERN_COUNT, patterns.length);
        String suffix = "xx";
        for (int i = 0; i < patterns.length; i++) {
            assertEquals("7791" + suffix, patterns[i]);
            suffix = suffix + "x";
        }
    }

    @Test
    public void testGetTransformedPatternsNoDigits() throws Exception {
        SiteToSiteDialingRule rule = new SiteToSiteDialingRule();
        rule.setDialPatterns(m_patternsList);
        rule.setEnabled(true);
        rule.setCallPattern(new CallPattern("77", CallDigits.NO_DIGITS));

        String[] patterns = rule.getTransformedPatterns(new Gateway());
        assertEquals(1, patterns.length);
        assertEquals("77", patterns[0]);
    }
}
