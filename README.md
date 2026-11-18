# Lab 2: Network Route Finder

CpE 211 Data Structures and Algorithms

## Description

A graph-based network route finder supporting adjacency-list representation,
vertex/edge management, breadth-first search (BFS), depth-first search (DFS),
and cycle detection for directed and undirected graphs.

## Project Status

**AUDIT CORRECTIONS complete.** `src/Graph.java` has been implemented and all
58 acceptance tests pass (27 test methods). Post-AUDIT corrections applied:
collision-safe EdgeKey for undirected edge tracking, conflict-before-mutation
in `addEdge()`, and 8 new acceptance tests.

## Folder Structure

```
Lab_2_Network_Route_Finder/
├── src/
│   └── Graph.java              (implemented — adjacency-list graph)
├── test/
│   └── LabTestRunner.java      (dependency-free test runner, 27 tests / 58 assertions)
├── AGENT_LOG.md
├── README.md
└── REFLECTION.md
```

## Required Graph

All seven edges are bidirectional:

```
A—B, A—C, B—D, C—D, C—E, D—F, E—F
```

## API

- `Graph()` — creates an empty graph
- `void addVertex(String v)` — adds a vertex (idempotent, rejects null)
- `void addEdge(String u, String v, boolean bidirectional)` — adds an edge
  (validates conflicts before mutation, auto-adds endpoints, idempotent duplicates,
  throws on conflicting direction)
- `List<String> breadthFirstSearch(String start, String target)` — returns
  inclusive shortest-by-edge-count path; empty list if unavailable
- `List<String> depthFirstSearch(String start, String target)` — returns
  first path by neighbor insertion order; empty list if unavailable
- `boolean hasCycle()` — detects directed and undirected cycles

## Expected Results (Required Graph)

- BFS A → F: `[A, B, D, F]`
- DFS A → F: `[A, B, D, C, E, F]`
- `hasCycle()` on required graph: `true`

## How to Build and Run

From the project root directory:

```bash
# Compile
javac src/Graph.java test/LabTestRunner.java

# Run tests (Windows)
java -cp "src;test" LabTestRunner

# Run tests (Linux/Mac)
java -cp "src:test" LabTestRunner
```

Expected output: `58 passed, 0 failed out of 58 tests` and exit code 0.

## License

Academic use — CpE 211 Lab 2.
