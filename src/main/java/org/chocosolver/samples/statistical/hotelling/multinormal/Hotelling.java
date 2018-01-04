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

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
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
import org.chocosolver.util.ESat;

/**
 * Hotelling t^2 statistical constraint to to compare means of two or more samples.
 *  
 * @author Roberto Rossi
 * @see <a>R. Rossi, O. Agkun, S. Prestwich, A. Tarim, "Declarative Statistics," arxiv:1708.01829, Section 5.3.</a>
 */

public class Hotelling extends AbstractProblem {
   public RealVar[] muVariable;
   public RealVar[][] observationVariable;
   public RealVar statisticVariable;

   double[][] muDomains;
   double[][] observations;
   double[] statistic;

   double precision = 1.e-2;

   public Hotelling(
         double[][] muDomains,
         double[][] observations,
         double[] statistic){

      this.muDomains = muDomains;
      this.observations = observations;
      this.statistic = statistic;
   }

   @Override
   public void createSolver() {
      solver = new Solver("Hotelling");
   }

   @Override
   public void buildModel() {
      muVariable = new RealVar[this.muDomains.length];
      for(int i = 0; i < this.muVariable.length; i++)
         muVariable[i] = VariableFactory.real("Mu "+(i+1), muDomains[i][0], muDomains[i][1], precision, solver);

      observationVariable = new RealVar[this.observations.length][this.observations[0].length];
      for(int i = 0; i < this.observations.length; i++){
         for(int j = 0; j < this.observations[i].length; j++){
            observationVariable[i][j] = VariableFactory.real("Obs_"+(i+1)+"_"+(j+1), observations[i][j], observations[i][j], precision, solver);
         }
      }
      
      /**
       * Additional constraints as discussed in the research paper. Uncomment to post.
       *
      solver.post(new RealConstraint("mean equality ",
                                     "{0}={1}",
                                     Ibex.HC4_NEWTON, 
                                     new RealVar[]{muVariable[0],muVariable[1]}
                                    ));
      
      solver.post(new RealConstraint("mean equality ",
                                     "{0}={1}",
                                     Ibex.HC4_NEWTON, 
                                     new RealVar[]{muVariable[1],muVariable[2]}
                                    ));
      /*********************************************************************************/
      
      statisticVariable = VF.real("T2", statistic[0], statistic[1], precision, solver);

      tSquareStatistic.decompose("scoreConstraint", muVariable, observationVariable, statisticVariable, precision);
   }

   @Override
   public void configureSearch() {
      RealStrategy strat1 = new RealStrategy(muVariable, new Cyclic(), new RealDomainMiddle());
      RealStrategy strat2 = new RealStrategy(flatten(observationVariable), new Cyclic(), new RealDomainMiddle());
      RealStrategy strat3 = new RealStrategy(new RealVar[]{statisticVariable}, new Cyclic(), new RealDomainMiddle());
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
      solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, statisticVariable, precision);
      st.append("---\n");
      if(solver.isFeasible() == ESat.TRUE) {
         for(int i = 0; i < muVariable.length; i++){
            st.append("("+muVariable[i].getLB()+","+muVariable[i].getUB()+"), ");
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
   
   public static void hotelling(){
      String[] str={"-log","SILENT"};
      
      double[][] observations = {
            {3.57329, 9.83132, 9.80335}, 
            {6.5655, 9.7379, 8.79726}, 
            {-2.06033, 6.6339, 13.6045}, 
            {0.469477, 8.20049, 9.4932}, 
            {3.05632, 7.19737, 8.50685}, 
            {5.54063, 9.19586, 9.22433}};
      
      double[][] muDomains = {{0,20},{0,20},{0,20}};
      double[] statistic = {0,26.9539};
      
      Hotelling scoreReal = new Hotelling(muDomains, observations, statistic);
      scoreReal.execute(str);
   }
   
   public static void main(String[] args) {
      
      hotelling();
      
   }
}
