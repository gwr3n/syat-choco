package org.chocosolver.samples.statistical.chisquare;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsPropagatorType;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitEmpirical;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.slf4j.LoggerFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public class ChiSquareReal extends AbstractProblem {
   
   public RealVar[] valueVariables;
   public IntVar[] binVariables;
   public IntVar[] targetFrequencyVariables;
   
   int[][] binCounts;
   double[][] values;
   double[] binBounds;
   int[][] targetFrequencies;
   double pValue;
   
   public ChiSquareReal(double[][] values,
                        int[][] binCounts, 
                        double[] binBounds,
                        int[][] targetFrequencies,
                        double pValue){
      this.values = values.clone();
      this.binCounts = binCounts.clone();
      this.binBounds = binBounds.clone();
      this.targetFrequencies = targetFrequencies.clone();
      this.pValue = pValue;
   }
   
   RealVar chiSqStatistics;
   RealVar[] allRV;
   
   double precision = 0.1;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("Frequency");
   }
   
   @Override
   public void buildModel() {
      valueVariables = new RealVar[this.values.length];
      for(int i = 0; i < this.values.length; i++)
         valueVariables[i] = VariableFactory.real("Value "+(i+1), values[i][0], values[i][1], precision, solver);
      
      binVariables = new IntVar[this.binCounts.length];
      for(int i = 0; i < this.binCounts.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
      
      targetFrequencyVariables = new IntVar[this.targetFrequencies.length];
      for(int i = 0; i < this.targetFrequencies.length; i++)
         targetFrequencyVariables[i] = VariableFactory.bounded("Target "+(i+1), this.targetFrequencies[i][0], this.targetFrequencies[i][1], solver);
      
      this.chiSqDist = new ChiSquareDist(this.binVariables.length-1);
      
      chiSqStatistics = VF.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-pValue), precision, solver);
      ChiSquareFitEmpirical.decomposition("chiSqTest", valueVariables, binVariables, binBounds, targetFrequencyVariables, chiSqStatistics, precision);
   }
   
   @SuppressWarnings("unused")
   private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
      IntVar[] var3 = new IntVar[var1.length+var2.length];
      System.arraycopy(var1, 0, var3, 0, var1.length);
      System.arraycopy(var2, 0, var3, var1.length, var2.length);
      return var3;
    }
      
   @Override
   public void configureSearch() {
      //AbstractStrategy<IntVar> strat = IntStrategyFactory.domOverWDeg(mergeArrays(valueVariables,binVariables),1234);
      //AbstractStrategy<IntVar> strat = IntStrategyFactory.activity(valueVariables,1234);
      
      //RealStrategy strat = new RealStrategy(valueVariables, new Cyclic(), new RealDomainMiddle());
      //solver.set(strat);
      
      // trick : top-down maximization
      
      solver.set(
            IntStrategyFactory.activity(binVariables,1234),
            new RealStrategy(valueVariables, new Cyclic(), new RealDomainMiddle())
       );
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     boolean solution = solver.findSolution();
     //do{
        st.append("---\n");
        if(solution) {
           for(int i = 0; i < valueVariables.length; i++){
              st.append(valueVariables[i].toString()+", ");
           }
           st.append("\n");
           for(int i = 0; i < binVariables.length; i++){
              st.append(binVariables[i].toString()+", ");
           }
           st.append("\n");
           st.append(chiSqStatistics.getLB()+" "+chiSqStatistics.getUB());
           st.append("\n");
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     LoggerFactory.getLogger("bench").info(st.toString());
   }
   
   @Override
   public void prettyOut() {
       
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
   
   public static double[][] generateRandomValues(Random rnd, int variables, int values, int valUB){ 
      int[][] rndValues = new int[variables][values];
      double[][] bounds = new double[variables][2];
      for(int i = 0; i < variables; i++){
         for(int j = 0; j < values; j++){
            rndValues[i][j] = rnd.nextInt(valUB);
         }
         rndValues[i] = removeDuplicates(rndValues[i]);
         bounds[i][0] = Arrays.stream(rndValues[i]).mapToDouble(k -> k).min().getAsDouble();
         bounds[i][1] = Arrays.stream(rndValues[i]).mapToDouble(k -> k).max().getAsDouble();
      }
      return bounds;
   }
   
   public static int[][] generateBinCounts(int bins, int binUB){
      int[][] rndValues = new int[bins][2];
      for(int i = 0; i < bins; i++){
         rndValues[i][0] = 0;
         rndValues[i][1] = binUB;
      }
      return rndValues;
   }
   
   public static void main(String[] args) {
      String[] str={"-log","SOLUTION"};
      /*double[][] values = {{2,4},{1,2},{0,5},{3,4},{1,1},{2,4}};
      int[][] binCounts = {{0,6},{0,6},{0,6}};
      double[] binBounds = {0,1.5,3.5,6};
      int[] targetFrequencies = {1,4,1};*/
      
      int vars = 24;
      int vals = 10;
      int valUB = 30;
      double[] binBounds = {0,5,10,15,20,25,valUB};                                  
      int bins = binBounds.length - 1;
      
      Random rnd = new Random(123);
      double[][] values = generateRandomValues(rnd, vars, vals, valUB);   
      int[][] binCounts = generateBinCounts(bins, vars);
      
      int[][] targetFrequencies = {{2,2},{4,4},{10,10},{4,4},{2,2},{2,2}};
      
      double pValue = 0.99;
      
      ChiSquareReal cs = new ChiSquareReal(values, binCounts, binBounds, targetFrequencies, pValue);
      cs.execute(str);
   }

}
