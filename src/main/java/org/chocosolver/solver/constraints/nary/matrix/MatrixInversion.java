/*
 * syat-choco: a Choco extension for Declarative Statistics
 * 
 * MIT License
 * 
 * Copyright (c) 2016 Roberto Rossi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.chocosolver.solver.constraints.nary.matrix;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.RealVar;

/**
 * Decomposition of the {@code MATRIX} constraint
 * 
 * @author Roberto Rossi
 * @see <a href="https://en.wikipedia.org/wiki/Gaussian_elimination">Gaussian elimination</a>
 */

public class MatrixInversion {
   
   /**
    * MATRIX constraint decomposition; {@code inverse} is the  
    * <a href="https://en.wikipedia.org/wiki/Invertible_matrix">
    * inverse matrix</a> of {@code matrix}  
    * 
    * @param name constraint name
    * @param matrix matrix
    * @param inverse inverse matrix
    */
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
}
