package maxclq;

/**
 *
 * @author MAZ
 */
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.Literal;
import java.util.ArrayList;
import java.util.List;
//

final class SCE {

    static void atLeast(final int k,
            final List<Literal> literals,
            final CpModel model) {

        final int n = literals.size();

        if (k > 0) { // k == 0 es de cumplimiento trivial

            if ((k > n) || (n == 0)) {
                throw new IllegalArgumentException("Restricción insatisfacible");
            }

            if (n > 0) {

                if (k == 1) {

                    model.addBoolOr(literals);

                }
                else if ((k > 1) && (k < n)) {
                    // Al menos k ovejas blancas == a lo sumo n - k ovejas negras
                    if ((n * k) <= ((n - 1) * (n - k))) {
                        _atLeast(k, literals, model);
                    }
                    else {
                        List<Literal> negated = new ArrayList<>();
                        for (Literal l : literals) {
                            negated.add(l.not());
                        }
                        _atMost(n - k, negated, model);
                    }
                }
                else if (k == n) {
                    // Al menos n de n literales es exigir que se cumplan todos.
                    model.addBoolAnd(literals);
                }//mejor hacerlo con un bucle sobre literles en lugar de sobre i
            }

        }

    }

    static void atMost(final int k,
            final List<Literal> literals,
            final CpModel model) {

        final int n = literals.size();

        if (n > 0) { // A lo sumo k de 0 literales es de cumplimiento trivial

            if (k == 0) {

                // A lo sumo 0 ovejas blancas == al menos n ovejas negras
                for (Literal x : literals) {
                    model.addEquality(x, 0);
                }
            }
            else if (k == 1) {

                for (int i = 0; i < n; i++) {
                    for (int j = i + 1; j < n; j++) {
                        model.addBoolOr(new Literal[]{literals.get(i).not(),literals.get(j).not()});
                    }
                }

            }
            else if ((k > 1) && (k < (n - 1))) {
                if (((n - 1) * k) < (n * (n - k))) {
                    _atMost(k, literals, model);
                }
                else {
                    List<Literal> negated = new ArrayList<>();
                    for (Literal l : literals) {
                        negated.add(l.not());
                    }
                    _atLeast(n - k, negated, model);
                }
            }
            else if (k == (n - 1)) {

                // A lo sumo n - 1 ovejas blancas == al menos 1 oveja negra
                List<Literal> negated = new ArrayList<>();
                for (Literal l : literals) {
                    negated.add(l.not());
                }
                //phi.addClause(new DisjunctiveBooleanClause(negated));
                model.addBoolOr(negated);
            }
            else if (k >= n) {

                // Cuando k >= n, se cumple trivialmente a lo sumo k de un total de n.
                // Se queda vacío.
            }

        }

    }

    static void _atMost(final int k,
            final List<Literal> literals,
            final CpModel model) {

        final int n = literals.size();
        final BoolVarMatrix R = new BoolVarMatrix(n - 1, k, model);
        // Codificación del artículo
        //1
        for (int i = 1; i < n; i++) {
            Literal litMat = R.getPositiveLiteral(i, k);
            Literal x = literals.get(i - 1); //el getPositive... ya te hace el i-1, el get es un get normal
            //como la R empieza en 1 hay que restarle 1, y el get normal no lo hace
            model.addBoolOr(new Literal[]{x.not(), litMat});
        }

        //2 
        for (int j = 2; j <= k; j++) {
            Literal litMat = R.getNegativeLiteral(1, j);
            model.addBoolOr(new Literal[]{litMat});

        }

        //3 
        for (int i = 2; i <= n - 1; i++) {
            for (int j = 1; j <= k; j++) {
                Literal litMat1 = R.getNegativeLiteral(i - 1, j);
                Literal litMat2 = R.getPositiveLiteral(i, j);
                model.addBoolOr(new Literal[]{litMat1, litMat2});
            }
        }
        //4 
        for (int i = 2; i <= n - 1; i++) {
            Literal l = literals.get(i - 1);//-1 por lo de antes, el get no resta el getNeg si 
            //como solo cambia en cada i, puedes sacarlo al primer bucle
            for (int j = 2; j <= k; j++) {
                Literal litMat1 = R.getNegativeLiteral(i - 1, j - 1);
                Literal litMat2 = R.getPositiveLiteral(i, j);
                model.addBoolOr(new Literal[]{l.not(), litMat1, litMat2});
            }
        }
        //5
        for (int i = 2; i <= n; i++) {
            Literal l = literals.get(i - 1);//-1 por lo de antes, el get no resta el getNeg si 
            Literal litMat1 = R.getNegativeLiteral(i - 1, k);
            model.addBoolOr(new Literal[]{l.not(), litMat1});
        }

    }

    static void _atLeast(final int k,
            final List<Literal> literals,
            final CpModel model) {

        final int n = literals.size();
        final BoolVarMatrix R = new BoolVarMatrix(n, k, model);
        for (int j = 1; j <= k; j++) {
            Literal litMat = R.getPositiveLiteral(n, j);
            //phi.addClause(new DisjunctiveBooleanClause(litMat));
            model.addBoolOr(new Literal[]{litMat});
        }

        for (int i = 2; i <= n; i++) {
            Literal l = literals.get(i - 1);
            for (int j = 2; j <= k; j++) {
                Literal litMat1 = R.getNegativeLiteral(i, j);
                Literal litMat2 = R.getPositiveLiteral(i - 1, j);
                Literal litMat3 = R.getPositiveLiteral(i - 1, j - 1);
                model.addBoolOr(new Literal[]{litMat1, litMat2, l});
                model.addBoolOr(new Literal[]{litMat1, litMat2, litMat3});
                //phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2, l));
                //phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2, litMat3));

            }
        }
        for (int i = 2; i <= n; i++) {
            Literal l = literals.get(i - 1);
            Literal litMat1 = R.getNegativeLiteral(i, 1);
            Literal litMat2 = R.getPositiveLiteral(i - 1, 1);
            //phi.addClause(new DisjunctiveBooleanClause(litMat1, litMat2, l));
            model.addBoolOr(new Literal[]{litMat1, litMat2, l});

        }
        //phi.addClause(new DisjunctiveBooleanClause(R.getNegativeLiteral(1, 1), literals.get(0)));
        model.addBoolOr(new Literal[]{R.getNegativeLiteral(1, 1), literals.get(0)});
        for (int j = 2; j <= k; j++) {
            //phi.addClause(new DisjunctiveBooleanClause(R.getNegativeLiteral(1, j)));
            model.addBoolOr(new Literal[]{R.getNegativeLiteral(1, j)});
        }
        //for (int i = 1; i < k; i++) {
            //phi.addClause(new DisjunctiveBooleanClause(R.getNegativeLiteral(i, k)));
           // model.addBoolOr(new Literal[]{R.getNegativeLiteral(i, k)});
        //}
    }

}
