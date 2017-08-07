package org.syad.statistics.test;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.contingency.ContingencyDecompositions;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class ContingencyTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void test() {
      String[] str={"-log","SOLUTION"};
      int[][] valuesA = {{1},{3},{3}};
      int[][] valuesB = {{2},{1},{2}};
      int[] binCounts = {0,3};
      int[][] binBounds = {{1,3,4},{1,2,4}};
      
      Contingency c = new Contingency(valuesA, valuesB, binCounts, binBounds);
      c.execute(str);
   }
   
   class Contingency extends AbstractProblem {
      public IntVar[] seriesA;
      public IntVar[] seriesB;
      public IntVar[] marginalsH;
      public IntVar[] marginalsV;
      public IntVar[][] binVariables;
      
      int[][] valuesA;
      int[][] valuesB;
      int[] binCounts;
      int[][] binBounds;
      
      public Contingency(int[][] valuesA,
                         int[][] valuesB,
                         int[] binCounts,
                         int[][] binBounds){
         this.valuesA = valuesA.clone();
         this.valuesB = valuesB.clone();
         this.binCounts = binCounts.clone();
         this.binBounds = binBounds.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("Contingency");
      }
      
      @Override
      public void buildModel() {
         seriesA = new IntVar[this.valuesA.length];
         for(int i = 0; i < this.valuesA.length; i++)
            seriesA[i] = VariableFactory.enumerated("Value A "+(i+1), valuesA[i], solver);
         
         seriesB = new IntVar[this.valuesB.length];
         for(int i = 0; i < this.valuesB.length; i++)
            seriesB[i] = VariableFactory.enumerated("Value B "+(i+1), valuesB[i], solver);
         
         binVariables = new IntVar[this.binCounts.length][this.binCounts.length];
         for(int i = 0; i < this.binCounts.length; i++){
            for(int j = 0; j < this.binCounts.length; j++){
               binVariables[i][j] = VariableFactory.bounded("Bin "+(i+1)+","+(j+1), this.binCounts[0], this.binCounts[1], solver);
            }
         }
         
         marginalsH = VariableFactory.boundedArray("Marginals H", binVariables.length, 0, seriesA.length, solver);
         
         marginalsV = VariableFactory.boundedArray("Marginals V", binVariables[0].length, 0, seriesA.length, solver);
         
         ContingencyDecompositions.decompose(seriesA, seriesB, binVariables, binBounds, marginalsH, marginalsV);
      }
      
      public void configureSearch() {
         
      }
      
      @Override
      public void solve() {
        //LoggerFactory.getLogger("bench").info("---");
        //this.prettyOut();
        try {
         solver.propagate();
        } catch (ContradictionException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
        }
        //LoggerFactory.getLogger("bench").info("---");
        //this.prettyOut();
        
        StringBuilder st = new StringBuilder();
        @SuppressWarnings("unused")
      boolean solution = solver.findSolution();
        do{
           st.append("\n---SOLUTION---\n");
           /*if(solution) {
              for(int i = 0; i < valueVariables.length; i++){
                 st.append(valueVariables[i].getValue()+", ");
              }
              st.append("\n");
              for(int i = 0; i < binVariables.length; i++){
                 st.append(binVariables[i].getValue()+", ");
              }
              st.append("\n");
           }else{
              st.append("No solution!");
           }*/
        }while(solution = solver.nextSolution());
        LoggerFactory.getLogger("bench").info(st.toString());
      }

      @Override
      public void prettyOut() {
         // TODO Auto-generated method stub
         
      }
   }

}
