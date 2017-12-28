/*
 * syat-choco: a Choco extension for Declarative Statistics
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

package org.chocosolver.solver.constraints.nary.mean;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;

/**
 * Decompositions of the {@code MEAN} constraint
 * 
 * @author Roberto Rossi
 * @see <a href="https://en.wikipedia.org/wiki/Mean">Mean</a>
 */

public class Mean {
   
   /**
    * {@code MEAN} constraint decomposition for integer valued observations
    * 
    * @param name constraint name
    * @param observations observations
    * @param mean mean value
    * @param precision Ibex precision
    */
   
   public static void decompose(String name,
                                IntVar[] observations,
                                RealVar mean,
                                double precision){
      
      Solver solver = mean.getSolver();
      
      String exp = "(";
      for(int i = 0; i < observations.length; i++){
         if(i < observations.length - 1)
            exp += "{"+i+"}+";
         else
            exp += "{"+i+"})/"+observations.length+"={"+(i+1)+"}";
      }
      
      RealVar[] allRealVariables = new RealVar[observations.length + 1];
      RealVar[] realObservations = VF.real(observations, precision);
      System.arraycopy(realObservations, 0, allRealVariables, 0, observations.length);
      allRealVariables[realObservations.length] = mean;
      
      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, allRealVariables));
   }
   
   /**
    * {@code MEAN} constraint decomposition for real valued observations
    * 
    * @param name constraint name
    * @param observations observations
    * @param mean mean value
    * @param precision Ibex precision
    */
   
   public static void decompose(String name,
                                RealVar[] observations,
                                RealVar mean,
                                double precision){
      
      Solver solver = mean.getSolver();
      
      String exp = "(";
      for(int i = 0; i < observations.length; i++){
         if(i < observations.length - 1)
            exp += "{"+i+"}+";
         else
            exp += "{"+i+"})/"+observations.length+"={"+(i+1)+"}";
      }
      
      RealVar[] allRealVariables = new RealVar[observations.length + 1];
      System.arraycopy(observations, 0, allRealVariables, 0, observations.length);
      allRealVariables[observations.length] = mean;
      
      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, allRealVariables));
   }
}
