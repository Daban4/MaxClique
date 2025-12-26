package maxclq;
/**
 *
 * @author MAZ
 */
import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.Literal;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
//
import ugraph.Node;
import ugraph.UGraph;
//
public final class BruteForceMaxCliqueSolver implements MaxCliqueSolver {
  
  static {
    System.loadLibrary("jniortools");
  }  
 
  public Set<Node> solve (final UGraph G) {
    
    // Casos básicos de resolución directa
    if (G.nodes() == 1) {
      final Set<Node> clique = new HashSet<>();
      clique.add(G.getMaxDegreeNode());
      return clique;
    } else if (G.nodes() == 2) {
      final Set<Node> clique = new HashSet<>();
      clique.add(G.getMaxDegreeNode());
      if (G.degree() == 1)
        clique.add(G.getMinDegreeNode());
      return clique;
    }
    
    // Se prueba si existe un clique de tamaño igual al grado
    // del grafo (no puede haber un clique de tamaño mayor).
    Set<Node> maxClique = searchClique(G.degree(), G);
    if (!maxClique.isEmpty()) // Si ese clique existe, es de tamaño máximo.
      return maxClique;
    else { // Se dejan en maxClique los nodos de un clique de tamaño 2.
      maxClique.add(G.getMaxDegreeNode());
      for (final Node u: G.getNeighbours(G.getMaxDegreeNode())) {
        maxClique.add(u);
        break;
      }
    }
    
    int left = 2;
    int right = G.degree();
    // Inv: maxClique es un clique de tamaño igual al valor de la variable left.
    // Inv: no existe un clique de tamaño igual valor de la variable right.
    while ((left + 1) < right) { // Búsqueda dicotómica
      final int k = (left + right) / 2;
        Set<Node> cliqueAct = searchClique(k, G);
        if(!cliqueAct.isEmpty()){
            left = k;
        }
        else{
            right = k;
        }
    }
    
    return maxClique;
    
  }
  
  private Set<Node> searchClique (final int k, final UGraph G) {
     
    // Correspondencia entre nodos y variables
    final Map<Node, BoolVar> nodeToVariable = new IdentityHashMap<>();

    // Se construye el modelo con restricciones.
    final CpModel model;
    try {
      model = getModel(k, G, nodeToVariable);
    } catch (final IllegalArgumentException ex) {
      return new HashSet<>();
    }

    // Se instancia un resolvedor para problemas de programación con restricciones.
    final CpSolver solver = new CpSolver();
    // Se resuelve el modelo.
    final CpSolverStatus resultStatus = solver.solve(model);

    // Se chequea el tipo resultado obtenido.
    final Set<Node> clique = new HashSet<>();    
    if ((resultStatus == CpSolverStatus.OPTIMAL) ||
        (resultStatus == CpSolverStatus.FEASIBLE)) {   
          
      // Se cargan los nodos asociados a variables
      // del modelo a las que la interpretación
      // positiva asigna valores TRUE.
      for (final Node u: nodeToVariable.keySet()) {
        if (solver.booleanValue(nodeToVariable.get(u))) {
          clique.add(u);
        }
      }
      
      return clique;

    } else if (resultStatus == CpSolverStatus.INFEASIBLE) {
      return clique;
    } else
      throw new IllegalArgumentException("Error en formulación del modelo: " + resultStatus);    
    
  }
  
  private CpModel getModel (final int k, final UGraph G,
                            final Map<Node, BoolVar> c) {

    final int n = G.nodes();      
    
    // Modelo
    final CpModel model = new CpModel();
        
    // Se identifican los nodos que podrían formar parte de un clique de tamaño k.
    final List<Literal> literals = new LinkedList<>();
      // Solo se consideran aquellos nodos que pertenecen a un k-core.
      // Solo para esos nodos:
      // - Se introduce una variable booleana
      // - Se imponen restricciones    
    for (int i = 1; i <= n; ++i) {
      final Node u = G.node(i);
      if(inCore(G, k, u)){
          BoolVar uBool = model.newBoolVar("u-core");
          c.put(u, uBool);
          literals.add(uBool);
      }
    }
    SCE.atLeast(k, literals, model);
    literals.clear();
    
    // Dos nodos de k-core que no sean vecinos
    // no pueden pertenecer al mismo clique de tamaño k.
    for (int i = 1; i < n; ++i) {
      final Node u = G.node(i);
      if(!c.containsKey(u)) continue;
      for(int j = i +1; j <= n; ++j){
          final Node v = G.node(j);
          if (G.neighbours(u, v)){
              Literal litU = c.get(u);
              Literal litV = c.get(v);
              literals.add(litU);
              literals.add(litV);
              SCE.atMost(1, literals, model);
          }
      }        
      
      
    }
    // NO uses el método inCore() en este doble bucle; el tiempo de ejecución
    // se alarga sensiblemente; pregunta si el nodo está en la estructura nodeToVariable.
    
    return model;
    
  }  
  
  private boolean inCore (final UGraph G, final int k, final Node u) {
    if (G.degree(u) < (k - 1))
      return false;
    int cont = 0;
    for (final Node v: G.getNeighbours(u))
      if (G.degree(v) >= (k - 1)) ++cont;
    return (cont >= (k - 1));
  } 
  
}
