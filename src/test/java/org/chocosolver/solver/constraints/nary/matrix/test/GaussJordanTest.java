package org.chocosolver.solver.constraints.nary.matrix.test;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.matrix.GaussJordan;
import org.chocosolver.solver.constraints.statistical.hotelling.tSquareStatistic;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class GaussJordanTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
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
      GaussJordanReal gjreal = new GaussJordanReal(matrix);
      gjreal.execute(str);
   }

   class GaussJordanReal extends AbstractProblem {
      public RealVar[][] matrixVariable;
      public RealVar[][] inverseVariable;
      
      double[][] matrix;
      
      double precision = 1.e-4;
      
      public GaussJordanReal(double[][] matrix){
         this.matrix = matrix;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("GaussJordan");
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
         
         GaussJordan.decompose("GaussJordan", matrixVariable, inverseVariable);
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
        LoggerFactory.getLogger("bench").info(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
}
