package org.chocosolver.samples.statistical.multinomial;

import java.util.Arrays;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.hotelling.tSquareStatistic;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.slf4j.LoggerFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.randvarmulti.MultinomialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class MultinomialCIChiSquareOpt extends AbstractProblem{

   public double[][] observations;
   public RealVar[] p;
   //public double[] actualP;
   public RealVar[][] observationVariable;
   public RealVar[][] covarianceMatrix;

   int categories;
   long nObs;
   double confidence;
   
   double[] statistic;

   public MultinomialCIChiSquareOpt(double[][] observations,
                                    double[] statistic){
      this.categories = observations[0].length;
      int[] values = new int[observations.length];
      for(int i = 0; i < observations.length; i++){
         for(int j = 0; j < observations[i].length; j++){
            if(observations[i][j] == 1)
               values[i] = j;
         }
      }
      this.nObs = Arrays.stream(values).count();
      this.observations = observations;
      this.statistic = statistic;
   }
   
   RealVar statisticVariable;
   
   double precision = 1.e-4;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("MultinomialCIChiSquare");
   }
   
   /**
    * https://www.ncbi.nlm.nih.gov/pubmed/9598426
    * http : // www.jstor.org/stable/1266673?seq = 1
    */
    
   @Override
   public void buildModel() {
      p = new RealVar[this.categories];
      for(int i = 0; i < this.categories; i++)
         p[i] = VariableFactory.real("p "+(i+1), 0+precision, 1-precision, precision, solver);
      //p[0] = VariableFactory.real("p "+(0+1), 0+precision, 1-precision, precision, solver);
      //p[1] = VariableFactory.real("p "+(1+1), 0.3, 0.3, precision, solver);
      //p[2] = VariableFactory.real("p "+(2+1), 0.3, 0.3, precision, solver);
      
      /*covarianceMatrix = new RealVar[this.categories][this.categories];
      for(int i = 0; i < this.covarianceMatrix.length; i++){
         for(int j = 0; j < this.covarianceMatrix[i].length; j++){
            covarianceMatrix[i][j] = VariableFactory.real("Sigma_"+(i+1)+"_"+(j+1), -1, 1, precision, solver);
            if(i==j){
               solver.post(new RealConstraint("cov_"+i+"_"+j,"{0}*(1-{0})={1}",
                                              Ibex.HC4_NEWTON,
                                              new RealVar[]{p[i],covarianceMatrix[i][j]})
                     );
            }else{
               solver.post(new RealConstraint("cov_"+i+"_"+j,"-{0}*{1}={2}",
                                              Ibex.HC4_NEWTON,
                                              new RealVar[]{p[i],p[j],covarianceMatrix[i][j]})
                     );
            }
         }
      }*/
      
      observationVariable = new RealVar[this.observations.length][this.observations[0].length];
      for(int i = 0; i < this.observations.length; i++){
         for(int j = 0; j < this.observations[i].length; j++){
            observationVariable[i][j] = VariableFactory.real("Obs_"+(i+1)+"_"+(j+1), observations[i][j], observations[i][j], precision, solver);
         }
      }
      
      statisticVariable = VF.real("chiSquare", statistic[0], statistic[1], precision, solver);
      
      tSquareStatistic.decompose("scoreConstraint", p, observationVariable, statisticVariable, precision);
   }
   
   @Override
   public void configureSearch() {
      RealStrategy strat1 = new RealStrategy(p, new Cyclic(), new RealDomainMiddle());
      //RealStrategy strat2 = new RealStrategy(flatten(covarianceMatrix), new Cyclic(), new RealDomainMiddle());
      RealStrategy strat4 = new RealStrategy(new RealVar[]{statisticVariable}, new Cyclic(), new RealDomainMiddle());
      solver.set(strat1, strat4);
   }
   
   @SuppressWarnings("unused")
   private RealVar[] flatten(RealVar[][] matrix){
      RealVar[] array = new RealVar[matrix.length*matrix[0].length];
      for(int i = 0; i < matrix.length; i++){
         for(int j = 0; j < matrix[i].length; j++){
            array[i*matrix[i].length+j] = matrix[i][j];
         }
      }
      return array;
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, statisticVariable, precision);
     //do{
        st.append("---\n");
        if(solver.isFeasible() == ESat.TRUE) {
           for(int i = 0; i < p.length; i++){
              st.append("("+p[i].getLB()+","+p[i].getUB()+"), ");
           }
           st.append("\n");
           st.append(statisticVariable.getLB()+" "+statisticVariable.getUB());
           st.append("\n");
           
           coverageProbability++;
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     LoggerFactory.getLogger("bench").info(st.toString());
   }
   
   @Override
   public void prettyOut() {
       
   }
   
   static double coverageProbability = 0;
   
   public static void main(String[] args) {
      String[] str={"-log","SOLUTION"};
      
      double confidence = 0.9;
      double[] p = {0.3,0.3,0.3}; 
      /** CAREFUL this is actually a ChiSquareDist with n-1 DOF. There is a bug in the library **/
      //double[] statistic = {0,(new ChiSquareDist(p.length)).inverseF(confidence)};
      
      int sampleSize = 50;
      
      double[] statistic = {0, (new umontreal.iro.lecuyer.probdist.FisherFDist(p.length, sampleSize - p.length)).inverseF(confidence)};
      
      MRG32k3a rng = new MRG32k3a();
      UniformGen gen1 = new UniformGen(rng);
      MultinomialGen multinomial = new MultinomialGen(gen1, p, 1);
      double[][] observations = new double[sampleSize][p.length];
      multinomial.nextArrayOfPoints(observations, 0, sampleSize);
      //Original
      //observations = new double[][]{{0,1,0},{0,1,0},{0,0,1},{0,1,0},{0,1,0},{0,1,0},{1,0,0},{0,0,1},{1,0,0},{0,0,0}};
      //Reduced
      //observations = new double[][]{{0,1},{0,1},{0,0},{0,1},{0,1},{0,1},{1,0},{0,0},{1,0},{1,0}};
      //observations = new double[][]{{0,1},{0,1},{1,0},{0,1},{0,1},{0,1},{0,0},{1,0},{0,0},{0,0}};
      MultinomialCIChiSquareOpt cs = new MultinomialCIChiSquareOpt(observations, statistic);
      cs.execute(str);
      System.gc();
      try {
         Thread.sleep(50);
      } catch (InterruptedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      int[] frequencies = {3,5,2};
      double[][] intervals = computeQuesenberryHurstCI(confidence, frequencies);
      System.out.println(Arrays.deepToString(intervals));
   }
   
   /* http://www.jstor.org/stable/1266673?seq=1 */
   public static double[][] computeQuesenberryHurstCI(double confidence, int[] counts){
      int N = Arrays.stream(counts).sum();
      double[][] intervals = new double[counts.length][2];
      ChiSquareDist chiSq = new ChiSquareDist(counts.length-1);
      double A = chiSq.inverseF(confidence);
      
      double[] n = new double[counts.length];
      
      for(int i = 0; i < counts.length; i++){
         n[i] = counts[i];
      }
      
      for(int i = 0; i < counts.length; i++){
         intervals[i][0] = (A + 2*n[i] - Math.sqrt(A*(A+4*n[i]*(N-n[i])/N)))/(2*(N+A));
         intervals[i][1] = (A + 2*n[i] + Math.sqrt(A*(A+4*n[i]*(N-n[i])/N)))/(2*(N+A));
      }
      return intervals;
   }
   
}
