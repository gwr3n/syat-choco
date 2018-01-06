package org.chocosolver.solver.constraints.nary.matrix;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.RealVar;;

/**
 * This class implements a matrix eigendecomposition constraint.
 * 
 * The computation is cumbersome and the decomposition only works for trivial matrixes.
 * 
 * @author Roberto Rossi
 *
 */
public class Eigendecomposition {
   
   /**
    * Eigendecomposition of symmetric matrix A.
    * 
    * As a special case, for every NxN real symmetric matrix, 
    * the eigenvalues are real and the eigenvectors can be 
    * chosen such that they are orthogonal to each other. 
    * Thus a real symmetric matrix A can be decomposed as
    * 
    * A=QTransposed.Lambda.Q
    * 
    * @param name constraint name
    * @param A original matrix
    * @param QTransposed matrix Q transposed
    * @param Lambda diagonal matrix of eigenvalues
    * @param Q eigenvector matrix
    * @param zeroReal a real constant representing zero
    * @param solver the solver
    */
   public static void decompose(String name, RealVar[][] A, RealVar[][] QTransposed, RealVar[][] Lambda, RealVar[][] Q, RealVar zeroReal, Solver solver) {
      
      int n = Q.length;
      String rhsTemplate[] = new String[Lambda.length];
      for(int j = 0; j < Lambda.length; j++) {
         rhsTemplate[j] = "";
         for(int i = 0; i < Q.length; i++) {
            if(i == j)
               rhsTemplate[j] += "{"+((j+1)*n+i)+"}*{"+i+"}";
            //rhsTemplate[j] += "{"+((j+1)*n+i)+"}*{"+i+"}"+((i < n - 1) ? "+" : "");
         }
      }
      
      int m = Lambda.length;
      String lhsTemplate = "";
      for(int j = 0; j < Lambda.length; j++) {
         lhsTemplate += "{"+(j+n+Lambda.length*Lambda[0].length)+"}*("+rhsTemplate[j]+")"+((j < m - 1) ? "+" : "");
      }
      
      for(int i = 0; i < QTransposed.length; i++) {
         for(int j = 0; j < Q[i].length; j++) {
            RealVar[] variables = new RealVar[n+m*n+m+1];
            System.arraycopy(extractColumnAsRow(Q,j), 0, variables, 0, Q.length);
            for(int k = 0; k < Lambda.length; k++)
               System.arraycopy(Lambda[k], 0, variables, Q.length+Lambda[k].length*k, Lambda[k].length);
            System.arraycopy(QTransposed[i], 0, variables, Q.length+Lambda[0].length*Lambda.length, QTransposed[i].length);
            variables[n+m*n+m] = A[i][j];
            solver.post(new RealConstraint(name, lhsTemplate+"={"+(n+m*n+m)+"}", Ibex.HC4_NEWTON, variables));
         }
      }
      
      /***TRACE****/
      RealVar[] traceVariables = new RealVar[A.length + Lambda.length];
      
      String traceA = "";
      for(int i = 0; i < A.length; i++) {
         traceA += "{"+i+"}" + ((i < A.length - 1) ? "+" : "");
         traceVariables[i] = A[i][i];
      }
      
      String traceLambda = "";
      for(int i = 0; i < Lambda.length; i++) {
         traceLambda += "{"+(i+A.length)+"}" + ((i < Lambda.length - 1) ? "+" : "");
         traceVariables[A.length + i] = Lambda[i][i];
      }      
            
      solver.post(new RealConstraint("Trace", traceA+"="+traceLambda, Ibex.HC4_NEWTON, traceVariables));
      
      /***ORTHOGONALITY****
      for(int j = 0; j < Q[0].length; j++) {
         for(int i = j+1; i < Q[0].length; i++) {
            RealVar[][] evi = new RealVar[1][]; 
            evi[0] = extractColumnAsRow(Q, i);
            RealVar[][] evj = extractColumnAsColumn(Q, j);
            DotProduct.decompose("Orthogonality"+i, evi, evj, new RealVar[][] {{zeroReal}});
         }
      }
      
      for(int j = 0; j < InverseQ[0].length; j++) {
         for(int i = j+1; i < InverseQ[0].length; i++) {
            RealVar[][] evi = new RealVar[1][]; 
            evi[0] = extractColumnAsRow(InverseQ, i);
            RealVar[][] evj = extractColumnAsColumn(InverseQ, j);
            DotProduct.decompose("Orthogonality"+i, evi, evj, new RealVar[][] {{zeroReal}});
         }
      }*/
   }
   
   private static RealVar[] extractColumnAsRow(RealVar[][] matrix, int j) {
      RealVar[] column = new RealVar[matrix.length];
      for(int i = 0; i < matrix.length; i++) {
         column[i] = matrix[i][j];
      }
      return column;
   }
   
   private static RealVar[][] extractColumnAsColumn(RealVar[][] matrix, int j) {
      RealVar[][] column = new RealVar[matrix.length][1];
      for(int i = 0; i < matrix.length; i++) {
         column[i][0] = matrix[i][j];
      }
      return column;
   }
}
