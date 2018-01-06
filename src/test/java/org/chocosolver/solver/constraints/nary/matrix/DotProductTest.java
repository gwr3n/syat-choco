package org.chocosolver.solver.constraints.nary.matrix;

import static org.junit.Assert.*;

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

public class DotProductTest {

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
      double[][] A = {
            {1, 0.5, 0.6},
            {0.5, 1, 0.2},
            {0.6, 0.2, 1}
            };
      double[][] B = {
            {2, 0.1, 0.5},
            {0.1, 3, 0.2},
            {0.5, 0.2, 2}
            };
      
      double[][] C = {{2.35, 1.72, 1.8}, {1.2, 3.09, 0.85}, {1.72, 0.86, 2.34}};
      
      DotProductReal dotProductTest = new DotProductReal(A, B, C);
      dotProductTest.execute(str);
   }
   
   class DotProductReal extends AbstractProblem {
      public RealVar[][] matrixA;
      public RealVar[][] matrixB;
      public RealVar[][] matrixC;
      
      double[][] A;
      double[][] B;
      double[][] C;
      
      double precision = 1.e-4;
      
      public DotProductReal(double[][] A, double B[][], double[][] C) {
         this.A = A;
         this.B = B;
         this.C = C;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("DotProduct");
      }
      
      @Override
      public void buildModel() {
         matrixA = new RealVar[this.A.length][this.A[0].length];
         for(int i = 0; i < matrixA.length; i++){
            for(int j = 0; j < matrixA[i].length; j++){
               matrixA[i][j] = VariableFactory.real("MatrixA_"+(i+1)+"_"+(j+1), this.A[i][j], this.A[i][j], precision, solver);
            }
         }
         
         matrixB = new RealVar[this.B.length][this.B[0].length];
         for(int i = 0; i < matrixB.length; i++){
            for(int j = 0; j < matrixB[i].length; j++){
               matrixB[i][j] = VariableFactory.real("MatrixB_"+(i+1)+"_"+(j+1), this.B[i][j], this.B[i][j], precision, solver);
            }
         }
         
         matrixC = new RealVar[this.A.length][this.B[0].length];
         for(int i = 0; i < matrixC.length; i++){
            for(int j = 0; j < matrixC[i].length; j++){
               matrixC[i][j] = VariableFactory.real("MatrixC_"+(i+1)+"_"+(j+1), VariableFactory.MIN_INT_BOUND, VariableFactory.MAX_INT_BOUND, precision, solver);
            }
         }
         
         DotProduct.decompose("DotProduct", matrixA, matrixB, matrixC);
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
         RealStrategy strat = new RealStrategy(flatten(matrixC), new Cyclic(), new RealDomainMiddle());
         solver.set(strat);
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < flatten(matrixC).length; i++){
                 st.append("("+flatten(matrixC)[i].getLB()+","+flatten(matrixC)[i].getUB()+"), ");
                 assertEquals(flatten(matrixC)[i].getLB(), C[i/matrixC[0].length][i%matrixC.length], precision);
              }
              st.append("\n");
           }else{
              st.append("No solution!");
              fail("No solution!");
           }
        //}while(solution = solver.nextSolution());
        System.out.println(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
}
