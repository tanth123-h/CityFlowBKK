package com.example.cityflowbkk.features.route

import android.util.Log
import com.example.cityflowbkk.features.map.MapLatLng
import com.example.cityflowbkk.features.map.RouteTransportType

/**
 * Local database of Bangkok rail transit stations with GPS coordinates.
 *
 * Sources: official BTS/MRTA published station data.
 * Covers: BTS Sukhumvit Line, BTS Silom Line, MRT Blue Line,
 *         MRT Purple Line, and Airport Rail Link.
 */
object StationRepository {

    private const val TAG = "StationRepository"

    /** Default search radius in metres. Configurable per call. */
    const val DEFAULT_SEARCH_RADIUS_METERS = 3000.0

    // ─────────────────────────────────────────────────────────────────────────
    // BTS Sukhumvit Line
    // ─────────────────────────────────────────────────────────────────────────
    private val btsSukhumvitStations = listOf(
        RailStation("Kheha",                   RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.6912, 100.5990)),
        RailStation("Samrong",                 RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.6994, 100.5985)),
        RailStation("Punawithi",               RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7068, 100.5977)),
        RailStation("Udom Suk",                RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7135, 100.5979)),
        RailStation("Bang Na",                 RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7202, 100.5982)),
        RailStation("Bearing",                 RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7269, 100.5975)),
        RailStation("On Nut",                  RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7017, 100.6003)),
        RailStation("Phra Khanong",            RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7152, 100.5892)),
        RailStation("Ekkamai",                 RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7200, 100.5849)),
        RailStation("Thong Lo",                RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7261, 100.5790)),
        RailStation("Asok",                    RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7360, 100.5604)),
        RailStation("Nana",                    RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7401, 100.5551)),
        RailStation("Phloenchit",              RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7440, 100.5478)),
        RailStation("Chit Lom",                RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7462, 100.5398)),
        RailStation("Siam",                    RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7457, 100.5331)),
        RailStation("National Stadium",        RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7462, 100.5290)),
        RailStation("Ratchadamri",             RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7428, 100.5413)),
        RailStation("Sala Daeng",              RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7287, 100.5337)),
        RailStation("Ari",                     RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7749, 100.5456)),
        RailStation("Saphan Khwai",            RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7845, 100.5473)),
        RailStation("Mo Chit",                 RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8025, 100.5537)),
        RailStation("Ha Yaek Lat Phrao",       RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8164, 100.5596)),
        RailStation("Phahon Yothin",           RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8126, 100.5568)),
        RailStation("Sena Nikhom",             RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8258, 100.5654)),
        RailStation("Ratchayothin",            RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8340, 100.5687)),
        RailStation("Lat Phrao 71",            RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8413, 100.5714)),
        RailStation("Lat Phrao 83",            RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8473, 100.5736)),
        RailStation("Chokchai 4",              RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8535, 100.5755)),
        RailStation("Wat Phra Sri Mahathat",   RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8618, 100.5779)),
        RailStation("Ram Inthra 3",            RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8688, 100.5810)),
        RailStation("Ram Inthra 40",           RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8780, 100.5853)),
        RailStation("Ram Inthra 54",           RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8858, 100.5897)),
        RailStation("Ram Inthra 63",           RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8910, 100.5939)),
        RailStation("Ram Inthra 83",           RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.9000, 100.6015)),
        RailStation("Nopparat",                RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.9068, 100.6065)),
        RailStation("Bueng Kum",               RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.8645, 100.5975)),
        RailStation("Fashion Island",          RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.9124, 100.6095)),
        // Gold Line (BTS-operated)
        RailStation("Charoen Nakhon",          RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7270, 100.5107)),
        RailStation("Khlong San",              RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7276, 100.5052)),
        RailStation("Prajadhipok",             RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7262, 100.4981)),
        RailStation("Krung Thon Buri",         RouteTransportType.BTS_SUKHUMVIT, MapLatLng(13.7236, 100.4921)),
    )

    // ─────────────────────────────────────────────────────────────────────────
    // BTS Silom Line
    // ─────────────────────────────────────────────────────────────────────────
    private val btsSilomStations = listOf(
        RailStation("National Stadium",        RouteTransportType.BTS_SILOM, MapLatLng(13.7462, 100.5290)),
        RailStation("Siam",                    RouteTransportType.BTS_SILOM, MapLatLng(13.7457, 100.5331)),
        RailStation("Ratchadamri",             RouteTransportType.BTS_SILOM, MapLatLng(13.7428, 100.5413)),
        RailStation("Sala Daeng",              RouteTransportType.BTS_SILOM, MapLatLng(13.7287, 100.5337)),
        RailStation("Chong Nonsi",             RouteTransportType.BTS_SILOM, MapLatLng(13.7218, 100.5284)),
        RailStation("Saint Louis",             RouteTransportType.BTS_SILOM, MapLatLng(13.7175, 100.5274)),
        RailStation("Surasak",                 RouteTransportType.BTS_SILOM, MapLatLng(13.7138, 100.5237)),
        RailStation("Saphan Taksin",           RouteTransportType.BTS_SILOM, MapLatLng(13.7186, 100.5143)),
        RailStation("Krung Thon Buri",         RouteTransportType.BTS_SILOM, MapLatLng(13.7236, 100.4921)),
        RailStation("Wongwian Yai",            RouteTransportType.BTS_SILOM, MapLatLng(13.7218, 100.4803)),
        RailStation("Pho Nimit",               RouteTransportType.BTS_SILOM, MapLatLng(13.7196, 100.4738)),
        RailStation("Talat Phlu",              RouteTransportType.BTS_SILOM, MapLatLng(13.7183, 100.4663)),
        RailStation("Wutthakat",               RouteTransportType.BTS_SILOM, MapLatLng(13.7160, 100.4568)),
        RailStation("Bang Wa",                 RouteTransportType.BTS_SILOM, MapLatLng(13.7245, 100.4424)),
    )

    // ─────────────────────────────────────────────────────────────────────────
    // MRT Blue Line
    // ─────────────────────────────────────────────────────────────────────────
    private val mrtBlueStations = listOf(
        RailStation("Tha Phra",                RouteTransportType.MRT_BLUE, MapLatLng(13.7467, 100.4573)),
        RailStation("Charan 13",               RouteTransportType.MRT_BLUE, MapLatLng(13.7559, 100.4648)),
        RailStation("Fai Chai",                RouteTransportType.MRT_BLUE, MapLatLng(13.7630, 100.4708)),
        RailStation("Bang Khun Non",           RouteTransportType.MRT_BLUE, MapLatLng(13.7705, 100.4745)),
        RailStation("Bang Yi Khan",            RouteTransportType.MRT_BLUE, MapLatLng(13.7794, 100.4792)),
        RailStation("Sirindhorn",              RouteTransportType.MRT_BLUE, MapLatLng(13.7858, 100.4860)),
        RailStation("Bang Phlat",              RouteTransportType.MRT_BLUE, MapLatLng(13.7893, 100.4959)),
        RailStation("Bang O",                  RouteTransportType.MRT_BLUE, MapLatLng(13.7940, 100.5063)),
        RailStation("Bang Pho",                RouteTransportType.MRT_BLUE, MapLatLng(13.8015, 100.5148)),
        RailStation("Tao Poon",                RouteTransportType.MRT_BLUE, MapLatLng(13.8068, 100.5249)),
        RailStation("Bang Sue",                RouteTransportType.MRT_BLUE, MapLatLng(13.8048, 100.5345)),
        RailStation("Kamphaeng Phet",          RouteTransportType.MRT_BLUE, MapLatLng(13.8007, 100.5484)),
        RailStation("Chatuchak Park",          RouteTransportType.MRT_BLUE, MapLatLng(13.7987, 100.5520)),
        RailStation("Phahon Yothin",           RouteTransportType.MRT_BLUE, MapLatLng(13.8126, 100.5568)),
        RailStation("Lat Phrao",               RouteTransportType.MRT_BLUE, MapLatLng(13.8198, 100.5680)),
        RailStation("Ratchadaphisek",          RouteTransportType.MRT_BLUE, MapLatLng(13.8082, 100.5762)),
        RailStation("Sutthisan",               RouteTransportType.MRT_BLUE, MapLatLng(13.7912, 100.5752)),
        RailStation("Huai Khwang",             RouteTransportType.MRT_BLUE, MapLatLng(13.7753, 100.5749)),
        RailStation("Thailand Cultural Centre",RouteTransportType.MRT_BLUE, MapLatLng(13.7635, 100.5709)),
        RailStation("Phra Ram 9",              RouteTransportType.MRT_BLUE, MapLatLng(13.7575, 100.5637)),
        RailStation("Phetchaburi",             RouteTransportType.MRT_BLUE, MapLatLng(13.7497, 100.5567)),
        RailStation("Sukhumvit",               RouteTransportType.MRT_BLUE, MapLatLng(13.7376, 100.5611)),
        RailStation("Queen Sirikit National Convention Centre", RouteTransportType.MRT_BLUE, MapLatLng(13.7235, 100.5591)),
        RailStation("Khlong Toei",             RouteTransportType.MRT_BLUE, MapLatLng(13.7213, 100.5546)),
        RailStation("Lumphini",                RouteTransportType.MRT_BLUE, MapLatLng(13.7248, 100.5450)),
        RailStation("Si Lom",                  RouteTransportType.MRT_BLUE, MapLatLng(13.7275, 100.5344)),
        RailStation("Sam Yan",                 RouteTransportType.MRT_BLUE, MapLatLng(13.7294, 100.5270)),
        RailStation("Hua Lamphong",            RouteTransportType.MRT_BLUE, MapLatLng(13.7379, 100.5168)),
        RailStation("Wat Mangkon",             RouteTransportType.MRT_BLUE, MapLatLng(13.7416, 100.5086)),
        RailStation("Sam Yot",                 RouteTransportType.MRT_BLUE, MapLatLng(13.7471, 100.5012)),
        RailStation("Sanam Chai",              RouteTransportType.MRT_BLUE, MapLatLng(13.7491, 100.4933)),
        RailStation("Itsaraphap",              RouteTransportType.MRT_BLUE, MapLatLng(13.7460, 100.4814)),
        RailStation("Tha Phra",                RouteTransportType.MRT_BLUE, MapLatLng(13.7467, 100.4573)),
        RailStation("Lak Song",                RouteTransportType.MRT_BLUE, MapLatLng(13.7239, 100.4204)),
        RailStation("Bang Khae",               RouteTransportType.MRT_BLUE, MapLatLng(13.7258, 100.4295)),
        RailStation("Phasi Charoen",           RouteTransportType.MRT_BLUE, MapLatLng(13.7206, 100.4370)),
        RailStation("Bang Wa",                 RouteTransportType.MRT_BLUE, MapLatLng(13.7245, 100.4424)),
        RailStation("Phetkasem 48",            RouteTransportType.MRT_BLUE, MapLatLng(13.7191, 100.4485)),
    )

    // ─────────────────────────────────────────────────────────────────────────
    // MRT Purple Line
    // ─────────────────────────────────────────────────────────────────────────
    private val mrtPurpleStations = listOf(
        RailStation("Tao Poon",                RouteTransportType.MRT_PURPLE, MapLatLng(13.8068, 100.5249)),
        RailStation("Bang Son",                RouteTransportType.MRT_PURPLE, MapLatLng(13.8174, 100.5283)),
        RailStation("Wong Sawang",             RouteTransportType.MRT_PURPLE, MapLatLng(13.8303, 100.5272)),
        RailStation("Khlong Bang Phai",        RouteTransportType.MRT_PURPLE, MapLatLng(13.8411, 100.5199)),
        RailStation("Yaek Tiwanon",            RouteTransportType.MRT_PURPLE, MapLatLng(13.8526, 100.5156)),
        RailStation("Nonthaburi Civic Centre", RouteTransportType.MRT_PURPLE, MapLatLng(13.8624, 100.5138)),
        RailStation("Bang Krasor",             RouteTransportType.MRT_PURPLE, MapLatLng(13.8698, 100.5110)),
        RailStation("Ministry of Public Health",RouteTransportType.MRT_PURPLE, MapLatLng(13.8783, 100.5101)),
        RailStation("Bang Rak Yai",            RouteTransportType.MRT_PURPLE, MapLatLng(13.8857, 100.5074)),
        RailStation("Bang Rak Noi Tha It",     RouteTransportType.MRT_PURPLE, MapLatLng(13.8941, 100.5042)),
        RailStation("Sai Ma",                  RouteTransportType.MRT_PURPLE, MapLatLng(13.9070, 100.4936)),
        RailStation("Phak Haeo",               RouteTransportType.MRT_PURPLE, MapLatLng(13.9143, 100.4852)),
        RailStation("Sam Yaek Bang Yai",       RouteTransportType.MRT_PURPLE, MapLatLng(13.9218, 100.4802)),
        RailStation("Bang Yai",                RouteTransportType.MRT_PURPLE, MapLatLng(13.9302, 100.4741)),
        RailStation("Bang Phlu",               RouteTransportType.MRT_PURPLE, MapLatLng(13.9399, 100.4674)),
        RailStation("Khlong Bang Phai (outer)",RouteTransportType.MRT_PURPLE, MapLatLng(13.9498, 100.4612)),
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Airport Rail Link
    // ─────────────────────────────────────────────────────────────────────────
    private val airportRailLinkStations = listOf(
        RailStation("Phaya Thai",              RouteTransportType.AIRPORT_RAIL_LINK, MapLatLng(13.7559, 100.5342)),
        RailStation("Ratchaprarop",            RouteTransportType.AIRPORT_RAIL_LINK, MapLatLng(13.7498, 100.5413)),
        RailStation("Makkasan",                RouteTransportType.AIRPORT_RAIL_LINK, MapLatLng(13.7490, 100.5544)),
        RailStation("Ramkhamhaeng",            RouteTransportType.AIRPORT_RAIL_LINK, MapLatLng(13.7567, 100.5822)),
        RailStation("Hua Mak",                 RouteTransportType.AIRPORT_RAIL_LINK, MapLatLng(13.7408, 100.6213)),
        RailStation("Ban Thap Chang",          RouteTransportType.AIRPORT_RAIL_LINK, MapLatLng(13.7240, 100.6539)),
        RailStation("Lat Krabang",             RouteTransportType.AIRPORT_RAIL_LINK, MapLatLng(13.7193, 100.7025)),
        RailStation("Suvarnabhumi",            RouteTransportType.AIRPORT_RAIL_LINK, MapLatLng(13.6912, 100.7501)),
    )

    /** All stations across all lines. */
    private val allStations: List<RailStation> = buildList {
        addAll(btsSukhumvitStations)
        addAll(btsSilomStations)
        addAll(mrtBlueStations)
        addAll(mrtPurpleStations)
        addAll(airportRailLinkStations)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public search API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Finds all stations within [maxRadiusMeters] of [location], sorted by distance ascending.
     *
     * @param location  The reference point.
     * @param maxRadiusMeters  Search radius in metres.
     * @return List of (station, distanceMeters) pairs, nearest first.
     */
    fun findNearest(
        location: MapLatLng,
        maxRadiusMeters: Double = DEFAULT_SEARCH_RADIUS_METERS,
    ): List<Pair<RailStation, Double>> {
        return allStations
            .map { station ->
                val dist = NavigationTracker.distanceMeters(location, station.location)
                station to dist
            }
            .filter { (_, dist) -> dist <= maxRadiusMeters }
            .sortedBy { (_, dist) -> dist }
    }

    /**
     * Finds the nearest station pair (origin + destination) using a tiered radius fallback:
     *   1 km → 2 km → 3 km (or [maxRadiusMeters] if provided).
     *
     * Returns null if no stations are found within the maximum radius for either endpoint.
     * Emits [StationSearchDiagnostics] via Android Log for debugging.
     */
    fun findNearestPairWithFallback(
        userLocation: MapLatLng,
        destinationLocation: MapLatLng,
        maxRadiusMeters: Double = DEFAULT_SEARCH_RADIUS_METERS,
    ): NearestStationPair? {
        val tiers = buildList {
            if (maxRadiusMeters >= 1000.0) add(1000.0)
            if (maxRadiusMeters >= 2000.0) add(2000.0)
            add(maxRadiusMeters)
        }.distinct()

        Log.d(TAG, "Station search — user: (${userLocation.latitude}, ${userLocation.longitude})" +
            "  dest: (${destinationLocation.latitude}, ${destinationLocation.longitude})" +
            "  maxRadius: ${maxRadiusMeters}m")

        for (radius in tiers) {
            val originCandidates  = findNearest(userLocation, radius)
            val destCandidates    = findNearest(destinationLocation, radius)

            Log.d(TAG, "  radius ${radius}m → ${originCandidates.size} origin candidates, " +
                "${destCandidates.size} dest candidates")

            originCandidates.take(3).forEachIndexed { i, (s, d) ->
                Log.d(TAG, "    origin[$i] ${s.name} (${s.lineName}) — %.0fm".format(d))
            }
            destCandidates.take(3).forEachIndexed { i, (s, d) ->
                Log.d(TAG, "    dest[$i]   ${s.name} (${s.lineName}) — %.0fm".format(d))
            }

            val nearestOrigin = originCandidates.firstOrNull()
            val nearestDest   = destCandidates.firstOrNull()

            if (nearestOrigin != null && nearestDest != null) {
                val (originStation, originDist) = nearestOrigin
                val (destStation,   destDist)   = nearestDest

                // Skip degenerate case: same station for both endpoints
                if (originStation.name == destStation.name &&
                    originStation.line == destStation.line) {
                    Log.d(TAG, "  skipping: origin == destination station (${originStation.name}), " +
                        "trying wider radius")
                    continue
                }

                val pair = NearestStationPair(
                    originStation        = originStation,
                    originDistanceMeters = originDist,
                    destinationStation   = destStation,
                    destinationDistanceMeters = destDist,
                )

                Log.d(TAG, "Selected pair at radius ${radius}m:")
                Log.d(TAG, "  origin station  : ${originStation.name} (${originStation.lineName})" +
                    " — %.0fm walk".format(originDist))
                Log.d(TAG, "  dest station    : ${destStation.name} (${destStation.lineName})" +
                    " — %.0fm walk".format(destDist))

                return pair
            }
        }

        Log.d(TAG, "No station pair found within ${maxRadiusMeters}m — will use Google default routing")
        return null
    }
}
