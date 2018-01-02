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

package org.chocosolver.samples.statistical.modelfit.nonlinear.poisson;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitPoisson;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.PoissonDist;

/**
 * We consider a set of random variates v_t generated according to
 * 
 * v_t = a*t+t^b+e_t
 * 
 * Our aim is to fit parameters a (slope) and b (exponent) given 
 * a random error e_t (Poisson)
 * 
 * @author Roberto Rossi
 *
 */

public class NonlinearModelFitPoisson extends AbstractProblem {
   
   static double truePoissonRate = 30;
   static double trueSlope = 1;
   static double trueExponent = 0.5;
   
   public RealVar slope;         // Model slope
   public RealVar exp;           // Model exponent
   public RealVar poissonRate;   // Poisson error rate
   
   public RealVar[] error;       // Model fit errors
   public IntVar[] binVariables; // Bin counts
   public RealVar[] realBinViews;
   
   double[] observations;        // Random variates
   double[] binBounds;           // Random error bin bounds
   double significance;          // Significance level
   
   public NonlinearModelFitPoisson(double[] observations,
                     double[] binBounds,
                     double significance){
      this.observations = observations;
      this.binBounds = binBounds.clone();
      this.significance = significance;
   }
   
   RealVar chiSqStatistics;
   RealVar[] allRV;
   
   double precision = 0.1;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("Nonlinear model fit - Poisson errors");
   }
   
   @Override
   public void buildModel() {
      
      // Model parameters
      slope = VariableFactory.real("Slope", 0, 10, precision, solver);
      exp = VariableFactory.real("Exponent", 0, 10, precision, solver);
      
      // Random error
      poissonRate = VariableFactory.real("Rate", 0, 50, precision, solver);
      
      // Nonlinear model
      error = new RealVar[this.observations.length];
      for(int i = 0; i < this.error.length; i++){
         error[i] = VariableFactory.real("Error "+(i+1), 0, this.binBounds[this.binBounds.length-2], precision, solver);
         String residualExp = "{0}="+this.observations[i]+"-{1}*"+(i+1.0)+"-"+(i+1.0)+"^{2}";
         solver.post(new RealConstraint("error "+i,
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{error[i],slope,exp}
               ));
      }
      
      // Chi square goodness-of-fit
      binVariables = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariables.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), 0, this.observations.length, solver);
      
      this.chiSqDist = new ChiSquareDist(this.binVariables.length-1);
      
      chiSqStatistics = VF.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTest", error, binVariables, binBounds, poissonRate, chiSqStatistics, precision, false);
   }
   
   @Override
   public void configureSearch() {
      // Search strategy
      solver.set(
            new RealStrategy(new RealVar[]{slope,exp}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{poissonRate}, new Cyclic(), new RealDomainMiddle())
       );
      
       // Uncomment if a time limit is necessary
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
      StringBuilder st = new StringBuilder();

      // Minimise chi squared statistics
      solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, chiSqStatistics, precision);

      st.append("---\n");
      if(solver.isFeasible() == ESat.TRUE) {
         st.append(slope.toString()+", "+exp.toString()+", "+poissonRate.toString()+"\n");
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
      }else{
         st.append("No solution!");
      }

      System.out.println(st.toString());
   }

   @Override
   public void prettyOut() {
       
   }
   
   /**
    * Random variate generation
    * 
    * @param rnd random seed
    * @param slope model slope
    * @param exp model exponent
    * @param truePoissonRate Poisson rate
    * @param nbObservations number of variates
    * @return the random variates
    */
   public static double[] generateObservations(Random rnd, double slope, double exp, double truePoissonRate, int nbObservations){
      PoissonDist dist = new PoissonDist(truePoissonRate);
      return DoubleStream.iterate(1, i -> i + 1).map(i -> i*slope+Math.pow(i, exp)).map(i -> i + dist.inverseF(rnd.nextDouble())).limit(nbObservations).toArray();
   }
   
   /** 
    * Nonlinear model fitting
    */
   public static void fitMostLikelyParameters(){
      String[] str={"-log","SOLUTION"};
      
      int nbObservations = 100;
      
      Random rnd = new Random(123);
      double[] observations = generateObservations(rnd, trueSlope, trueExponent, truePoissonRate, nbObservations);
      Arrays.stream(observations).forEach(k -> System.out.print(k+"\t"));
      System.out.println();
      
      int bins = 10;
      double[] binBounds = DoubleStream.iterate(0, i -> i + 5).limit(bins+1).toArray();                                 
      double significance = 0.05;
   
      NonlinearModelFitPoisson fit = new NonlinearModelFitPoisson(observations, binBounds, significance);
      fit.execute(str);
      fit.getSolver().getIbex().release();
      fit = null;
      System.gc();
   }
   
   public static void main(String[] args) {
      
      fitMostLikelyParameters();
      
   }
}
