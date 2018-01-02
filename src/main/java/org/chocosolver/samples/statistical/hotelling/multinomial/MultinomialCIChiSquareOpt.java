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

package org.chocosolver.samples.statistical.hotelling.multinomial;

import java.util.Arrays;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.hotelling.tSquareStatistic;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.randvarmulti.MultinomialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

/**
 * Computation of multinomial proportions confidence intervals.
 * 
 * This class is concerned with determining interval bounds.
 * 
 * @author Roberto Rossi
 * @see R. Rossi, O. Agkun, S. Prestwich, A. Tarim, "Declarative Statistics," arxiv:1708.01829, Section 5.4
 */

public class MultinomialCIChiSquareOpt extends AbstractProblem{

   public double[][] observations;
   public RealVar[] p;
   public RealVar[][] observationVariable;
   public RealVar[][] covarianceMatrix;

   int categories;
   long nObs;
   double confidence;
   
   double[] statistic;

   public MultinomialCIChiSquareOpt(double[][] observations,
                                    double[] statistic){
      this.categories = observations[0].length;
      int[] values = new int[observations.length];
      for(int i = 0; i < observations.length; i++){
         for(int j = 0; j < observations[i].length; j++){
            if(observations[i][j] == 1)
               values[i] = j;
         }
      }
      this.nObs = Arrays.stream(values).count();
      this.observations = observations;
      this.statistic = statistic;
   }
   
   RealVar statisticVariable;
   
   double precision = 1.e-4;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("MultinomialCIChiSquare");
   }
   
   /**
    * https://www.ncbi.nlm.nih.gov/pubmed/9598426
    * http : // www.jstor.org/stable/1266673?seq = 1
    */
    
   @Override
   public void buildModel() {
      p = new RealVar[this.categories];
      for(int i = 0; i < this.categories; i++)
         p[i] = VariableFactory.real("p "+(i+1), 0+precision, 1-precision, precision, solver);
      
      observationVariable = new RealVar[this.observations.length][this.observations[0].length];
      for(int i = 0; i < this.observations.length; i++){
         for(int j = 0; j < this.observations[i].length; j++){
            observationVariable[i][j] = VariableFactory.real("Obs_"+(i+1)+"_"+(j+1), observations[i][j], observations[i][j], precision, solver);
         }
      }
      
      statisticVariable = VF.real("chiSquare", statistic[0], statistic[1], precision, solver);
      
      tSquareStatistic.decompose("scoreConstraint", p, observationVariable, statisticVariable, precision);
   }
   
   @Override
   public void configureSearch() {
      RealStrategy strat1 = new RealStrategy(p, new Cyclic(), new RealDomainMiddle());
      RealStrategy strat2 = new RealStrategy(new RealVar[]{statisticVariable}, new Cyclic(), new RealDomainMiddle());
      solver.set(strat1, strat2);
   }
   
   @SuppressWarnings("unused")
   private RealVar[] flatten(RealVar[][] matrix){
      RealVar[] array = new RealVar[matrix.length*matrix[0].length];
      for(int i = 0; i < matrix.length; i++){
         for(int j = 0; j < matrix[i].length; j++){
            array[i*matrix[i].length+j] = matrix[i][j];
         }
      }
      return array;
   }
   
   @Override
   public void solve() {
      StringBuilder st = new StringBuilder();
      
      // This computes the upper bound for p[0]
      solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, p[0], precision);
      
      // This computes the lower bound for p[0]
      //solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, p[0], precision);
      
      st.append("---\n");
      st.append("Optimal solution\n");
      st.append("---\n");
      if(solver.isFeasible() == ESat.TRUE) {
         for(int i = 0; i < p.length; i++){
            st.append("p["+i+"]("+p[i].getLB()+","+p[i].getUB()+"), ");
         }
         st.append("\n");
         st.append(statisticVariable.getLB()+" "+statisticVariable.getUB());
         st.append("\n");

      }else{
         st.append("No solution!");
      }
      System.out.println(st.toString());
   }
   
   @Override
   public void prettyOut() {
       
   }
   
   public static void main(String[] args) {
      String[] str={"-log","SOLUTION"};
      
      double confidence = 0.9;
      double[] p = {0.3,0.3,0.3}; 
      
      int sampleSize = 50;
      
      double[] statistic = {
            (new umontreal.iro.lecuyer.probdist.FisherFDist(p.length, sampleSize - p.length)).inverseF(confidence), 
            (new umontreal.iro.lecuyer.probdist.FisherFDist(p.length, sampleSize - p.length)).inverseF(confidence)
            };
      
      MRG32k3a rng = new MRG32k3a();
      UniformGen gen1 = new UniformGen(rng);
      MultinomialGen multinomial = new MultinomialGen(gen1, p, 1);
      double[][] observations = new double[sampleSize][p.length];
      multinomial.nextArrayOfPoints(observations, 0, sampleSize);
      MultinomialCIChiSquareOpt cs = new MultinomialCIChiSquareOpt(observations, statistic);
      cs.execute(str);
      cs.getSolver().getIbex().release();
      cs = null;
      System.gc();
      try {
         Thread.sleep(100);
      } catch (InterruptedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      int[] frequencies = {3,5,2};
      double[][] intervals = computeQuesenberryHurstCI(confidence, frequencies);
      
      System.out.println("Quesenberry-Hurst Confidence Intervals");
      System.out.println(Arrays.deepToString(intervals));
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
   
}
