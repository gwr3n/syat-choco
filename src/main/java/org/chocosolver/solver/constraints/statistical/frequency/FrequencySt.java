package org.chocosolver.solver.constraints.statistical.frequency;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;

public class FrequencySt extends Constraint {
   
   public FrequencySt(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      super("FrequencySt", (Propagator<IntVar>) new PropFrequencySt(valueVariables, binVariables, binBounds));
   }

}
