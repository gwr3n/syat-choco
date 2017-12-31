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

package org.chocosolver.samples.statistical.modelfit.linear.normal;

import java.math.BigDecimal;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitNormal;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.NormalDist;

public class LinearModelFitNormalCIBatch extends AbstractProblem {
   
   private RealVar slope;
   private RealVar intercept;
   private RealVar mean;
   private RealVar stDeviation;
   
   private RealVar[] residual;
   private IntVar[] binVariables;
   
   private RealVar chiSqStatistics;
   
   private double[] residualBounds;
   
   private double[] observations;
   private double[] binBounds;
   private double significance;
   
   private double precision = 1.e-2;
   
   private ChiSquareDist chiSqDist;
   
   public LinearModelFitNormalCIBatch(double[] observations,
                                  double[] residualBounds,
                                  double[] binBounds,
                                  double significance){
      this.observations = observations;
      this.residualBounds = residualBounds;
      this.binBounds = binBounds;
      this.significance = significance;
   }
   
   @Override
   public void createSolver() {
       solver = new Solver("Regression Normal");
   }
   
   @Override
   public void buildModel() {
      slope = VariableFactory.real("Slope", 1, 1, precision, solver);
      intercept = VariableFactory.real("Intercept", 5, 5, precision, solver);
      mean = VariableFactory.real("Mean", 0, 0, precision, solver);
      stDeviation = VariableFactory.real("stDeviation", 5, 5, precision, solver);
      
      residual = new RealVar[this.observations.length];
      for(int i = 0; i < this.residual.length; i++){
         residual[i] = VariableFactory.real("Residual "+(i+1), this.residualBounds[0], this.residualBounds[1], precision, solver);
         String residualExp = "{0}="+(new BigDecimal(this.observations[i]).toPlainString())+"-{1}*"+(i+1)+"+{2}";
         solver.post(new RealConstraint("residual "+i,
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{residual[i],slope,intercept}
               ));
      }
      
      binVariables = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariables.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), 0, this.observations.length, solver);
      
      this.chiSqDist = new ChiSquareDist(this.binVariables.length-1);
      
      chiSqStatistics = VF.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitNormal.decomposition("chiSqTest", residual, binVariables, binBounds, mean, stDeviation, chiSqStatistics, precision, true);
   }
   
   @Override
   public void configureSearch() {
      
      solver.set(
            new RealStrategy(new RealVar[]{slope,intercept,stDeviation}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{chiSqStatistics}, new Cyclic(), new RealDomainMiddle())
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
           st.append(slope.toString()+", "+intercept.toString()+", "+mean.toString()+", "+stDeviation.toString()+"\n");
           for(int i = 0; i < residual.length; i++){
              st.append(residual[i].toString()+", ");
           }
           st.append("\n");
           for(int i = 0; i < binVariables.length; i++){
              st.append(binVariables[i].toString()+", ");
           }
           st.append("\n");
           st.append(chiSqStatistics.getLB()+" "+chiSqStatistics.getUB());
           st.append("\n");
           feasibleCount++;
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     System.out.println(st.toString());
   }

   @Override
   public void prettyOut() {
       
   }
   
   public static double[] generateObservations(Random rnd, double slope, double intercept, double normalMean, double normalstd, int nbObservations){
      NormalDist dist = new NormalDist(normalMean, normalstd);
      return DoubleStream.iterate(1, i -> i + 1).map(i -> slope*i - intercept + dist.inverseF(rnd.nextDouble())).limit(nbObservations).toArray();
   }
   
   static int feasibleCount = 0;
   
   public static void coverageProbability(){
      String[] str={"-log","SOLUTION"};
      
      double replications = 1000;
      
      int nbObservations = 50;
      
      double slope = 1;
      double intercept = 5;
      double normalMean = 0;
      double normalstd = 5;
      
      double[] residualBounds = {normalMean-4*normalstd,normalMean+4*normalstd};
      
      Random rnd = new Random(1234);
      
      for(int k = 0; k < replications; k++){
         double[] observations = generateObservations(rnd, slope, intercept, normalMean, normalstd, nbObservations);
                  
         int bins = 5;
         double[] binBounds = DoubleStream.iterate(-10, i -> i + 4).limit(bins + 1).toArray();                                 
         double significance = 0.05;
      
         LinearModelFitNormalCIBatch regression = new LinearModelFitNormalCIBatch(observations, residualBounds, binBounds, significance);
         regression.execute(str);
         try {
            regression.finalize();
         } catch (Throwable e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         regression = null;
         System.gc();
         
         System.out.println(feasibleCount/(k+1.0) + "(" + k + ")");
         try {
            Thread.sleep(100);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      System.out.println(feasibleCount/replications);
   }
   
   public static void main(String[] args) {
      
      coverageProbability();
      
   }
}
