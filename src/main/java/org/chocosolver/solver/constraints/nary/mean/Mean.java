package org.chocosolver.solver.constraints.nary.mean;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;

public class Mean {
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
   
   public static void decompose(String name,
                                RealVar[] observations,
                                RealVar mean){
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
