package org.chocosolver.solver.constraints.statistical.chisquare.test;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitNormal;
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

public class ChiSquareFitNormalTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      Thread.sleep(1000);
      System.gc();
   }

   @Test
   public void test() {
      String[] str={"-log","SOLUTION"};
      
      double[][] values = {{2,2},{-1,-1},{0,0},{3,3},{1,1},{2,2}};
      int[][] binCounts = {{0,6},{0,6},{0,6},{0,6},{0,6},{0,6},{0,6}};
      double [] binBounds = {-3.5,-2.5,-1.5,-0.5,0.5,1.5,2.5,3.5};
      double[] normalMean = {0,0};
      double[] normalStd = {1,1};
      
      double[] chiSqStatistic = {0,50};
      
      ChiSquareFitNormalReal cs = new ChiSquareFitNormalReal(values, binCounts, binBounds, normalMean, normalStd, chiSqStatistic);
      cs.execute(str);
   }
   
   public class ChiSquareFitNormalReal extends AbstractProblem {
      
      public RealVar[] valueVariables;
      public IntVar[] binVariables;
      public RealVar meanVariable;
      public RealVar stdVariable;
      public RealVar chiSqstatisticVariable;
      
      int[][] binCounts;
      double[][] values;
      double[] binBounds;
      double[] normalMean;
      double[] normalStd;
      double[] chiSqStatistic;
      
      double precision = 1.e-4;
      
      public ChiSquareFitNormalReal(double[][] values,
                                    int[][] binCounts, 
                                    double[] binBounds,
                                    double[] normalMean,
                                    double[] normalStd,
                                    double[] chiSqStatistic){
         this.values = values.clone();
         this.binCounts = binCounts.clone();
         this.binBounds = binBounds.clone();
         this.normalMean = normalMean.clone();
         this.normalStd = normalStd.clone();
         this.chiSqStatistic = chiSqStatistic.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquare");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new RealVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.real("Value "+(i+1), values[i][0], values[i][1], precision, solver);
         
         binVariables = new IntVar[this.binCounts.length];
         for(int i = 0; i < this.binCounts.length; i++)
            binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
         
         meanVariable = VF.real("Normal mean", normalMean[0], normalMean[1], precision, solver);
         
         stdVariable = VF.real("Normal std", normalStd[0], normalStd[1], precision, solver);
         
         chiSqstatisticVariable = VF.real("chiSqStatistics", chiSqStatistic[0], chiSqStatistic[1], precision, solver);
         
         ChiSquareFitNormal.decomposition("chiSqConstraint", valueVariables, binVariables, binBounds, meanVariable, stdVariable, chiSqstatisticVariable, precision);
      }
      
      @Override
      public void configureSearch() {
         solver.set(
               new RealStrategy(valueVariables, new Cyclic(), new RealDomainMiddle()),
               IntStrategyFactory.activity(binVariables,1234),
               new RealStrategy(new RealVar[]{meanVariable, stdVariable}, new Cyclic(), new RealDomainMiddle()),
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
              st.append(meanVariable.getLB()+" "+meanVariable.getUB());
              st.append("\n");
              st.append(stdVariable.getLB()+" "+stdVariable.getUB());
              st.append("\n");
              st.append(chiSqstatisticVariable.getLB()+" "+chiSqstatisticVariable.getUB());
              st.append("\n");
              
              assertTrue(chiSqstatisticVariable.getLB() <= 35);
              assertTrue(chiSqstatisticVariable.getUB() >= 34);
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
