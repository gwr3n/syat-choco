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

package org.chocosolver.samples.statistical.anova;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

import umontreal.iro.lecuyer.probdist.FisherFDist;
import umontreal.iro.lecuyer.probdist.NormalDist;

/**
 * This class follows the discussion  
 * 
 * R. Rossi, O. Agkun, S. Prestwich, A. Tarim, 
 * "Declarative Statistics," arxiv:1708.01829, Section 5.3.
 * 
 * It is a declarative implementation of a classical ANOVA analysis.
 * 
 * The model is infeasible for the data provided ({@code observations}) 
 * at significance level 0.05.
 * 
 * We recall that in declarative statistics, an infeasibility signifies rejection 
 * of the null hypothesis. In ANOVA the null hypothesis is that the means of the 
 * normal distributions that generated the three group realisations are equal, 
 * we therefore conclude that there is evidence that the expected values in the 
 * three groups differ at the prescribed significance level.
 * 
 * @author Roberto Rossi
 *
 */

public class ANOVA extends AbstractProblem {
   
   private RealVar[] meansWithinGroups;
   private RealVar overallMean;
   
   private RealVar ssdBetweenGroups;
   private RealVar ssdWithinGroups;
   
   private RealVar fStatistics;
   
   private double[][] observations;
   private double significance;
   
   private double precision = 1.e-2;
   
   private FisherFDist fDist;
   
   public ANOVA(double[][] observations,
                double significance){
      this.observations = observations;
      this.significance = significance;
   }
   
   @Override
   public void createSolver() {
       solver = new Solver("ANOVA");
   }
   
   @Override
   public void buildModel() {
      int groups = observations.length;
      int samples = observations[0].length;
      
      double overall = 0;
      meansWithinGroups = new RealVar[groups];
      for(int i = 0; i < groups; i++){
         double mean = Arrays.stream(this.observations[i]).average().getAsDouble();
         overall += mean;
         meansWithinGroups[i] = VariableFactory.real("meansWithinGroups", mean, mean, precision, solver);
      }
      overall /= groups;
      
      overallMean = VariableFactory.real("overallMean", overall, overall, precision, solver);
      
      String overallMeanExp = "(";
      for(int i = 0; i < groups; i++){
         overallMeanExp += "{"+i+"}" + (i == groups - 1 ? "" : "+");
         
      }
      overallMeanExp += ")/"+groups+"={"+groups+"}";
      
      RealVar[] overallMeanRV = new RealVar[groups+1]; 
      System.arraycopy(meansWithinGroups, 0, overallMeanRV, 0, groups);
      overallMeanRV[groups] = overallMean;
      solver.post(new RealConstraint("overallMean ",
            overallMeanExp,
            Ibex.HC4_NEWTON, 
            overallMeanRV
            ));
      
      
      ssdBetweenGroups = VariableFactory.real("ssdBetweenGroups", 0, 1000, precision, solver);
      
      String ssdBetweenGroupsExp = "";
      for(int i = 0; i < groups; i++){
         ssdBetweenGroupsExp += samples + "*({"+i+"}-{"+groups+"})^2" + (i == groups - 1 ? "" : "+");
      }
      ssdBetweenGroupsExp += "={"+(groups+1)+"}";
      
      RealVar[] ssdBetweenGroupsRV = new RealVar[groups+2]; 
      System.arraycopy(meansWithinGroups, 0, ssdBetweenGroupsRV, 0, groups);
      ssdBetweenGroupsRV[groups] = overallMean;
      ssdBetweenGroupsRV[groups+1] = ssdBetweenGroups;
      
      /* "between groups" mean squared differences */
      solver.post(new RealConstraint("ssdBetweenGroups", ssdBetweenGroupsExp, Ibex.HC4_NEWTON, ssdBetweenGroupsRV));
      
      
      ssdWithinGroups = VariableFactory.real("ssdWithinGroups", 0, 1000, precision, solver);
      
      String ssdWithinGroupsExp = "";
      for(int i = 0; i < groups; i++){
         for(int j = 0; j < samples; j++){
            ssdWithinGroupsExp += "("+observations[i][j]+"-{"+i+"})^2" + (i == groups - 1 && j == samples - 1 ? "" : "+");
         }
      }
      ssdWithinGroupsExp += "={"+groups+"}";
      
      RealVar[] ssdWithinGroupsRV = new RealVar[groups+1];
      System.arraycopy(meansWithinGroups, 0, ssdWithinGroupsRV, 0, groups);
      ssdWithinGroupsRV[groups] = ssdWithinGroups;
      
      /* "within groups" mean squared differences */
      solver.post(new RealConstraint("ssdWithinGroups", ssdWithinGroupsExp, Ibex.HC4_NEWTON, ssdWithinGroupsRV));
      
      
      this.fDist = new FisherFDist(groups-1,(samples-1)*groups);
      fStatistics = VF.real("fStatistics", 0, this.fDist.inverseF(1-significance), precision, solver);
      
      /* Fisher's F-ratio statistic */
      solver.post(new RealConstraint("fStatistics ",
                                     "({0}/"+(groups-1)+")/({1}/"+((samples-1)*groups)+")={2}",
                                     Ibex.HC4_NEWTON, 
                                     new RealVar[]{ssdBetweenGroups, ssdWithinGroups, fStatistics}
                                    ));
   }
   
   @Override
   public void configureSearch() {
      
      solver.set(
            new RealStrategy(meansWithinGroups, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{overallMean, ssdBetweenGroups, ssdWithinGroups}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{fStatistics}, new Cyclic(), new RealDomainMiddle())
       );
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     boolean solution = solver.findSolution();
     //do{
        st.append("---\n");
        if(solution) {
           st.append(overallMean.toString()+", "+ssdBetweenGroups.toString()+", "+ssdWithinGroups.toString()+"\n");
           for(int i = 0; i < observations.length; i++){
              st.append(meansWithinGroups[i].toString()+", ");
           }
           st.append("\n");
           st.append(fStatistics.getLB()+" "+fStatistics.getUB());
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
   
   public static double[] generateObservations(Random rnd, double slope, double intercept, double normalMean, double normalstd, int nbObservations){
      NormalDist dist = new NormalDist(normalMean, normalstd);
      return DoubleStream.iterate(1, i -> i + 1).map(i -> slope*i - intercept + dist.inverseF(rnd.nextDouble())).limit(nbObservations).toArray();
   }
   
   public static void anova(){
      String[] str={"-log","SOLUTION"};
      
      double[][] observations = {
            {3.57329, 6.5655, -2.06033, 0.469477, 3.05632, 5.54063}, 
            {9.83132, 9.7379, 6.6339, 8.20049, 7.19737, 9.19586}, 
            {9.80335, 8.79726, 13.6045, 9.4932, 8.50685, 9.22433}};
      
      double significance = 0.05;
   
      ANOVA anova = new ANOVA(observations, significance);
      anova.execute(str);
   }
   
   public static void main(String[] args) {
      
      anova();
      
   }
}
