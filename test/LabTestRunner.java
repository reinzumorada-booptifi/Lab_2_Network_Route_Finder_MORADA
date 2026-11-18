/*
 * LabTestRunner.java
 * CpE 211 Data Structures and Algorithms — Lab 2: Network Route Finder
 * Phase: TEST
 *
 * Dependency-free test runner (no JUnit). Default package.
 * All tests are student-authored and are expected to FAIL initially because
 * src/Graph.java has not yet been implemented.
 *
 * Assumed Graph API (to be implemented in BUILD phase):
 *   Graph()
 *   void addVertex(String v)
 *   void addEdge(String u, String v, boolean bidirectional)
 *   List<String> breadthFirstSearch(String start, String target)
 *   List<String> depthFirstSearch(String start, String target)
 *   boolean hasCycle()
 *
 * Required graph (all bidirectional): A-B, A-C, B-D, C-D, C-E, D-F, E-F
 */

import java.util.List;
import java.util.Arrays;

public class LabTestRunner {

    /* ------------------------------------------------------------------ */
    /*  Test counters                                                     */
    /* ------------------------------------------------------------------ */
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    /* ------------------------------------------------------------------ */
    /*  Assertion helper                                                  */
    /* ------------------------------------------------------------------ */
    private static void check(boolean condition, String testName, String detail) {
        if (condition) {
            testsPassed++;
            System.out.println("  PASS: " + testName + (detail.isEmpty() ? "" : " — " + detail));
        } else {
            testsFailed++;
            System.out.println("  FAIL: " + testName + (detail.isEmpty() ? "" : " — " + detail));
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Required graph builder                                            */
    /*  A-B, A-C, B-D, C-D, C-E, D-F, E-F  (all bidirectional)           */
    /* ------------------------------------------------------------------ */
    private static Graph buildRequiredGraph() {
        Graph g = new Graph();
        g.addEdge("A", "B", true);
        g.addEdge("A", "C", true);
        g.addEdge("B", "D", true);
        g.addEdge("C", "D", true);
        g.addEdge("C", "E", true);
        g.addEdge("D", "F", true);
        g.addEdge("E", "F", true);
        return g;
    }

    /* ================================================================== */
    /*  TEST 1: Empty graph                                               */
    /* ================================================================== */
    private static void testEmptyGraph() {
        System.out.println("\n[TEST] Empty graph");
        Graph g = new Graph();
        check(g.breadthFirstSearch("A", "B").isEmpty(), "Empty graph BFS absent start", "");
        check(g.breadthFirstSearch("A", "A").isEmpty(), "Empty graph BFS start==target absent", "");
        check(g.depthFirstSearch("A", "B").isEmpty(), "Empty graph DFS absent start", "");
        check(!g.hasCycle(), "Empty graph hasCycle returns false", "");
    }

    /* ================================================================== */
    /*  TEST 2: Single vertex                                             */
    /* ================================================================== */
    private static void testSingleVertex() {
        System.out.println("\n[TEST] Single vertex");
        Graph g = new Graph();
        g.addVertex("A");
        check(g.breadthFirstSearch("A", "A").equals(Arrays.asList("A")),
              "Single vertex BFS start==target", "[A]");
        check(g.depthFirstSearch("A", "A").equals(Arrays.asList("A")),
              "Single vertex DFS start==target", "[A]");
        check(g.breadthFirstSearch("A", "Z").isEmpty(), "Single vertex BFS unreachable", "");
        check(!g.hasCycle(), "Single vertex hasCycle returns false", "");
    }

    /* ================================================================== */
    /*  TEST 3: Start equals target                                       */
    /* ================================================================== */
    private static void testStartEqualsTarget() {
        System.out.println("\n[TEST] Start equals target");
        Graph g = buildRequiredGraph();
        check(g.breadthFirstSearch("A", "A").equals(Arrays.asList("A")),
              "BFS start==target returns [A]", "[A]");
        check(g.depthFirstSearch("A", "A").equals(Arrays.asList("A")),
              "DFS start==target returns [A]", "[A]");
    }

    /* ================================================================== */
    /*  TEST 4: BFS shortest path                                         */
    /* ================================================================== */
    private static void testBfsShortestPath() {
        System.out.println("\n[TEST] BFS shortest path");
        Graph g = buildRequiredGraph();
        List<String> path = g.breadthFirstSearch("A", "F");
        check(path.equals(Arrays.asList("A", "B", "D", "F")),
              "BFS A→F exact shortest path", path.toString());
    }

    /* ================================================================== */
    /*  TEST 5: BFS unreachable target                                    */
    /* ================================================================== */
    private static void testBfsUnreachable() {
        System.out.println("\n[TEST] BFS unreachable target");
        Graph g = buildRequiredGraph();
        check(g.breadthFirstSearch("A", "Z").isEmpty(), "BFS A→Z (absent target) returns empty", "");
        check(g.breadthFirstSearch("Z", "F").isEmpty(), "BFS Z→F (absent start) returns empty", "");
        // Disconnected: add isolated vertex
        Graph g2 = new Graph();
        g2.addVertex("X");
        g2.addVertex("Y");
        check(g2.breadthFirstSearch("X", "Y").isEmpty(), "BFS between disconnected vertices returns empty", "");
    }

    /* ================================================================== */
    /*  TEST 6: DFS first path by insertion order                         */
    /* ================================================================== */
    private static void testDfsFirstPathByInsertionOrder() {
        System.out.println("\n[TEST] DFS first path by insertion order");
        Graph g = buildRequiredGraph();
        // Insertion order: A-B, A-C, B-D, C-D, C-E, D-F, E-F
        // Adjacency: A:[B,C] B:[A,D] C:[A,D,E] D:[B,C,F] E:[C,F] F:[D,E]
        // DFS from A: A→B→D→C→E→F (first path by neighbor insertion order)
        List<String> path = g.depthFirstSearch("A", "F");
        check(path.equals(Arrays.asList("A", "B", "D", "C", "E", "F")),
              "DFS A→F exact insertion-order path", path.toString());
    }

    /* ================================================================== */
    /*  TEST 7: Directed edge direction                                   */
    /* ================================================================== */
    private static void testDirectedEdgeDirection() {
        System.out.println("\n[TEST] Directed edge direction");
        Graph g = new Graph();
        g.addEdge("A", "B", false); // directed A→B
        // BFS from A to B should succeed
        check(!g.breadthFirstSearch("A", "B").isEmpty(), "Directed A→B: BFS A→B reachable", "");
        // BFS from B to A should fail (no reverse edge)
        check(g.breadthFirstSearch("B", "A").isEmpty(), "Directed A→B: BFS B→A not reachable", "");
    }

    /* ================================================================== */
    /*  TEST 8: Undirected edge direction                                 */
    /* ================================================================== */
    private static void testUndirectedEdgeDirection() {
        System.out.println("\n[TEST] Undirected edge direction");
        Graph g = new Graph();
        g.addEdge("A", "B", true); // bidirectional A–B
        check(!g.breadthFirstSearch("A", "B").isEmpty(), "Undirected A–B: BFS A→B reachable", "");
        check(!g.breadthFirstSearch("B", "A").isEmpty(), "Undirected A–B: BFS B→A reachable", "");
    }

    /* ================================================================== */
    /*  TEST 9: Duplicate vertex                                          */
    /* ================================================================== */
    private static void testDuplicateVertex() {
        System.out.println("\n[TEST] Duplicate vertex");
        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("A"); // should be idempotent, no throw
        check(g.breadthFirstSearch("A", "A").equals(Arrays.asList("A")),
              "Duplicate addVertex does not break graph", "[A]");
    }

    /* ================================================================== */
    /*  TEST 10: Duplicate edge                                           */
    /* ================================================================== */
    private static void testDuplicateEdge() {
        System.out.println("\n[TEST] Duplicate edge");
        Graph g = new Graph();
        g.addEdge("A", "B", true);
        g.addEdge("A", "B", true); // idempotent
        check(!g.breadthFirstSearch("A", "B").isEmpty(), "Duplicate addEdge still reachable", "");
        check(!g.breadthFirstSearch("B", "A").isEmpty(), "Duplicate addEdge reverse still reachable", "");
    }

    /* ================================================================== */
    /*  TEST 11: Conflicting edge direction                               */
    /* ================================================================== */
    private static void testConflictingEdgeDirection() {
        System.out.println("\n[TEST] Conflicting edge direction");
        Graph g = new Graph();
        g.addEdge("A", "B", false); // directed A→B
        boolean threw = false;
        try {
            g.addEdge("A", "B", true); // conflict: make it bidirectional
        } catch (Exception e) {
            threw = true;
        }
        check(threw, "Conflicting direction (directed→undirected) throws exception", "");

        // Also test undirected→directed conflict
        Graph g2 = new Graph();
        g2.addEdge("C", "D", true); // undirected C–D
        boolean threw2 = false;
        try {
            g2.addEdge("C", "D", false); // conflict: make it directed
        } catch (Exception e) {
            threw2 = true;
        }
        check(threw2, "Conflicting direction (undirected→directed) throws exception", "");
    }

    /* ================================================================== */
    /*  TEST 12: Self-loop                                                */
    /* ================================================================== */
    private static void testSelfLoop() {
        System.out.println("\n[TEST] Self-loop");
        Graph g = new Graph();
        g.addEdge("A", "A", true); // self-loop
        // BFS from A to A should return [A] (start==target takes precedence)
        check(g.breadthFirstSearch("A", "A").equals(Arrays.asList("A")),
              "Self-loop BFS A→A returns [A]", "");
        // hasCycle should detect self-loop as a cycle
        check(g.hasCycle(), "Self-loop hasCycle returns true", "");
    }

    /* ================================================================== */
    /*  TEST 13: Undirected cycle                                         */
    /* ================================================================== */
    private static void testUndirectedCycle() {
        System.out.println("\n[TEST] Undirected cycle");
        Graph g = buildRequiredGraph(); // contains cycles e.g. A-B-D-C-A
        check(g.hasCycle(), "Required graph (undirected) hasCycle returns true", "");
    }

    /* ================================================================== */
    /*  TEST 14: Directed cycle                                           */
    /* ================================================================== */
    private static void testDirectedCycle() {
        System.out.println("\n[TEST] Directed cycle");
        Graph g = new Graph();
        g.addEdge("A", "B", false);
        g.addEdge("B", "C", false);
        g.addEdge("C", "A", false); // A→B→C→A forms a directed cycle
        check(g.hasCycle(), "Directed graph A→B→C→A hasCycle returns true", "");
    }

    /* ================================================================== */
    /*  TEST 15: Directed acyclic graph                                   */
    /* ================================================================== */
    private static void testDirectedAcyclicGraph() {
        System.out.println("\n[TEST] Directed acyclic graph");
        Graph g = new Graph();
        g.addEdge("A", "B", false);
        g.addEdge("B", "C", false);
        g.addEdge("A", "C", false); // A→B→C, A→C — no cycle
        check(!g.hasCycle(), "DAG hasCycle returns false", "");
    }

    /* ================================================================== */
    /*  TEST 16: Disconnected components                                  */
    /* ================================================================== */
    private static void testDisconnectedComponents() {
        System.out.println("\n[TEST] Disconnected components");
        Graph g = new Graph();
        // Component 1: A–B–C (undirected, has cycle if we add C–A)
        g.addEdge("A", "B", true);
        g.addEdge("B", "C", true);
        // Component 2: D→E→F→D (directed cycle)
        g.addEdge("D", "E", false);
        g.addEdge("E", "F", false);
        g.addEdge("F", "D", false);
        check(g.hasCycle(), "Disconnected graph with cycle in one component returns true", "");
        // BFS cannot reach across components
        check(g.breadthFirstSearch("A", "D").isEmpty(), "BFS across disconnected components returns empty", "");
    }

    /* ================================================================== */
    /*  TEST 17: Undirected tree parent-edge handling                     */
    /* ================================================================== */
    private static void testUndirectedTreeParentEdge() {
        System.out.println("\n[TEST] Undirected tree parent-edge handling");
        Graph g = new Graph();
        g.addEdge("A", "B", true);
        g.addEdge("B", "C", true);
        g.addEdge("C", "D", true); // A–B–C–D, a tree (no cycles)
        check(!g.hasCycle(), "Undirected tree hasCycle returns false", "");
        // BFS should find path A→D
        List<String> path = g.breadthFirstSearch("A", "D");
        check(!path.isEmpty() && path.get(0).equals("A") && path.get(path.size() - 1).equals("D"),
              "Undirected tree BFS A→D returns path", path.toString());
    }

    /* ================================================================== */
    /*  TEST 18: Traversal graph immutability                             */
    /* ================================================================== */
    private static void testTraversalGraphImmutability() {
        System.out.println("\n[TEST] Traversal graph immutability");
        Graph g = buildRequiredGraph();
        boolean cycleBefore = g.hasCycle();
        check(cycleBefore == g.hasCycle(), "hasCycle unchanged after traversals", "");
        check(!g.breadthFirstSearch("A", "F").isEmpty(), "BFS still works after previous traversals", "");
        check(!g.depthFirstSearch("A", "F").isEmpty(), "DFS still works after previous traversals", "");
    }

    /* ================================================================== */
    /*  TEST 19: Null-input validation                                    */
    /* ================================================================== */
    private static void testNullInputValidation() {
        System.out.println("\n[TEST] Null-input validation");
        Graph g = new Graph();

        boolean threw1 = false;
        try {
            g.addVertex(null);
        } catch (IllegalArgumentException e) {
            threw1 = true;
        }
        check(threw1, "addVertex(null) throws IllegalArgumentException", "");

        boolean threw2 = false;
        try {
            g.addEdge(null, "B", false);
        } catch (IllegalArgumentException e) {
            threw2 = true;
        }
        check(threw2, "addEdge(null, \"B\", false) throws IllegalArgumentException", "");

        boolean threw3 = false;
        try {
            g.addEdge("A", null, false);
        } catch (IllegalArgumentException e) {
            threw3 = true;
        }
        check(threw3, "addEdge(\"A\", null, false) throws IllegalArgumentException", "");
    }

    /* ================================================================== */
    /*  TEST 20: Hyphenated vertex labels remain distinct                 */
    /* ================================================================== */
    private static void testHyphenatedLabelsDistinct() {
        System.out.println("\n[TEST] Hyphenated vertex labels remain distinct");
        Graph g = new Graph();
        // Two different undirected edges that would collide with a
        // delimiter-based string key: "A-B"+"C" vs "A"+"B-C" both yield "A-B-C"
        g.addEdge("A-B", "C", true);
        g.addEdge("A", "B-C", true);
        // Both edges must exist independently
        check(!g.breadthFirstSearch("A-B", "C").isEmpty(),
              "Hyphenated edge A-B → C reachable", "");
        check(!g.breadthFirstSearch("A", "B-C").isEmpty(),
              "Hyphenated edge A → B-C reachable", "");
        // They must NOT be treated as the same edge:
        // BFS from "A-B" should NOT reach "B-C" (no path between them)
        check(g.breadthFirstSearch("A-B", "B-C").isEmpty(),
              "Hyphenated edges A-B→C and A→B-C are distinct", "");
    }

    /* ================================================================== */
    /*  TEST 21: Reversed undirected edge is idempotent                   */
    /* ================================================================== */
    private static void testReversedUndirectedIdempotent() {
        System.out.println("\n[TEST] Reversed undirected edge is idempotent");
        Graph g = new Graph();
        g.addEdge("A", "B", true);
        g.addEdge("B", "A", true); // same undirected edge, reversed endpoints
        check(!g.breadthFirstSearch("A", "B").isEmpty(),
              "Reversed undirected edge still reachable", "");
        check(!g.breadthFirstSearch("B", "A").isEmpty(),
              "Reversed undirected edge reverse still reachable", "");
    }

    /* ================================================================== */
    /*  TEST 22: Rejected conflicting edge leaves graph unchanged         */
    /* ================================================================== */
    private static void testRejectedConflictLeavesGraphUnchanged() {
        System.out.println("\n[TEST] Rejected conflicting edge leaves graph unchanged");
        Graph g = new Graph();
        g.addEdge("A", "B", false); // directed A→B

        // Record state before the rejected operation
        boolean abReachableBefore = !g.breadthFirstSearch("A", "B").isEmpty();
        boolean baReachableBefore = !g.breadthFirstSearch("B", "A").isEmpty();
        boolean cycleBefore = g.hasCycle();

        boolean threw = false;
        try {
            g.addEdge("A", "B", true); // conflict: directed→undirected
        } catch (IllegalArgumentException e) {
            threw = true;
        }

        // State must be unchanged after the rejected operation
        check(threw, "Conflicting addEdge throws IllegalArgumentException", "");
        check(abReachableBefore == !g.breadthFirstSearch("A", "B").isEmpty(),
              "A→B reachability unchanged after rejected conflict", "");
        check(baReachableBefore == !g.breadthFirstSearch("B", "A").isEmpty(),
              "B→A reachability unchanged after rejected conflict", "");
        check(cycleBefore == g.hasCycle(), "hasCycle unchanged after rejected conflict", "");
    }

    /* ================================================================== */
    /*  TEST 23: BFS with null start and null target                      */
    /* ================================================================== */
    private static void testBfsNullInputs() {
        System.out.println("\n[TEST] BFS with null start and null target");
        Graph g = buildRequiredGraph();
        check(g.breadthFirstSearch(null, "F").isEmpty(), "BFS null start returns empty", "");
        check(g.breadthFirstSearch("A", null).isEmpty(), "BFS null target returns empty", "");
        check(g.breadthFirstSearch(null, null).isEmpty(), "BFS both null returns empty", "");
    }

    /* ================================================================== */
    /*  TEST 24: DFS with null start and null target                      */
    /* ================================================================== */
    private static void testDfsNullInputs() {
        System.out.println("\n[TEST] DFS with null start and null target");
        Graph g = buildRequiredGraph();
        check(g.depthFirstSearch(null, "F").isEmpty(), "DFS null start returns empty", "");
        check(g.depthFirstSearch("A", null).isEmpty(), "DFS null target returns empty", "");
        check(g.depthFirstSearch(null, null).isEmpty(), "DFS both null returns empty", "");
    }

    /* ================================================================== */
    /*  TEST 25: DFS with absent start vertex                             */
    /* ================================================================== */
    private static void testDfsAbsentStart() {
        System.out.println("\n[TEST] DFS with absent start vertex");
        Graph g = buildRequiredGraph();
        check(g.depthFirstSearch("Z", "F").isEmpty(), "DFS absent start returns empty", "");
        check(g.depthFirstSearch("A", "Z").isEmpty(), "DFS absent target returns empty", "");
    }

    /* ================================================================== */
    /*  TEST 26: Mixed directed and undirected edges with cycle           */
    /* ================================================================== */
    private static void testMixedGraphWithCycle() {
        System.out.println("\n[TEST] Mixed directed and undirected edges with cycle");
        Graph g = new Graph();
        g.addEdge("A", "B", false); // directed A→B
        g.addEdge("B", "C", true);  // undirected B–C
        g.addEdge("C", "A", false); // directed C→A
        // Cycle: A→B→C→A (mixed directed/undirected)
        check(g.hasCycle(), "Mixed graph A→B, B–C, C→A hasCycle returns true", "");
    }

    /* ================================================================== */
    /*  TEST 27: Mixed directed and undirected edges without cycle        */
    /* ================================================================== */
    private static void testMixedGraphAcyclic() {
        System.out.println("\n[TEST] Mixed directed and undirected edges without cycle");
        Graph g = new Graph();
        g.addEdge("A", "B", false); // directed A→B
        g.addEdge("B", "C", true);  // undirected B–C
        g.addEdge("D", "E", true);  // undirected D–E (separate component)
        // No cycle in either component
        check(!g.hasCycle(), "Mixed acyclic graph hasCycle returns false", "");
    }

    /* ================================================================== */
    /*  Main                                                              */
    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("LabTestRunner — CpE 211 Lab 2 Network Route Finder (TEST)");
        System.out.println("Dependency-free runner, default package");
        System.out.println("All tests are student-authored and expected to FAIL initially");
        System.out.println("Reason: src/Graph.java has not been implemented yet.");
        System.out.println("============================================================");

        testEmptyGraph();
        testSingleVertex();
        testStartEqualsTarget();
        testBfsShortestPath();
        testBfsUnreachable();
        testDfsFirstPathByInsertionOrder();
        testDirectedEdgeDirection();
        testUndirectedEdgeDirection();
        testDuplicateVertex();
        testDuplicateEdge();
        testConflictingEdgeDirection();
        testSelfLoop();
        testUndirectedCycle();
        testDirectedCycle();
        testDirectedAcyclicGraph();
        testDisconnectedComponents();
        testUndirectedTreeParentEdge();
        testTraversalGraphImmutability();
        testNullInputValidation();
        testHyphenatedLabelsDistinct();
        testReversedUndirectedIdempotent();
        testRejectedConflictLeavesGraphUnchanged();
        testBfsNullInputs();
        testDfsNullInputs();
        testDfsAbsentStart();
        testMixedGraphWithCycle();
        testMixedGraphAcyclic();

        System.out.println("\n============================================================");
        System.out.println("RESULTS: " + testsPassed + " passed, " + testsFailed + " failed out of "
                + (testsPassed + testsFailed) + " tests");
        System.out.println("============================================================");
        if (testsFailed > 0) {
            System.out.println("NOTE: Tests are expected to fail because Graph.java is not yet implemented.");
            System.exit(1);
        } else {
            System.out.println("All tests passed.");
            System.exit(0);
        }
    }
}
