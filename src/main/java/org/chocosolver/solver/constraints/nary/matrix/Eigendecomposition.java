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
      
      for(int j = 0; j < QTransposed[0].length; j++) {
         for(int i = j+1; i < QTransposed[0].length; i++) {
            RealVar[][] evi = new RealVar[1][]; 
            evi[0] = extractColumnAsRow(QTransposed, i);
            RealVar[][] evj = extractColumnAsColumn(QTransposed, j);
            DotProduct.decompose("Orthogonality"+i, evi, evj, new RealVar[][] {{zeroReal}});
         }
      }*/
      
      /***Rayleigh quotient***
      String template = "(";
      for(int i = 0; i < A.length; i++) {
         template += "{"+i+"}*{"+i+"}"+((i < A.length - 1) ? "+" : "");
      }
      template+=")";
      
      for(int k = 0; k < Lambda.length; k++) {
         String statisticString = "(";
         for(int i = 0; i < A.length; i++){
            statisticString += "({"+i+"})*(";
            for(int j = 0; j < A.length; j++){
               statisticString += "({"+j+"})*({"+(A.length + i*A.length+j)+(j == A.length - 1 ? "})" : "})+");
            }
            statisticString += (i == A.length - 1) ? "))/"+template+"={"+(A.length+A.length*A.length)+"}" : ")+";
         }

         RealVar[] lambdaVars = new RealVar[A.length + A.length*A.length + 1];
         System.arraycopy(Q[k], 0, lambdaVars, 0, A.length);
         System.arraycopy(flatten(A), 0, lambdaVars, A.length, A.length*A.length);
         lambdaVars[lambdaVars.length - 1] = Lambda[k][k];

         solver.post(new RealConstraint(name+"Lambda_"+k, statisticString, Ibex.HC4_NEWTON, lambdaVars));
      }*/
      
   }
   
   private static RealVar[] extractColumnAsRow(RealVar[][] matrix, int j) {
      RealVar[] column = new RealVar[matrix.length];
      for(int i = 0; i < matrix.length; i++) {
         column[i] = matrix[i][j];
      }
      return column;
   }
   
   @SuppressWarnings("unused")
   private static RealVar[][] extractColumnAsColumn(RealVar[][] matrix, int j) {
      RealVar[][] column = new RealVar[matrix.length][1];
      for(int i = 0; i < matrix.length; i++) {
         column[i][0] = matrix[i][j];
      }
      return column;
   }
   
   @SuppressWarnings("unused")
   private static RealVar[] flatten(RealVar[][] matrix){
      int n = matrix.length;
      RealVar[] array = new RealVar[n*n];
      for(int i = 0; i < n; i++){
         for(int j = 0; j < n; j++){
            array[i*n+j] = matrix[i][j];
         }
      }
      return array;
   }
}
