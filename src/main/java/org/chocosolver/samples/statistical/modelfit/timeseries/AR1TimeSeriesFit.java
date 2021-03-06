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

package org.chocosolver.samples.statistical.modelfit.timeseries;

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
 * We consider an AR(1) stochastic process and try to fit its parameters.
 * 
 * This problem is discussed in 
 * 
 * R. Rossi, O. Agkun, S. Prestwich, A. Tarim, 
 * "Declarative Statistics," arxiv:1708.01829, Section 5.2
 * 
 * @author Roberto Rossi
 *
 */

public class AR1TimeSeriesFit extends AbstractProblem {
   
   public RealVar parameter; // AR(1) parameter
   public RealVar constant;  // AR(1) constant
   
   public RealVar lambda;    // Instead of a normally distributed error, we consider a Poisson error with rate lambda
   
   public RealVar[] error;
   
   public IntVar[] binVariable;
   
   double[] observation;
   double[] binBound;
   
   double significance;
   
   public AR1TimeSeriesFit(double[] observation,
                 double[] binBound,
                 double significance){
      this.observation = observation;
      this.binBound = binBound;
      this.significance = significance;
   }
   
   RealVar chiSqStatistic;
   
   RealVar[] allRV;
   
   double precision = 0.1;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("AR(1) model fit");
   }
   
   @Override
   public void buildModel() {
      parameter = VariableFactory.real("Parameter", 0, 2, precision, solver);
      constant = VariableFactory.real("Constant", 0, 20, precision, solver);
      
      error = new RealVar[this.observation.length];
      for(int i = 0; i < this.error.length; i++){
         error[i] = VariableFactory.real("Error "+(i+1), 0, Arrays.stream(observation).max().getAsDouble(), precision, solver);
         String residualExp = "{0}="+this.observation[i]+"-{1}" + ((i > 0) ? "-{2}*"+this.observation[i-1] : "");
         solver.post(new RealConstraint("residual constraint "+(i+1),
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{error[i],constant,parameter}
               ));
      }
      
      // Chi square goodness-of-fit
      binVariable = new IntVar[this.binBound.length-1];
      for(int i = 0; i < this.binVariable.length; i++)
         binVariable[i] = VariableFactory.bounded("Bin "+(i+1), 0, this.observation.length, solver);
      
      this.chiSqDist = new ChiSquareDist(this.binBound.length - 1);
      
      lambda = VariableFactory.real("lambda 1", 0, 20, precision, solver);
      chiSqStatistic = VF.real("chiSqStatistic", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTest", error, binVariable, binBound, lambda, chiSqStatistic, precision, false);
   }
   
   @Override
   public void configureSearch() {
      /*solver.plugMonitor(new IMonitorSolution() {
         public void onSolution() {
            // DO SOMETHING
         }
      });*/
      
      // Search strategy
      solver.set(
            new RealStrategy(new RealVar[]{constant,parameter,lambda}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{chiSqStatistic}, new Cyclic(), new RealDomainMiddle())
       );
      
       // Uncomment if a time limit is necessary
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, chiSqStatistic, precision);
     //do{
        st.append("---\n");
        if(solver.isFeasible() == ESat.TRUE) {
           st.append("Curve: "+parameter.toString()+" "+constant.toString());
           st.append("\n");
           st.append("Lambda: "+lambda.toString());
           st.append("\n");
           st.append(chiSqStatistic.getLB()+" "+chiSqStatistic.getUB());
           st.append("\n");
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     System.out.println(st.toString());
   }

   @Override
   public void prettyOut() {
       
   }
   
   public static double[] generateObservations(Random rnd, double c, double phi, double truePoissonRate, int nbObservations){
      PoissonDist dist = new PoissonDist(truePoissonRate);
      double[] observations = new double[nbObservations]; 
      double[] poisson = new double[nbObservations];
      double zt = 0;
      for(int i = 0; i < nbObservations; i++){
         poisson[i] = dist.inverseF(rnd.nextDouble());
         zt = c + poisson[i] + phi*zt;
         observations[i] = zt;
      }
      return observations;
   }
   
   public static void fitParameters(){
      String[] str={"-log","SOLUTION"};
      
      double[] observations;
      
      int nbObservations = 150;
      
      Random rnd = new Random(123);
      
      double constant = 5;
      double parameter = 0.5;
      double truePoissonRate = 5;
      
      observations = generateObservations(rnd, constant, parameter, truePoissonRate, nbObservations);
      
      int bins = 10;
      double[] binBounds = DoubleStream.iterate(0, i -> i + 2).limit(bins).toArray();
      double significance = 0.05;
   
      AR1TimeSeriesFit fit = new AR1TimeSeriesFit(observations, binBounds, significance);
      fit.execute(str);
      fit.getSolver().getIbex().release();
      fit = null;
      System.gc();
   }
   
   public static void main(String[] args) {
      
      fitParameters();
      
   }
}
