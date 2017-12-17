package org.chocosolver.solver.constraints.nary.mean.test;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.mean.Mean;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MeanTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void test() {
      String[] str={"-log","SOLUTION"};
      
      int[][] values = {{1},{2},{3},{4},{5},{6},{7},{8},{9}}; 
      
      IntegerMean mean = new IntegerMean(values, new double[]{0,10});
      mean.execute(str);
   }

   class IntegerMean extends AbstractProblem {
      public IntVar[] valueVariables;
      public RealVar meanVariable;
      
      public int[][] values;
      public double[] mean;
      
      double precision = 1.e-4;
      
      public IntegerMean(int[][] values, double[] mean){
         this.values = values;
         this.mean = mean;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("Mean");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new IntVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.enumerated("Value "+(i+1), values[i], solver);
         
         meanVariable = VariableFactory.real("Mean ", mean[0], mean[1], precision, solver);
         
         Mean.decompose("MeanConstraint", valueVariables, meanVariable, precision);
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
              st.append(meanVariable.getLB()+" "+meanVariable.getUB());
              st.append("\n");
              
              assertTrue(meanVariable.getLB() <= 5);
              assertTrue(meanVariable.getUB() >= 5);
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
