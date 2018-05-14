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


package org.chocosolver.samples.statistical.hotelling.multinormal;

import java.util.ArrayList;
import java.util.Arrays;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareIndependence;
import org.chocosolver.solver.constraints.statistical.hotelling.tSquareStatistic;
import org.chocosolver.solver.constraints.statistical.t.tStatistic;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.FisherFDist;
import umontreal.iro.lecuyer.probdist.StudentDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

/***
 * https://people.richland.edu/james/lecture/m170/ch13-2wy.html
 * https://en.wikipedia.org/wiki/Two-way_analysis_of_variance
 * 
 * @author Roberto Rossi
 *
 */

public class HotellingTwoWay extends AbstractProblem {
   
   public RealVar muVar; //Grand mean
   public RealVar alphaVar[]; //additive main effect from the first factor
   public RealVar betaVar[]; //additive main effect from the second factor
   public RealVar gammaVar[][]; //non-additive interaction effect from both factors 
   
   public RealVar[][][] error;
   
   public double[][][] observations;
   
   public RealVar t;
   
   int A, B, K;
   
   double precision = 0.1;
   
   double significance;
   
   public HotellingTwoWay(double[][][] observations, double significance) {
      this.observations = observations;
      this.A = this.observations.length;
      this.B = this.observations[0].length;
      this.K = this.observations[0][0].length;
      this.significance = significance;
   }
   
   @Override
   public void createSolver() {
      solver = new Solver("HotellingTwoWay");
   }
   
   @Override
   public void buildModel() {
      muVar = VariableFactory.real("GrandMean", 0, 0, precision, solver);
      alphaVar = VariableFactory.realArray("Alpha", A, 0, 0, precision, solver);
      betaVar = new RealVar[B];//VariableFactory.realArray("Beta", B, 0, 2, precision, solver);
      for(int b = 0; b < B; b++) 
         betaVar[b] = (b != 2) ? VariableFactory.real("Beta", 0, 0, precision, solver) : VariableFactory.real("Beta", 0, 0, precision, solver);
      gammaVar = new RealVar[A][];
      for(int a = 0; a < A; a++)
         gammaVar[a] = VariableFactory.realArray("Gamma ("+a+")", B, 0, 0, precision, solver);
      
      // Errors
      error = new RealVar[A][B][K];
      for(int a = 0; a < A; a++) {
         for(int b = 0; b < B; b++) {
            for(int k = 0; k < K; k++) {
               error[a][b][k] = VariableFactory.real("Error ("+(a+1)+","+(b+1)+","+(k+1)+")", 
                                                     -Integer.MAX_VALUE, Integer.MAX_VALUE, 
                                                     precision, solver);
               String errorExp = "{0}="+this.observations[a][b][k]+"-{1}-{2}-{3}-{4}";
               solver.post(new RealConstraint("error constraint ("+(a+1)+","+(b+1)+","+(k+1)+")", 
                     errorExp,
                     Ibex.HC4_NEWTON, 
                     new RealVar[]{error[a][b][k],muVar,alphaVar[a],betaVar[b],gammaVar[a][b]}
                     ));
            }
         }
      }
      
      /*RealVar mean = VariableFactory.real("mean ", 0, 0, precision, solver);
      StudentDist tDist = new StudentDist(A*B*K - 1);
      t = VariableFactory.real("tStatistic", tDist.inverseF(significance/2), tDist.inverseF(1-significance/2), precision, solver);
      tStatistic.decompose("tStatistic_cons", flatten(error), mean, t, precision);*/
      
      RealVar[] muVariable = new RealVar[A];
      for(int a = 0; a < A; a++)
         muVariable[a] = VariableFactory.real("Mu "+(a+1), 0, 0, precision, solver);
      
      RealVar[][] observationVariable = new RealVar[A][B*K];
      for(int a = 0; a < A; a++)
         observationVariable[a] = extract_i(a, error);
      
      // https://pdfs.semanticscholar.org/96d5/80ccf7c2c15426231efd5c4bb8327317cf72.pdf
      
      int p = A;
      int m = B*K;
      double[] statistic = {0,((p*m)/(m-p+1))*FisherFDist.inverseF(p, m-p+1, 1-significance)};
      
      tSquareStatistic.decompose("scoreConstraint", muVariable, transpose(observationVariable), 
            VF.real("T2A", statistic[0], statistic[1], precision, solver), precision);
      
      muVariable = new RealVar[A];
      for(int a = 0; a < A; a++)
         muVariable[a] = VariableFactory.real("Mu "+(a+1), 0, 0, precision, solver);
      
      observationVariable = new RealVar[A][B*K];
      for(int b = 0; b < B; b++)
         observationVariable[b] = extract_j(b, error);
      
      tSquareStatistic.decompose("scoreConstraint", muVariable, transpose(observationVariable), 
            VF.real("T2B", statistic[0], statistic[1], precision, solver), precision);
      
      double[][] binBounds = {{-3,-1,1,3},{-3,-1,1,3}};
      double chiSqUB = new ChiSquareDist((binBounds[0].length-1)*(binBounds[1].length-1)).inverseF(1-significance);
      double[] chiSqStatistic = {0,chiSqUB};
      
      for(int a = 0; a < A; a++) {
         for(int b = 0; b < B; b++) {
            ChiSquareIndependence.decomposition("chiSqConstraint", extract_i(a, error), extract_j(b, error), 
                  binBounds, VF.real("chiSqStatistics", chiSqStatistic[0], chiSqStatistic[1], precision, solver), precision, false);
         }
      }
   }
   
   private RealVar[][] transpose(RealVar[][] src) {
      RealVar[][] dest = new RealVar[src[0].length][src.length];
      for(int i = 0; i < src.length; i++) {
         for(int j = 0; j < src[0].length; j++) {
            dest[j][i] = src[i][j];
         }
      }
      return dest;
   }
   
   private RealVar[] extract_i(int i, RealVar[][][] matrix) {
      ArrayList<RealVar> flattened = new ArrayList<RealVar>();
      for(int j = 0; j < matrix[i].length; j++) {
         for(int k = 0; k < matrix[i][j].length; k++) {
            flattened.add(matrix[i][j][k]);
         }
      }
      return flattened.stream().toArray(RealVar[]::new);
   }
   
   private RealVar[] extract_j(int j, RealVar[][][] matrix) {
      ArrayList<RealVar> flattened = new ArrayList<RealVar>();
      for(int i = 0; i < matrix.length; i++) {
         for(int k = 0; k < matrix[i][j].length; k++) {
            flattened.add(matrix[i][j][k]);
         }
      }
      return flattened.stream().toArray(RealVar[]::new);
   }
   
   private RealVar[] flatten(RealVar[][][] matrix) {
      ArrayList<RealVar> flattened = new ArrayList<RealVar>();
      for(int i = 0; i < matrix.length; i++) {
         for(int j = 0; j < matrix[i].length; j++) {
            for(int k = 0; k < matrix[i][j].length; k++) {
               flattened.add(matrix[i][j][k]);
            }
         }
      }
      return flattened.stream().toArray(RealVar[]::new);
   }
   
   @Override
   public void configureSearch() {
      solver.set(
            new RealStrategy(new RealVar[]{muVar}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(alphaVar, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(betaVar, new Cyclic(), new RealDomainMiddle())
       );
   }
   
   @Override
   public void solve() {
      StringBuilder st = new StringBuilder();
      solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, betaVar[2], precision);
      //do{
         st.append("---\n");
         if(solver.isFeasible() == ESat.TRUE) {
            st.append("Mu: "+muVar.toString());
            st.append("\n");
            st.append("Alpha: "+Arrays.toString(alphaVar));
            st.append("\n");
            st.append("Beta: "+Arrays.toString(betaVar));
            st.append("\n");
            st.append("Gamma: "+Arrays.deepToString(gammaVar));
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
   
   public static double[][][] generateObservations(double mu, double[] alpha, double[] beta, double[][] gamma, double sigma, int K, MRG32k3a rng){
      NormalGen gen = new NormalGen(rng, 0, sigma);
      double[][][] observations = new double [alpha.length][beta.length][K];
      for(int a = 0; a < alpha.length; a++) {
         for(int b = 0; b < beta.length; b++) {
            for(int k = 0; k < K; k++) {
               observations[a][b][k] = mu + alpha[a] + beta[b] + gamma[a][b] + gen.nextDouble();
            }
         }
      }
      return observations;
   }
   
   public static void hotellingTwoWay() {
      String[] str={"-log","SOLUTION"};
      
      double mu = 0;
      double[] alpha = {0, 0, 0};
      double[] beta = {0, 0, 1};
      double[][] gamma = {
         {0, 0, 0},
         {0, 0, 0},
         {0, 0, 0}};
      double sigma = 1;
      int K = 5;
      
      MRG32k3a rng = new MRG32k3a();
      double[][][] observations = generateObservations(mu, alpha, beta, gamma, sigma, K, rng);
      observations = generateObservations(mu, alpha, beta, gamma, sigma, K, rng);
      observations = generateObservations(mu, alpha, beta, gamma, sigma, K, rng);
      System.out.println(Arrays.deepToString(observations));
      double significance = 0.05;
      
      HotellingTwoWay hotelling = new HotellingTwoWay(observations, significance);
      hotelling.execute(str);
   }
   
   public static void main(String[] args) {
      hotellingTwoWay();
   }
}
