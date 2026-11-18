# Reflection — Lab 2 Network Route Finder

**Status:** AUDIT CORRECTIONS complete — implementation finished, all 58 tests passing

## What I Learned

- **Adjacency lists with insertion order:** Using `LinkedHashMap<String, List<String>>` preserves both vertex insertion order and neighbor insertion order. This is critical for deterministic DFS traversal, where the "first path by insertion order" depends on the order neighbors were added.

- **BFS vs. DFS trade-offs:** BFS with a FIFO queue and parent tracking guarantees the shortest path by edge count in an unweighted graph. DFS with recursion explores depth-first and returns the first path found according to neighbor insertion order, which is not necessarily the shortest. For the required graph, BFS A→F yields `[A, B, D, F]` (3 edges), while DFS A→F yields `[A, B, D, C, E, F]` (5 edges).

- **Cycle detection in mixed graphs:** The graph can contain both directed and undirected edges simultaneously. A unified DFS using three-color marking (WHITE/GRAY/BLACK) handles directed cycles (GRAY neighbor = back edge), while parent tracking handles undirected cycles (visited non-parent neighbor = back edge). Self-loops are naturally detected by both approaches.

- **Undirected edge metadata and key safety:** The `undirectedEdges` set tracks which edges are undirected. Initially I used delimiter-joined Strings (`"A-B"`) as keys, but the AUDIT revealed this causes collisions when vertex labels contain hyphens (e.g., `"A-B"`+`"C"` and `"A"`+`"B-C"` both produce `"A-B-C"`). I replaced this with a collision-safe `EdgeKey` pair class that sorts endpoints and uses proper `equals()`/`hashCode()`, so hyphenated labels remain valid.

- **Validation before mutation:** The AUDIT found that `addEdge()` called `addVertex()` before checking for conflicting edge directions, violating the "reject before mutation" principle. I reordered `addEdge()` to check `adj.containsKey(u)` and `adj.containsKey(v)` first, detect conflicts, and throw before any graph state is modified. Valid edges still auto-add missing endpoints.

- **Traversal immutability:** BFS and DFS use only local `visited` sets, queues/stacks, and parent maps. Nothing is written to `adj` or `undirectedEdges`, so traversals never modify permanent graph state. This was verified by the traversal immutability test and the new rejected-conflict-immutability test.

- **Edge case handling:** Null inputs, duplicate vertices/edges, self-loops, disconnected components, absent/unreachable targets, and hyphenated vertex labels all require careful handling. The conflict-before-mutation rule in `addEdge` ensures graph state remains consistent even when an exception is thrown.

## Challenges Anticipated vs. Actual

- **Challenge:** Handling both directed and undirected edges in one `hasCycle()` method.
  **Resolution:** A unified DFS with three-color marking and parent tracking works for both. Directed edges use GRAY-neighbor detection; undirected edges skip the parent and use GRAY-neighbor detection for non-parent neighbors.

- **Challenge:** Ensuring DFS returns the *first* path by insertion order, not just any path.
  **Resolution:** Recursive DFS that iterates neighbors in `List` insertion order and returns immediately upon finding the target, with proper backtracking via `path.remove()`.

- **Challenge:** BFS shortest-path tie-breaking when multiple shortest paths exist.
  **Resolution:** For the required graph, BFS A→F deterministically yields `[A, B, D, F]` because neighbors are processed in insertion order (B before C from A, and D is discovered from B before C can reach D).

- **Challenge (post-AUDIT):** Preventing edge-key collisions with hyphenated labels.
  **Resolution:** Replaced delimiter-joined String keys with an immutable `EdgeKey` pair class.

- **Challenge (post-AUDIT):** Ensuring rejected `addEdge()` operations leave the graph unchanged.
  **Resolution:** Moved conflict detection before `addVertex()` calls; verified with a new test that checks reachability and `hasCycle()` are unchanged after a rejected operation.

## What Went Well

- All 58 assertions pass on the first run after corrections.
- Exact expected paths match: BFS `[A, B, D, F]` and DFS `[A, B, D, C, E, F]`.
- All original 19 tests remain intact and pass; 8 new tests added and pass.
- Null-input validation, conflicting-direction exceptions, and graph immutability all work correctly.
- Graph state remains immutable across traversals and after rejected operations.

## What Could Be Improved

- The DFS recursion could hit `StackOverflowError` on very deep graphs; an iterative stack-based alternative would be more robust for production use.
- The graph is unweighted; adding weighted-edge support would require Dijkstra's algorithm instead of BFS for shortest paths.
- No `removeVertex` or `removeEdge` methods were required by the lab, but they would be useful extensions.

## Questions for Instructor

None — all requirements were clear and implemented successfully.
