package com.example.cityflowbkk.features.btsmap.data

import android.content.Context
import com.example.cityflowbkk.features.btsmap.model.RouteEdge
import com.example.cityflowbkk.features.btsmap.model.RouteResult
import com.example.cityflowbkk.features.btsmap.model.StationNode
import com.example.cityflowbkk.features.btsmap.model.StationNodeType
import com.example.cityflowbkk.features.stationmapping.model.StationLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.PriorityQueue
import kotlin.math.sqrt

/**
 * RouteGraphRepository
 *
 * Responsibilities:
 *  1. Load stations.json from assets.
 *  2. Parse every entry into a [StationNode].
 *  3. Auto-build directed edges between adjacent stations on the same line
 *     (ordered by their JSON position — the file lists stations sequentially
 *     along each line).
 *  4. Add transfer edges at interchange stations.
 *  5. Expose [findShortestPath] using Dijkstra (minimises duration).
 *
 * Thread safety:
 *  Call [init] once (e.g. from ViewModel.init) before querying.
 *  All public methods are safe to call from any thread after [init] completes.
 */
object RouteGraphRepository {

    // ── internal graph ────────────────────────────────────────────────────────

    /** Station ID → StationNode */
    private val graph: MutableMap<String, StationNode> = mutableMapOf()

    /** True after [init] has been called successfully */
    @Volatile
    private var isReady = false

    // ── public API ────────────────────────────────────────────────────────────

    /**
     * Load and build the graph.  Must be called before any query.
     * Safe to call multiple times — re-builds on each call.
     */
    suspend fun init(context: Context) = withContext(Dispatchers.IO) {
        val json = context.assets
            .open("stations.json")
            .bufferedReader()
            .use { it.readText() }

        buildGraph(json)
        isReady = true
    }

    /** All nodes, ordered by the sequence they appear in the JSON. */
    fun allNodes(): List<StationNode> = graph.values.toList()

    /** Look up a single node by station code. */
    fun nodeById(id: String): StationNode? = graph[id]

    /** All nodes on a given [line]. */
    fun nodesByLine(line: StationLine): List<StationNode> =
        graph.values.filter { it.line == line }

    /** Interchange nodes only (BTS↔BTS or BTS↔MRT). */
    fun interchangeNodes(): List<StationNode> =
        graph.values.filter { it.isInterchange }

    /**
     * Dijkstra shortest-path between [originId] and [destinationId].
     *
     * Weight = durationMin on each edge (minimises travel time).
     *
     * Returns null when:
     *  - graph is not ready
     *  - either station ID does not exist
     *  - no path exists (disconnected graph — should never happen for BTS)
     */
    fun findShortestPath(originId: String, destinationId: String): RouteResult? {
        if (!isReady) return null
        val origin = graph[originId] ?: return null
        val dest   = graph[destinationId] ?: return null
        if (originId == destinationId) {
            return RouteResult(
                path          = listOf(origin),
                edges         = emptyList(),
                totalFare     = 0,
                totalDuration = 0,
                hasTransfer   = false,
                lineSegments  = listOf(origin.line.displayName)
            )
        }

        // ── Dijkstra ──────────────────────────────────────────────────────────
        // dist: stationId → best total durationMin found so far
        val dist    = mutableMapOf<String, Int>().withDefault { Int.MAX_VALUE }
        val prev    = mutableMapOf<String, String?>()      // stationId → predecessor
        val prevEdge = mutableMapOf<String, RouteEdge?>()  // stationId → edge used

        dist[originId] = 0

        // PriorityQueue ordered by cumulative duration (ascending)
        val pq = PriorityQueue<Pair<Int, String>>(compareBy { it.first })
        pq.offer(0 to originId)

        while (pq.isNotEmpty()) {
            val (currentDist, currentId) = pq.poll()
            if (currentDist > dist.getValue(currentId)) continue  // stale entry
            if (currentId == destinationId) break

            val node = graph[currentId] ?: continue
            for ((neighbourId, edge) in node.connections) {
                val newDist = currentDist + edge.durationMin
                if (newDist < dist.getValue(neighbourId)) {
                    dist[neighbourId]    = newDist
                    prev[neighbourId]    = currentId
                    prevEdge[neighbourId] = edge
                    pq.offer(newDist to neighbourId)
                }
            }
        }

        // No path found
        if (dist.getValue(destinationId) == Int.MAX_VALUE) return null

        // ── Reconstruct path ──────────────────────────────────────────────────
        val pathIds   = mutableListOf<String>()
        val pathEdges = mutableListOf<RouteEdge>()
        var cursor    = destinationId
        while (cursor != originId) {
            pathIds.add(0, cursor)
            val edge = prevEdge[cursor]
            if (edge != null) pathEdges.add(0, edge)
            cursor = prev[cursor] ?: break
        }
        pathIds.add(0, originId)

        val pathNodes = pathIds.mapNotNull { graph[it] }

        // ── Aggregates ────────────────────────────────────────────────────────
        val hopCount      = pathEdges.size
        val totalDuration = pathEdges.sumOf { it.durationMin }
        val totalFare     = FareCalculator.calculate(hopCount)
        val hasTransfer   = pathEdges.any { it.isTransfer }

        // Collect distinct line names in order of travel
        val lineSegments = buildLineSegments(pathNodes)

        return RouteResult(
            path          = pathNodes,
            edges         = pathEdges,
            totalFare     = totalFare,
            totalDuration = totalDuration,
            hasTransfer   = hasTransfer,
            lineSegments  = lineSegments
        )
    }

    // ── graph construction ────────────────────────────────────────────────────

    private fun buildGraph(json: String) {
        graph.clear()

        val array = JSONArray(json)

        // ── Pass 1: create all nodes ──────────────────────────────────────────
        // Bucket stations by line so we can connect them in sequence
        val byLine = mutableMapOf<StationLine, MutableList<StationNode>>()

        for (i in 0 until array.length()) {
            val obj  = array.getJSONObject(i)
            val code = obj.getString("code")
            val line = parseLine(obj.getString("line")) ?: continue

            val connectedArr = obj.getJSONArray("connected_lines")
            val connectedLines = (0 until connectedArr.length())
                .map { connectedArr.getString(it) }

            val node = StationNode(
                id             = code,
                nameEn         = obj.getString("name_en"),
                nameTh         = obj.getString("name_th"),
                line           = line,
                stationType    = parseType(obj.getString("station_type")),
                connectedLines = connectedLines,
                absX           = obj.getInt("x"),
                absY           = obj.getInt("y")
            )

            graph[code] = node
            byLine.getOrPut(line) { mutableListOf() }.add(node)
        }

        // ── Pass 2: edges between consecutive stations on the same line ────────
        for ((_, stations) in byLine) {
            // stations are already in JSON order (sequential along line)
            for (i in 0 until stations.size - 1) {
                val a = stations[i]
                val b = stations[i + 1]
                addUndirectedEdge(a, b, isTransfer = false)
            }
        }

        // ── Pass 3: transfer edges at BTS interchange stations ─────────────────
        // Siam (CEN) links Sukhumvit ↔ Silom — insert a zero-fare transfer edge
        // between the two StationNode objects that represent the same physical
        // station on different lines.
        // In stations.json Siam appears once on Sukhumvit ("CEN") and the Silom
        // line treats it as the same node, so we only need intra-line links.
        // For Gold Line, G1 (Krung Thon Buri / Gold) ↔ S7 (Krung Thon Buri / Silom)
        addTransferEdgeIfExists("CEN", "W1",  walkMin = 3)  // Siam: Sukhumvit↔Silom side
        addTransferEdgeIfExists("G1",  "S7",  walkMin = 2)  // Krung Thon Buri Gold↔Silom
        addTransferEdgeIfExists("S12", "S12", walkMin = 0)  // Bang Wa already single node
    }

    /**
     * Creates a pair of directed edges (a→b and b→a) with distance and
     * duration derived from pixel coordinates.
     */
    private fun addUndirectedEdge(a: StationNode, b: StationNode, isTransfer: Boolean) {
        val distKm   = pixelDistanceToKm(a.absX, a.absY, b.absX, b.absY)
        val durMin   = DurationCalculator.forEdge(distKm, isTransfer)

        val edgeAB = RouteEdge(
            fromId     = a.id,
            toId       = b.id,
            distanceKm = distKm,
            durationMin = durMin,
            fare       = 0,   // fare computed for whole trip by FareCalculator
            isTransfer = isTransfer
        )
        val edgeBA = edgeAB.copy(fromId = b.id, toId = a.id)

        a.connections[b.id] = edgeAB
        b.connections[a.id] = edgeBA
    }

    /**
     * Inserts a transfer edge between two different-line nodes that share the
     * same physical station (interchange).  [walkMin] is extra penalty added
     * on top of the DurationCalculator transfer penalty.
     */
    private fun addTransferEdgeIfExists(idA: String, idB: String, walkMin: Int) {
        if (idA == idB) return
        val a = graph[idA] ?: return
        val b = graph[idB] ?: return

        val distKm  = pixelDistanceToKm(a.absX, a.absY, b.absX, b.absY)
        val durMin  = DurationCalculator.forEdge(distKm, isTransfer = true) + walkMin

        val edgeAB  = RouteEdge(idA, idB, distKm, durMin, fare = 0, isTransfer = true)
        val edgeBA  = RouteEdge(idB, idA, distKm, durMin, fare = 0, isTransfer = true)

        a.connections[idB] = edgeAB
        b.connections[idA] = edgeBA
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Convert pixel distance (on 4961×4961 image) to approximate km.
     *
     * Calibration:
     *   The BTS Sukhumvit line is ~59 km end-to-end (N24→E23).
     *   Pixel distance N24→E23 ≈ 3 450 px (measured from JSON coords).
     *   → 1 px ≈ 0.0171 km
     */
    private const val PX_TO_KM = 0.0171f

    private fun pixelDistanceToKm(x1: Int, y1: Int, x2: Int, y2: Int): Float {
        val dx = (x2 - x1).toFloat()
        val dy = (y2 - y1).toFloat()
        return sqrt(dx * dx + dy * dy) * PX_TO_KM
    }

    private fun parseType(raw: String): StationNodeType = when (raw) {
        "terminal"    -> StationNodeType.TERMINAL
        "interchange" -> StationNodeType.INTERCHANGE
        else          -> StationNodeType.REGULAR
    }

    private fun parseLine(raw: String): StationLine? = when (raw) {
        "Sukhumvit"         -> StationLine.SUKHUMVIT
        "Silom"             -> StationLine.SILOM
        "Gold"              -> StationLine.GOLD
        "Airport Rail Link" -> StationLine.AIRPORT_RAIL_LINK
        "MRT Blue"          -> StationLine.MRT_BLUE
        "MRT Purple"        -> StationLine.MRT_PURPLE
        "MRT Yellow"        -> StationLine.MRT_YELLOW
        "MRT Pink"          -> StationLine.MRT_PINK
        else                -> null
    }

    /**
     * Collapse consecutive same-line stations into a single segment label.
     * E.g. [Sukhumvit, Sukhumvit, Silom] → ["Sukhumvit Line", "Silom Line"]
     */
    private fun buildLineSegments(nodes: List<StationNode>): List<String> {
        if (nodes.isEmpty()) return emptyList()
        val segments = mutableListOf(nodes.first().line.displayName)
        for (i in 1 until nodes.size) {
            val lineName = nodes[i].line.displayName
            if (lineName != segments.last()) segments.add(lineName)
        }
        return segments
    }
}
