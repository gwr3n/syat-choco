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

package org.chocosolver.solver.constraints.nary.bincounts;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.SyatConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

public class BincountsDecompositions {
   /**
    * Bincounts decomposition (Rossi, 2016)
    * 
    * BEWARE : it is automatically posted (it cannot be reified)
    * 
    * @param valueVariables
    * @param binVariables
    * @param binBounds
    */
   public static void bincountsDecomposition1(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      Solver solver = valueVariables[0].getSolver();
      
      IntVar[] valueOccurrenceVariables = new IntVar[binBounds[binBounds.length-1]-binBounds[0]];
      int[] valuesArray = new int[valueOccurrenceVariables.length];
      for(int i = 0; i < valueOccurrenceVariables.length; i++){
         valueOccurrenceVariables[i] = VariableFactory.bounded("Value Occurrence "+i, 0, valueVariables.length, solver);
         valuesArray[i] = i + binBounds[0];
      }
      
      for(int i = 0; i < binBounds.length - 1; i++){
         IntVar[] binOccurrences = new IntVar[binBounds[i+1]-binBounds[i]];
         System.arraycopy(valueOccurrenceVariables, binBounds[i]-binBounds[0], binOccurrences, 0, binBounds[i+1]-binBounds[i]);
         solver.post(SyatConstraintFactory.sum(binOccurrences, binVariables[i]));
      }
      
      solver.post(SyatConstraintFactory.sum(binVariables, VariableFactory.fixed(valueVariables.length, solver)));
      
      solver.post(SyatConstraintFactory.global_cardinality(valueVariables, valuesArray, valueOccurrenceVariables, true));
   }
   
   /**
    * Bincounts decomposition (Agkun, 2016) first decomposition 
    * 
    * BEWARE : it is automatically posted (it cannot be reified)
    * 
    * @param valueVariables
    * @param binVariables
    * @param binBounds
    */
   public static void bincountsDecomposition2(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      Solver solver = valueVariables[0].getSolver();
      
      IntVar[] valueBinVariables = new IntVar[valueVariables.length];
      for(int i = 0; i < valueBinVariables.length; i++){
         valueBinVariables[i] = VariableFactory.bounded("Value-Bin "+i, 0, binBounds.length - 2, solver);
         for(int j = 0; j < binBounds.length - 1; j++){
            solver.post(LogicalConstraintFactory.ifThen_reifiable(
                  SyatConstraintFactory.arithm(valueBinVariables[i], "=", j), 
                  LogicalConstraintFactory.and(
                        SyatConstraintFactory.arithm(valueVariables[i], ">=", binBounds[j]),
                        SyatConstraintFactory.arithm(valueVariables[i], "<", binBounds[j+1])
                        )));
            
            solver.post(LogicalConstraintFactory.ifThen_reifiable( 
                  LogicalConstraintFactory.and(
                        SyatConstraintFactory.arithm(valueVariables[i], ">=", binBounds[j]),
                        SyatConstraintFactory.arithm(valueVariables[i], "<", binBounds[j+1])
                        ),
                        SyatConstraintFactory.arithm(valueBinVariables[i], "=", j)
                  ));
         }
      }
      
      int[] bins = new int[binBounds.length-1];
      for(int k = 0; k < binBounds.length - 1; k++) bins[k] = k;
      
      solver.post(SyatConstraintFactory.global_cardinality(valueBinVariables, bins, binVariables, true));
   }
   
   /**
    * Bincounts decomposition (Agkun, 2016) first decomposition (real)
    * 
    * BEWARE : it is automatically posted (it cannot be reified)
    * 
    * @param valueVariables
    * @param binVariables
    * @param binBounds
    */
   public static void bincountsDecomposition2(RealVar[] valueVariables, IntVar[] binVariables, double[] binBounds, double precision){
      Solver solver = valueVariables[0].getSolver();
      
      IntVar[] valueBinVariables = new IntVar[valueVariables.length];
      for(int i = 0; i < valueBinVariables.length; i++){
         valueBinVariables[i] = VariableFactory.bounded("Value-Bin "+i, 0, binBounds.length - 2, solver);
         for(int j = 0; j < binBounds.length - 1; j++){
            String constraintGEStr = "{0}>="+binBounds[j];
            String constraintLEStr = "{0}<"+binBounds[j+1];
            
            RealConstraint constraintGE = new RealConstraint("constraintGE_"+i+"_"+j,constraintGEStr,Ibex.HC4_NEWTON, new RealVar[]{valueVariables[i]});
            RealConstraint constraintLE = new RealConstraint("constraintLE_"+i+"_"+j,constraintLEStr,Ibex.HC4_NEWTON, new RealVar[]{valueVariables[i]});
           
            solver.post(LogicalConstraintFactory.ifThen_reifiable(
                  SyatConstraintFactory.arithm(valueBinVariables[i], "=", j), 
                  LogicalConstraintFactory.and(constraintGE,constraintLE))
                  );
            
            solver.post(LogicalConstraintFactory.ifThen_reifiable(
                  LogicalConstraintFactory.and(constraintGE,constraintLE),
                  SyatConstraintFactory.arithm(valueBinVariables[i], "=", j))
                  );
         }
      }
      
      int[] bins = new int[binBounds.length-1];
      for(int k = 0; k < binBounds.length - 1; k++) 
         bins[k] = k;
      
      solver.post(SyatConstraintFactory.global_cardinality(valueBinVariables, bins, binVariables, true));
   }
   
   /**
    * Bincounts decomposition (Agkun, 2016) second decomposition ***
    * 
    * BEWARE : it is automatically posted (it cannot be reified)
    * 
    * @param valueVariables
    * @param binVariables
    * @param binBounds
    */
   public static void bincountsDecomposition3(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds, boolean forceEquality){
      Solver solver = valueVariables[0].getSolver();
      
      for(int j = 0; j < binBounds.length - 1; j++){
         BoolVar[] valueBinVariables = new BoolVar[valueVariables.length];
         for(int i = 0; i < valueBinVariables.length; i++){
            valueBinVariables[i] = VariableFactory.bool("Value-Bin "+i+" "+j, solver);
            
            solver.post(LogicalConstraintFactory.reification_reifiable(
                  valueBinVariables[i], 
                  LogicalConstraintFactory.and(
                        SyatConstraintFactory.arithm(valueVariables[i], ">=", binBounds[j]),
                        SyatConstraintFactory.arithm(valueVariables[i], "<", binBounds[j+1])
                        )));
            
         }
         solver.post(SyatConstraintFactory.sum(valueBinVariables, binVariables[j]));
      }
      
      if(forceEquality) 
         solver.post(SyatConstraintFactory.sum(binVariables, VariableFactory.fixed(valueVariables.length, solver)));
   }
   
   /**
    * Bincounts decomposition (Agkun, 2016) second decomposition (real) ***
    * 
    * BEWARE : it is automatically posted (it cannot be reified)
    * 
    * @param valueVariables
    * @param binVariables
    * @param binBounds
    */
   public static void bincountsDecomposition3(RealVar[] valueVariables, IntVar[] binVariables, double[] binBounds, double precision, boolean forceEquality){
      Solver solver = valueVariables[0].getSolver();
      
      for(int j = 0; j < binBounds.length - 1; j++){
         BoolVar[] valueBinVariables = new BoolVar[valueVariables.length];
         for(int i = 0; i < valueBinVariables.length; i++){
            valueBinVariables[i] = VariableFactory.bool("Value-Bin "+i+" "+j, solver);
            
            String constraintGEStr = "{0}>="+binBounds[j];
            String constraintLEStr = "{0}<"+binBounds[j+1];
            
            RealConstraint constraintGE = new RealConstraint("constraintGE_"+i+"_"+j,constraintGEStr,Ibex.HC4_NEWTON, new RealVar[]{valueVariables[i]});
            RealConstraint constraintLE = new RealConstraint("constraintLE_"+i+"_"+j,constraintLEStr,Ibex.HC4_NEWTON, new RealVar[]{valueVariables[i]});
            
            solver.post(LogicalConstraintFactory.reification_reifiable(
                  valueBinVariables[i], 
                  LogicalConstraintFactory.and(constraintGE,constraintLE)
                  ));
            
         }
         solver.post(SyatConstraintFactory.sum(valueBinVariables, binVariables[j]));
      }
      
      if(forceEquality) 
         solver.post(SyatConstraintFactory.sum(binVariables, VariableFactory.fixed(valueVariables.length, solver)));
   }
}
