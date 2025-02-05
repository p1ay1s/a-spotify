package com.niki.spotify.web

/**
 * The maximum number of objects to return.
 */
const val LIMIT: String = "limit"

/**
 * The index of the first playlist to return. Default: 0 (the first object).
 * Use with limit to get the next set of objects (albums, playlists, etc).
 */
const val OFFSET: String = "offset"

/**
 * A comma-separated list of keywords that will be used to filter the response.
 * Valid values are: `album`, `single`, `appears_on`, `compilation`
 */
const val ALBUM_TYPE: String = "album_type"

/**
 * The country: an ISO 3166-1 alpha-2 country code.
 * Limit the response to one particular geographical market.
 * Synonym to [.COUNTRY]
 */
const val MARKET: String = "market"

/**
 * Same as [.MARKET]
 */
const val COUNTRY: String = "country"

/**
 * The desired language, consisting of a lowercase ISO 639 language code
 * and an uppercase ISO 3166-1 alpha-2 country code, joined by an underscore.
 * For example: es_MX, meaning "Spanish (Mexico)".
 */
const val LOCALE: String = "locale"

/**
 * Filters for the query: a comma-separated list of the fields to return.
 * If omitted, all fields are returned.
 */
const val FIELDS: String = "fields"

/**
 * A timestamp in ISO 8601 format: yyyy-MM-ddTHH:mm:ss. Use this parameter to
 * specify the user's local time to get results tailored for that specific date
 * and time in the day. If not provided, the response defaults to the current UTC time
 */
const val TIMESTAMP: String = "timestamp"

/**
 * A Unix timestamp in milliseconds. Returns all items before (but not including)
 * this cursor position. If before is specified, after must not be specified.
 * Integer type.
 */
const val BEFORE: String = "before"

/**
 * A Unix timestamp in milliseconds. Returns all items after (but not including)
 * this cursor position. If after is specified, before must not be specified.
 * Integer type.
 */
const val AFTER: String = "after"
