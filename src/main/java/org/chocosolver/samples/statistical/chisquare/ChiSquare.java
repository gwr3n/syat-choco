package org.chocosolver.samples.statistical.chisquare;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.slf4j.LoggerFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public class ChiSquare extends AbstractProblem {
   
   public IntVar[] valueVariables;
   public IntVar[] binVariables;
   
   RealVar chiSqStatistics;
   RealVar[] allRV;
   
   int[] binBounds;
   int[] targetFrequencies = {1,4,1};
   
   double precision = 1.e-4;
   double pValue = 0.95;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("Frequency");
   }
   
   @Override
   public void buildModel() {
      valueVariables = new IntVar[6];
      valueVariables[0] = VariableFactory.enumerated("Value "+0, new int[]{2,3,4}, solver);
      valueVariables[1] = VariableFactory.enumerated("Value "+1, new int[]{1,2}, solver);
      valueVariables[2] = VariableFactory.enumerated("Value "+2, new int[]{0,3,5}, solver);
      valueVariables[3] = VariableFactory.enumerated("Value "+3, new int[]{3,4}, solver);
      valueVariables[4] = VariableFactory.enumerated("Value "+4, new int[]{1}, solver);
      valueVariables[5] = VariableFactory.enumerated("Value "+5, new int[]{2,3,4}, solver);
      
      binBounds = new int[]{0,2,4,6};
      
      binVariables = new IntVar[3];
      binVariables[0] = VariableFactory.bounded("Bin "+0, 0, 6, solver);
      binVariables[1] = VariableFactory.bounded("Bin "+1, 0, 6, solver);
      binVariables[2] = VariableFactory.bounded("Bin "+2, 0, 6, solver);
      
      this.chiSqDist = new ChiSquareDist(this.binVariables.length-1);
      
      chiSqStatistics = VF.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-pValue), precision, solver);
      
      solver.post(IntConstraintFactorySt.bincountsSt(valueVariables, binVariables, binBounds));
      
      RealVar[] realViews = VF.real(binVariables, precision);
      
      allRV = new RealVar[realViews.length+1];
      System.arraycopy(realViews, 0, allRV, 0, realViews.length);
      allRV[realViews.length] = chiSqStatistics;
      solver.post(new RealConstraint("chiSqStatistics",
            "(({0}-"+targetFrequencies[0]+")^2)/"+targetFrequencies[0]+"+"+
            "(({1}-"+targetFrequencies[1]+")^2)/"+targetFrequencies[1]+"+"+
            "(({2}-"+targetFrequencies[2]+")^2)/"+targetFrequencies[2]+"={3}",
            Ibex.HC4_NEWTON, allRV
            ));
   }
   
   @Override
   public void configureSearch() {
       solver.set(
             new RealStrategy(allRV, new Cyclic(), new RealDomainMiddle()),
             new RealStrategy(new RealVar[]{chiSqStatistics}, new Cyclic(), new RealDomainMiddle())
       );
       SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     boolean solution = solver.findSolution();
     do{
        st.append("---\n");
        if(solution) {
           for(int i = 0; i < valueVariables.length; i++){
              st.append(valueVariables[i].getValue()+", ");
           }
           st.append("\n");
           for(int i = 0; i < binVariables.length; i++){
              st.append(binVariables[i].getValue()+", ");
           }
           st.append("\n");
           st.append(chiSqStatistics.getLB()+" "+chiSqStatistics.getUB());
           st.append("\n");
        }else{
           st.append("No solution!");
        }
     }while(solution = solver.nextSolution());
     LoggerFactory.getLogger("bench").info(st.toString());
   }
   
   @Override
   public void prettyOut() {
       
   }
   
   public static void main(String[] args) {
      String[] str={"-log","SILENT"};
      new ChiSquare().execute(str);
   }

}
