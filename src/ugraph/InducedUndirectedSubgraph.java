package ugraph;
/**
 *
 * @author MAZ
 */
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
//
public final class InducedUndirectedSubgraph implements UGraph {

  private final UndirectedGraph G;
  private final Map<Node, Set<Node>> neighbours;
  private final Node[] nodes;
//  private final int m;
  
//  public InducedUndirectedSubgraph (final UndirectedGraph G,
//                                    final Set<Node> nodes) {
//    this.G = G;
//
//    this.neighbours = new IdentityHashMap<>();
//    for (final Node u: nodes) {
//      final Set<Node> _neigh = new HashSet<>();
//      this.neighbours.put(u, _neigh);
//      for (final Node v: G.getNeighbours(u))
//        if (nodes.contains(v)) _neigh.add(v);
//    }
//    
//    this.nodes = new Node[nodes.size()];
//    int i = 0;
//    for (final Node u: nodes) {
//      this.nodes[i] = u;
//      ++i;
//    }
//    // Se ordenan los nodos en orden decreciente de grado.
//    Arrays.sort(this.nodes, (final Node a, final Node b) -> {
//      final int da = degree(a);
//      final int db = degree(b);
//      return (da < db) ? +1 : (da == db) ? 0 : -1;
//    });
//    
//    int _m = 0;
//    for (i = 1; i < nodes.size(); ++i) {
//      final Node u = this.nodes[i - 1];
//      for (int j = i + 1; j <= nodes.size(); ++j) {
//        final Node v = this.nodes[j - 1];
//        if (G.neighbours(u, v))
//          ++_m;
//      }
//    }
//    this.m = _m;
//
//  }  
  
  public InducedUndirectedSubgraph (final UGraph G,
                                    final Set<Node> nodes) {
    this.G = G.support();

    this.neighbours = new IdentityHashMap<>();
    for (final Node u: nodes) {
      final Set<Node> _neigh = new HashSet<>();
      this.neighbours.put(u, _neigh);
      for (final Node v: G.getNeighbours(u))
        if (nodes.contains(v)) _neigh.add(v);
    }
    
    this.nodes = new Node[nodes.size()];
    int i = 0;
    for (final Node u: nodes) {
      this.nodes[i] = u;
      ++i;
    }
    // Se ordenan los nodos en orden decreciente de grado.
    Arrays.sort(this.nodes, (final Node a, final Node b) -> {
      final int da = degree(a);
      final int db = degree(b);
      return (da < db) ? -1 : (da == db) ? 0 : +1;
    });

  }

  @Override
  public int nodes () { return nodes.length; }
  @Override
  public UndirectedGraph support () { return G; }

  @Override
  public Node getMinDegreeNode () { return nodes[0]; }  
  @Override
  public Node getMaxDegreeNode () { return nodes[nodes.length - 1]; }
  @Override
  public int degree () { return degree(nodes[nodes.length - 1]); }
  
  @Override
  public Node node (final int i) {
    if ((i < 1) || (i > nodes.length))
      throw new IllegalArgumentException("Índice de nodo inválido");    
    return nodes[i - 1];
  }
  
  @Override
  public int degree (final Node u) {
    return neighbours.get(u).size();
  }
  
  @Override
  public int degree (final int i) {
    return degree(node(i));
  }  

  @Override
  public boolean neighbours (final Node u, final Node v) {
    return G.neighbours(u, v);
  }

  @Override
  public boolean neighbours (final int i1, final int i2) {
    return G.neighbours(node(i1), node(i2));
  }

  @Override
  public Collection<Node> getNeighbours (final Node u) {
    return neighbours.get(u);
  }
  
}
