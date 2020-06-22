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
 * We consider two AR(1) stochastic process. We post a 
 * chi square independence statistical constraint on the
 * errors. This problem is discussed in 
 * 
 * R. Rossi, O. Agkun, S. Prestwich, A. Tarim, 
 * "Declarative Statistics," arxiv:1708.01829, Section 5.2
 * 
 * @author Roberto Rossi
 *
 */

public class TwoAR1TimeSeriesFit extends AbstractProblem {
   
   public RealVar parameter1;
   public RealVar constant1;
   
   public RealVar parameter2;
   public RealVar constant2;
   
   public RealVar[] residual1;
   public RealVar[] residual2;
   
   public IntVar[] binVariables1;
   public IntVar[] binVariables2;
   
   double[] observations1;
   double[] binBounds1;
   
   double[] observations2;
   double[] binBounds2;
   
   double significance;
   
   public TwoAR1TimeSeriesFit(double[] observations1,
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
   
   RealVar chiSqStatistics;
   
   RealVar[] allRV;
   
   double precision = 0.5;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("Two AR(1)");
   }
   
   @Override
   public void buildModel() {
      parameter1 = VariableFactory.real("Parameter 1", -2, 2, precision, solver);
      constant1 = VariableFactory.real("Constant 1", 0, 20, precision, solver);
      
      residual1 = new RealVar[this.observations1.length];
      for(int i = 0; i < this.residual1.length; i++){
         residual1[i] = VariableFactory.real("Residual 1 "+(i+1), 0, Arrays.stream(observations1).max().getAsDouble(), precision, solver);
         String residualExp = "{0}="+this.observations1[i]+"-{1}" + ((i > 0) ? "-{2}*"+this.observations1[i-1] : "");
         solver.post(new RealConstraint("residual 1 "+(i+1),
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{residual1[i],constant1,parameter1}
               ));
      }
      
      parameter2 = VariableFactory.real("Parameter 2", -2, 2, precision, solver);
      constant2 = VariableFactory.real("Constant 2", 0, 20, precision, solver);
      
      residual2 = new RealVar[this.observations2.length];
      for(int i = 0; i < this.residual2.length; i++){
         residual2[i] = VariableFactory.real("Residual 2 "+(i+1), 0, Arrays.stream(observations2).max().getAsDouble(), precision, solver);
         String residualExp = "{0}="+this.observations2[i]+"-{1}" + ((i > 0) ? "-{2}*"+this.observations2[i-1] : "");
         solver.post(new RealConstraint("residual 2 "+(i+1),
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{residual2[i],constant2,parameter2}
               ));
      }
      
      this.chiSqDist = new ChiSquareDist((this.binBounds1.length - 1)*(this.binBounds2.length - 1));
      
      double[][] binBounds = new double[2][];
      binBounds[0] = this.binBounds1;
      binBounds[1] = this.binBounds2;
      
      chiSqStatistics = VF.real("chiSqStatistics", this.chiSqDist.inverseF(1-significance), this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareIndependence.decomposition("chiSqTest", residual1, residual2, binBounds, chiSqStatistics, precision, true);
   }
   
   @Override
   public void configureSearch() {
      // Search strategy
      solver.set(
            new RealStrategy(new RealVar[]{constant1,parameter1,constant2,parameter2}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{chiSqStatistics}, new Cyclic(), new RealDomainMiddle())
       );
       // Uncomment if a time limit is necessary
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, constant1, precision);
     //do{
        st.append("---\n");
        if(solver.isFeasible() == ESat.TRUE) {
           st.append("Curve 1: "+parameter1.toString()+" "+constant1.toString());
           st.append("\n");
           st.append("Curve 2: "+parameter2.toString()+" "+constant2.toString());
           st.append("\n");
           st.append(chiSqStatistics.getLB()+" "+chiSqStatistics.getUB());
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
         poisson[i] = dist.inverseF(rnd.nextDouble()) + 0.5;
         zt = c + poisson[i] + phi*zt;
         observations[i] = zt;
      }
      return observations;
   }
   
   @SuppressWarnings("deprecation")
   public static void fitMostLikelyParameters(){
      String[] str={"-log","SOLUTION"};
      
      double[] observations1;
      double[] observations2;
      
      int nbObservations = 100;
      
      Random rnd = new Random(123);
      
      double constant = 5;
      double parameter = 0.5;
      double truePoissonRate = 5;
      
      observations1 = generateObservations(rnd, constant, parameter, truePoissonRate, nbObservations);
      observations2 = generateObservations(rnd, constant, parameter, truePoissonRate, nbObservations);
      
      int bins = 5;
      double[] binBounds1 = DoubleStream.iterate(0, i -> i + 3).limit(bins).toArray();
      double[] binBounds2 = DoubleStream.iterate(0, i -> i + 3).limit(bins).toArray();
      double significance = 0.05;
   
      TwoAR1TimeSeriesFit regression = new TwoAR1TimeSeriesFit(observations1, binBounds1, observations2, binBounds2, significance);
      regression.execute(str);
      try {
         regression.finalize();
      } catch (Throwable e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      regression = null;
      System.gc();
   }
   
   public static void main(String[] args) {
      
      fitMostLikelyParameters();
      
   }
}
