package org.chocosolver.samples.statistical.bincounts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.slf4j.LoggerFactory;

public class BincountsDomainReductionDec extends AbstractProblem {
   public IntVar[] valueVariables;
   public IntVar[] binVariables;
   public IntVar[] valueOccurrenceVariables;
   
   int[][] binCounts;
   int[][] values;
   int[] binBounds;
   
   boolean wipeout = false;
   
   int assignedVariables = 0;
   
   public BincountsDomainReductionDec(int[][] values,
                                   int[][] binCounts, 
                                   int[] binBounds,
                                   int assignedVariables){
      this.values = values.clone();
      this.binCounts = binCounts.clone();
      this.binBounds = binBounds.clone();
      this.assignedVariables = assignedVariables;
   }
   
   public void setUp() {
       // read data
   }
   
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
      
      valueOccurrenceVariables = new IntVar[this.binBounds[this.binBounds.length-1]-this.binBounds[0]];
      int[] valuesArray = new int[valueOccurrenceVariables.length];
      for(int i = 0; i < this.valueOccurrenceVariables.length; i++){
         valueOccurrenceVariables[i] = VariableFactory.bounded("Value Occurrence "+i, 0, this.values.length, solver);
         valuesArray[i] = i;
      }
      
      binVariables = new IntVar[this.binCounts.length];
      for(int i = 0; i < this.binCounts.length; i++){
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
      }
      
      for(int i = 0; i < this.binBounds.length - 1; i++){
         IntVar[] binOccurrences = new IntVar[this.binBounds[i+1]-this.binBounds[i]];
         System.arraycopy(valueOccurrenceVariables, this.binBounds[i]-this.binBounds[0], binOccurrences, 0, this.binBounds[i+1]-this.binBounds[i]);
         solver.post(IntConstraintFactorySt.sum(binOccurrences, binVariables[i]));
      }
      
      solver.post(IntConstraintFactorySt.sum(binVariables, VariableFactory.fixed(valueVariables.length, solver)));
      
      solver.post(IntConstraintFactorySt.global_cardinality(valueVariables, valuesArray, valueOccurrenceVariables, true));  
   }
   
   private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
      IntVar[] var3 = new IntVar[var1.length+var2.length];
      System.arraycopy(var1, 0, var3, 0, var1.length);
      System.arraycopy(var2, 0, var3, var1.length, var2.length);
      return var3;
    }
   
   @Override
   public void configureSearch() {
     if(assignedVariables > 0){
        IntVar[] reducedVariableArray = Arrays.copyOf(valueVariables, assignedVariables);
        AbstractStrategy<IntVar> strat = IntStrategyFactory.minDom_LB(reducedVariableArray);
       // trick : top-down maximization
       solver.set(strat);
     }
   }
   
   @Override
   public void solve() {
     if(assignedVariables == 0){
        //LoggerFactory.getLogger("bench").info("---");
        //this.prettyOut();
        try {
           solver.propagate();
        } catch (ContradictionException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         wipeout = true;
        }
        //LoggerFactory.getLogger("bench").info("---");
        //this.prettyOut();
     }else{
        wipeout = !solver.findSolution();
     }
     
     /*StringBuilder st = new StringBuilder();
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
     LoggerFactory.getLogger("bench").info(st.toString());*/
   }
   
   public double getPercentRemainingValuesCount(int[][] values, int[][] binCounts){
      double totalCount = 0;
      double totalFilteredCount = 0;
      
      {
         int[][] filteredValues = new int[values.length][];
         for(int v = 0; v < filteredValues.length; v++){
            int c = 0;
            int[] domain = new int[valueVariables[v].getDomainSize()]; 
            DisposableValueIterator vit = valueVariables[v].getValueIterator(true);
            while(vit.hasNext()){
               domain[c++] = vit.next();
            }
            vit.dispose();
            filteredValues[v] = domain;
         }
         
         for(int v = 0; v < values.length; v++){
            totalCount += values[v].length;
            totalFilteredCount += filteredValues[v].length;
         }
      }
      
      {
         int[][] filteredValues = new int[binCounts.length][];
         for(int v = 0; v < filteredValues.length; v++){
            int c = 0;
            int[] domain = new int[binVariables[v].getDomainSize()]; 
            DisposableValueIterator vit = binVariables[v].getValueIterator(true);
            while(vit.hasNext()){
               domain[c++] = vit.next();
            }
            vit.dispose();
            filteredValues[v] = domain;
         }
         
         for(int v = 0; v < binCounts.length; v++){
            totalCount += binCounts[v][1]-binCounts[v][0]+1;
            totalFilteredCount += filteredValues[v].length;      
         }
      }
      
      return wipeout ? 0 : totalFilteredCount/totalCount;
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
      //LoggerFactory.getLogger("bench").info(st.toString());
   }
   
   public static int[] removeDuplicates(int[] arr) {
      Set<Integer> alreadyPresent = new HashSet<Integer>();
      int[] whitelist = new int[0];

      for (int nextElem : arr) {
        if (!alreadyPresent.contains(nextElem)) {
          whitelist = Arrays.copyOf(whitelist, whitelist.length + 1);
          whitelist[whitelist.length - 1] = nextElem;
          alreadyPresent.add(nextElem);
        }
      }

      return whitelist;
    }

   public static int[][] generateRandomValues(Random rnd, int variables, int values, int valUB){ 
      int[][] rndValues = new int[variables][values];
      for(int i = 0; i < variables; i++){
         for(int j = 0; j < values; j++){
            rndValues[i][j] = rnd.nextInt(valUB);
         }
         rndValues[i] = removeDuplicates(rndValues[i]);
      }
      return rndValues;
   }
   
   public static int[][] generateRandomBinCounts(Random rnd, int bins, int binUB){
      int[][] rndValues = new int[bins][2];
      for(int i = 0; i < bins; i++){
         rndValues[i][0] = 0;
         rndValues[i][1] = rnd.nextInt(binUB + 1);
      }
      return rndValues;
   }
   
   public static void main(String[] args) {
     String[] str={"-log","SILENT"};
     //String[] str={"-log","SOLUTION"};
     int vars = 40;
     int vals = 10;
     int valUB = 30;
     int[] binBounds = {0,10,20,valUB};                                  // {1,3,5};
     int bins = binBounds.length - 1;
     
     int instances = 50;
     double percentageVarAssigned = 0.8;
     
     StringBuilder results = new StringBuilder();
     
     Random rnd = new Random(123);
     int counter = 0;
     int instance = 0;
     int[] instanceNos = {1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 31, 32, 33, 34, 35, 36, 37, 39, 40, 41, 42, 43, 45, 47, 49, 50, 51, 52, 53, 54, 55, 56, 57, 59, 60};
     do{
        int[][] values = generateRandomValues(rnd, vars, vals, valUB);    // {{3,4},{1,2,4},{2,3,4}};
        int[][] binCounts = generateRandomBinCounts(rnd, bins, vars);     // {{1,3},{0,1}};
        
        BincountsDomainReductionDec bc = new BincountsDomainReductionDec(values, binCounts, binBounds, (int)Math.floor(vars*percentageVarAssigned));
        
        long timeBefore = System.nanoTime();
        
        bc.execute(str);
        
        long timeAfter = System.nanoTime();
        
        instance++;
        if(Arrays.binarySearch(instanceNos, instance)<0) 
           continue;
        else
           counter++;
        
        LoggerFactory.getLogger("bench").info("Domain reduction: "+(1 - bc.getPercentRemainingValuesCount(values, binCounts)));
        LoggerFactory.getLogger("bench").info("Time(sec): "+(timeAfter-timeBefore)*1e-9);
        
        results.append(instance+"\t"+(1 - bc.getPercentRemainingValuesCount(values, binCounts))+"\t"+(timeAfter-timeBefore)*1e-9+"\n");
     }while(counter < instances);
     System.out.println(results);
   }
}
