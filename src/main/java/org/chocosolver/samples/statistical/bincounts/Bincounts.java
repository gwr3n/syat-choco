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
   public IntVar[] valueOccurrenceVariables;
   
   int[][] binCounts;
   int[][] values;
   int[] binBounds;
   
   public Bincounts(int[][] values,
                    int[][] binCounts, 
                    int[] binBounds){
      this.values = values.clone();
      this.binCounts = binCounts.clone();
      this.binBounds = binBounds.clone();
   }
   
   /*public void setUp() {
       // read data
   }*/
   
   @Override
   public void createSolver() {
       solver = new Solver("Frequency");
   }
   
   @Override
   public void buildModel() {
      //setUp();
      valueVariables = new IntVar[this.values.length];
      for(int i = 0; i < this.values.length; i++)
         valueVariables[i] = VariableFactory.enumerated("Value "+(i+1), values[i], solver);
      
      binVariables = new IntVar[this.binCounts.length];
      for(int i = 0; i < this.binCounts.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
      
      solver.post(IntConstraintFactorySt.bincountsSt(valueVariables, binVariables, binBounds));      
   }
   
   /*@Override
   public void buildModel() {
      //setUp();
      valueVariables = new IntVar[this.values.length];
      for(int i = 0; i < this.values.length; i++)
         valueVariables[i] = VariableFactory.enumerated("Value "+(i+1), values[i], solver);
      
      valueOccurrenceVariables = new IntVar[this.binBounds[this.binBounds.length-1]-this.binBounds[0]];
      int[] valuesArray = new int[valueOccurrenceVariables.length];
      for(int i = 0; i < this.valueOccurrenceVariables.length; i++){
         valueOccurrenceVariables[i] = VariableFactory.bounded("Value Occurrence "+i, 0, this.values.length, solver);
         valuesArray[i] = i + this.binBounds[0];
      }
      
      binVariables = new IntVar[this.binCounts.length];
      for(int i = 0; i < this.binCounts.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
      
      for(int i = 0; i < this.binBounds.length - 1; i++){
         IntVar[] binOccurrences = new IntVar[this.binBounds[i+1]-this.binBounds[i]];
         System.arraycopy(valueOccurrenceVariables, this.binBounds[i]-this.binBounds[0], binOccurrences, 0, this.binBounds[i+1]-this.binBounds[i]);
         solver.post(IntConstraintFactorySt.sum(binOccurrences, binVariables[i]));
      }
      
      solver.post(IntConstraintFactorySt.sum(binVariables, VariableFactory.fixed(valueVariables.length, solver)));
      
      solver.post(IntConstraintFactorySt.global_cardinality(valueVariables, valuesArray, valueOccurrenceVariables, true));    
   }*/
   
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
     int[][] values = {{3,4},{1,2,4},{2,3,4}};
     int[][] binCounts = {{1,3},{0,1}};
     int[] binBounds = {1,3,5};
     
     Bincounts bc = new Bincounts(values, binCounts, binBounds);
     bc.execute(str);
   }

}
