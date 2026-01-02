package maxclq;

/**
 *
 * @author MAZ
 */
import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.Literal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
//
import ugraph.Node;
import ugraph.UGraph;
//

public final class MaxCLQ implements MaxCliqueSolver {

    static {
        System.loadLibrary("jniortools");
    }

    private final BruteForceMaxCliqueSolver pereborSolver = new BruteForceMaxCliqueSolver();

    @Override
    public Set<Node> solve(final UGraph G) {

        final Set<Node> C = _solve(G, new HashSet<>(), 0); //el hashet es el clike en construccion 

        if (!verify(G.support(), C)) {
            throw new IllegalArgumentException("Clique mal formado");
        }

        return C;

    }

    private Set<Node> _solve(final UGraph G, Set<Node> C, int LB) {
//pasamos el grafo que nos va quedando, no siempre es el original 
// el set es el clique en construccion y LB es el mejor tamaño hasta ahora

        // Si el grafo es vacÃ­o se devuelve el clique en construcciÃ³n.
        if (G.nodes() == 0) {
            return C;
        }

        // Si el grafo tiene un Ãºnico nodo o no tiene aristas, se devuelve
        // el nodo de grado mÃ¡ximo.
        if ((G.nodes() == 1) || (G.degree() == 0)) {
            C.add(G.getMaxDegreeNode());
            return C;
        }

        // Si la instancia es pequeÃ±a respecto al grafo original,
        // se resuelve mediante fruerza bruta.
        if (10 * G.nodes() <= G.support().nodes()) {
            C.addAll(pereborSolver.solve(G)); //fuerza bruta de la semana anaterior
            return C;
        }

        final int k = LB - C.size();
        final int UB = overstimation(G, k) + C.size();

        // Si la estimaciÃ³n informa que en el grafo G no hay
        // un clique de tamaÃ±o mayor que LB, se corta esta rama
        // de la exploraciÃ³n recursiva.
        if (LB >= UB) {
            return new HashSet<>();
        }

        final Node v = G.getMaxDegreeNode();
        final UGraph Gv = G.subgraphInducedBy(v);
        final Set<Node> _C = new HashSet<>(C); //vecinos de V sin V creo que es esto 
        _C.add(v);

        final Set<Node> C1 = _solve(Gv, _C, LB);
        if (C1.size() > LB) {
            LB = C1.size();
        }

        final UGraph Gm = G.subgraphInducedMinus(v);
        final Set<Node> C2 = _solve(Gm, C, LB);

        return (C1.size() > C2.size()) ? C1 : C2;

    }

    private int overstimation(final UGraph G, final int k) {

        // Se obtiene una particiÃ³n del grafo.
        final List<Set<Node>> P = UGraphUtilities.partition(G); //implementar en este metodo estatico el algoritmo voraz
        //P es una particion

        // Sil particiÃ³n no tiene suficientes subconjuntos, no se resuelve.
        if (P.size() < k) {
            return 0;
        }

        return solveMaxSATModel(G, k, P); //intentamos mejorar la sobreestimacion

    }

    private int solveMaxSATModel(final UGraph G, final int k,
            final List<Set<Node>> P) {

        // Sirve para mantener relaciÃ³n entre nodo y variable asociada.
        final Map<Node, BoolVar> nodeToBoolVar = new IdentityHashMap<>();

        // Se crea el modelo con las clÃ¡usulas duras
        // (clÃ¡usulas de satisfacciÃ³n obligatoria).
        final CpModel model = getHardModel(G, k, nodeToBoolVar);

        // Se aÃ±aden las clÃ¡usulas blandas (de satisfacciÃ³n a maximizar).
        final BoolVar[] equivalents = addSoftClauses(model, P, nodeToBoolVar);

        // Se instancia un resolvedor para problemas de programaciÃ³n con restricciones.
        final CpSolver solver = new CpSolver();
        // Se resuelve el modelo.
        final CpSolverStatus resultStatus = solver.solve(model);

        // Se chequea el tipo resultado obtenido.
        if ((resultStatus == CpSolverStatus.OPTIMAL)
                || (resultStatus == CpSolverStatus.FEASIBLE)) {
            final int satisfied = countSatisfiedSoftClauses(solver, equivalents);
            return satisfied;
        }
        else if (resultStatus == CpSolverStatus.INFEASIBLE) {
            return 0;
        }
        else // Modelo mal formado o problemas en el resolvedor
        {
            throw new IllegalArgumentException("Error en formulaciÃ³n del modelo");
        }

    }

    private CpModel getHardModel(final UGraph G, final int k,
            final Map<Node, BoolVar> nodeToBoolVar) {

        // Modelo para la instancia del problema MaxSAT
        final CpModel model = new CpModel();

        // ClaÃºsulas booleanas del modelo
        return model;

    }

    private BoolVar[] addSoftClauses(final CpModel model,
            final List<Set<Node>> P,
            final Map<Node, BoolVar> nodeToBoolVar) {

        // ObtenciÃ³n de clÃ¡usulas blandas
        final Set<Literal[]> softClauses = getSoftClauses(P, nodeToBoolVar);

        // IncorporaciÃ³n de restricciones de clÃ¡usulas blandas
        // Por cada clÃ¡usula blanda, se introduce una variable equivalente.
        final BoolVar[] s = new BoolVar[softClauses.size()];
        int j = 0;
        for (final Literal[] clause : softClauses) {

            s[j] = OR(model, clause);
            ++j;

        }

        // ExpresiÃ³n lineal formada por variables booleanas; cada variable booleana
        // se considera como una variable entera que puede tomar valores 0 Ã³ 1. Se
        // trata de maximizar la suma de los valores de las variables equivalentes
        // a las clÃ¡usulas blandas.
        model.maximize(LinearExpr.sum(s));

        // Devuelve el conjunto de variables equivalentes
        return s;

    }

    private Set<Literal[]> getSoftClauses(final List<Set<Node>> P,
            final Map<Node, BoolVar> nodeToBoolVar) {

        final Set<Literal[]> softClauses = new LinkedHashSet<>();

        final ArrayList<Literal> literals = new ArrayList<>();
        for (final Set<Node> part : P) {
            for (final Node v : part) {
                if (nodeToBoolVar.containsKey(v)) {
                    literals.add(nodeToBoolVar.get(v));
                }
            }
            softClauses.add(literals.toArray(Literal[]::new));
            literals.clear();
        }

        return softClauses;

    }

    private BoolVar OR(final CpModel model, final Literal[] literals) {
        final BoolVar or = model.newBoolVar("or");
        for (Literal lit : literals) {
            model.addImplication(lit, or);
        }
        model.addBoolOr(literals).onlyEnforceIf(or);

        return or;

    }

    private int countSatisfiedSoftClauses(final CpSolver solver,
            final BoolVar[] equivalents) { //las blandas no las implementamos al modelo, solo las manejamos 

        int satisfied = 0;
        for (final BoolVar x : equivalents) {
            if (solver.booleanValue(x)) {
                ++satisfied;
            }
        }

        return satisfied;

    }

}
