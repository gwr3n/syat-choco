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
import org.chocosolver.solver.constraints.nary.deviation.StandardError;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.RealStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StandardErrorTest {

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
      
      IntegerStandardError standardError = new IntegerStandardError(values, new double[]{0,100});
      standardError.execute(str);
      standardError.getSolver().getIbex().release();
   }
   
   @Test
   public void testReal() {
      String[] str={"-log","SOLUTION"};
      
      double[][] values = {{1},{2},{3},{4},{5},{6},{7},{8},{9}}; 
      
      RealStandardError standardError = new RealStandardError(values, new double[]{0,100});
      standardError.execute(str);
      standardError.getSolver().getIbex().release();
   }

   class IntegerStandardError extends AbstractProblem {
      public IntVar[] valueVariables;
      public RealVar standardErrorVariable;
      
      public int[][] values;
      public double[] standardError;
      
      double precision = 1.e-4;
      
      public IntegerStandardError(int[][] values, double[] standardError){
         this.values = values;
         this.standardError = standardError;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("IntegerStandardError");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new IntVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.enumerated("Value"+(i+1), values[i], solver);
         
         standardErrorVariable = VariableFactory.real("StandardError", standardError[0], standardError[1], precision, solver);
         
         StandardError.decompose("StandardDeviationConstraint", valueVariables, standardErrorVariable, precision);
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
              st.append(standardErrorVariable.getLB()+" "+standardErrorVariable.getUB());
              st.append("\n");
              
              assertTrue(standardErrorVariable.getLB() <= Math.sqrt(7.5)/Math.sqrt(9));
              assertTrue(standardErrorVariable.getUB() >= Math.sqrt(7.5)/Math.sqrt(9));
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
   
   class RealStandardError extends AbstractProblem {
      public RealVar[] valueVariables;
      public RealVar standardErrorVariable;
      
      public double[][] values;
      public double[] standardError;
      
      double precision = 1.e-4;
      
      public RealStandardError(double[][] values, double[] standardError){
         this.values = values;
         this.standardError = standardError;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("RealStandardError");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new RealVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.real("Value"+(i+1), values[i][0], values[i][0], precision, solver);
         
         standardErrorVariable = VariableFactory.real("StandardError", standardError[0], standardError[1], precision, solver);
         
         StandardError.decompose("StandardDeviationConstraint", valueVariables, standardErrorVariable, precision);
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
              st.append(standardErrorVariable.getLB()+" "+standardErrorVariable.getUB());
              st.append("\n");
              
              assertTrue(standardErrorVariable.getLB() <= Math.sqrt(7.5)/Math.sqrt(9));
              assertTrue(standardErrorVariable.getUB() >= Math.sqrt(7.5)/Math.sqrt(9));
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
