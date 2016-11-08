package org.chocosolver.solver.constraints.statistical.frequency;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;

import gnu.trove.map.hash.THashMap;

public class PropFrequencySt extends Propagator<IntVar> {
   
   int m, n;
   int[] binBounds;
   ExpressionsBasedModel[] models;
   
   private static IntVar[] joinVariables(IntVar[] valueVariables, IntVar[] binVariables){
      IntVar[] variables = new IntVar[valueVariables.length + binVariables.length];
      System.arraycopy(valueVariables, 0, variables, 0, valueVariables.length);
      System.arraycopy(binVariables, 0, variables, valueVariables.length, binVariables.length);
      return variables;
   }
   
   public PropFrequencySt(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      super(joinVariables(valueVariables, binVariables), PropagatorPriority.LINEAR, true);
      this.n = valueVariables.length;
      this.m = binVariables.length;
      this.binBounds = binBounds.clone();
      this.prepare();
   }
   
   protected void prepare() {
      this.models = new ExpressionsBasedModel[n*m + m];
      for(int k = 0; k < n*m + m; k++)
         this.models[k] = this.buildLPModel(k);
   }
   
   @Override
   public void propagate(int evtmask) throws ContradictionException {
      if (PropagatorEventType.isFullPropagation(evtmask)) {
         //prepare();
         updateDomains();
      }
   }
   
   @Override
   public void propagate(int i, int mask) throws ContradictionException {
      /*if(i < n){
         addInstatiatedVarLPConstraints(i);
      }else{
         addBinBoundLPConstraints(i-n);
      }*/
      //forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
      if (PropagatorEventType.isFullPropagation(mask)) {
         prepare();
         updateDomains();  
      }
   }
   
   private void updateDomains() throws ContradictionException{
      //Cast to integer justified by the fact matrix is TUM
      for(int k = 0; k < n*m; k++){
         int i = k / m;
         int j = k % m;
         Result result = models[k].minimise();
         if(!result.getState().isFeasible()){
            this.vars[i].wipeOut(aCause);
         }else if((int) result.getValue() == 1){
            for(int l = 0; l < m; l++){
               if(l != j)
                  this.vars[i].removeInterval(getBinLB(l), getBinUB(l), aCause);
            }
         }
         
         result = models[k].maximise();
         if(!result.getState().isFeasible()){
            this.vars[i].wipeOut(aCause);
         }else if((int) result.getValue() == 0){
            this.vars[i].removeInterval(getBinLB(j), getBinUB(j), aCause);
         }
      }
      
      for(int k = n*m; k < n*m + m; k++){
         int j = k-n*m;
         Result result = models[k].minimise();
         if(result.getState().isFeasible()){
            int lb = (int) result.getValue();
            this.vars[j+n].updateLowerBound(lb, aCause);
         }else{
            this.vars[j+n].wipeOut(aCause);
         }
         result = models[k].maximise();
         if(result.getState().isFeasible()){
            int ub = (int) models[k].maximise().getValue();
            this.vars[j+n].updateUpperBound(ub, aCause);
         }else{
            this.vars[j+n].wipeOut(aCause);
         }
      }
   }

   @Override
   public int getPropagationConditions(int vIdx) {
     return IntEventType.boundAndInst();
   }

   @Override
   public ESat isEntailed() {
      // TODO Auto-generated method stub
      return ESat.UNDEFINED;
   }
   
   @Override
   public String toString() {
       return "not implemented";
   }
   
   @Override
   public void explain(ExplanationEngine xengine, Deduction d, Explanation e) {
       //e.add(xengine.getPropagatorActivation(this));
     throw new SolverException("Constraint duplication not implemented");
   }
   
   @Override
   public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
       throw new SolverException("Constraint duplication not implemented");
   }
   
   
   private int getBinLB(int j){
      return this.binBounds[j];
   }
   
   private int getBinUB(int j){
      return this.binBounds[j+1]-1;
   }
   
   private int getBinCountLB(int j){
      return this.vars[n+j].getLB();
   }
   
   private int getBinCountUB(int j){
      return this.vars[n+j].getUB();
   }
   
   public boolean fitsInBin(IntVar val, int j){
      DisposableValueIterator vit = val.getValueIterator(true);
      while(vit.hasNext()){
          int v = vit.next();
          if(v >= getBinLB(j) && v <= getBinUB(j)){
             vit.dispose();
             return true;
          }
      }
      vit.dispose();
      return false;
   }
   
   public ExpressionsBasedModel buildLPModel(int index){
      ExpressionsBasedModel model = new ExpressionsBasedModel();
      Variable[][] lpVars = new Variable[m][n];
      Variable[] binVars = new Variable[m];
      
      for(int i = 0; i < n; i++){
         for(int j = 0; j < m; j++){
            if(i*m + j < n*m && i*m + j == index)
               lpVars[j][i] = Variable.make("x"+j+"_"+i).lower(0).upper(1).weight(1);
            else
               lpVars[j][i] = Variable.make("x"+j+"_"+i).lower(0).upper(1).weight(0);
            model.addVariable(lpVars[j][i]);
         }
      }
      
      for(int j = 0; j < m; j++){
         binVars[j] = Variable.make("c"+j).lower(this.getBinCountLB(j)).upper(this.getBinCountUB(j));
         if(index == j + n*m) 
            binVars[j].weight(1);
         else
            binVars[j].weight(0);
         model.addVariable(binVars[j]);
      }
      
      for(int i = 0; i < n; i++){
         Expression totalProbability = model.addExpression("Total probability "+i).lower(1).upper(1);
         for(int j = 0; j < m; j++){
            totalProbability.set(lpVars[j][i], 1);
         }
      }
      
      for(int j = 0; j < m; j++){
         Expression binCount = model.addExpression("Bin count "+j).lower(0).upper(0);
         for(int i = 0; i < n; i++){
            binCount.set(lpVars[j][i], 1);
         }
         binCount.set(binVars[j],-1);
      }
      
      Expression binTotal = model.addExpression("Bin total").lower(n).upper(n);
      for(int j = 0; j < m; j++){
         binTotal.set(binVars[j], 1);
      }
      
      for(int j = 0; j < m; j++){
         Expression binCount = model.addExpression("Bin bounds "+j).lower(getBinCountLB(j)).upper(getBinCountUB(j));
         binCount.set(binVars[j], 1);
      }
      
      for(int i = 0; i < n; i++){
         for(int j = 0; j < m; j++){
            Expression connection = model.addExpression("Connection "+i+"_"+j).lower(0).upper(fitsInBin(this.vars[i], j) ? 1 : 0);
            connection.set(lpVars[j][i], 1);
         }
      }
      
      return model;
   }
   
   /*protected void addBinBoundLPConstraints(int j){
      for(int k = 0; k < n*m + m; k++){
         Expression binCount = this.models[k].addExpression("Bin bounds "+(j-n)+"_"+System.currentTimeMillis()).lower(getBinCountLB(j)).upper(getBinCountUB(j));
         binCount.set(this.models[k].getVariable(j), 1);
      }
   }

   protected void addInstatiatedVarLPConstraints(int i){
      for(int k = 0; k < n*m + m; k++){
         for(int j = 0; j < m; j++){
            Expression connection = this.models[k].addExpression("Connection "+i+"_"+(j-m)+"_"+System.currentTimeMillis()).lower(0).upper(fitsInBin(this.vars[i], j) ? 1 : 0);
            connection.set(this.models[k].getVariable(m+i*m+j), 1);
         }
      }
   }*/
}
