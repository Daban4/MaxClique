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
  
  static void atLeast (final int k,
                       final List<Literal> literals,
                       final CpModel model) {

    final int n = literals.size();
    
    //
    // TODO
    //
    
  }  
  
  static void atMost (final int k,
                      final List<Literal> literals,
                      final CpModel model) {
    
    final int n = literals.size();
    
    //
    // TODO
    //
    
  }
  
  static void _atMost (final int k,
                       final List<Literal> literals,
                       final CpModel model) {
    
    final int n = literals.size();
    final BoolVarMatrix R = new BoolVarMatrix(n - 1, k, model);
    
    //
    // TODO
    //
    
  }
  
  static void _atLeast (final int k,
                        final List<Literal> literals,
                        final CpModel model) {
    
    final int n = literals.size();
    final BoolVarMatrix R = new BoolVarMatrix(n, k, model);
    
    //
    // TODO
    //
    
  }
  
}
