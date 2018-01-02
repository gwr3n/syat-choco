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

package org.chocosolver.samples.statistical.modelfit.linear.poisson;

import java.util.Random;
import java.util.stream.DoubleStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitPoisson;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareIndependence;
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
 * 
 * We consider two sets of random variates v^1_t and v^2_t generated according to
 * 
 * v^1_t = a*t+e^1_t
 * v^2_t = b*t+e^2_t
 * 
 * where e^1_t and e^2_t are independently and identically distributed Poisson random 
 * errors with rate lambda. 
 * 
 * Our aim is to fit parameters a, b and the common error rate lambda.
 *
 * @author Roberto Rossi
 *
 */

public class TwoLinearModelsFitPoisson extends AbstractProblem {
   
   public RealVar slope1;
   public RealVar slope2;
   public RealVar lambda;
   
   public RealVar[] error1;
   public RealVar[] error2;
   
   public IntVar[] binVariables;
   
   double[] observations1;
   double[] binBounds1;
   
   double[] observations2;
   double[] binBounds2;
   
   double significance;
   
   public TwoLinearModelsFitPoisson(double[] observations1,
                                  double[] binBounds1,
                                  double[] observations2,
                                  double[] binBounds2,
                                  double significance){
      this.observations1 = observations1;
      this.binBounds1 = binBounds1.clone();
      
      this.observations2 = observations2;
      this.binBounds2 = binBounds2.clone();
      
      this.significance = significance;
   }
   
   RealVar chiSqStatistics1;
   RealVar chiSqStatistics2;
   RealVar chiSqStatistics3;
   RealVar[] allRV;
   
   double precision = 0.5;
   
   ChiSquareDist chiSqDist1;
   ChiSquareDist chiSqDist2;
   ChiSquareDist chiSqDist3;
   
   @Override
   public void createSolver() {
       solver = new Solver("Regression");
   }
   
   @Override
   public void buildModel() {
      slope1 = VariableFactory.real("Slope 1", 0, 10, precision, solver);
      
      error1 = new RealVar[this.observations1.length];
      for(int i = 0; i < this.error1.length; i++){
         error1[i] = VariableFactory.real("Error 1"+(i+1), 0, this.binBounds1[this.binBounds1.length-2], precision, solver);
         String residualExp = "{0}="+this.observations1[i]+"-{1}*"+(i+1.0);
         solver.post(new RealConstraint("error 1"+i,
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{error1[i],slope1}
               ));
      }
      
      slope2 = VariableFactory.real("Slope 2", 0, 10, precision, solver);
      
      error2 = new RealVar[this.observations2.length];
      for(int i = 0; i < this.error2.length; i++){
         error2[i] = VariableFactory.real("Error 2"+(i+1), 0, this.binBounds2[this.binBounds2.length-2], precision, solver);
         String residualExp = "{0}="+this.observations2[i]+"-{1}*"+(i+1.0);
         solver.post(new RealConstraint("error 2"+i,
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{error2[i],slope2}
               ));
      }
      
      this.chiSqDist1 = new ChiSquareDist((this.binBounds1.length - 1)*(this.binBounds2.length - 1));
      
      double[][] binBounds = new double[2][];
      binBounds[0] = this.binBounds1;
      binBounds[1] = this.binBounds2;
      
      // This test ensures random error are independent
      chiSqStatistics1 = VF.real("chiSqStatistics 1", this.chiSqDist1.inverseF(1-significance), this.chiSqDist1.inverseF(1-significance), precision, solver);
      ChiSquareIndependence.decomposition("chiSqTest 1", error1, error2, binBounds, chiSqStatistics1, precision, true);
      
      binVariables = new IntVar[this.binBounds1.length-1];
      for(int i = 0; i < this.binVariables.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), 0, this.observations1.length, solver);
      
      this.chiSqDist2 = new ChiSquareDist(error1.length - 1);
      
      lambda = VariableFactory.real("lambda", 0, 20, precision, solver);
      
      // This test ensures rate of random errors is lambda
      chiSqStatistics2 = VF.real("chiSqStatistics 2", this.chiSqDist2.inverseF(1-significance), this.chiSqDist2.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTest 2", error1, binVariables, binBounds1, lambda, chiSqStatistics2, precision, true);
      
      this.chiSqDist3 = new ChiSquareDist(error2.length - 1);
      
      chiSqStatistics3 = VF.real("chiSqStatistics 3", this.chiSqDist3.inverseF(1-significance), this.chiSqDist3.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTest 3", error2, binVariables, binBounds2, lambda, chiSqStatistics3, precision, true);
   }
   
   @Override
   public void configureSearch() {
      // Search strategy
      solver.set(
            new RealStrategy(new RealVar[]{lambda,slope1,slope2}, new Cyclic(), new RealDomainMiddle())
       );
       // Uncomment if a time limit is necessary
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
      StringBuilder st = new StringBuilder();
      solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, slope2, precision);

      st.append("---\n");
      if(solver.isFeasible() == ESat.TRUE) {
         st.append("Curve 1: "+slope1.toString());
         st.append("\n");
         st.append("Curve 2: "+slope2.toString());
         st.append("\n");
         st.append("Lambda: "+lambda.toString());
         st.append("\n");
         st.append(chiSqStatistics1.getLB()+" "+chiSqStatistics1.getUB());
         st.append("\n");
         st.append(chiSqStatistics2.getLB()+" "+chiSqStatistics2.getUB());
         st.append("\n");
         st.append(chiSqStatistics3.getLB()+" "+chiSqStatistics3.getUB());
         st.append("\n");
      }else{
         st.append("No solution!");
      }

      System.out.println(st.toString());
   }

   @Override
   public void prettyOut() {
       
   }
   
   public static double[] generateObservations(Random rnd, double trueSlope, double truePoissonRate, int nbObservations){
      PoissonDist dist = new PoissonDist(truePoissonRate);
      return DoubleStream.iterate(1, i -> i + 1).map(i -> i*trueSlope + dist.inverseF(rnd.nextDouble())).limit(nbObservations).toArray();
   }
   
   public static void fitParameters(){
      String[] str={"-log","SOLUTION"};
      
      double[] observations1;
      double[] observations2;
      
      int nbObservations = 100;
      
      double truePoissonRate = 15;
      double trueSlope1 = 1;
      double trueSlope2 = 1;
      
      Random rnd = new Random(123);
      observations1 = generateObservations(rnd, trueSlope1, truePoissonRate, nbObservations);
      observations2 = generateObservations(rnd, trueSlope2, truePoissonRate, nbObservations);
      System.out.println();
      
      int bins = 20;
      double[] binBounds1 = DoubleStream.iterate(0, i -> i + 2).limit(bins).toArray();
      double[] binBounds2 = DoubleStream.iterate(0, i -> i + 2).limit(bins).toArray();
      double significance = 0.05;
   
      TwoLinearModelsFitPoisson fit = new TwoLinearModelsFitPoisson(observations1, binBounds1, observations2, binBounds2, significance);
      fit.execute(str);
      fit.getSolver().getIbex().release();
      fit = null;
      System.gc();
   }
   
   public static void main(String[] args) {
      
      fitParameters();
      
   }
}
