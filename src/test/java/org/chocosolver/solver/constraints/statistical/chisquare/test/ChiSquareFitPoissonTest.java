package org.chocosolver.solver.constraints.statistical.chisquare.test;

import static org.junit.Assert.assertTrue;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitPoisson;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class ChiSquareFitPoissonTest {
   
   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }
   
   /**
    * Run one test at a time. Multiple Solver instances seem to create conflicts.
    */
   
   @Test
   public void testInteger() {
      String[] str={"-log","SOLUTION"};
      
      int[][] values = {{2,2},{1,1},{4,4},{4,4},{1,1},{2,2}};
      int[][] binCounts = {{0,6},{0,6},{0,6},{0,6},{0,6},{0,6},{0,6},{0,6},{0,6},{0,6}};
      int[] binBounds = {0,1,2,3,4,5,6,7,8,9,10};
      double[] poissonRate = {5,5};
      
      double[] chiSqStatistic = {0,50};
      
      ChiSquareFitPoissonInteger cs = new ChiSquareFitPoissonInteger(values, binCounts, binBounds, poissonRate, chiSqStatistic);
      cs.execute(str);
   }
   
   //@Test
   public void testReal() {
      String[] str={"-log","SOLUTION"};
      
      double[][] values = {{2,2},{1,1},{4,4},{4,4},{1,1},{2,2}};
      int[][] binCounts = {{0,6},{0,6},{0,6},{0,6},{0,6},{0,6},{0,6},{0,6},{0,6},{0,6}};
      double[] binBounds = {0,1,2,3,4,5,6,7,8,9,10};
      double[] poissonRate = {5,5};
      
      double[] chiSqStatistic = {0,50};
      
      ChiSquareFitPoissonReal cs = new ChiSquareFitPoissonReal(values, binCounts, binBounds, poissonRate, chiSqStatistic);
      cs.execute(str);
   }
   
   class ChiSquareFitPoissonInteger extends AbstractProblem {
      public IntVar[] valueVariables;
      public IntVar[] binVariables;
      public RealVar poissonRateVariable;
      public RealVar chiSqstatisticVariable;
      
      int[][] binCounts;
      int[][] values;
      int[] binBounds;
      double[] poissonRate;
      double[] chiSqStatistic;
      
      double precision = 1.e-4;
      
      public ChiSquareFitPoissonInteger(int[][] values,
                                        int[][] binCounts, 
                                        int[] binBounds,
                                        double[] poissonRate,
                                        double[] chiSqStatistic){
         this.values = values.clone();
         this.binCounts = binCounts.clone();
         this.binBounds = binBounds.clone();
         this.poissonRate = poissonRate.clone();
         this.chiSqStatistic = chiSqStatistic.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquareInteger");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new IntVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.enumerated("Value "+(i+1), values[i], solver);
         
         binVariables = new IntVar[this.binCounts.length];
         for(int i = 0; i < this.binCounts.length; i++)
            binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
         
         poissonRateVariable = VF.real("Poisson rate", poissonRate[0], poissonRate[1], precision, solver);
         
         chiSqstatisticVariable = VF.real("chiSqStatistics", chiSqStatistic[0], chiSqStatistic[1], precision, solver);
         
         ChiSquareFitPoisson.decomposition("chiSqConstraint", valueVariables, binVariables, binBounds, poissonRateVariable, chiSqstatisticVariable, precision);
      }
      
      @Override
      public void configureSearch() {
         solver.set(
               IntStrategyFactory.activity(valueVariables,1234),
               IntStrategyFactory.activity(binVariables,1234),
               new RealStrategy(new RealVar[]{poissonRateVariable}, new Cyclic(), new RealDomainMiddle()),
               new RealStrategy(new RealVar[]{chiSqstatisticVariable}, new Cyclic(), new RealDomainMiddle())
               );
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariables.length; i++){
                 st.append("("+valueVariables[i].getLB()+", "+valueVariables[i].getUB()+"), ");
              }
              st.append("\n");
              for(int i = 0; i < binVariables.length; i++){
                 st.append(binVariables[i].getValue()+", ");
              }
              st.append("\n");
              st.append(poissonRateVariable.getLB()+" "+poissonRateVariable.getUB());
              st.append("\n");
              st.append(chiSqstatisticVariable.getLB()+" "+chiSqstatisticVariable.getUB());
              st.append("\n");
              
              assertTrue(chiSqstatisticVariable.getLB() <= 24);
              assertTrue(chiSqstatisticVariable.getUB() >= 23);
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
   
   class ChiSquareFitPoissonReal extends AbstractProblem {
      public RealVar[] valueVariables;
      public IntVar[] binVariables;
      public RealVar poissonRateVariable;
      public RealVar chiSqstatisticVariable;
      
      int[][] binCounts;
      double[][] values;
      double[] binBounds;
      double[] poissonRate;
      double[] chiSqStatistic;
      
      double precision = 1.e-4;
      
      public ChiSquareFitPoissonReal(double[][] values,
                                        int[][] binCounts, 
                                        double[] binBounds,
                                        double[] poissonRate,
                                        double[] chiSqStatistic){
         this.values = values.clone();
         this.binCounts = binCounts.clone();
         this.binBounds = binBounds.clone();
         this.poissonRate = poissonRate.clone();
         this.chiSqStatistic = chiSqStatistic.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquareReal");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new RealVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.real("Value "+(i+1), values[i][0], values[i][1], precision, solver);
         
         binVariables = new IntVar[this.binCounts.length];
         for(int i = 0; i < this.binCounts.length; i++)
            binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
         
         poissonRateVariable = VF.real("Poisson rate", poissonRate[0], poissonRate[1], precision, solver);
         
         chiSqstatisticVariable = VF.real("chiSqStatistics", chiSqStatistic[0], chiSqStatistic[1], precision, solver);
         
         ChiSquareFitPoisson.decomposition("chiSqConstraint", valueVariables, binVariables, binBounds, poissonRateVariable, chiSqstatisticVariable, precision);
      }
      
      @Override
      public void configureSearch() {
         solver.set(
               new RealStrategy(valueVariables, new Cyclic(), new RealDomainMiddle()),
               IntStrategyFactory.activity(binVariables,1234),
               new RealStrategy(new RealVar[]{poissonRateVariable}, new Cyclic(), new RealDomainMiddle()),
               new RealStrategy(new RealVar[]{chiSqstatisticVariable}, new Cyclic(), new RealDomainMiddle())
               );
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariables.length; i++){
                 st.append("("+valueVariables[i].getLB()+", "+valueVariables[i].getUB()+"), ");
              }
              st.append("\n");
              for(int i = 0; i < binVariables.length; i++){
                 st.append(binVariables[i].getValue()+", ");
              }
              st.append("\n");
              st.append(poissonRateVariable.getLB()+" "+poissonRateVariable.getUB());
              st.append("\n");
              st.append(chiSqstatisticVariable.getLB()+" "+chiSqstatisticVariable.getUB());
              st.append("\n");
              
              assertTrue(chiSqstatisticVariable.getLB() <= 24);
              assertTrue(chiSqstatisticVariable.getUB() >= 23);
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
