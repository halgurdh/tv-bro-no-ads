package com.phlox.tvwebbrowser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DedicatedSiteTest {
    @Test
    fun anyDomainEndingIsTheSite() {
        for (host in listOf("brsteej.site", "brsteej.casa", "www.brsteej.lol", "BRSTEEJ.Net", "brstej.lol", "brstej.xyz")) {
            assertTrue(host, DedicatedSite.isSiteHost(host))
        }
        for (host in listOf("notbrsteej.site", "brsteej.site.evil.com", "ads.brsteej.casa", "duckduckgo.com", "")) {
            assertFalse(host, DedicatedSite.isSiteHost(host))
        }
        assertFalse(DedicatedSite.isSiteHost(null))
        assertEquals("brsteej.lol", DedicatedSite.domainOf("WWW.Brsteej.lol"))
    }

    @Test
    fun searchIsOnTheCurrentDomain() {
        assertEquals("https://brstej.new/search.php?keywords=a+b", DedicatedSite.searchUrl("brstej.new", "a+b"))
    }
}
