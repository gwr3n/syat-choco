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
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.hotelling.tSquareStatistic;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.randvarmulti.MultinomialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class MultinomialCIChiSquare extends AbstractProblem{

   public double[][] observations;
   public RealVar[] p;
   public double[] actualP;
   public RealVar[][] observationVariable;
   public RealVar[][] covarianceMatrix;

   int categories;
   long nObs;
   double confidence;
   
   double[] statistic;

   public MultinomialCIChiSquare(double[][] observations,
                                 double[] actualP,
                                 double[] statistic){
      this.categories = actualP.length;
      int[] values = new int[observations.length];
      for(int i = 0; i < observations.length; i++){
         for(int j = 0; j < observations[i].length; j++){
            if(observations[i][j] == 1)
               values[i] = j;
         }
      }
      this.nObs = Arrays.stream(values).count();
      this.observations = observations;
      this.actualP = actualP;
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
         p[i] = VariableFactory.real("p "+(i+1), actualP[i], actualP[i], precision, solver);
      
      covarianceMatrix = new RealVar[this.categories][this.categories];
      for(int i = 0; i < this.covarianceMatrix.length; i++){
         for(int j = 0; j < this.covarianceMatrix[i].length; j++){
            covarianceMatrix[i][j] = VariableFactory.real("Sigma_"+(i+1)+"_"+(j+1), -1, 1, precision, solver);
            if(i==j){
               solver.post(new RealConstraint("cov_"+i+"_"+j,"{0}*(1-{0})={1}",
                                              Ibex.HC4_NEWTON,
                                              new RealVar[]{p[i],covarianceMatrix[i][j]})
                     );
            }else{
               solver.post(new RealConstraint("cov_"+i+"_"+j,"-{0}*{1}={2}",
                                              Ibex.HC4_NEWTON,
                                              new RealVar[]{p[i],p[j],covarianceMatrix[i][j]})
                     );
            }
         }
      }
      
      observationVariable = new RealVar[this.observations.length][this.observations[0].length];
      for(int i = 0; i < this.observations.length; i++){
         for(int j = 0; j < this.observations[i].length; j++){
            observationVariable[i][j] = VariableFactory.real("Obs_"+(i+1)+"_"+(j+1), observations[i][j], observations[i][j], precision, solver);
         }
      }
      
      statisticVariable = VF.real("chiSquare", statistic[0], statistic[1], precision, solver);
      
      tSquareStatistic.decompose("scoreConstraint", p, observationVariable, covarianceMatrix, statisticVariable, precision);
   }

   @Override
   public void configureSearch() {
      RealStrategy strat1 = new RealStrategy(p, new Cyclic(), new RealDomainMiddle());
      RealStrategy strat2 = new RealStrategy(flatten(observationVariable), new Cyclic(), new RealDomainMiddle());
      RealStrategy strat3 = new RealStrategy(flatten(covarianceMatrix), new Cyclic(), new RealDomainMiddle());
      solver.set(strat1, strat2, strat3);
   }
   
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
     boolean solution = solver.findSolution();
     //do{
        st.append("---\n");
        if(solution) {
           for(int i = 0; i < p.length; i++){
              st.append("("+p[i].getLB()+","+p[i].getUB()+"), ");
           }
           st.append("\n");
           st.append(statisticVariable.getLB()+" "+statisticVariable.getUB());
           st.append("\n");
           
           coverageProbability++;
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     System.out.println(st.toString());
   }
   
   @Override
   public void prettyOut() {
       
   }
   
   static double coverageProbability = 0;
   
   public static void main(String[] args) {
      String[] str={"-log","SOLUTION"};
      
      double confidence = 0.9;
      double[] p = {0.3,0.3,0.2}; 
      /** CAREFUL this is actually a ChiSquareDist with n-1 DOF. There is a bug in the library **/
      double[] statistic = {0,(new ChiSquareDist(p.length)).inverseF(confidence)};
      
      int replications = 200;
      int sampleSize = 30;
      
      MRG32k3a rng = new MRG32k3a();
      UniformGen gen1 = new UniformGen(rng);
      MultinomialGen multinomial = new MultinomialGen(gen1, p, 1);
      for(int i = 0; i < replications; i++){
         double[][] observations = new double[sampleSize][p.length];
         multinomial.nextArrayOfPoints(observations, 0, sampleSize);
         MultinomialCIChiSquare cs = new MultinomialCIChiSquare(observations, p, statistic);
         cs.execute(str);
         System.gc();
         try {
            Thread.sleep(50);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      
      System.out.println("Coverage probability: "+coverageProbability/replications);
   }
   
}
