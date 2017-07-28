package org.chocosolver.solver.constraints.statistical.fisherratio.test;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.fisherratio.FisherRatioStatistic;
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

public class FisherRatioStatisticTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      Thread.sleep(2000);
      System.gc();
   }

   @Test
   public void testInteger() {
      String[] str={"-log","SOLUTION"};
      
      int[][] valuesA = {{3},{3},{1},{1},{2}};
      int[][] valuesB = {{1},{1},{2},{2},{3}};
      double confidence = 0.95;
      
      double[] fStatistic = {0,1000};
      
      FisherRationStatisticInteger fr = new FisherRationStatisticInteger(valuesA, valuesB, fStatistic);
      fr.execute(str);
   }
   
   @Test
   public void testReal() {
      String[] str={"-log","SOLUTION"};
      
      double[][] valuesA = {{9.84321,9.84321}, {11.0656,11.0656}, {10.0497,10.0497}, {8.9333,8.9333}, {7.94843,7.94843}};
      double[][] valuesB = {{10.3289,10.3289}, {8.83811,8.83811}, {11.344,11.344}, {13.2178,13.2178}, {9.64139,9.64139}};
      double confidence = 0.95;
      
      double[] fStatistic = {0,1000};
      
      FisherRationStatisticReal fr = new FisherRationStatisticReal(valuesA, valuesB, fStatistic);
      fr.execute(str);
   }
   
   class FisherRationStatisticInteger extends AbstractProblem {
      public IntVar[] seriesA;
      public IntVar[] seriesB;
      public RealVar fStatisticVariable;
      
      int[][] valuesA;
      int[][] valuesB;
      
      double[] fStatistic;
      
      double precision = 1.e-4;
      
      public FisherRationStatisticInteger(int[][] valuesA,
                                          int[][] valuesB, 
                                          double[] chiSqStatistic){
         this.valuesA = valuesA;
         this.valuesB = valuesB;
         this.fStatistic = chiSqStatistic.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("Fisher");
      }
      
      @Override
      public void buildModel() {
         seriesA = new IntVar[this.valuesA.length];
         for(int i = 0; i < this.valuesA.length; i++)
            seriesA[i] = VariableFactory.enumerated("Value A"+(i+1), valuesA[i], solver);
         
         seriesB = new IntVar[this.valuesB.length];
         for(int i = 0; i < this.valuesB.length; i++)
            seriesB[i] = VariableFactory.enumerated("Value B"+(i+1), valuesB[i], solver);
         
         fStatisticVariable = VF.real("fStatistics", fStatistic[0], fStatistic[1], precision, solver);
         
         FisherRatioStatistic.decomposition("fConstraint", seriesA, seriesB, fStatisticVariable, precision);
      }
      
      @Override
      public void configureSearch() {         
         solver.set(
               IntStrategyFactory.activity(seriesA,1234),
               IntStrategyFactory.activity(seriesB,1234),
               new RealStrategy(new RealVar[]{fStatisticVariable}, new Cyclic(), new RealDomainMiddle())
               );
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              /*for(int i = 0; i < valueVariables.length; i++){
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
              st.append("\n");*/
              
              //assertTrue(fStatisticVariable.getLB() <= 10);
              //assertTrue(fStatisticVariable.getUB() >= 9);
              
              st.append(fStatisticVariable.getLB()+" "+fStatisticVariable.getUB());
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
   
   class FisherRationStatisticReal extends AbstractProblem {
      public RealVar[] seriesA;
      public RealVar[] seriesB;
      public RealVar fStatisticVariable;
      
      double[][] valuesA;
      double[][] valuesB;
      
      double[] fStatistic;
      
      double precision = 1.e-4;
      
      public FisherRationStatisticReal(double[][] valuesA,
                                       double[][] valuesB, 
                                       double[] fStatistic){
         this.valuesA = valuesA;
         this.valuesB = valuesB;
         this.fStatistic = fStatistic;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("Fisher");
      }
      
      @Override
      public void buildModel() {
         seriesA = new RealVar[this.valuesA.length];
         for(int i = 0; i < this.valuesA.length; i++)
            seriesA[i] = VariableFactory.real("Value A"+(i+1), valuesA[i][0], valuesA[i][1], precision, solver);
         
         seriesB = new RealVar[this.valuesB.length];
         for(int i = 0; i < this.valuesB.length; i++)
            seriesB[i] = VariableFactory.real("Value B"+(i+1), valuesB[i][0], valuesB[i][1], precision, solver);
         
         fStatisticVariable = VF.real("fStatistics", fStatistic[0], fStatistic[1], precision, solver);
         
         FisherRatioStatistic.decomposition("fConstraint", seriesA, seriesB, fStatisticVariable, precision);
      }
      
      @Override
      public void configureSearch() {         
         solver.set(
               new RealStrategy(seriesA, new Cyclic(), new RealDomainMiddle()),
               new RealStrategy(seriesB, new Cyclic(), new RealDomainMiddle()),
               new RealStrategy(new RealVar[]{fStatisticVariable}, new Cyclic(), new RealDomainMiddle())
               );
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              /*for(int i = 0; i < valueVariables.length; i++){
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
              st.append("\n");*/
              
              //assertTrue(fStatisticVariable.getLB() <= 10);
              //assertTrue(fStatisticVariable.getUB() >= 9);
              
              st.append(fStatisticVariable.getLB()+" "+fStatisticVariable.getUB());
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
