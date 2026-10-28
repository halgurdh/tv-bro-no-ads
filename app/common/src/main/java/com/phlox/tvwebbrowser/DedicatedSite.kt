package com.phlox.tvwebbrowser

/**
 * The site this build is made for: it is the home page and every search (typed or by voice) is limited to it.
 * To make the build for another site, change [NAME], [NAMES], [VOICE_LANGUAGE], [KNOWN_DOMAINS] and [searchUrl].
 *
 * The site moves to a new domain now and then (brsteej.site, brsteej.casa, brstej.lol...): any `<name>.<ending>`
 * is the site, and the domain the app last landed on is remembered (see [Config.onSiteVisited]).
 */
object DedicatedSite {
    const val NAME = "brsteej"

    /** the spellings the site's domains use */
    val NAMES = listOf("brsteej", "brstej")

    /** the language its shows are searched in by voice (one of [Config.VoiceSearchLanguages]) */
    const val VOICE_LANGUAGE = "ar"

    /** the site's settings are applied once per value of this (see Config's init) */
    const val SETUP_ID = "$NAME/$VOICE_LANGUAGE"

    /** the domains the site has used, newest first */
    val KNOWN_DOMAINS = listOf("brsteej.casa", "brsteej.site", "brsteej.lol")

    private val SITE_HOST = Regex("""^(www\.)?(${NAMES.joinToString("|") { Regex.escape(it) }})\.[a-z0-9-]+$""",
        RegexOption.IGNORE_CASE)

    /** whether [host] is the site, whatever its domain ending is */
    fun isSiteHost(host: String?) = host != null && SITE_HOST.matches(host)

    fun domainOf(host: String) = host.lowercase().removePrefix("www.")

    fun homeUrl(domain: String) = "https://$domain/"

    /**
     * The site's own search for the url-encoded [query]. Search engines don't help: DuckDuckGo (Bing) has none of
     * the site's pages, so `site:` finds nothing.
     */
    fun searchUrl(domain: String, query: String) = "https://$domain/search.php?keywords=$query"
}
