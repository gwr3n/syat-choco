package org.chocosolver.solver.constraints.nary.matrix;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.RealVar;

public class DotProduct {
   
   /**
    * C = A.B
    * 
    * @param name
    * @param A a matrix A
    * @param B a matrix B
    * @param C a matrix C such that C = A.B
    */
   public static void decompose(String name, RealVar[][] A, RealVar[][] B, RealVar[][] C){
      /**
       * Checks
       */
      
      if(A[0].length != B.length)
         throw new SolverException("Matrix A columns should be equal to matrix B rows");
      
      if(A.length != C.length)
         throw new SolverException("Matrix A rows should be equal to matrix C rows");
      
      if(B[0].length != C[0].length)
         throw new SolverException("Matrix B columns should be equal to matrix C columns");
      
      Solver solver = A[0][0].getSolver();
      
      int n = B.length;
      String template = "";
      for(int i = 0; i < n; i++) {
         template += "{"+i+"}*{"+(n+i)+"}"+((i < n - 1) ? "+" : "");
      }
      
      for(int i = 0; i < C.length; i++) {
         for(int j = 0; j < C[i].length; j++) {
            RealVar[] variables = new RealVar[2*n+1];
            System.arraycopy(A[i], 0, variables, 0, n);
            System.arraycopy(extractColumn(B,j), 0, variables, n, n);
            variables[2*n] = C[i][j];
            solver.post(new RealConstraint(name, template+"={"+(2*n)+"}", Ibex.HC4_NEWTON, variables));
         }
      }
   }
   
   private static RealVar[] extractColumn(RealVar[][] matrix, int j) {
      RealVar[] column = new RealVar[matrix.length];
      for(int i = 0; i < matrix.length; i++) {
         column[i] = matrix[i][j];
      }
      return column;
   }
}
