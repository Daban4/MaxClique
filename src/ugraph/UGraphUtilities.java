package maxclq;
/**
 *
 * @author MAZ
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
//
import ugraph.Node;
import ugraph.UGraph;
//
public class UGraphUtilities {
  
  static public List<Set<Node>> partition (final UGraph G) {  
    
    final List<Set<Node>> P = new LinkedList<>();

    final int numNodes = G.nodes();
    for (int i = 1; i <= numNodes; ++i) {
      final Node v = G.node(i);

      boolean foundSet = false;
      for (final Set<Node> S : P) {
        
        boolean isCompatible = true; 
        
        for (final Node u : S) {
          if (G.neighbours(v, u)) { 
            isCompatible = false;
            break; 
          }
        }
        
        if (isCompatible) {
          S.add(v);
          foundSet = true;
          break; 
        }
      }
      
      if (!foundSet) {
        final Set<Node> newSet = new LinkedHashSet<>();
        newSet.add(v);
        P.add(newSet);
      }
    }
    
    return P;
    
  }
      
}