package constraints;

/**
 *
 * @author MAZ
 */
import java.util.ArrayList;
import java.util.List;
//
import cnfcomponents.CNFBooleanFormula;
import cnfcomponents.DisjunctiveBooleanClause;
import cnfcomponents.BooleanLiteral;
import cnfcomponents.BooleanVariable;
import cnfcomponents.BooleanOperators;
import static cnfcomponents.BooleanOperators.AND;
import static cnfcomponents.BooleanOperators.OR;
import constraints.CardinalityConstraints;

//
public class SCE implements CardinalityConstraints {

    @Override
    public void atMost(final int k, final List<BooleanLiteral> literals, final CNFBooleanFormula phi) {

        final int n = literals.size();

        if (n > 0) { // A lo sumo k de 0 literales es de cumplimiento trivial

            if (k == 0) {

                // A lo sumo 0 ovejas blancas == al menos n ovejas negras
                for (BooleanLiteral x : literals) {
                    phi.addClause(new DisjunctiveBooleanClause(x.not()));
                }
            }
            else if (k == 1) {

                atMost1(literals, phi);

            }
            else if ((k > 1) && (k < (n - 1))) {
                if (((n - 1) * k) < (n * (n - k))) {
                    _atMost(k, literals, phi);
                }
                else {
                    List<BooleanLiteral> negated = new ArrayList<>();
                    for (BooleanLiteral l : literals) {
                        negated.add(l.not());
                    }
                    _atLeast(n - k, negated, phi);
                }
            }
            else if (k == (n - 1)) {

                // A lo sumo n - 1 ovejas blancas == al menos 1 oveja negra
                List<BooleanLiteral> negated = new ArrayList<>();
                for (BooleanLiteral l : literals) {
                    negated.add(l.not());
                }
                phi.addClause(new DisjunctiveBooleanClause(negated));
            }
            else if (k >= n) {

                // Cuando k >= n, se cumple trivialmente a lo sumo k de un total de n.
                // Se queda vacío.
            }

        }

    }

    @Override
    public void atMost1(final List<BooleanLiteral> literals, final CNFBooleanFormula phi
    ) {

        final int n = literals.size();

        if (n > 0) {

            final RMatrix R = new RMatrix(n - 1, 1, phi);

            // Condiciones simplificadas del artículo; en
            // concreto, desaparecen las cláusulas (2) y (4).
            //1
            for (int i = 1; i < n; i++) {
                BooleanLiteral litMat = R.getPositiveLiteral(i, 1);
                BooleanLiteral x = literals.get(i - 1);//-1 explicado en atMost
                phi.addClause(new DisjunctiveBooleanClause(x.not(), litMat));

            }
            //3 
            for (int i = 2; i <= n - 1; i++) { //desaparece J porque j = k = 1
                BooleanLiteral litMat1 = R.getNegativeLiteral(i - 1, 1);
                BooleanLiteral litMat2 = R.getPositiveLiteral(i, 1);
                phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2));

            }
            //5
            for (int i = 2; i <= n; i++) {
                BooleanLiteral x = literals.get(i - 1);
                BooleanLiteral litMat1 = R.getNegativeLiteral(i - 1, 1);
                phi.addClause(new DisjunctiveBooleanClause(x.not(), litMat1));
            }
        }

    }

    @Override
    public void atLeast(final int k, final List<BooleanLiteral> literals, final CNFBooleanFormula phi
    ) {

        final int n = literals.size();

        if (k > 0) { // k == 0 es de cumplimiento trivial

            if ((k > n) || (n == 0)) {
                throw new IllegalArgumentException("Restricción insatisfacible");
            }

            if (n > 0) {

                if (k == 1) {

                    atLeast1(literals, phi);

                }
                else if ((k > 1) && (k < n)) {
                    // Al menos k ovejas blancas == a lo sumo n - k ovejas negras
                    if ((n * k) <= ((n - 1) * (n - k))) {
                        _atLeast(k, literals, phi);
                    }
                    else {
                        List<BooleanLiteral> negated = new ArrayList<>();
                        for (BooleanLiteral l : literals) {
                            negated.add(l.not());
                        }
                        _atMost(n - k, negated, phi);
                    }
                }
                else if (k == n) {
                    // Al menos n de n literales es exigir que se cumplan todos.
                    for (BooleanLiteral l : literals) {
                        phi.addClause(new DisjunctiveBooleanClause(l));
                    }//mejor hacerlo con un bucle sobre literles en lugar de sobre i
                }

            }

        }

    }

    private void _atLeast(final int k, final List<BooleanLiteral> literals, final CNFBooleanFormula phi) {
        final int n = literals.size();
        final RMatrix R = new RMatrix(n, k, phi);
        for (int j = 1; j <= k; j++) {
            BooleanLiteral litMat = R.getPositiveLiteral(n, j);
            phi.addClause(new DisjunctiveBooleanClause(litMat));
        }

        for (int i = 2; i <= n; i++) {
            BooleanLiteral l = literals.get(i - 1);
            for (int j = 2; j <= k; j++) {
                BooleanLiteral litMat1 = R.getNegativeLiteral(i, j);
                BooleanLiteral litMat2 = R.getPositiveLiteral(i - 1, j);
                BooleanLiteral litMat3 = R.getPositiveLiteral(i - 1, j - 1);

                phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2, l));
                phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2, litMat3));

            }
        }
        for (int i = 2; i <= n; i++) {
            BooleanLiteral l = literals.get(i - 1);
            BooleanLiteral litMat1 = R.getNegativeLiteral(i, 1);
            BooleanLiteral litMat2 = R.getPositiveLiteral(i - 1, 1);
            phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2, l));
        }
        phi.addClause(new DisjunctiveBooleanClause(R.getNegativeLiteral(1, 1), literals.get(0)));
        for (int j = 2; j <= k; j++) {
            phi.addClause(new DisjunctiveBooleanClause(R.getNegativeLiteral(1, j)));
        }
        for (int i = 1; i < k; i++) {
            phi.addClause(new DisjunctiveBooleanClause(R.getNegativeLiteral(i, k)));
        }
    }

    private BooleanVariable _atLeastR(final int k, final List<BooleanLiteral> literals, final CNFBooleanFormula phi) {
        final int n = literals.size();
        final RMatrix R = new RMatrix(n, k, phi);
        List<BooleanLiteral> aux = new ArrayList<>();
        for (int j = 1; j <= k; j++) {
            BooleanLiteral litMat = R.getPositiveLiteral(n, j);
            //phi.addClause(new DisjunctiveBooleanClause(litMat));
            List<BooleanLiteral> listaAux = new ArrayList<>();
            listaAux.add(litMat);
            aux.add(OR(phi, listaAux).getPositiveLiteral());
        }

        for (int i = 2; i <= n; i++) {
            BooleanLiteral l = literals.get(i - 1);
            for (int j = 2; j <= k; j++) {
                BooleanLiteral litMat1 = R.getNegativeLiteral(i, j);
                BooleanLiteral litMat2 = R.getPositiveLiteral(i - 1, j);
                BooleanLiteral litMat3 = R.getPositiveLiteral(i - 1, j - 1);
                List<BooleanLiteral> listaAux = new ArrayList<>();
                listaAux.add(litMat1);
                listaAux.add(litMat2);
                listaAux.add(l);
                aux.add(OR(phi, listaAux).getPositiveLiteral());
                listaAux.clear();
                listaAux.add(litMat1);
                listaAux.add(litMat2);
                listaAux.add(litMat3);
                aux.add(OR(phi, listaAux).getPositiveLiteral());
                //phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2, l));
                //phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2, litMat3));

            }
        }
        for (int i = 2; i <= n; i++) {
            BooleanLiteral l = literals.get(i - 1);
            BooleanLiteral litMat1 = R.getNegativeLiteral(i, 1);
            BooleanLiteral litMat2 = R.getPositiveLiteral(i - 1, 1);
            //phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2, l));
            List<BooleanLiteral> listaAux = new ArrayList<>();
            listaAux.add(litMat1);
            listaAux.add(litMat2);
            listaAux.add(l);
            aux.add(OR(phi, listaAux).getPositiveLiteral());
        }
        //phi.addClause(new DisjunctiveBooleanClause(R.getNegativeLiteral(1, 1), literals.get(0)));
        aux.add(OR(phi, R.getNegativeLiteral(1, 1), literals.get(0)).getPositiveLiteral());
        for (int j = 2; j <= k; j++) {
            //phi.addClause(new DisjunctiveBooleanClause(R.getNegativeLiteral(1, j)));
            List<BooleanLiteral> listaAux = new ArrayList<>();
            listaAux.add(R.getNegativeLiteral(1, j));
            aux.add(OR(phi, listaAux).getPositiveLiteral());
        }
        for (int i = 1; i < k; i++) {
            //phi.addClause(new DisjunctiveBooleanClause(R.getNegativeLiteral(i, k)));
            List<BooleanLiteral> listaAux = new ArrayList<>();
            listaAux.add(R.getNegativeLiteral(i, k));
            aux.add(OR(phi, listaAux).getPositiveLiteral());
        }
        BooleanVariable s = AND(phi, aux);
        return s;
    }

    private void _atMost(final int k, final List<BooleanLiteral> literals, final CNFBooleanFormula phi) {

        final int n = literals.size();

        final RMatrix R = new RMatrix(n - 1, k, phi);

        // Codificación del artículo
        //1
        for (int i = 1; i < n; i++) {
            BooleanLiteral litMat = R.getPositiveLiteral(i, k);
            BooleanLiteral x = literals.get(i - 1); //el getPositive... ya te hace el i-1, el get es un get normal
            //como la R empieza en 1 hay que restarle 1, y el get normal no lo hace
            phi.addClause(new DisjunctiveBooleanClause(x.not(), litMat));

        }

        //2 
        for (int j = 2; j <= k; j++) {
            BooleanLiteral litMat = R.getNegativeLiteral(1, j);
            phi.addClause(new DisjunctiveBooleanClause(litMat)); //no se hace el not porque ya se ha 
            //hecho el getNegative

        }

        //3 
        for (int i = 2; i <= n - 1; i++) {
            for (int j = 1; j <= k; j++) {
                BooleanLiteral litMat1 = R.getNegativeLiteral(i - 1, j);
                BooleanLiteral litMat2 = R.getPositiveLiteral(i, j);
                phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2));
            }
        }
        //4 
        for (int i = 2; i <= n - 1; i++) {
            BooleanLiteral l = literals.get(i - 1);//-1 por lo de antes, el get no resta el getNeg si 
            //como solo cambia en cada i, puedes sacarlo al primer bucle
            for (int j = 2; j <= k; j++) {
                BooleanLiteral litMat1 = R.getNegativeLiteral(i - 1, j - 1);
                BooleanLiteral litMat2 = R.getPositiveLiteral(i, j);
                phi.addClause(new DisjunctiveBooleanClause(l.not(), litMat1, litMat2));
            }
        }
        //5
        for (int i = 2; i <= n; i++) {
            BooleanLiteral l = literals.get(i - 1);//-1 por lo de antes, el get no resta el getNeg si 
            BooleanLiteral litMat1 = R.getNegativeLiteral(i - 1, k);
            phi.addClause(new DisjunctiveBooleanClause(l.not(), litMat1));
        }
    }

    private BooleanVariable _atMostR(final int k, final List<BooleanLiteral> literals, final CNFBooleanFormula phi) {
        final int n = literals.size();
        List<BooleanLiteral> aux = new ArrayList<>();

        final RMatrix R = new RMatrix(n - 1, k, phi);

        // Codificación del artículo
        //1
        for (int i = 1; i < n; i++) {
            BooleanLiteral litMat = R.getPositiveLiteral(i, k);
            BooleanLiteral x = literals.get(i - 1); //el getPositive... ya te hace el i-1, el get es un get normal
            //como la R empieza en 1 hay que restarle 1, y el get normal no lo hace
            //phi.addClause(new DisjunctiveBooleanClause(x.not(), litMat));
            aux.add(OR(phi, x.not(), litMat).getPositiveLiteral());

        }

        //2 
        for (int j = 2; j <= k; j++) {
            BooleanLiteral litMat = R.getNegativeLiteral(1, j);
            //phi.addClause(new DisjunctiveBooleanClause(litMat)); //no se hace el not porque ya se ha 
            //hecho el getNegative
            List<BooleanLiteral> listaAux = new ArrayList<>();
            listaAux.add(litMat);
            aux.add(OR(phi, listaAux).getPositiveLiteral());

        }

        //3 
        for (int i = 2; i <= n - 1; i++) {
            for (int j = 1; j <= k; j++) {
                BooleanLiteral litMat1 = R.getNegativeLiteral(i - 1, j);
                BooleanLiteral litMat2 = R.getPositiveLiteral(i, j);
                //phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2));
                aux.add(OR(phi, litMat1, litMat2).getPositiveLiteral());
            }
        }
        //4 
        for (int i = 2; i <= n - 1; i++) {
            BooleanLiteral l = literals.get(i - 1);//-1 por lo de antes, el get no resta el getNeg si 
            //como solo cambia en cada i, puedes sacarlo al primer bucle
            for (int j = 2; j <= k; j++) {
                BooleanLiteral litMat1 = R.getNegativeLiteral(i - 1, j - 1);
                BooleanLiteral litMat2 = R.getPositiveLiteral(i, j);
                //phi.addClause(new DisjunctiveBooleanClause(l.not(), litMat1, litMat2));
                List<BooleanLiteral> listaAux = new ArrayList<>();
                listaAux.add(l.not());
                listaAux.add(litMat1);
                listaAux.add(litMat2);
                aux.add(OR(phi, listaAux).getPositiveLiteral());
            }
        }
        //5
        for (int i = 2; i <= n; i++) {
            BooleanLiteral l = literals.get(i - 1);//-1 por lo de antes, el get no resta el getNeg si 
            BooleanLiteral litMat1 = R.getNegativeLiteral(i - 1, k);
            //phi.addClause(new DisjunctiveBooleanClause(l.not(), litMat1));
            aux.add(OR(phi, litMat1, l.not()).getPositiveLiteral());
        }

        BooleanVariable s = AND(phi, aux);
        return s;
    }

    @Override
    public BooleanVariable atMostR(int k, List<BooleanLiteral> literals, CNFBooleanFormula phi) {
        int n = literals.size();
        BooleanVariable s = phi.newVariable(" ");
        if ((k < 0) || (k > n)) {
            throw new IllegalArgumentException("Error en el valor de la n");
        }
        if (n > 0) {
            if (k == 0) {
                List<BooleanLiteral> negated = new ArrayList<>();
                for (BooleanLiteral l : literals) {
                    negated.add(l.not());
                }
                s = atLeastR(n, negated, phi);
            }
            else if (k == 1) {
                s = atMost1R(literals, phi);
            }
            else if ((k > 1) && (k < (n - 1))) {
                if (((n - 1) * k) <= ((n - k) * k)) {
                    s = _atMostR(k, literals, phi);
                }
                else {
                    List<BooleanLiteral> negated = new ArrayList<>();
                    for (BooleanLiteral l : literals) {
                        negated.add(l.not());
                    }
                    s = _atLeastR(n - k, negated, phi);
                }
            }
            else if (k == (n - 1)) {
                List<BooleanLiteral> negated = new ArrayList<>();
                for (BooleanLiteral l : literals) {
                    negated.add(l.not());
                }
                s = atLeast1R(negated, phi);
            }
        }
        return s;
    }

    @Override
    public BooleanVariable atMost1R(List<BooleanLiteral> literals, CNFBooleanFormula phi) {

        final int n = literals.size();
        List<BooleanLiteral> aux = new ArrayList<>();

        if (n > 0) {
            if (n <= 1) {
                BooleanVariable s = phi.newVariable("atMost1R_trivial_true");
                phi.addClause(new DisjunctiveBooleanClause(s.getPositiveLiteral()));
                return s;
            }
            final RMatrix R = new RMatrix(n - 1, 1, phi);

            // Condiciones simplificadas del artículo; en
            // concreto, desaparecen las cláusulas (2) y (4).
            //1
            for (int i = 1; i < n; i++) {
                BooleanLiteral litMat = R.getPositiveLiteral(i, 1);
                BooleanLiteral x = literals.get(i - 1);//-1 explicado en atMost
                //phi.addClause(new DisjunctiveBooleanClause(x.not(), litMat));
                aux.add(OR(phi, x.not(), litMat).getPositiveLiteral());

            }
            //3 
            for (int i = 2; i <= n - 1; i++) { //desaparece J porque j = k = 1
                BooleanLiteral litMat1 = R.getNegativeLiteral(i - 1, 1);
                BooleanLiteral litMat2 = R.getPositiveLiteral(i, 1);
                //phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2));
                aux.add(OR(phi, litMat1, litMat2).getPositiveLiteral());

            }
            //5
            for (int i = 2; i <= n; i++) {
                BooleanLiteral x = literals.get(i - 1);
                BooleanLiteral litMat1 = R.getNegativeLiteral(i - 1, 1);
                //phi.addClause(new DisjunctiveBooleanClause(x.not(), litMat1));
                aux.add(OR(phi, x.not(), litMat1).getPositiveLiteral());
            }
        }
        return AND(phi, aux);
    }

    @Override
    public BooleanVariable atLeastR(int k, List<BooleanLiteral> literals,
            CNFBooleanFormula phi) {
        int n = literals.size();
        BooleanVariable s = phi.newVariable(" ");
        if (n > 0) {
            if (k == 1) {
                s = atLeast1R(literals, phi);
            }
            else if ((k > 1) && (k < n)) {
                if (((n - k) * k) <= ((n - 1) * k)) {
                    s = _atLeastR(k, literals, phi);
                }
                else {
                    List<BooleanLiteral> negated = new ArrayList<>();
                    for (BooleanLiteral l : literals) {
                        negated.add(l.not());
                    }
                    s = _atMostR(n - k, negated, phi);
                }
            }
            else if (k == n) {
                s = AND(phi, literals);
            }
        }
        return s;
    }

    @Override
    public BooleanVariable atLeast1R(List<BooleanLiteral> literals, CNFBooleanFormula phi) {
        return OR(phi, literals);

    }

    static private final class RMatrix {

        final BooleanVariable[][] pool;
        final int n;
        final int k;

        RMatrix(final int n, final int k, final CNFBooleanFormula phi) {

            this.n = n;
            this.k = k;
            pool = new BooleanVariable[n][k];

            int index = 0;
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < k; ++j) {

                    ++index;
                    final String name = Integer.toString(index);
                    pool[i][j] = phi.newVariable(name);

                }
            }

        }

        BooleanVariable setVariable(final int i, final int j, final BooleanVariable v) {
            if ((i < 1) || (n < i) || (j < 1) || (k < j)) {
                throw new IllegalArgumentException();
            }
            return pool[i - 1][j - 1] = v;
        }

        BooleanVariable getVariable(final int i, final int j) {
            if ((i < 1) || (n < i) || (j < 1) || (k < j)) {
                throw new IllegalArgumentException();
            }
            return pool[i - 1][j - 1];
        }

        BooleanLiteral getPositiveLiteral(final int i, final int j) {
            if ((i < 1) || (n < i) || (j < 1) || (k < j)) {
                throw new IllegalArgumentException();
            }
            return pool[i - 1][j - 1].getPositiveLiteral();
        }

        BooleanLiteral getNegativeLiteral(final int i, final int j) {
            if ((i < 1) || (n < i) || (j < 1) || (k < j)) {
                throw new IllegalArgumentException();
            }
            return pool[i - 1][j - 1].getNegativeLiteral();
        }

    }

}
