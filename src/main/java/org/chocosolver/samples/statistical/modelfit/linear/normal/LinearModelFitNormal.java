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

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
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
import org.chocosolver.util.ESat;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.NormalDist;

/**
 * This class implements the example described in 
 * 
 * R. Rossi, O. Agkun, S. Prestwich, A. Tarim, 
 * "Declarative Statistics," arxiv:1708.01829, Section 5.1
 * 
 * We consider a set of random variates v_t generated according to
 * 
 * v_t = a*t+b+e_t
 * 
 * Our aim is to fit parameters a and b given a random error e_t (normal)
 * 
 * @author Roberto Rossi
 *
 */

public class LinearModelFitNormal extends AbstractProblem {
   
   private RealVar slope;           // Linear model slope
   private RealVar intercept;       // Linear model intercept
   
   private RealVar mean;            // Random error mean (assumed 0)
   private RealVar stDeviation;     // Random error standard deviation
   
   private RealVar[] error;         // Fitting errors
   private IntVar[] binVariables;   // Fitting errors bin counts
   
   private RealVar chiSqStatistics; // Chi square statistics (goodness-of-fit)
   
   private double[] errorBounds;    // Min and max observable error
   
   private double[] observations;   // Random variates
   private double[] binBounds;      // Random error bin bounds
   private double significance;     // Significance level
   
   private double precision = 1.e-2;   // Ibex precision
   
   private ChiSquareDist chiSqDist;    
   
   private boolean verboseOutput;
   
   public LinearModelFitNormal(double[] observations,
                               double[] residualBounds,
                               double[] binBounds,
                               double significance,
                               boolean verboseOutput){
      this.observations = observations;
      this.errorBounds = residualBounds;
      this.binBounds = binBounds;
      this.significance = significance;
      this.verboseOutput = verboseOutput;
   }
   
   @Override
   public void createSolver() {
       solver = new Solver("Linear model fit - Normal errors");
   }
   
   @Override
   public void buildModel() {
      
      // Linear model parameters
      slope = VariableFactory.real("Slope", -2, 2, precision, solver);
      intercept = VariableFactory.real("Intercept", -10, 10, precision, solver);
      
      // Random errors
      mean = VariableFactory.real("Mean", 0, 0, precision, solver);
      stDeviation = VariableFactory.real("stDeviation", 0, 10, precision, solver);
      
      // Linear model
      error = new RealVar[this.observations.length];
      for(int i = 0; i < this.error.length; i++){
         error[i] = VariableFactory.real("Error "+(i+1), this.errorBounds[0], this.errorBounds[1], precision, solver);
         String residualExp = "{0}="+this.observations[i]+"-{1}*"+(i+1)+"+{2}";
         solver.post(new RealConstraint("error "+i,
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{error[i],slope,intercept}
               ));
      }
      
      // Chi square goodness-of-fit
      binVariables = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariables.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), 0, this.observations.length, solver);
      
      this.chiSqDist = new ChiSquareDist(this.binVariables.length-1);
      
      chiSqStatistics = VF.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitNormal.decomposition("chiSqTest", error, binVariables, binBounds, mean, stDeviation, chiSqStatistics, precision, false);
   }
   
   @Override
   public void configureSearch() {
      // Search strategy
      solver.set(
            new RealStrategy(new RealVar[]{slope,intercept,stDeviation}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{chiSqStatistics}, new Cyclic(), new RealDomainMiddle())
       );
      
      // Uncomment if a time limit is necessary
      //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
      StringBuilder st = new StringBuilder();
      
      // Minimise chi squared statistics
      solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, chiSqStatistics, precision);
      if(solver.isFeasible() == ESat.TRUE) {
         st.append(slope.getLB()+", "+intercept.getLB()+", "+mean.toString()+", "+stDeviation.getLB()+"\n");
         if(verboseOutput) {
            for(int i = 0; i < error.length; i++){
               st.append(error[i].toString()+", ");
            }
            st.append("\n");
            for(int i = 0; i < binVariables.length; i++){
               st.append(binVariables[i].toString()+", ");
            }
            st.append("\n");
            st.append(chiSqStatistics.getLB()+" "+chiSqStatistics.getUB());
            st.append("\n");
         }
      }else{
         st.append("No solution!");
      }
      System.out.print(st.toString());
   }

   @Override
   public void prettyOut() {
       
   }
   
   /**
    * Random variate generation
    * 
    * @param rnd random seed
    * @param slope linear model slope
    * @param intercept linear model intercept
    * @param normalMean random error mean
    * @param normalstd random error standard deviation
    * @param nbObservations number of variates
    * @return the random variates
    */
   public static double[] generateObservations(Random rnd, double slope, double intercept, double normalMean, double normalstd, int nbObservations){
      NormalDist dist = new NormalDist(normalMean, normalstd);
      return DoubleStream.iterate(1, i -> i + 1).map(i -> slope*i - intercept + dist.inverseF(rnd.nextDouble())).limit(nbObservations).toArray();
   }
   
   /**
    * Linear model fitting
    * 
    * @param batchSize number of linear models to be fit
    * @param verboseOutput print detailed information
    */
   public static void fitParameters(int batchSize, boolean verboseOutput){
      String[] str={"-log", verboseOutput ? "SOLUTION" : "SILENT"};
      
      int nbObservations = 30;
      
      double slope = 1;
      double intercept = 5;
      double normalMean = 0;
      double normalstd = 5;
      
      double[] residualBounds = {normalMean-4*normalstd,normalMean+4*normalstd};
      
      Random rnd = new Random(1234);
      
      double[][] observations = new double[batchSize][];
      
      for(int i = 0; i < batchSize; i++){
         observations[i] = generateObservations(rnd, slope, intercept, normalMean, normalstd, nbObservations);
         Arrays.stream(observations[i]).forEach(k -> System.out.print(k+", "));
         System.out.println();
      }
      
      for(int i = 0; i < batchSize; i++){
         
         // Bin bounds for random error (5 bins of size 4 starting from -10)
         int bins = 5;
         double[] binBounds = DoubleStream.iterate(-10, a -> a + 4).limit(bins + 1).toArray(); 
         
         double significance = 0.05;
      
         LinearModelFitNormal fit = new LinearModelFitNormal(observations[i], residualBounds, binBounds, significance, verboseOutput);
         fit.execute(str);
         fit.getSolver().getIbex().release();
      }
   }
   
   public static void main(String[] args) {
      
      int batchSize = 1;               // Number of linear models to fit
      boolean verboseOutput = true;    // Print detailed information
      
      fitParameters(batchSize, verboseOutput);
      
   }
}
