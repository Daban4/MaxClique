package ugraph;
/**
 *
 * @author MAZ
 */
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
//
public interface UGraph {

  // Métodos consultores generales
  public int nodes ();
  //public int edges ();
  
  public int degree ();  
  public Node getMinDegreeNode (); 
  public Node getMaxDegreeNode ();
  
  public UndirectedGraph support ();
  
  //public List<Node> getNodes ();

  //Métodos consultores sobre aristas
//  public Node node1 (final int j);
//  public Node node2 (final int j);
//  public boolean inEdge (final Node v, final int j);
//  public boolean inEdge (final int i, final int j);
    
  // Métodos consultores sobre nodos
  public Node node (final int i);
  public int degree (final int i);
  public int degree (final Node u);

  public boolean neighbours (final Node u, final Node v);
  public boolean neighbours (final int i1, final int i2);
  public Collection<Node> getNeighbours (final Node u);
  
  default public UGraph subgraphInducedBy (final Node u) {
    
    final Set<Node> nodes = new HashSet<>(getNeighbours(u));
    
    return new InducedUndirectedSubgraph(support(), nodes);
  }
  
  default public UGraph subgraphInducedMinus (final Node u) {
    
    final Set<Node> nodes = new HashSet();
    for (int i = 1; i <= nodes(); ++i)
      if (node(i) != u) nodes.add(node(i));
    
    return new InducedUndirectedSubgraph(support(), nodes);

  }

}
