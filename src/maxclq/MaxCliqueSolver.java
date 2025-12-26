package maxclq;
//
import java.util.Set;
import ugraph.Node;
import ugraph.UGraph;
import ugraph.UndirectedGraph;
//
public interface MaxCliqueSolver {
  
  public Set<Node> solve (final UGraph G);
  
  default public boolean verify (final UndirectedGraph G, final Set<Node> C) {
    
    for (final Node u: C) {
      
      for (final Node v: C) {
        
        // Clique mal formado: los nodos u y v pertenecen al clique y no son vecinos.
        if ((u != v) && !G.neighbours(u, v))
          return false;

      }  
      
    }
    
    return true;
    
  }
  
}
