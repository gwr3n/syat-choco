package org.chocosolver.solver.constraints.nary.contingency;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.RealStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContingencyDecompositionsTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      System.gc();
      Thread.sleep(1000);
   }

   @Test
   public void testInteger() {
      String[] str={"-log","SOLUTION"};
      
      int[][] valuesA = {{3},{3},{1},{1},{2}};
      int[][] valuesB = {{1},{1},{2},{2},{3}};
      int[] binCounts = {0,valuesA.length};
      int[][] binBounds = {{1,2,3,4},{1,2,3,4}};
      
      ContingencyDecompositionsInteger cd = new ContingencyDecompositionsInteger(valuesA, valuesB, binCounts, binBounds);
      cd.execute(str);
   }
   
   @Test
   public void testReal() {
      String[] str={"-log","SOLUTION"};
      
      double[][] valuesA = {{3},{3},{1},{1},{2}};
      double[][] valuesB = {{1},{1},{2},{2},{3}};
      int[] binCounts = {0,valuesA.length};
      double[][] binBounds = {{0.5,1.5,2.5,3.5},{0.5,1.5,2.5,3.5}};
      
      ContingencyDecompositionsReal cd = new ContingencyDecompositionsReal(valuesA, valuesB, binCounts, binBounds);
      cd.execute(str);
   }

   class ContingencyDecompositionsInteger extends AbstractProblem {
      public IntVar[] seriesA;
      public IntVar[] seriesB;
      public IntVar[][] binVariables;
      IntVar[] marginalsH; 
      IntVar[] marginalsV; 
      
      int[][] valuesA;
      int[][] valuesB;
      int[] binCounts;
      int[][] binBounds;
      
      public ContingencyDecompositionsInteger(int[][] valuesA,
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
          solver = new Solver("ContingencyInteger");
      }
      
      @Override
      public void buildModel() {
         seriesA = new IntVar[this.valuesA.length];
         for(int i = 0; i < this.valuesA.length; i++)
            seriesA[i] = VariableFactory.enumerated("Value A"+(i+1), valuesA[i], solver);
         
         seriesB = new IntVar[this.valuesB.length];
         for(int i = 0; i < this.valuesB.length; i++)
            seriesB[i] = VariableFactory.enumerated("Value B"+(i+1), valuesB[i], solver);
         
         int observations = seriesA.length;

         binVariables = new IntVar[binBounds[0].length - 1][binBounds[1].length - 1];
         for(int i = 0; i < binVariables.length; i++){
            for(int j = 0; j < binVariables[0].length; j++){
               binVariables[i][j] = VariableFactory.bounded("CD_Bin "+(i+1)+","+(j+1), 0, observations, solver);
            }
         }
         
         marginalsH = VariableFactory.boundedArray("CD_Marginals H", binVariables.length, 0, observations, solver);
         
         marginalsV = VariableFactory.boundedArray("CD_Marginals V", binVariables[0].length, 0, observations, solver);
         
         ContingencyDecompositions.decompose(seriesA, seriesB, binVariables, binBounds, marginalsH, marginalsV);
      }
      
      @Override
      public void configureSearch() {
         IntVar[] flattenedBins = new IntVar[binVariables.length*binVariables[0].length];
         for(int i = 0; i < binVariables.length; i++){
            for(int j = 0; j < binVariables[0].length; j++){
               flattenedBins[binVariables[0].length*i + j] = binVariables[i][j]; 
            }
         }
         
         solver.set(
               IntStrategyFactory.activity(seriesA,1234),
               IntStrategyFactory.activity(seriesB,1234),
               IntStrategyFactory.activity(flattenedBins,1234),
               IntStrategyFactory.activity(marginalsH,1234),
               IntStrategyFactory.activity(marginalsV,1234)
               );
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              
              assertTrue(binVariables[2][0].getValue()==2);
              assertTrue(binVariables[0][1].getValue()==2);
              assertTrue(binVariables[1][2].getValue()==1);
              
           }else{
              st.append("No solution!");
              assertTrue(false);
           }
        //}while(solution = solver.nextSolution());
        System.out.println(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
   
   class ContingencyDecompositionsReal extends AbstractProblem {
      public RealVar[] seriesA;
      public RealVar[] seriesB;
      public IntVar[][] binVariables;
      IntVar[] marginalsH; 
      IntVar[] marginalsV; 
      
      double[][] valuesA;
      double[][] valuesB;
      int[] binCounts;
      double[][] binBounds;
      
      double precision = 1.e-4;
      
      public ContingencyDecompositionsReal(double[][] valuesA,
                                           double[][] valuesB, 
                                           int[] binCounts,
                                           double[][] binBounds){
         this.valuesA = valuesA.clone();
         this.valuesB = valuesB.clone();
         this.binCounts = binCounts.clone();
         this.binBounds = binBounds.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ContingencyReal");
      }
      
      @Override
      public void buildModel() {
         seriesA = new RealVar[this.valuesA.length];
         for(int i = 0; i < this.valuesA.length; i++)
            seriesA[i] = VariableFactory.real("Value A"+(i+1), valuesA[i][0], valuesA[i][0], precision, solver);
         
         seriesB = new RealVar[this.valuesB.length];
         for(int i = 0; i < this.valuesB.length; i++)
            seriesB[i] = VariableFactory.real("Value B"+(i+1), valuesB[i][0], valuesB[i][0], precision, solver);
         
         int observations = seriesA.length;

         binVariables = new IntVar[binBounds[0].length - 1][binBounds[1].length - 1];
         for(int i = 0; i < binVariables.length; i++){
            for(int j = 0; j < binVariables[0].length; j++){
               binVariables[i][j] = VariableFactory.bounded("CD_Bin "+(i+1)+","+(j+1), 0, observations, solver);
            }
         }
         
         marginalsH = VariableFactory.boundedArray("CD_Marginals H", binVariables.length, 0, observations, solver);
         
         marginalsV = VariableFactory.boundedArray("CD_Marginals V", binVariables[0].length, 0, observations, solver);
         
         ContingencyDecompositions.decompose(seriesA, seriesB, binVariables, binBounds, marginalsH, marginalsV);
      }
      
      @Override
      public void configureSearch() {
         IntVar[] flattenedBins = new IntVar[binVariables.length*binVariables[0].length];
         for(int i = 0; i < binVariables.length; i++){
            for(int j = 0; j < binVariables[0].length; j++){
               flattenedBins[binVariables[0].length*i + j] = binVariables[i][j]; 
            }
         }
         
         solver.set(
               RealStrategyFactory.cyclic_middle(seriesA),
               RealStrategyFactory.cyclic_middle(seriesB),
               IntStrategyFactory.activity(flattenedBins,1234),
               IntStrategyFactory.activity(marginalsH,1234),
               IntStrategyFactory.activity(marginalsV,1234)
               );
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              
              assertTrue(binVariables[2][0].getValue()==2);
              assertTrue(binVariables[0][1].getValue()==2);
              assertTrue(binVariables[1][2].getValue()==1);
              
           }else{
              st.append("No solution!");
              assertTrue(false);
           }
        //}while(solution = solver.nextSolution());
        System.out.println(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
}
