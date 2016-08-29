package org.chocosolver.samples.statistical.frequency;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.slf4j.LoggerFactory;

public class Frequency extends AbstractProblem {
   
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
      valueVariables[0] = VariableFactory.bounded("Value "+0, 0, 3, solver);
      valueVariables[1] = VariableFactory.bounded("Value "+1, 3, 3, solver);
      valueVariables[2] = VariableFactory.bounded("Value "+2, 1, 3, solver);
      
      binVariables = new IntVar[2];
      binVariables[0] = VariableFactory.bounded("Bin "+0, 0, 4, solver);
      binVariables[1] = VariableFactory.bounded("Bin "+1, 0, 4, solver);
      
      binBounds = new int[]{0,2,4};
      
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
     StringBuilder st = new StringBuilder();
     boolean solution = solver.findSolution();
     do{
        st.append("---\n");
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
       
   }

   public static void main(String[] args) {
     String[] str={"-log","SILENT"};
       new Frequency().execute(str);
   }

}
