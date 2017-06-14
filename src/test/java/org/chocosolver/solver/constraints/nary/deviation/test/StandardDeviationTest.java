package org.chocosolver.solver.constraints.nary.deviation.test;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.deviation.StandardDeviation;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class StandardDeviationTest {

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
      
      IntegerStandardDeviation standardDeviation = new IntegerStandardDeviation(values, new double[]{0,100});
      standardDeviation.execute(str);
   }

   class IntegerStandardDeviation extends AbstractProblem {
      public IntVar[] valueVariables;
      public RealVar standardDeviationVariable;
      
      public int[][] values;
      public double[] standardDeviation;
      
      double precision = 1.e-4;
      
      public IntegerStandardDeviation(int[][] values, double[] standardDeviation){
         this.values = values;
         this.standardDeviation = standardDeviation;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("StandardDeviation");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new IntVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.enumerated("Value"+(i+1), values[i], solver);
         
         standardDeviationVariable = VariableFactory.real("StandardDeviation", standardDeviation[0], standardDeviation[1], precision, solver);
         
         StandardDeviation.decompose("StandardDeviationConstraint", valueVariables, standardDeviationVariable, precision);
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
              st.append(standardDeviationVariable.getLB()+" "+standardDeviationVariable.getUB());
              st.append("\n");
              
              assertTrue(standardDeviationVariable.getLB() <= Math.sqrt(7.5));
              assertTrue(standardDeviationVariable.getUB() >= Math.sqrt(7.5));
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
