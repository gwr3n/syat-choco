package org.chocosolver.solver.constraints.statistical.bincounts.deprecated;

import java.util.ArrayList;
import java.util.List;

public class DecisionVariable {
   ArrayList<Integer> domain;
   
   public DecisionVariable(int[] domain){
      this.domain = new ArrayList<Integer>();
      for(int v : domain)
         this.domain.add(v);
      this.domain.sort(null);
   }
   
   public int getMin(){
      return this.domain.get(0).intValue();
   }
   
   public int getMax(){
      return this.domain.get(this.domain.size()-1).intValue();
   }
   
   public List<Integer> getDomain(){
      return domain;
   }
}
