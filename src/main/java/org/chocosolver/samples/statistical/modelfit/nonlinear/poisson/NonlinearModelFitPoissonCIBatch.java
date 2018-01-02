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

import java.util.Random;
import java.util.stream.DoubleStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitPoisson;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.PoissonDist;

/**
 * We consider a set of random variates v_t generated according to
 * 
 * v_t = a*t+t^b+e_t
 * 
 * Our aim is to determine nominal coverage probability for 
 * model parameter - a (slope) and b (exponent) - confidence intervals 
 * represented by the declarative statistics model given a 
 * random error e_t (normal)
 * 
 * @author Roberto Rossi
 *
 */

public class NonlinearModelFitPoissonCIBatch extends AbstractProblem {
   
   static double truePoissonRate = 10;
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
   
   public NonlinearModelFitPoissonCIBatch(double[] observations,
                                      double[] binBounds,
                                      double significance){
      this.observations = observations;
      this.binBounds = binBounds.clone();
      this.significance = significance;
   }
   
   RealVar chiSqStatistics;
   RealVar[] allRV;
   
   double precision = 0.0001;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("Nonlinear model fit - Poisson");
   }
   
   @Override
   public void buildModel() {
      
      // Model parameters
      slope = VariableFactory.real("Slope", trueSlope-precision, trueSlope+precision, precision, solver);
      exp = VariableFactory.real("Exponent", trueExponent-precision, trueExponent+precision, precision, solver);
      
      // Random error
      poissonRate = VariableFactory.real("Rate", truePoissonRate-precision, truePoissonRate+precision, precision, solver);
      
      // Nonlinear model
      error = new RealVar[this.observations.length];
      for(int i = 0; i < this.observations.length; i++)
         error[i] = VariableFactory.real("Error "+(i+1), 0, this.binBounds[this.binBounds.length-2], precision, solver);
      
      for(int i = 0; i < this.error.length; i++){
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
            new RealStrategy(new RealVar[]{slope,exp,poissonRate}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(error, new Cyclic(), new RealDomainMiddle()),
            IntStrategyFactory.activity(binVariables,1234)
       );
       // Uncomment if a time limit is necessary
       SearchMonitorFactory.limitTime(solver,5000);
   }
   
   @Override
   public void solve() {
      StringBuilder st = new StringBuilder();
      boolean solution = solver.findSolution();
      st.append("---\n");
      if(solution) {
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
         feasibleCount++;
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
   
   static int feasibleCount = 0;
   
   public static void coverageProbability(){
      String[] str={"-log","SOLUTION"};
      
      Random rnd = new Random(1234);
      
      int nbObservations = 50;
      
      double replications = 1000;
      
      for(int k = 0; k < replications; k++){
         double[] observations = generateObservations(rnd, trueSlope, trueExponent, truePoissonRate, nbObservations);
         
         // Bin bounds for random error (20 bins of size 2 starting from 0)
         int bins = 20;
         double[] binBounds = DoubleStream.iterate(0, i -> i + 2).limit(bins).toArray();                
         
         double significance = 0.05;
      
         NonlinearModelFitPoissonCIBatch fit = new NonlinearModelFitPoissonCIBatch(observations, binBounds, significance);
         fit.execute(str);
         fit.getSolver().getIbex().release();
         fit = null;
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
