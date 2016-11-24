package org.chocosolver.solver.constraints.statistical.bincounts;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateBool;
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

/**
 * This propagator for the Bincounts constraint operates under the 
 * assumption that, if there exist values that do not fall into any 
 * available bin, the constraint.is inconsistent
 * 
 * @author Roberto Rossi
 *
 */
public class PropBincountsEQFastSt extends Propagator<IntVar> {
   
   public static ExpressionsBasedModel modelInstance = null;
   Variable[][] lpVars;
   Variable[] binVars;
   
   int m, n;
   int[] binBounds;
   IStateBool[][] fitsInBin;
   
   private static IntVar[] joinVariables(IntVar[] valueVariables, IntVar[] binVariables){
      IntVar[] variables = new IntVar[valueVariables.length + binVariables.length];
      System.arraycopy(valueVariables, 0, variables, 0, valueVariables.length);
      System.arraycopy(binVariables, 0, variables, valueVariables.length, binVariables.length);
      return variables;
   }
   
   public PropBincountsEQFastSt(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      super(joinVariables(valueVariables, binVariables), PropagatorPriority.VERY_SLOW, true);
      this.n = valueVariables.length;
      this.m = binVariables.length;
      this.binBounds = binBounds.clone();
      
      IEnvironment environment = solver.getEnvironment();
      
      fitsInBin = new IStateBool[n][m];
      for(int i = 0; i < n; i++){
         for(int j = 0; j < m; j++){
            if(fitsInBin(this.vars[i], j))
               fitsInBin[i][j]=environment.makeBool(true);
            else
               fitsInBin[i][j]=environment.makeBool(false);
         }
      }
   }
   
   @Override
   public void propagate(int evtmask) throws ContradictionException {
      modelInstance = null;
      //long timeBefore = System.nanoTime();
      if (PropagatorEventType.isFullPropagation(evtmask)) {
         boolean anyChange = updateDomains();
         //if(anyChange) 
            //this.forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
      }
      //long timeAfter = System.nanoTime();
      //LoggerFactory.getLogger("bench").info("Prop main time: "+(timeAfter-timeBefore)*1e-9);
   }
   
   @Override
   public void propagate(int i, int mask) throws ContradictionException {
      modelInstance = null;
      
      boolean anyChangeBin = updateDomainsBin(i);  
      boolean anyChangeValue = updateDomainsValue(i);
   }
   
   private boolean updateDomainsValue(int varIndex) throws ContradictionException{
      boolean anyChange = false;
      //Cast to integer justified by the fact matrix is TUM
      boolean[] tempFitsInBin = new boolean[m];
      for(int j = 0; j < m; j++){
         tempFitsInBin[j] = varIndex < n && fitsInBin[varIndex][j].get();
      }
      
      for(int k = 0; k < n*m; k++){
         int i = k / m;
         int j = k % m;
         
         if(!(tempFitsInBin[j] && fitsInBin[i][j].get()))
            continue;
         
         ExpressionsBasedModel model = this.getLPModel(k);
         
         Result result = model.minimise();     
         if(!result.getState().isFeasible()){
            this.vars[i].wipeOut(aCause);
         }else if((int) result.getValue() == 1){
            for(int l = 0; l < m; l++){
               if(l != j){
                  anyChange = this.vars[i].removeInterval(getBinLB(l), getBinUB(l), aCause);
                  fitsInBin[i][l].set(false);
               }
            }
         }
         
         result = model.maximise();            
         if(!result.getState().isFeasible()){
            this.vars[i].wipeOut(aCause);
         }else if((int) result.getValue() == 0){
            anyChange = this.vars[i].removeInterval(getBinLB(j), getBinUB(j), aCause);
            fitsInBin[i][j].set(false);
         }
         
         this.unsetLPModelWeight(k);
      }
      
      return anyChange;
   }
   
   private boolean updateDomainsBin(int varIndex) throws ContradictionException{
      boolean anyChange = false;
      //Cast to integer justified by the fact matrix is TUM
      for(int k = n*m; k < n*m + m; k++){
         int j = k-n*m;  
         
         ExpressionsBasedModel model = this.getLPModel(k);
         
         Result result = model.minimise();    
         if(!result.getState().isFeasible()){
            this.vars[j+n].wipeOut(aCause);
         }else{
            int lb = (int) result.getValue();
            anyChange = this.vars[j+n].updateLowerBound(lb, aCause);
         }
         result = model.maximise();           
         if(!result.getState().isFeasible()){
            this.vars[j+n].wipeOut(aCause);
         }else{
            int ub = (int) result.getValue();
            anyChange = this.vars[j+n].updateUpperBound(ub, aCause);
         }
         
         this.unsetLPModelWeight(k);
      }
      
      return anyChange;
   }
   
   private boolean verifyDomains() {
      //Cast to integer justified by the fact matrix is TUM
      for(int k = n*m; k < n*m + m; k++){
         int j = k-n*m;
         
         ExpressionsBasedModel model = this.getLPModel(k);
         
         Result result = model.minimise();
         if(result.getState().isFeasible()){
            int lb = (int) result.getValue();
            if(lb != this.vars[j+n].getLB())
               return false;
         }else{
            return false;
         }
         result = model.maximise();
         if(result.getState().isFeasible()){
            int ub = (int) result.getValue();
            if(ub != this.vars[j+n].getUB())
               return false;
         }else{
            return false;
         }
         
         this.unsetLPModelWeight(k);
      }
      
      for(int k = 0; k < n*m; k++){
         int i = k / m;
         int j = k % m;
         
         ExpressionsBasedModel model = this.getLPModel(k);
         
         Result result = model.minimise();
         if(!result.getState().isFeasible()){
            return false;
         }else if((int) result.getValue() == 1){
            for(int l = 0; l < m; l++){
               if(l != j){
                  if(this.fitsInBin(this.vars[i], l))
                     return false;
               }
            }
         }
         
         result = model.maximise();
         if(!result.getState().isFeasible()){
            return false;
         }else if((int) result.getValue() == 0){
            if(this.fitsInBin(this.vars[i], j))
               return false;
         }
         
         this.unsetLPModelWeight(k);
      }
      
      return true;
   }
   
   private boolean updateDomains() throws ContradictionException{
      boolean anyChange = false;
      //Cast to integer justified by the fact matrix is TUM
      for(int k = n*m; k < n*m + m; k++){
         int j = k-n*m;
         
         ExpressionsBasedModel model = this.getLPModel(k);
         
         Result result = model.minimise();
         if(result.getState().isFeasible()){
            int lb = (int) result.getValue();
            anyChange = this.vars[j+n].updateLowerBound(lb, aCause);
         }else{
            this.vars[j+n].wipeOut(aCause);
         }
         result = model.maximise();
         if(result.getState().isFeasible()){
            int ub = (int) result.getValue();
            anyChange = this.vars[j+n].updateUpperBound(ub, aCause);
         }else{
            this.vars[j+n].wipeOut(aCause);
         }
         
         this.unsetLPModelWeight(k);
      }
      
      for(int k = 0; k < n*m; k++){
         int i = k / m;
         int j = k % m;
         
         ExpressionsBasedModel model = this.getLPModel(k);
         
         Result result = model.minimise();
         if(!result.getState().isFeasible()){
            this.vars[i].wipeOut(aCause);
         }else if((int) result.getValue() == 1){
            for(int l = 0; l < m; l++){
               if(l != j){
                  anyChange = this.vars[i].removeInterval(getBinLB(l), getBinUB(l), aCause);
                  fitsInBin[i][l].set(false);
               }
            }
         }
         
         result = model.maximise();
         if(!result.getState().isFeasible()){
            this.vars[i].wipeOut(aCause);
         }else if((int) result.getValue() == 0){
            anyChange = this.vars[i].removeInterval(getBinLB(j), getBinUB(j), aCause);
            fitsInBin[i][j].set(false);
         }
         
         this.unsetLPModelWeight(k);
      }
      
      return anyChange;
   }

   @Override
   public int getPropagationConditions(int vIdx) {
      return IntEventType.all();
   }

   @Override
   public ESat isEntailed() {
      // TODO Auto-generated method stub
      modelInstance = null;
      return verifyDomains() ? ESat.TRUE : ESat.FALSE;
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
   
   public ExpressionsBasedModel getLPModel(int index){
      if(modelInstance == null){
         return modelInstance = buildLPModel(index);
      }else{
         if(index < n*m){
            int i = index / m;
            int j = index % m;
            lpVars[j][i].weight(1);
         }else{
            binVars[index - n*m].weight(1);
         }
         return modelInstance;
      }
   }
   
   public void unsetLPModelWeight(int index){
      if(index < n*m){
         int i = index / m;
         int j = index % m;
         lpVars[j][i].weight(0);
      }else{
         binVars[index - n*m].weight(0);
      }
   }
   
   public ExpressionsBasedModel buildLPModel(int index){
      ExpressionsBasedModel model = new ExpressionsBasedModel();
      lpVars = new Variable[m][n];
      binVars = new Variable[m];
      
      for(int i = 0; i < n; i++){
         for(int j = 0; j < m; j++){
            if(i*m + j < n*m && i*m + j == index)
               lpVars[j][i] = Variable.make("x"+j+"_"+i).lower(0).upper(fitsInBin(this.vars[i], j) ? 1 : 0).weight(1);
            else
               lpVars[j][i] = Variable.make("x"+j+"_"+i).lower(0).upper(fitsInBin(this.vars[i], j) ? 1 : 0).weight(0);
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
      
      /*Expression binTotal = model.addExpression("Bin total").lower(n).upper(n);
      for(int j = 0; j < m; j++){
         binTotal.set(binVars[j], 1);
      }*/
      
      /*for(int j = 0; j < m; j++){
         Expression binCount = model.addExpression("Bin bounds "+j).lower(getBinCountLB(j)).upper(getBinCountUB(j));
         binCount.set(binVars[j], 1);
      }*/
      
      /*for(int i = 0; i < n; i++){
         for(int j = 0; j < m; j++){
            Expression connection = model.addExpression("Connection "+i+"_"+j).lower(0).upper(fitsInBin(this.vars[i], j) ? 1 : 0);
            connection.set(lpVars[j][i], 1);
         }
      }*/
      
      return model;
   }
}
