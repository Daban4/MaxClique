package maxclq;
/**
 *
 * @author MAZ
 */
import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.Literal;
//
final class BoolVarMatrix {
  
  private final BoolVar[][] pool;
  private final int n;
  private final int m;
  
  BoolVarMatrix (final int n, final int m, final CpModel model) {
    
    this.n = n;
    this.m = m;
    this.pool = new BoolVar[n][m];
    
    int index = 0;
    for (int i = 0; i < n; ++i) {
      
      for (int j = 0; j < m; ++j) {
        final String name = Integer.toString(++index);
        this.pool[i][j] = model.newBoolVar(name);  
      }
      
    }
    
  }
  
  BoolVar getVariable (final int i, final int j) {
    if ((i < 1) || (n < i) || (j < 1) || (m < j))
      throw new IllegalArgumentException("índice fuera de rango");
    return pool[i - 1][j - 1];
  }  
    
  Literal getPositiveLiteral (final int i, final int j) {
    if ((i < 1) || (n < i) || (j < 1) || (m < j))
      throw new IllegalArgumentException("índice fuera de rango");
    return pool[i - 1][j - 1];
  }
    
  Literal getNegativeLiteral (final int i, final int j) {
    if ((i < 1) || (n < i) || (j < 1) || (m < j))
      throw new IllegalArgumentException("índice fuera de rango");
    return pool[i - 1][j - 1].not();
  }

}
