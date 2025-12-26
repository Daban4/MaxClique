package ugraph;
/**
 *
 * @author MAZ
 */
public final class Node {
  
  public final int index;
  
  public Node (final int i) {
    this.index = i;
  }
  
  @Override
  public String toString () {
    return index + "";
  }
  
}
