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

package org.chocosolver.solver.constraints;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.nary.contingency.ContingencyDecompositions;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositions;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.KolmogorovSmirnov;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.distributions.DistributionVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;

import umontreal.iro.lecuyer.probdist.Distribution;

public class SyatConstraintFactory extends IntConstraintFactory {

   public static KolmogorovSmirnov kolmogorov_smirnov(IntVar[] VAR1, IntVar[] VAR2, String OP1, double confidence) {
      Operator op1 = Operator.get(OP1);
      return new KolmogorovSmirnov(VAR1, VAR2, op1, confidence);
   }

   public static KolmogorovSmirnov kolmogorov_smirnov(IntVar[] VAR1, Distribution DIST, String OP1, double confidence) {
      Operator op1 = Operator.get(OP1);
      return new KolmogorovSmirnov(VAR1, DIST, op1, confidence);
   }

   public static KolmogorovSmirnov kolmogorov_smirnov(IntVar[] VAR1, DistributionVar DIST, String OP1, double confidence) {
      Operator op1 = Operator.get(OP1);
      return new KolmogorovSmirnov(VAR1, DIST, op1, confidence);
   }
   
   public static void bincountsDecomposition(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds, BincountsDecompositionType decompositionType){
      switch(decompositionType){
      case Rossi2016:
         BincountsDecompositions.bincountsDecomposition1(valueVariables, binVariables, binBounds);
         break;
      case Agkun2016_1:
         BincountsDecompositions.bincountsDecomposition2(valueVariables, binVariables, binBounds);
         break;
      case Agkun2016_2_EQ:
         BincountsDecompositions.bincountsDecomposition3(valueVariables, binVariables, binBounds, true);
         break;
      case Agkun2016_2_LE:
         BincountsDecompositions.bincountsDecomposition3(valueVariables, binVariables, binBounds, false);
         break;
      default:
         throw new NullPointerException();
      }
   }
   
   public static void bincountsDecomposition(RealVar[] valueVariables, IntVar[] binVariables, double[] binBounds, double precision, BincountsDecompositionType decompositionType){
      switch(decompositionType){
      case Agkun2016_1:
         BincountsDecompositions.bincountsDecomposition2(valueVariables, binVariables, binBounds, precision);
         break;
      case Agkun2016_2_EQ:
         BincountsDecompositions.bincountsDecomposition3(valueVariables, binVariables, binBounds, precision, true);
         break;
      case Agkun2016_2_LE:
         BincountsDecompositions.bincountsDecomposition3(valueVariables, binVariables, binBounds, precision, false);
         break;
      default:
         throw new NullPointerException();
      }
   }
   
   public static void contingencyDecomposition(IntVar[] seriesA, IntVar[] seriesB, IntVar[][] binVariables, int[][] binBounds, IntVar[] marginalsH, IntVar[] marginalsV){
      ContingencyDecompositions.decompose(seriesA, seriesB, binVariables, binBounds, marginalsH, marginalsV);
   }
   
   public static void contingencyDecomposition(RealVar[] seriesA, RealVar[] seriesB, IntVar[][] binVariables, double[][] binBounds, IntVar[] marginalsH, IntVar[] marginalsV){
      ContingencyDecompositions.decompose(seriesA, seriesB, binVariables, binBounds, marginalsH, marginalsV);
   }
}
