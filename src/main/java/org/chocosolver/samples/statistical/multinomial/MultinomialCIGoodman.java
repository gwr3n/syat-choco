/*
 * syat-choco: a Choco extension for Declarative Statistics.
 * 
 * MIT License
 * 
 * Copyright (c) 2016 Roberto Rossi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.chocosolver.samples.statistical.multinomial;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.SyatConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.real.RealPropagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.randvarmulti.MultinomialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class MultinomialCIGoodman extends AbstractProblem {
   
   public IntVar[] valueVariables;
   public IntVar[] binVariables;
   public RealVar[] targetFrequencies;
   
   int[][] binCounts;
   int[][] values;
   long nObs;
   int[] binBounds;
   double confidence;
   
   public MultinomialCIGoodman(double[][] observations,
                        int[][] binCounts, 
                        int[] binBounds,
                        double confidence){
      this.values = new int[observations.length][1];
      for(int i = 0; i < observations.length; i++){
         for(int j = 0; j < observations[i].length; j++){
            if(observations[i][j] == 1)
               this.values[i][0] = j;
         }
      }
      this.nObs = Arrays.stream(this.values).count();
      this.binCounts = binCounts.clone();
      this.binBounds = binBounds.clone();
      this.confidence = confidence;
   }
   
   RealVar chiSqStatistics;
   RealVar[] allRV;
   
   double precision = 1.e-4;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("Frequency");
   }
   
   @Override
   public void buildModel() {
      valueVariables = new IntVar[this.values.length];
      for(int i = 0; i < this.values.length; i++)
         valueVariables[i] = VariableFactory.enumerated("Value "+(i+1), values[i], solver);
      
      binVariables = new IntVar[this.binCounts.length];
      for(int i = 0; i < this.binCounts.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
      
      this.chiSqDist = new ChiSquareDist(this.binCounts.length-1);
      
      chiSqStatistics = VF.real("chiSqStatistics", this.chiSqDist.inverseF(confidence), this.chiSqDist.inverseF(confidence), precision, solver);
      
      //solver.post(IntConstraintFactorySt.bincounts(valueVariables, binVariables, binBounds, BincountsPropagatorType.EQFast));
      SyatConstraintFactory.bincountsDecomposition(valueVariables, binVariables, binBounds, BincountsDecompositionType.Agkun2016_2_EQ);
      
      RealVar[] realViews = VF.real(binVariables, precision);
      
      targetFrequencies = VF.realArray("tf", this.binCounts.length, 0, 1, precision, solver);
      
      allRV = new RealVar[realViews.length+targetFrequencies.length+1];
      System.arraycopy(realViews, 0, allRV, 0, realViews.length);
      System.arraycopy(targetFrequencies, 0, allRV, realViews.length, targetFrequencies.length);
      allRV[realViews.length+targetFrequencies.length] = chiSqStatistics;
      
      String chiSqExp = "";
      RealPropagator[] propagators = new RealPropagator[binVariables.length];
      for(int i = 0; i < binVariables.length; i++){
         
         /** Solving Goodman's quadratic form **/
         chiSqExp = "(({0}/"+this.nObs+"-{1})^2)={2}*(1-{1})*{1}/"+this.nObs;
         propagators[i] = new RealPropagator(chiSqExp, new RealVar[]{allRV[i],allRV[realViews.length+i],allRV[realViews.length+targetFrequencies.length]}, Ibex.HC4_NEWTON);
               
         /**
          * Closed form solution
          */
         //(A + 2*n[i] - Math.sqrt(A*(A+4*n[i]*(N-n[i])/N)))/(2*(N+A));
         /*chiSqExp = "{1} >= ({2} + 2*{0} - sqrt({2}*({2}+4*{0}*("+this.nObs+"-{0})/"+this.nObs+")))/(2*("+this.nObs+"+{2}))";
         propagators[2*i] = new RealPropagator(chiSqExp, new RealVar[]{allRV[i],allRV[realViews.length+i],allRV[realViews.length+targetFrequencies.length]}, Ibex.HC4_NEWTON);
         chiSqExp = "{1} <= ({2} + 2*{0} + sqrt({2}*({2}+4*{0}*("+this.nObs+"-{0})/"+this.nObs+")))/(2*("+this.nObs+"+{2}))";
         propagators[2*i+1] = new RealPropagator(chiSqExp, new RealVar[]{allRV[i],allRV[realViews.length+i],allRV[realViews.length+targetFrequencies.length]}, Ibex.HC4_NEWTON);
         */
      }
      RealConstraint rc = new RealConstraint("Q&H", propagators);
      solver.post(rc);
      
      RealPropagator[] propagatorsLB = new RealPropagator[binVariables.length];
      RealPropagator[] propagatorsUB = new RealPropagator[binVariables.length];
      for(int i = 0; i < binVariables.length; i++){
         chiSqExp = "{0}/"+this.nObs+">{1}";
         propagatorsLB[i] = new RealPropagator(chiSqExp, new RealVar[]{allRV[i],allRV[realViews.length+i],allRV[realViews.length+targetFrequencies.length]}, Ibex.HC4_NEWTON);
         chiSqExp = "{0}/"+this.nObs+"<{1}";
         propagatorsUB[i] = new RealPropagator(chiSqExp, new RealVar[]{allRV[i],allRV[realViews.length+i],allRV[realViews.length+targetFrequencies.length]}, Ibex.HC4_NEWTON);
      }
      RealConstraint rcLB = new RealConstraint("Q&H", propagatorsLB);
      RealConstraint rcUB = new RealConstraint("Q&H", propagatorsUB);
      solver.post(LogicalConstraintFactory.or(rcLB.reif(),rcUB.reif()));
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
      solver.set(
             new RealStrategy(targetFrequencies, new Cyclic(), new RealDomainMiddle())
       );
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @SuppressWarnings("serial")
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     try {
        solver.propagate();
        solver.plugMonitor(new IMonitorSolution() {
           public void onSolution() {
              st.append("-------------NEW SOLUTION----------------");
              for(int i = 0; i < valueVariables.length; i++){
                 st.append(valueVariables[i].getValue()+"\n");
              }
              st.append("\n");
              for(int i = 0; i < binVariables.length; i++){
                 st.append(binVariables[i]+"\n");
              }
              st.append("\n");
              for(int i = 0; i < targetFrequencies.length; i++){
                 st.append(targetFrequencies[i]+"\n");
              }
              st.append("\n");
              st.append(chiSqStatistics.getLB()+" "+chiSqStatistics.getUB());
              st.append("\n");
           }
        });
        solver.findAllSolutions();
     } catch (ContradictionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        st.append("No solution!");
     }
     /*st.append("-------------NEW SOLUTION----------------");
     for(int i = 0; i < valueVariables.length; i++){
        st.append(valueVariables[i]+"\n");
     }
     st.append("\n");
     for(int i = 0; i < binVariables.length; i++){
        st.append(binVariables[i]+"\n");
     }
     st.append("\n");
     for(int i = 0; i < targetFrequencies.length; i++){
        st.append(targetFrequencies[i]+"\n");
     }
     st.append("\n");
     st.append(chiSqStatistics.getLB()+" "+chiSqStatistics.getUB());
     st.append("\n");*/
     System.out.println(st.toString());
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
   
   public static int[][] generateBinCounts(int bins, int binUB){
      int[][] rndValues = new int[bins][2];
      for(int i = 0; i < bins; i++){
         rndValues[i][0] = 0;
         rndValues[i][1] = binUB;
      }
      return rndValues;
   }
   
   /* http://www.jstor.org/stable/1266673?seq=1 */
   public static double[][] computeQuesenberryHurstCI(double confidence, int[] counts){
      int N = Arrays.stream(counts).sum();
      double[][] intervals = new double[counts.length][2];
      ChiSquareDist chiSq = new ChiSquareDist(counts.length-1);
      double A = chiSq.inverseF(confidence);
      
      double[] n = new double[counts.length];
      
      for(int i = 0; i < counts.length; i++){
         n[i] = counts[i];
      }
      
      for(int i = 0; i < counts.length; i++){
         intervals[i][0] = (A + 2*n[i] - Math.sqrt(A*(A+4*n[i]*(N-n[i])/N)))/(2*(N+A));
         intervals[i][1] = (A + 2*n[i] + Math.sqrt(A*(A+4*n[i]*(N-n[i])/N)))/(2*(N+A));
      }
      return intervals;
   }
   
   public static void main(String[] args) {
      String[] str={"-log","SOLUTION"};
      
      double confidence = 0.9;
      double[] p = {0.3,0.3,0.4}; 
      int replications = 1000;
      int sampleSize = 30;
      
      double coverageProbability = 0;
      MRG32k3a rng = new MRG32k3a();
      UniformGen gen1 = new UniformGen(rng);
      MultinomialGen multinomial = new MultinomialGen(gen1, p, 1);
      for(int i = 0; i < replications; i++){
         double[][] variates = new double[sampleSize][p.length];
         multinomial.nextArrayOfPoints(variates, 0, sampleSize);
         int[][] binCounts = new int[p.length][2];
         for(int k = 0; k < binCounts.length; k++){
            binCounts[k] = new int[]{0, sampleSize};
         }
         int[] binBounds = IntStream.iterate(0, k -> k + 1).limit(p.length + 1).toArray();
         MultinomialCIGoodman cs = new MultinomialCIGoodman(variates, binCounts, binBounds, confidence);
         cs.execute(str);
         System.gc();
         try {
            Thread.sleep(50);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         
         boolean covered = true;
         
         /*for(int j = 0; j < p.length; j++){
            if(cs.targetFrequencies[j].getLB() >= p[j] || p[j] >= cs.targetFrequencies[j].getUB()){
               covered = false;
            }
         }*/
         
         int[] frequencies = IntStream.iterate(0, k -> k + 1).limit(p.length).map(k -> cs.binVariables[k].getValue()).toArray();
         double[][] intervals = computeQuesenberryHurstCI(confidence, frequencies);
         System.out.println(Arrays.deepToString(intervals));
         //boolean covered = true;
         for(int j = 0; j < p.length; j++){
            if(intervals[j][0] >= p[j] || p[j] >= intervals[j][1]){
               covered = false;
            }
         }
         
         if(covered) coverageProbability++;
      }
      
      System.out.println("Coverage probability: "+coverageProbability/replications);
   }

}

