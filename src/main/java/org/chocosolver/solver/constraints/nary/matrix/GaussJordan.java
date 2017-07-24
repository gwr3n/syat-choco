package org.chocosolver.solver.constraints.nary.matrix;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.RealVar;

public class GaussJordan {
   
   public static void decompose(String name, RealVar[][] matrix, RealVar[][] inverse){
      Solver solver = matrix[0][0].getSolver();
      int n = matrix.length;
      
      String[][] matrixString = initialiseMatrixString(n);
      String[][] inverseString = initialiseInverseMatrixString(n);
      
      gaussJordan(matrixString, inverseString);
      
      for(int i = 0; i < n; i++){
         for(int j = 0; j < n; j++){
            solver.post(new RealConstraint(name, inverseString[i][j]+"={"+(n*n)+"}", Ibex.HC4_NEWTON, flatten(matrix,inverse[i][j])));
         }
      }
      System.out.println();
   }
   
   private static String[][] initialiseMatrixString(int n){
      String[][] matrixString = new String[n][n];
      for(int i = 0; i < n; i++){
         for(int j = 0; j < n; j++){
            matrixString[i][j] = "{"+convertIndexMatrix(i,j,n)+"}";
         }
      }
      return matrixString;
   }
   
   private static String[][] initialiseInverseMatrixString(int n){
      String[][] inverseString = new String[n][n];
      for(int i = 0; i < n; i++){
         for(int j = 0; j < n; j++){
            inverseString[i][j] = i == j ? "1" : "0";
         }
      }
      return inverseString;
   }
   
   private static RealVar[] flatten(RealVar[][] matrix, RealVar out){
      int n = matrix.length;
      RealVar[] array = new RealVar[n*n+1];
      for(int i = 0; i < n; i++){
         for(int j = 0; j < n; j++){
            array[i*n+j] = matrix[i][j];
         }
      }
      array[n*n]=out;
      return array;
   }
   
   private static void gaussJordan(String[][] matrixString, String[][] inverseString){
      int n = matrixString.length;
      
      for(int i = 0; i < n; i++){
         gaussJordanForwardStep(i, matrixString, inverseString);
      }
      
      for(int i = n-1; i >= 0; i--){
         gaussJordanBackwardStep(i, matrixString, inverseString);
      }
   }

   private static void gaussJordanBackwardStep(int i, String[][] matrixString, String[][] inverseString) {
      int n = matrixString.length;
      
      for(int k = i - 1; k >= 0; k--){
         String multiplier = "("+matrixString[k][i]+")"; 
         for(int j = n - 1; j >= i; j--){
            matrixString[k][j] = "("+matrixString[k][j] +"-"+ matrixString[i][j]+"*"+multiplier+")"; 
         }
         for(int j = n - 1; j >= 0; j--){
            inverseString[k][j] = "("+inverseString[k][j] +"-"+ inverseString[i][j]+"*"+multiplier+")"; 
         }
      }
   }

   private static void gaussJordanForwardStep(int i, String[][] matrixString, String[][] inverseString) {
      int n = matrixString.length;
      
      String pivot = "("+matrixString[i][i]+")";
      for(int j = i; j < n; j++){
         matrixString[i][j] = "("+matrixString[i][j]+"/"+pivot+")"; 
      }
      for(int j = 0; j < n; j++){
         inverseString[i][j] = "("+inverseString[i][j]+"/"+pivot+")";
      }
      
      for(int k = i + 1; k < n; k++){
         String multiplier = "("+matrixString[k][i]+")"; 
         for(int j = i; j < n; j++){
            matrixString[k][j] = "("+matrixString[k][j] +"-"+ matrixString[i][j]+"*"+multiplier+")"; 
         }
         for(int j = 0; j < n; j++){
            inverseString[k][j] = "("+inverseString[k][j] +"-"+ inverseString[i][j]+"*"+multiplier+")"; 
         }
      }
   }

   private static int convertIndexMatrix(int i, int j, int n){
      return n*i + j;
   }
   
   /*private static int convertIndexInverseMatrix(int i, int j, int n){
      return (int) Math.pow(n, 2) + n*i + j;
   }*/
}
