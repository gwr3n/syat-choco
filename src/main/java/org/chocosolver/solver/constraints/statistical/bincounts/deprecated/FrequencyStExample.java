package org.chocosolver.solver.constraints.statistical.bincounts.deprecated;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

public class FrequencyStExample {
   
   DecisionVariable[] values;
   BinVariable[] bins;
   ExpressionsBasedModel[] models;
   
   public FrequencyStExample(DecisionVariable[] values, BinVariable[] bins){
      this.values = values;
      this.bins = bins;
      this.models = new ExpressionsBasedModel[this.values.length*this.bins.length + this.bins.length];
      for(int i = 0; i < this.values.length*this.bins.length + this.bins.length; i++)
         this.models[i] = this.buildLPModel(i);
   }
   
   public static boolean fitsInBin(DecisionVariable val, BinVariable bin){
      for(Integer v : val.getDomain()){
         if(v >= bin.getBinLB() && v <= bin.getBinUB()){
            return true;
         }
      }
      return false;
   }
   
   public static void main(String[] args){
      DecisionVariable[] values = new DecisionVariable[3];
      values[0] = new DecisionVariable(new int[]{0,1,2,3});
      values[1] = new DecisionVariable(new int[]{0,1,2,3});
      values[2] = new DecisionVariable(new int[]{0,1,2,3});
      
      BinVariable[] bins = new BinVariable[2];
      bins[0] = new BinVariable(new int[]{0,1,2,3},0,1,0,4);
      bins[1] = new BinVariable(new int[]{0,1,2,3},2,3,0,4);
      
      FrequencyStExample freq = new FrequencyStExample(values, bins);
      
      Optimisation.Result modelResults;
      
      modelResults = freq.models[freq.values.length*freq.bins.length].maximise();
      System.out.println(modelResults.getValue());      
      
      modelResults = freq.models[freq.values.length*freq.bins.length].minimise();
      System.out.println(modelResults.getValue()); 
   }
   
   public ExpressionsBasedModel buildLPModel(int index){
      int n = values.length;
      int m = bins.length;
      ExpressionsBasedModel model = new ExpressionsBasedModel();
      Variable[][] lpVars = new Variable[m][n];
      Variable[] binVars = new Variable[m];
      
      for(int j = 0; j < m; j++){
         binVars[j] = Variable.make("c"+j).lower(bins[j].getBinCountLB()).upper(bins[j].getBinCountUB());
         if(index == j + this.values.length*this.bins.length) 
            binVars[j].weight(1);
         else
            binVars[j].weight(0);
         model.addVariable(binVars[j]);
      }
      
      for(int i = 0; i < n; i++){
         for(int j = 0; j < m; j++){
            if(i*m + j < this.values.length*this.bins.length && i*m + j == index)
               lpVars[j][i] = Variable.make("x"+j+"_"+i).lower(0).upper(1).weight(1);
            else
               lpVars[j][i] = Variable.make("x"+j+"_"+i).lower(0).upper(1).weight(0);
            model.addVariable(lpVars[j][i]);
         }
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
         Expression binCount = model.addExpression("Bin bounds "+j).lower(bins[j].getBinCountLB()).upper(bins[j].getBinCountUB());
         binCount.set(binVars[j], 1);
      }
      
      for(int i = 0; i < n; i++){
         for(int j = 0; j < m; j++){
            Expression connection = model.addExpression("Connection "+i+"_"+j).lower(0).upper(fitsInBin(values[i], bins[j]) ? 1 : 0);
            connection.set(lpVars[j][i], 1);
         }
      }
      
      return model;
   }
}
