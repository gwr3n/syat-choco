package org.chocosolver.solver.constraints.statistical.t.test;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.t.tStatistic;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class tStatisticTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void testInteger() {
      String[] str={"-log","SOLUTION"};
      
      int[][] values = {{1},{2},{3},{4},{5},{6},{7},{8},{9}}; 
      
      tStatisticInteger standardError = new tStatisticInteger(values, new double[]{5.5,5.5}, new double[]{0,10});
      standardError.execute(str);
   }
   
   @Test
   public void testReal() {
      String[] str={"-log","SOLUTION"};
      
      double[][] values = {{1},{2},{3},{4},{5},{6},{7},{8},{9}}; 
      
      tStatisticReal standardError = new tStatisticReal(values, new double[]{5.5,5.5}, new double[]{0,10});
      standardError.execute(str);
   }

   class tStatisticInteger extends AbstractProblem {
      public IntVar[] valueVariables;
      public RealVar tVariable;
      public RealVar meanVariable;
      
      public int[][] values;
      public double[] mean;
      public double[] t;
      
      double precision = 1.e-4;
      
      public tStatisticInteger(int[][] values, double mean[], double[] t){
         this.values = values;
         this.mean = mean;
         this.t = t;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("StandardError");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new IntVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.enumerated("Value"+(i+1), values[i], solver);
         
         meanVariable = VariableFactory.real("mean", mean[0], mean[1], precision, solver);
         
         tVariable = VariableFactory.real("t", t[0], t[1], precision, solver);
         
         tStatistic.decompose("tConstraint", valueVariables, meanVariable, tVariable, precision);
      }
      
      public void configureSearch() {
         AbstractStrategy<IntVar> strat = IntStrategyFactory.activity(valueVariables,1234);
         solver.set(strat);
      }
    
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariables.length; i++){
                 st.append(valueVariables[i].getValue()+", ");
              }
              st.append("\n");
              st.append(tVariable.getLB()+" "+tVariable.getUB());
              st.append("\n");
              
              assertTrue(tVariable.getLB() <= (5.5-5)/(Math.sqrt(7.5)/Math.sqrt(9)));
              assertTrue(tVariable.getUB() >= (5.5-5)/(Math.sqrt(7.5)/Math.sqrt(9)));
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
   
   class tStatisticReal extends AbstractProblem {
      public RealVar[] valueVariables;
      public RealVar tVariable;
      public RealVar meanVariable;
      
      public double[][] values;
      public double[] mean;
      public double[] t;
      
      double precision = 1.e-4;
      
      public tStatisticReal(double[][] values, double mean[], double[] t){
         this.values = values;
         this.mean = mean;
         this.t = t;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("StandardError");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new RealVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.real("Value"+(i+1), values[i][0], values[i][0], precision, solver);
         
         meanVariable = VariableFactory.real("mean", mean[0], mean[1], precision, solver);
         
         tVariable = VariableFactory.real("t", t[0], t[1], precision, solver);
         
         tStatistic.decompose("tConstraint", valueVariables, meanVariable, tVariable, precision);
      }
      
      public void configureSearch() {
         RealStrategy strat = new RealStrategy(valueVariables, new Cyclic(), new RealDomainMiddle());
         solver.set(strat);
      }
    
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariables.length; i++){
                 st.append("("+valueVariables[i].getLB()+","+valueVariables[i].getUB()+"), ");
              }
              st.append("\n");
              st.append(tVariable.getLB()+" "+tVariable.getUB());
              st.append("\n");
              
              assertTrue(tVariable.getLB() <= (5.5-5)/(Math.sqrt(7.5)/Math.sqrt(9)));
              assertTrue(tVariable.getUB() >= (5.5-5)/(Math.sqrt(7.5)/Math.sqrt(9)));
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
