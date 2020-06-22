/*
 * syat-choco: a Choco extension for Declarative Statistics.
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

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MatrixInversionTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      System.gc();
      Thread.sleep(1000);
   }

   @Test
   public void test() {
      String[] str={"-log","SOLUTION"};
      double[][] matrix = new double[][]{
         { 1.0, 0.1, 0.2 },
         { 0.1, 1.0, 0.1 },
         { 0.2, 0.1, 1.0 }
      };
      /*double[][] matrix = new double[][]{
         { 2, 6 },
         { 1, 3 }
      };*/
      /*double[][] matrix = new double[][]{
         { 1.0, 0.1, 0.2 },
         { 0.1, 1.0, 0.1 },
         { 0.2, 0.1, 1.0 }
      };*/
      MatrixInversionReal matrixInversionReal = new MatrixInversionReal(matrix);
      matrixInversionReal.execute(str);
   }

   class MatrixInversionReal extends AbstractProblem {
      public RealVar[][] matrixVariable;
      public RealVar[][] inverseVariable;
      
      double[][] matrix;
      
      double precision = 1.e-4;
      
      public MatrixInversionReal(double[][] matrix){
         this.matrix = matrix;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("MatrixInversion");
      }
      
      @Override
      public void buildModel() {
         int n = this.matrix.length;
         matrixVariable = new RealVar[n][n];
         inverseVariable = new RealVar[n][n];
         for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
               matrixVariable[i][j] = VariableFactory.real("Matrix_"+(i+1)+"_"+(j+1), this.matrix[i][j], this.matrix[i][j], precision, solver);
               inverseVariable[i][j] = VariableFactory.real("InverseMatrix_"+(i+1)+"_"+(j+1), -100, 100, precision, solver);
            }
         }
         
         MatrixInversion.decompose("GaussJordan", matrixVariable, inverseVariable);
      }
      
      public RealVar[] flatten(RealVar[][] matrix){
         int n = matrix.length;
         RealVar[] array = new RealVar[n*n];
         for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
               array[i*n+j] = matrix[i][j];
            }
         }
         return array;
      }
      
      @Override
      public void configureSearch() {
         RealStrategy strat = new RealStrategy(flatten(inverseVariable), new Cyclic(), new RealDomainMiddle());
         solver.set(strat);
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < flatten(inverseVariable).length; i++){
                 st.append("("+flatten(inverseVariable)[i].getLB()+","+flatten(inverseVariable)[i].getUB()+"), ");
              }
              st.append("\n");
           }else{
              st.append("No solution!");
           }
        //}while(solution = solver.nextSolution());
        System.out.println(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
}
