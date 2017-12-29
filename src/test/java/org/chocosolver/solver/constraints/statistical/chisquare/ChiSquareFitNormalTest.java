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

package org.chocosolver.solver.constraints.statistical.chisquare;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitNormal;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChiSquareFitNormalTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      System.gc();
      Thread.sleep(3000);
   }

   @Test
   public void test() {
      String[] str={"-log","SOLUTION"};
      
      double[][] values = {{2,2},{-1,-1},{0,0},{3,3},{1,1},{2,2}};
      int[][] binCounts = {{0,6},{0,6},{0,6},{0,6},{0,6},{0,6},{0,6}};
      double [] binBounds = {-3.5,-2.5,-1.5,-0.5,0.5,1.5,2.5,3.5};
      double[] normalMean = {0,0};
      double[] normalStd = {1,1};
      
      double[] chiSqStatistic = {0,50};
      
      ChiSquareFitNormalReal cs = new ChiSquareFitNormalReal(values, binCounts, binBounds, normalMean, normalStd, chiSqStatistic);
      cs.execute(str);
      cs.getSolver().getIbex().release();
   }
   
   public class ChiSquareFitNormalReal extends AbstractProblem {
      
      public RealVar[] valueVariables;
      public IntVar[] binVariables;
      public RealVar meanVariable;
      public RealVar stdVariable;
      public RealVar chiSqstatisticVariable;
      
      int[][] binCounts;
      double[][] values;
      double[] binBounds;
      double[] normalMean;
      double[] normalStd;
      double[] chiSqStatistic;
      
      double precision = 1.e-4;
      
      public ChiSquareFitNormalReal(double[][] values,
                                    int[][] binCounts, 
                                    double[] binBounds,
                                    double[] normalMean,
                                    double[] normalStd,
                                    double[] chiSqStatistic){
         this.values = values.clone();
         this.binCounts = binCounts.clone();
         this.binBounds = binBounds.clone();
         this.normalMean = normalMean.clone();
         this.normalStd = normalStd.clone();
         this.chiSqStatistic = chiSqStatistic.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquare");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new RealVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.real("Value "+(i+1), values[i][0], values[i][1], precision, solver);
         
         binVariables = new IntVar[this.binCounts.length];
         for(int i = 0; i < this.binCounts.length; i++)
            binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
         
         meanVariable = VF.real("Normal mean", normalMean[0], normalMean[1], precision, solver);
         
         stdVariable = VF.real("Normal std", normalStd[0], normalStd[1], precision, solver);
         
         chiSqstatisticVariable = VF.real("chiSqStatistics", chiSqStatistic[0], chiSqStatistic[1], precision, solver);
         
         ChiSquareFitNormal.decomposition("chiSqConstraint", valueVariables, binVariables, binBounds, meanVariable, stdVariable, chiSqstatisticVariable, precision, false);
      }
      
      @Override
      public void configureSearch() {
         solver.set(
               new RealStrategy(valueVariables, new Cyclic(), new RealDomainMiddle()),
               IntStrategyFactory.activity(binVariables,1234),
               new RealStrategy(new RealVar[]{meanVariable, stdVariable}, new Cyclic(), new RealDomainMiddle()),
               new RealStrategy(new RealVar[]{chiSqstatisticVariable}, new Cyclic(), new RealDomainMiddle())
               );
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariables.length; i++){
                 st.append("("+valueVariables[i].getLB()+", "+valueVariables[i].getUB()+"), ");
              }
              st.append("\n");
              for(int i = 0; i < binVariables.length; i++){
                 st.append(binVariables[i].getValue()+", ");
              }
              st.append("\n");
              st.append(meanVariable.getLB()+" "+meanVariable.getUB());
              st.append("\n");
              st.append(stdVariable.getLB()+" "+stdVariable.getUB());
              st.append("\n");
              st.append(chiSqstatisticVariable.getLB()+" "+chiSqstatisticVariable.getUB());
              st.append("\n");
              
              assertTrue(chiSqstatisticVariable.getLB() <= 35);
              assertTrue(chiSqstatisticVariable.getUB() >= 34);
           }else{
              st.append("No solution!");
           }
        //}while(solution = solver.nextSolution());
        System.out.println(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }

}
