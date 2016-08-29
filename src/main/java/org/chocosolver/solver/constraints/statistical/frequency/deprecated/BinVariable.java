package org.chocosolver.solver.constraints.statistical.frequency.deprecated;

public class BinVariable extends DecisionVariable{
   
   int binLB;
   int binUB;
   
   int binCountLB;
   int binCountUB;
   
   public BinVariable(int[] domain, int binLB, int binUB, int binCountLB, int binCountUB){
      super(domain);
      this.binLB = binLB;
      this.binUB = binUB;
      this.binCountLB = binCountLB;
      this.binCountUB = binCountUB;
   }

   public int getBinLB(){
      return this.binLB;
   }
   
   public int getBinUB(){
      return this.binUB;
   }
   
   public int getBinCountLB(){
      return this.binCountLB;
   }
   
   public int getBinCountUB(){
      return this.binCountUB;
   }
}
