package ugraph;
/**
 *
 * @author MAZ
 */
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
//
public class UndirectedGraph implements UGraph {

  static private final String GRAPH_FORMAT_PATTERN = "%%.*|c";

  private final int m; // Número de aristas
  private final int n; // Número de nodos
  private final Map<Node, Set<Node>> neighbours; // Correspondencia: nodo -> { nodo }
  private final Node[][] edges; // Correspondencia: índice de arista -> { a, b }
  private final Node[] nodes;   // Colección de nodos ordenada en creciente por grado

  private UndirectedGraph (final Map<Node, Set<Node>> neighbours,
                           final List<Node[]> edges,
                           final boolean deepCopy) {
    
    this.n = neighbours.size();
    this.m = edges.size();
 
    // Se crea el array de nodos.
    this.nodes = new Node[n];
    int i = 0;
    for (final Node u: neighbours.keySet()) {
      this.nodes[i] = u;
      ++i;
    }

    // Aristas (ya generadas)
    this.edges = edges.toArray(Node[][]::new);
      
    if (deepCopy) {
    
      // Nodos y vecinos (copia profunda)
      this.neighbours = new IdentityHashMap<>();
      for (final Node v: neighbours.keySet()) {
        final Set<Node> ngh = neighbours.get(v);
        this.neighbours.put(v, new LinkedHashSet<>(ngh));
      }

    } else
      this.neighbours = neighbours;
    
    // Se ordenan los nodos en orden decreciente de grado.
    sort(nodes);
    
  }
  
  public UndirectedGraph (final InputStream is) {

    final Scanner scanner = new Scanner(is);

    try {
      final Pattern pattern = Pattern.compile(GRAPH_FORMAT_PATTERN);
      do {
        scanner.next(pattern);
        scanner.nextLine();
      } while(true);
    } catch (final NoSuchElementException ex) {}

    final String initialToken = scanner.next();

    if (initialToken.compareTo("p") == 0) {

      scanner.next("edge");

      this.n = scanner.nextInt();
      this.m = scanner.nextInt();

    } else {

      final int rows = Integer.parseInt(initialToken); // Número de filas
      final int columns = scanner.nextInt(); // Número de columnas
      this.n = (rows > columns) ? rows : columns;
      this.m = scanner.nextInt(); // Número de aristas

    }
    
    //System.out.printf("Nodos: %d\tVertices: %d\n", n, m);    

    this.neighbours = new IdentityHashMap<>();
    this.nodes = new Node[n];
    for (int i = 1; i <= n; ++i) {
      final Node u = new Node(i);
      nodes[i - 1] = u;
      neighbours.put(u, new HashSet<>());
    }

    this.edges = new Node[m][2];
    
    if (initialToken.compareTo("p") == 0)
      dataScan(scanner, nodes);
    else
      mtxScan(scanner, nodes);
    
    // Se ordenan los nodos en orden decreciente de grado.
    sort(nodes);
    
  }
  
  private void sort (final Node[] nodes) {
    Arrays.sort(nodes, (final Node a, final Node b) -> {
      final int da = degree(a);
      final int db = degree(b);
      return (da < db) ? -1 : (da == db) ? 0 : +1;
    });        
  }

  private void dataScan (final Scanner scanner, final Node[] nodes) {

    try {

      if (!scanner.hasNextLine()) {
        System.err.println("Formato de fichero de descripción de grafos incorrecto");
        throw new NoSuchElementException();
      }

      for (int j = 1; scanner.hasNextLine();) {

        final String initialToken = scanner.next();
        if (initialToken.compareTo("e") == 0) {

          final Node u = nodes[scanner.nextInt() - 1];
          final Node v = nodes[scanner.nextInt() - 1];

          this.edges[j - 1] = new Node[] { u, v };

          this.neighbours.get(u).add(v);
          this.neighbours.get(v).add(u);          

          ++j;

        }

        scanner.nextLine();

      }

    } catch (final NumberFormatException ex) {
      System.out.println(ex);
      throw ex;
    }

  }

  private void mtxScan (final Scanner scanner, final Node[] nodes) {

    for (int j = 1; j <= m; ++j) {

      scanner.nextLine();

      final Node u = nodes[scanner.nextInt() - 1];
      final Node v = nodes[scanner.nextInt() - 1];

      this.edges[j - 1] = new Node[]{u, v};

      this.neighbours.get(u).add(v);
      this.neighbours.get(v).add(u);  

    }

  }
  
  @Override
  public int nodes () { return n; }
  public int edges () { return m; }
  public float density () { return (2.0f * m) / ((n - 1) * n); }  

  @Override
  public UndirectedGraph support() { return this; }  
  
  @Override
  public Node getMinDegreeNode () { return nodes[0]; }  
  @Override
  public Node getMaxDegreeNode () { return nodes[n - 1]; }
  @Override
  public int degree () { return degree(nodes[n - 1]); }
  
  private Node getNode (final int j, final int i) {
    if ((j >= 1) && (j <= m))
      return edges[j -1][i];
    else
      throw new IllegalArgumentException("Índice de arista inválido");
  }

  public Node node1 (final int j) {
    return getNode(j, 0);
  }

  public Node node2 (final int j) {
    return getNode(j, 1);
  }

  public boolean inEdge (final Node v, final int j) {
    if (!neighbours.containsKey(v))
      throw new IllegalArgumentException("Nodo inválido");
    if ((j < 1) || (j > m))
      throw new IllegalArgumentException("Índice de arista inválido");
    return ((node1(j) == v) || (node2(j) == v));
  }
  
//  public boolean inEdge (final int i, final int j) {
//    return inEdge(node(i), j);
//  }  

  @Override
  public boolean neighbours (final Node u, final Node v) {
    if (!neighbours.containsKey(u))
      throw new IllegalArgumentException("Nodo inválido: " + u.toString());
    if (!neighbours.containsKey(v))
      throw new IllegalArgumentException("Nodo inválido");
    return neighbours.get(v).contains(u);
  }
  
  @Override
  public boolean neighbours (final int i1, final int i2) {
    return neighbours.get(node(i1)).contains(node(i2));
  }

  @Override
  public Collection<Node> getNeighbours (final Node u) {
    return neighbours.get(u);
  }
  
  @Override
  public Node node (final int i) {
    if ((i < 1) || (i > n))
      throw new IllegalArgumentException("Índice de nodo inválido");    
    return nodes[i - 1];
  }

  @Override
  public int degree (final Node v) {
    if (!neighbours.containsKey(v))
      throw new IllegalArgumentException("Nodo inválido");
    return neighbours.get(v).size();
  }

  @Override
  public int degree (final int i) {
    if ((i < 1) || (n < i))
      throw new IllegalArgumentException("Índice de nodo inválido");
    return neighbours.get(nodes[i - 1]).size();
  }
  
//  public UGraph subgraphInducedMinus (final Node u) {
//    
//    final Set<Node> _nodes = new HashSet(neighbours.keySet());
//    _nodes.remove(u);
//    
//    return new InducedUndirectedSubgraph(this, _nodes);
//    
//    final Node[] ndes = new Node[n - 1];
//
//    // Vecinos
//    final Map<Node, Set<Node>> nghs = new LinkedHashMap<>();
//    int i = 0;
//    for (final Node v: neighbours.keySet()) {
//      if (u != v) {
//        final Set<Node> ngh = new HashSet<>(neighbours.get(v));
//        if (ngh.contains(u))
//          ngh.remove(u);
//        nghs.put(v, ngh);
//        ndes[i++] = v;
//      } 
//    }
//    
//    // Aristas
//    final List<Node[]> edgs = new LinkedList<>();
//    for (int j = 1; j <= m; ++j)
//      if (!inEdge(u, j))
//        edgs.add(edges[j - 1]);
//    
//    return new UndirectedGraph(nghs, edgs, false);
//    
//  } 
  
//  public UGraph subgraphInducedBy (final Node u) {
//    
//    final Set<Node> _nodes = new HashSet<>(this.getNeighbours(u));
//    _nodes.add(u);
//    
//    return new InducedUndirectedSubgraph(this, _nodes);
//    
//    // Se seleccionan vecinos de u
//    final Map<Node, Set<Node>> nghs = new LinkedHashMap<>();
//    final Set<Node> nghu = neighbours.get(u);
//    final Node[] ndes = new Node[nghu.size()];
//    int i = 0;
//    for (final Node v: nghu) {
//      final Set<Node> nghv = new HashSet<>(neighbours.get(v));
//      // Intersección de vecinos de uv y vecinos de v      
//      nghv.retainAll(nghu);
//      nghs.put(v, nghv);
//      ndes[i++] = v;
//    }
//  
//    // Se seleccionan las aristas formadas 
//    final List<Node[]> edgs = new LinkedList<>();
//    for (int j = 1; j <= m; ++j) {
//      final Node[] edge = edges[j - 1];
//      final Node v1 = edge[0];
//      final Node v2 = edge[1];
//      if (nghu.contains(v1) && nghu.contains(v2))
//        edgs.add(edge);
//    }
//    
//    return new UndirectedGraph(nghs, edgs, false);
//    
//  }
  
  public UndirectedGraph complement () {

    final Map<Node, Set<Node>> nghs = new IdentityHashMap<>();
    for (int i = 1; i <= n; ++i)
      nghs.put(node(i), new HashSet<>());
    
    final List<Node[]> edgs = new LinkedList<>();
    
    for (int i1 = 1; i1 < n; ++i1) {
      
      final Node u = node(i1);      
      
      for (int i2 = i1 + 1; i2 <= n; ++i2) {
        
        final Node v = node(i2);
        
        if (!UndirectedGraph.this.neighbours(u, v)) {
          edgs.add(new Node[] { u, v });
          nghs.get(u).add(v);
          nghs.get(v).add(u);
        }
        
      }
      
    }
    
    return new UndirectedGraph(nghs, edgs, false);
    
  }
  
  public void print (final OutputStream _os) {
    try (final PrintStream os = new PrintStream(_os)) {
      os.println("p edge " + n + " " + m);
      for (int j = 1; j <= m; ++j) {
        final int v1 = node1(j).index;
        final int v2 = node2(j).index;
        os.println("e " + v1 + " " + v2);
      }
    }
  }
  
  public void printDot (final OutputStream os) {
    try (final PrintStream writer = new PrintStream(os)) {
      
      writer.println("graph {\n");

      writer.println("\tnode [shape=circle];\n");
      for (final Node v: neighbours.keySet()) {
        writer.printf("\tnode [label=%d] %d;\n", v.index, v.index);
      }

      writer.println("\n\tedge [labeldistance=2];\n");
      for (int j = 1; j <= edges(); ++j) {

        final int source = node1(j).index;
        final int target = node2(j).index;
        writer.printf("\t%d -- %d;\n", source, target);

      }

      writer.println("\n}");
      
    }
  }  

}