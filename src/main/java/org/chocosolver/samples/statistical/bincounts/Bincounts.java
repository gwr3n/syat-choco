package org.chocosolver.samples.statistical.bincounts;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.slf4j.LoggerFactory;

public class Bincounts extends AbstractProblem {
   
   public IntVar[] valueVariables;
   public IntVar[] binVariables;
   int[] binBounds;
   
   public void setUp() {
       // read data
   }
   
   @Override
   public void createSolver() {
       solver = new Solver("Frequency");
   }
   
   @Override
   public void buildModel() {
      setUp();
      valueVariables = new IntVar[3];
      valueVariables[0] = VariableFactory.enumerated("Value "+1, new int[]{3,4}, solver);
      valueVariables[1] = VariableFactory.enumerated("Value "+2, new int[]{1,2,4}, solver);
      valueVariables[2] = VariableFactory.enumerated("Value "+3, new int[]{2,3,4}, solver);
      
      binVariables = new IntVar[2];
      binVariables[0] = VariableFactory.bounded("Bin "+1, 1, 3, solver);
      binVariables[1] = VariableFactory.bounded("Bin "+2, 0, 1, solver);
      
      binBounds = new int[]{1,3,5};
      
      solver.post(IntConstraintFactorySt.frequencySt(valueVariables, binVariables, binBounds));      
   }
   
   private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
      IntVar[] var3 = new IntVar[var1.length+var2.length];
      System.arraycopy(var1, 0, var3, 0, var1.length);
      System.arraycopy(var2, 0, var3, var1.length, var2.length);
      return var3;
    }
   
   @Override
   public void configureSearch() {
     AbstractStrategy<IntVar> strat = IntStrategyFactory.domOverWDeg(mergeArrays(valueVariables,binVariables),1234);
       // trick : top-down maximization
       solver.set(strat);
   }
   
   @Override
   public void solve() {
     LoggerFactory.getLogger("bench").info("---");
     this.prettyOut();
     try {
      solver.propagate();
     } catch (ContradictionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
     }
     LoggerFactory.getLogger("bench").info("---");
     this.prettyOut();
     
     StringBuilder st = new StringBuilder();
     boolean solution = solver.findSolution();
     do{
        st.append("\n---SOLUTION---\n");
        if(solution) {
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
        }
     }while(solution = solver.nextSolution());
     LoggerFactory.getLogger("bench").info(st.toString());
   }
   
   @Override
   public void prettyOut() {
      StringBuilder st = new StringBuilder();
      st.append("\n");
      for(int i = 0; i < valueVariables.length; i++){
         st.append(valueVariables[i].toString()+", ");
      }
      st.append("\n");
      for(int i = 0; i < binVariables.length; i++){
         st.append(binVariables[i].toString()+", ");
      }
      LoggerFactory.getLogger("bench").info(st.toString());
   }

   public static void main(String[] args) {
     String[] str={"-log","SILENT"};
       new Bincounts().execute(str);
   }

}
