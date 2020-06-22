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

package org.chocosolver.solver.constraints.nary.deviation;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.RealStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StandardDeviationTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      System.gc();
      Thread.sleep(3000);
   }

   @Test
   public void testInteger() {
      String[] str={"-log","SOLUTION"};
      
      int[][] values = {{1},{2},{3},{4},{5},{6},{7},{8},{9}}; 
      
      IntegerStandardDeviation standardDeviation = new IntegerStandardDeviation(values, new double[]{0,100});
      standardDeviation.execute(str);
      standardDeviation.getSolver().getIbex().release();
   }
   
   @Test
   public void testReal() {
      String[] str={"-log","SOLUTION"};
      
      double[][] values = {{1},{2},{3},{4},{5},{6},{7},{8},{9}}; 
      
      RealStandardDeviation standardDeviation = new RealStandardDeviation(values, new double[]{0,100});
      standardDeviation.execute(str);
      standardDeviation.getSolver().getIbex().release();
   }

   class IntegerStandardDeviation extends AbstractProblem {
      public IntVar[] valueVariables;
      public RealVar standardDeviationVariable;
      
      public int[][] values;
      public double[] standardDeviation;
      
      double precision = 1.e-4;
      
      public IntegerStandardDeviation(int[][] values, double[] standardDeviation){
         this.values = values;
         this.standardDeviation = standardDeviation;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("IntegerStandardDeviation");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new IntVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.enumerated("Value"+(i+1), values[i], solver);
         
         standardDeviationVariable = VariableFactory.real("StandardDeviation", standardDeviation[0], standardDeviation[1], precision, solver);
         
         StandardDeviation.decompose("StandardDeviationConstraint", valueVariables, standardDeviationVariable, precision);
      }
      
      public void configureSearch() {
         AbstractStrategy<IntVar> strat = IntStrategyFactory.activity(valueVariables,1234);
         solver.set(strat);
      }
    
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariables.length; i++){
                 st.append(valueVariables[i].getValue()+", ");
              }
              st.append("\n");
              st.append(standardDeviationVariable.getLB()+" "+standardDeviationVariable.getUB());
              st.append("\n");
              
              assertTrue(standardDeviationVariable.getLB() <= Math.sqrt(7.5));
              assertTrue(standardDeviationVariable.getUB() >= Math.sqrt(7.5));
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
   
   class RealStandardDeviation extends AbstractProblem {
      public RealVar[] valueVariables;
      public RealVar standardDeviationVariable;
      
      public double[][] values;
      public double[] standardDeviation;
      
      double precision = 1.e-4;
      
      public RealStandardDeviation(double[][] values, double[] standardDeviation){
         this.values = values;
         this.standardDeviation = standardDeviation;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("RealStandardDeviation");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new RealVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.real("Value"+(i+1), values[i][0], values[i][0], precision, solver);
         
         standardDeviationVariable = VariableFactory.real("StandardDeviation", standardDeviation[0], standardDeviation[1], precision, solver);
         
         StandardDeviation.decompose("StandardDeviationConstraint", valueVariables, standardDeviationVariable, precision);
      }
      
      public void configureSearch() {
         AbstractStrategy<RealVar> strat = RealStrategyFactory.cyclic_middle(valueVariables);
         solver.set(strat);
      }
    
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariables.length; i++){
                 st.append("("+valueVariables[i].getLB()+","+valueVariables[i].getUB()+"), ");
              }
              st.append("\n");
              st.append(standardDeviationVariable.getLB()+" "+standardDeviationVariable.getUB());
              st.append("\n");
              
              assertTrue(standardDeviationVariable.getLB() <= Math.sqrt(7.5));
              assertTrue(standardDeviationVariable.getUB() >= Math.sqrt(7.5));
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
