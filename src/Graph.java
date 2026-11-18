/*
 * Graph.java
 * CpE 211 Data Structures and Algorithms — Lab 2: Network Route Finder
 * Phase: BUILD (post-AUDIT corrections)
 *
 * Adjacency-list graph supporting directed and undirected unweighted edges.
 * Vertex labels are Strings. Uses insertion-order-preserving maps/lists so
 * that traversals follow neighbor insertion order.
 *
 * Undirected edges are tracked with a collision-safe EdgeKey pair (not a
 * delimiter-joined String), so vertex labels containing hyphens or other
 * punctuation cannot cause false key collisions.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class Graph {

    // Adjacency list: vertex -> neighbors in insertion order
    private final Map<String, List<String>> adj;

    // Metadata: canonical EdgeKey of each undirected edge.
    // Used to distinguish an undirected parent edge from a directed edge
    // during cycle detection and to detect conflicting edge directions.
    private final Set<EdgeKey> undirectedEdges;

    // Colors for cycle detection (DFS three-color marking)
    private static final String WHITE = "WHITE";
    private static final String GRAY = "GRAY";
    private static final String BLACK = "BLACK";

    public Graph() {
        this.adj = new LinkedHashMap<>();
        this.undirectedEdges = new HashSet<>();
    }

    /* ------------------------------------------------------------------ */
    /*  Vertex management                                                 */
    /* ------------------------------------------------------------------ */

    /**
     * Adds a vertex with no edges. Idempotent: adding an existing vertex
     * is a no-op. Rejects null with IllegalArgumentException.
     */
    public void addVertex(String v) {
        if (v == null) {
            throw new IllegalArgumentException("Vertex cannot be null");
        }
        if (!adj.containsKey(v)) {
            adj.put(v, new ArrayList<>());
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Edge management                                                   */
    /* ------------------------------------------------------------------ */

    /**
     * Adds an edge from u to v.
     *   - Validates null endpoints first.
     *   - Checks for conflicting edge directions BEFORE adding missing
     *     vertices or modifying graph state. A rejected operation leaves
     *     the entire graph unchanged.
     *   - Automatically adds missing endpoints for valid edges.
     *   - Duplicate edges are idempotent (no-op if already present with
     *     the same directionality).
     *
     * @param u           source vertex
     * @param v           destination vertex
     * @param bidirectional if true, the edge is undirected (u--v); if false, directed (u->v)
     */
    public void addEdge(String u, String v, boolean bidirectional) {
        if (u == null || v == null) {
            throw new IllegalArgumentException("Edge endpoints cannot be null");
        }

        EdgeKey key = makeEdgeKey(u, v);

        // --- Conflict detection BEFORE any mutation -------------------
        boolean uExists = adj.containsKey(u);
        boolean vExists = adj.containsKey(v);

        if (uExists && vExists) {
            boolean edgeExists = adj.get(u).contains(v) || adj.get(v).contains(u);

            if (bidirectional) {
                if (undirectedEdges.contains(key)) {
                    // Already undirected — idempotent, no change
                    return;
                }
                if (edgeExists) {
                    // Edge exists as directed — conflict before mutation
                    throw new IllegalArgumentException(
                        "Conflicting edge direction: " + u + "-" + v +
                        " already exists as a directed edge");
                }
            } else {
                if (undirectedEdges.contains(key)) {
                    // Edge is undirected — conflict before mutation
                    throw new IllegalArgumentException(
                        "Conflicting edge direction: " + u + "-" + v +
                        " already exists as an undirected edge");
                }
                // Directed→directed (same or opposite direction) is not a conflict;
                // opposite-direction directed edges are allowed as separate edges.
            }
        }

        // --- No conflict — safe to mutate -----------------------------
        addVertex(u);
        addVertex(v);

        if (bidirectional) {
            // Add as undirected: v in adj[u], and u in adj[v] (unless self-loop)
            adj.get(u).add(v);
            if (!u.equals(v)) {
                adj.get(v).add(u);
            }
            undirectedEdges.add(key);
        } else {
            // Add as directed: v in adj[u] only (unless already present)
            if (!adj.get(u).contains(v)) {
                adj.get(u).add(v);
            }
            // Self-loop directed: already added once above; do not add twice
        }
    }

    /**
     * Creates a canonical, order-independent EdgeKey for an undirected edge.
     * Endpoints are sorted lexicographically so that makeEdgeKey("A","B")
     * and makeEdgeKey("B","A") produce equal keys.
     * Using a pair object (not a delimiter-joined String) prevents key
     * collisions when vertex labels contain hyphens or other punctuation.
     */
    private EdgeKey makeEdgeKey(String a, String b) {
        return new EdgeKey(a, b);
    }

    /**
     * Returns true if the edge between u and v is recorded as undirected.
     */
    private boolean isUndirected(String u, String v) {
        return undirectedEdges.contains(makeEdgeKey(u, v));
    }

    /* ------------------------------------------------------------------ */
    /*  EdgeKey — collision-safe undirected edge identifier               */
    /* ------------------------------------------------------------------ */

    /**
     * Immutable pair representing an undirected edge. Order-independent:
     * EdgeKey("A","B") equals EdgeKey("B","A"). Safe for use as a HashSet
     * key even when vertex labels contain hyphens or other punctuation.
     */
    private static final class EdgeKey {
        private final String u;
        private final String v;

        EdgeKey(String a, String b) {
            if (a.compareTo(b) <= 0) {
                this.u = a;
                this.v = b;
            } else {
                this.u = b;
                this.v = a;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            EdgeKey edgeKey = (EdgeKey) o;
            return u.equals(edgeKey.u) && v.equals(edgeKey.v);
        }

        @Override
        public int hashCode() {
            return Objects.hash(u, v);
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Breadth-First Search                                              */
    /* ------------------------------------------------------------------ */

    /**
     * Returns the inclusive shortest-by-edge-count path from start to target
     * using BFS with a FIFO queue and parent tracking.
     *
     * Returns an empty list if:
     *   - start or target is null
     *   - start or target is not in the graph
     *   - target is unreachable from start
     *
     * Returns [start] when start.equals(target).
     *
     * This traversal does not modify permanent graph state.
     */
    public List<String> breadthFirstSearch(String start, String target) {
        if (start == null || target == null) {
            return new ArrayList<>();
        }
        if (!adj.containsKey(start) || !adj.containsKey(target)) {
            return new ArrayList<>();
        }
        if (start.equals(target)) {
            return Arrays.asList(start);
        }

        Set<String> visited = new HashSet<>();
        Map<String, String> parent = new LinkedHashMap<>();
        Queue<String> queue = new LinkedList<>();

        visited.add(start);
        queue.offer(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(target)) {
                return reconstructPath(parent, start, target);
            }

            for (String neighbor : adj.get(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        // Target unreachable
        return new ArrayList<>();
    }

    /**
     * Reconstructs the inclusive path from start to target by walking
     * backward through the parent map, then reversing.
     */
    private List<String> reconstructPath(Map<String, String> parent, String start, String target) {
        List<String> path = new ArrayList<>();
        String current = target;
        while (current != null) {
            path.add(current);
            if (current.equals(start)) {
                break;
            }
            current = parent.get(current);
        }

        List<String> reversed = new ArrayList<>();
        for (int i = path.size() - 1; i >= 0; i--) {
            reversed.add(path.get(i));
        }
        return reversed;
    }

    /* ------------------------------------------------------------------ */
    /*  Depth-First Search                                                */
    /* ------------------------------------------------------------------ */

    /**
     * Returns the first path from start to target according to neighbor
     * insertion order, using recursive DFS. The path is inclusive.
     *
     * Returns an empty list if:
     *   - start or target is null
     *   - start or target is not in the graph
     *   - target is unreachable from start
     *
     * Returns [start] when start.equals(target).
     *
     * This traversal does not modify permanent graph state.
     */
    public List<String> depthFirstSearch(String start, String target) {
        if (start == null || target == null) {
            return new ArrayList<>();
        }
        if (!adj.containsKey(start) || !adj.containsKey(target)) {
            return new ArrayList<>();
        }
        if (start.equals(target)) {
            return Arrays.asList(start);
        }

        Set<String> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        List<String> result = dfsVisit(start, target, visited, path);
        return result != null ? result : new ArrayList<>();
    }

    /**
     * Recursive DFS helper. Explores neighbors in insertion order and
     * returns the first path found to target. Uses backtracking (removes
     * the current vertex from the path when backtracking).
     */
    private List<String> dfsVisit(String current, String target,
                                  Set<String> visited, List<String> path) {
        visited.add(current);
        path.add(current);

        if (current.equals(target)) {
            return new ArrayList<>(path); // return a copy of the found path
        }

        for (String neighbor : adj.get(current)) {
            if (!visited.contains(neighbor)) {
                List<String> found = dfsVisit(neighbor, target, visited, path);
                if (found != null) {
                    return found;
                }
            }
        }

        // Backtrack: remove current from path before returning to caller
        path.remove(path.size() - 1);
        return null;
    }

    /* ------------------------------------------------------------------ */
    /*  Cycle Detection                                                   */
    /* ------------------------------------------------------------------ */

    /**
     * Returns true if the graph contains a cycle, considering both directed
     * and undirected edges. Inspects every disconnected component.
     *
     * Undirected cycles: detected via DFS with parent tracking — a visited
     * neighbor that is not the DFS parent indicates a cycle.
     *
     * Directed cycles: detected via three-color marking (WHITE/GRAY/BLACK)
     * — encountering a GRAY neighbor indicates a back edge (cycle).
     *
     * Self-loops are treated as cycles.
     */
    public boolean hasCycle() {
        Map<String, String> color = new LinkedHashMap<>();
        for (String v : adj.keySet()) {
            color.put(v, WHITE);
        }

        for (String v : adj.keySet()) {
            if (color.get(v).equals(WHITE)) {
                if (dfsCycleVisit(v, null, color)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Recursive cycle-detection DFS.
     *
     * For undirected edges: skips the DFS parent edge; a GRAY neighbor
     * that is not the parent indicates a cycle.
     *
     * For directed edges: a GRAY neighbor indicates a back edge (cycle).
     */
    private boolean dfsCycleVisit(String v, String parent, Map<String, String> color) {
        color.put(v, GRAY);

        for (String w : adj.get(v)) {
            boolean undirected = isUndirected(v, w);

            if (undirected) {
                // Undirected edge: skip the edge we came from (parent)
                if (w.equals(parent)) {
                    continue;
                }
                if (color.get(w).equals(GRAY)) {
                    return true; // back edge in undirected graph
                }
                if (color.get(w).equals(WHITE)) {
                    if (dfsCycleVisit(w, v, color)) {
                        return true;
                    }
                }
            } else {
                // Directed edge: GRAY neighbor = back edge = cycle
                if (color.get(w).equals(GRAY)) {
                    return true;
                }
                if (color.get(w).equals(WHITE)) {
                    if (dfsCycleVisit(w, v, color)) {
                        return true;
                    }
                }
            }
        }

        color.put(v, BLACK);
        return false;
    }
}
