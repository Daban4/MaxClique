package main;
/**
 *
 * @author MAZ
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Set;
import java.util.zip.ZipInputStream;
//
import maxclq.BruteForceMaxCliqueSolver;
import maxclq.MaxCliqueSolver;
import ugraph.Node;
import ugraph.UndirectedGraph;
//
public class MaxCLIQUEMain {

  public static void main (final String[] args) {
 
    if (args.length < 1) {
      System.err.println("Uso: <graph: file> [complement: boolean]");
      return;
    }

    final String graphFileName  = args[0];
    final boolean complement = (args.length > 1) ? Boolean.parseBoolean(args[1]) : false;    

    final FileSystem fileSystem = FileSystems.getDefault();
    final File graphFile = fileSystem.getPath("data", "graphs", graphFileName).toFile();

    try (final InputStream  is = getInputStream(graphFile)) {
      
      final UndirectedGraph G = complement ? new UndirectedGraph(is).complement()
                                           : new UndirectedGraph(is);
      
      System.out.printf("Construido el grafo del fichero %s\n", graphFile.getName());
      System.out.printf(" Nodos:    %d\n", G.nodes());
      System.out.printf(" Aristas:  %d\n", G.edges());
      System.out.printf(" Densidad: %4.3f\n", G.density());

      final MaxCliqueSolver solver = new BruteForceMaxCliqueSolver();
      final long t0 = System.nanoTime();
      final Set<Node> clique = solver.solve(G);
      final long t1 = System.nanoTime();
      System.out.printf("Tiempo de resolución (ms): %f\n", (t1 - t0) * 1.0E-6);
      
      if (!clique.isEmpty()) {
        if (solver.verify(G, clique)) {
          System.out.printf("Tamaño del clique encontrado: %d\n", clique.size());
          System.out.println("Nodos que forman el clique: ");
          for (final Node v: clique)
            System.out.println(v.index);
        } else
          System.err.println("Error: conjunto de nodos encontrado no forman un clique");
      } else
        System.err.println("Error: clique vacío");
      
    } catch (final FileNotFoundException ex) {
      System.err.println("Fichero no encontrado");
    } catch (final IOException ex) {
      System.err.println("Problema de I/O");
    }
    
  }
  
  static private InputStream getInputStream (final File inputFile)
          throws FileNotFoundException, IOException {
    
    final String fileName = inputFile.getName();    
    if (fileName.endsWith(".zip")) {
      
      final FileInputStream fis = new FileInputStream(inputFile);
      final ZipInputStream  zis = new ZipInputStream(fis);
      
      if (zis.getNextEntry() != null) {
        return new BufferedInputStream(zis);
      } else {
        return null;
      }
      
    } else {
      return new FileInputStream(inputFile);   
    }
    
  }
  
}
