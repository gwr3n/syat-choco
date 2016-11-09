package org.chocosolver.solver.constraints.statistical.bincounts;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;

public class BincountsSt extends Constraint {
   
   public BincountsSt(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      super("FrequencySt", (Propagator<IntVar>) new PropBincountsLESt(valueVariables, binVariables, binBounds));
   }

}
