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
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

public class PooledVariance {

   public static void decompose(String name,
                                IntVar[] observationsA,
                                IntVar[] observationsB,
                                RealVar standardDeviation,
                                double precision){
      
      Solver solver = standardDeviation.getSolver();

      int minA = Arrays.stream(observationsA).mapToInt(o -> o.getLB()).min().getAsInt();
      int maxA = Arrays.stream(observationsA).mapToInt(o -> o.getUB()).max().getAsInt();

      RealVar meanA = VariableFactory.real("MeanA", minA, maxA, precision, solver);
      Mean.decompose("MeanConstraintA", observationsA, meanA, precision);
      
      int minB = Arrays.stream(observationsB).mapToInt(o -> o.getLB()).min().getAsInt();
      int maxB = Arrays.stream(observationsB).mapToInt(o -> o.getUB()).max().getAsInt();
      
      RealVar meanB = VariableFactory.real("MeanB", minB, maxB, precision, solver);
      Mean.decompose("MeanConstraintB", observationsB, meanB, precision);

      String exp = "(";
      for(int i = 0; i < observationsA.length + observationsB.length; i++){
         if(i < observationsA.length)
            exp += "({"+i+"}-{"+(observationsA.length+observationsB.length+1)+"})^2+";
         else if(i >= observationsA.length && i < observationsA.length + observationsB.length - 1)
            exp += "({"+i+"}-{"+(observationsA.length+observationsB.length+2)+"})^2+";
         else
            exp += "({"+i+"}-{"+(observationsA.length+observationsB.length+2)+"})^2)/"+(observationsA.length+observationsB.length-2)+"={"+(observationsA.length+observationsB.length)+"}";
      }

      RealVar[] allRealVariables = new RealVar[observationsA.length + observationsB.length + 3];
      RealVar[] realObservationsA = VariableFactory.real(observationsA, precision);
      RealVar[] realObservationsB = VariableFactory.real(observationsB, precision);
      System.arraycopy(realObservationsA, 0, allRealVariables, 0, observationsA.length);
      System.arraycopy(realObservationsB, 0, allRealVariables, observationsA.length, observationsB.length);
      allRealVariables[observationsA.length + observationsB.length] = standardDeviation;
      allRealVariables[observationsA.length + observationsB.length + 1] = meanA;
      allRealVariables[observationsA.length + observationsB.length + 2] = meanB;

      solver.post(new RealConstraint(exp, allRealVariables));
   }

   public static void decompose(String name,
                                RealVar[] observationsA,
                                RealVar[] observationsB,
                                RealVar standardDeviation,
                                double precision){
      
      Solver solver = standardDeviation.getSolver();
      
      double minA = Arrays.stream(observationsA).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double maxA = Arrays.stream(observationsA).mapToDouble(o -> o.getUB()).max().getAsDouble();

      RealVar meanA = VariableFactory.real("MeanB", minA, maxA, precision, solver);
      Mean.decompose("MeanConstraintA", observationsA, meanA, precision);
      
      double minB = Arrays.stream(observationsB).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double maxB = Arrays.stream(observationsB).mapToDouble(o -> o.getUB()).max().getAsDouble();
      
      RealVar meanB = VariableFactory.real("MeanB", minB, maxB, precision, solver);
      Mean.decompose("MeanConstraintB", observationsB, meanB, precision);

      String exp = "(";
      for(int i = 0; i < observationsA.length + observationsB.length; i++){
         if(i < observationsA.length)
            exp += "({"+i+"}-{"+(observationsA.length+observationsB.length+1)+"})^2+";
         else if(i >= observationsA.length && i < observationsA.length + observationsB.length - 1)
            exp += "({"+i+"}-{"+(observationsA.length+observationsB.length+2)+"})^2+";
         else
            exp += "({"+i+"}-{"+(observationsA.length+observationsB.length+2)+"})^2)/"+(observationsA.length+observationsB.length-2)+"={"+(observationsA.length+observationsB.length)+"}";
      }
      
      RealVar[] allRealVariables = new RealVar[observationsA.length + observationsB.length + 3];
      System.arraycopy(observationsA, 0, allRealVariables, 0, observationsA.length);
      System.arraycopy(observationsB, 0, allRealVariables, observationsA.length, observationsB.length);
      allRealVariables[observationsA.length + observationsB.length] = standardDeviation;
      allRealVariables[observationsA.length + observationsB.length + 1] = meanA;
      allRealVariables[observationsA.length + observationsB.length + 2] = meanB;

      solver.post(new RealConstraint(exp, allRealVariables));
   }
}
