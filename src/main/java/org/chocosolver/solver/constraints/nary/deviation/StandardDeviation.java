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

import java.util.Arrays;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.mean.Mean;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Decompositions of the {@code STANDARD_DEVIATION} constraint
 * 
 * @author Roberto Rossi
 * @see <a href="https://en.wikipedia.org/wiki/Standard_deviation">Standard deviation</a>
 */

public class StandardDeviation {
   
   /**
    * {@code STANDARD_DEVIATION} constraint decomposition for integer valued observations
    * 
    * @param name constraint name
    * @param observations observations
    * @param standardDeviation standard deviation
    * @param precision Ibex precision
    */
   
   public static void decompose(String name,
                                IntVar[] observations,
                                RealVar standardDeviation,
                                double precision){
      Solver solver = standardDeviation.getSolver();

      int min = Arrays.stream(observations).mapToInt(o -> o.getLB()).min().getAsInt();
      int max = Arrays.stream(observations).mapToInt(o -> o.getUB()).max().getAsInt();

      RealVar mean = VariableFactory.real("Mean", min, max, precision, solver);
      Mean.decompose("MeanConstraint", observations, mean, precision);
      
      String exp = "sqrt((";
      for(int i = 0; i < observations.length; i++){
         if(i < observations.length - 1)
            exp += "({"+i+"}-{"+(observations.length+1)+"})^2+";
         else
            exp += "({"+i+"}-{"+(observations.length+1)+"})^2)/"+(observations.length-1)+")={"+(i+1)+"}";
      }

      RealVar[] allRealVariables = new RealVar[observations.length + 2];
      RealVar[] realObservations = VF.real(observations, precision);
      System.arraycopy(realObservations, 0, allRealVariables, 0, observations.length);
      allRealVariables[observations.length] = standardDeviation;
      allRealVariables[observations.length+1] = mean;

      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, allRealVariables));
   }

   /**
    * {@code STANDARD_DEVIATION} constraint decomposition for real valued observations
    * 
    * @param name constraint name
    * @param observations observations
    * @param standardDeviation standard deviation
    * @param precision Ibex precision
    */
   
   public static void decompose(String name,
                                RealVar[] observations,
                                RealVar standardDeviation,
                                double precision){
      Solver solver = standardDeviation.getSolver();

      double min = Arrays.stream(observations).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double max = Arrays.stream(observations).mapToDouble(o -> o.getUB()).max().getAsDouble();

      RealVar mean = VariableFactory.real("Mean", min, max, precision, solver);
      Mean.decompose("MeanConstraint", observations, mean, precision);

      String exp = "sqrt((";
      for(int i = 0; i < observations.length; i++){
         if(i < observations.length - 1)
            exp += "({"+i+"}-{"+(observations.length+1)+"})^2+";
         else
            exp += "({"+i+"}-{"+(observations.length+1)+"})^2)/"+(observations.length-1)+")={"+(i+1)+"}";
      }

      RealVar[] allRealVariables = new RealVar[observations.length + 2];
      System.arraycopy(observations, 0, allRealVariables, 0, observations.length);
      allRealVariables[observations.length] = standardDeviation;
      allRealVariables[observations.length+1] = mean;

      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, allRealVariables));
   }
}
