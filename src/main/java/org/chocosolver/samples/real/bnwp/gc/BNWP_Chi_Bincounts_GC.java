package org.chocosolver.samples.real.bnwp.gc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public class BNWP_Chi_Bincounts_GC extends AbstractProblem {
   
   String instance;
   int zone;
   
   public BNWP_Chi_Bincounts_GC(String instance, int zone){
      super();
      this.instance = instance;
      this.zone = zone;
   }

   public void loadInstance(){
      FileReader fr = null;
      String model = "";
      try {
         ClassLoader classLoader = getClass().getClassLoader();
         File file = new File(classLoader.getResource(instance).getFile()); 
         fr = new FileReader(file);
         char[] buffer = new char[1000];
         while(fr.read(buffer) > -1){
               model += new String(buffer);
               buffer = new char[1000];
         }
         fr.close();
         String[] lines = model.split("\n");
         
         String[] line0 = lines[0].split(" ");
         nbZones = Integer.parseInt(line0[0]);
         nbNurses = Integer.parseInt(line0[1]);
         
         String[] line1 = lines[1].split(" ");
         minPatientsPerNurse = Integer.parseInt(line1[0]);
         maxPatientsPerNurse = Integer.parseInt(line1[1]);
         maxWorkloadPerNurse = Integer.parseInt(line1[2]);
         
         nbPatientsZone = new int[nbZones];
         acuityPatients = new int[nbZones][];
         for(int i = 0; i < nbZones; i++){
            String[] linei = lines[i+2].split(" ");
            nbPatientsZone[i] = Integer.parseInt(linei[0]);
            acuityPatients[i] = new int[nbPatientsZone[i]];
            for(int j = 0; j < nbPatientsZone[i]; j++){
               acuityPatients[i][j] = Integer.parseInt(linei[j+1]);
            }
         }
      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   /**
    * 2zones0.txt
    */
   int nbZones = 2;
   int nbNurses = 11;
   int minPatientsPerNurse = 1;
   int maxPatientsPerNurse = 3;
   int maxWorkloadPerNurse = 105;
   int nbPatientsZone[] = {17, 11};
   int acuityPatients[][] = {
         {59,57,50,44,42,40,39,39,33,33,32,27,26,22,20,17,11},
         {49,47,39,39,38,30,30,28,27,15,14}
   };
   
   int[] binBounds;
   int[] targetFrequencies;
   
   IntVar[] patientNurse;
   IntVar[] workloadNurse;
   IntVar[] maxPatientPerNurseIntVar;
   
   IntVar[] binVariables;
   
   RealVar chiSqStatistics;
   RealVar[] allRV;
   
   double precision = 0.01;

   ChiSquareDist chiSqDist;
   double pValue = 0.95;
   
   @Override
   public void createSolver() {
       solver = new Solver("BNWP");
   }
   
   @Override
   public void buildModel() {  
       loadInstance();
       
       int nursesNeeded = Math.min(nbNurses, (int) Math.ceil((1.0*Arrays.stream(acuityPatients[zone]).sum())/maxWorkloadPerNurse));
       
       int averageWorkload = (int) Math.ceil((1.0*Arrays.stream(acuityPatients[zone]).sum())/nursesNeeded);
       
       binBounds = new int[]{0,
                             averageWorkload-(maxWorkloadPerNurse-averageWorkload)/2,
                             averageWorkload+(maxWorkloadPerNurse-averageWorkload)/2,
                             maxWorkloadPerNurse+1};
       int[] targetFrequencies = new int[]{2,nursesNeeded-4,2};
       
       patientNurse = VariableFactory.enumeratedArray("n", nbPatientsZone[zone], 0, nursesNeeded-1, solver);
       workloadNurse = VariableFactory.enumeratedArray("w", nursesNeeded, 0, maxWorkloadPerNurse, solver);
       maxPatientPerNurseIntVar = VariableFactory.enumeratedArray("patientsPerNurse", nursesNeeded, minPatientsPerNurse, maxPatientsPerNurse, solver);
       
       solver.post(IntConstraintFactory.global_cardinality(patientNurse, 
                                                           IntStream.iterate(0, i -> i + 1).limit(nursesNeeded).toArray(), 
                                                           maxPatientPerNurseIntVar, 
                                                           true));
       
       solver.post(IntConstraintFactory.bin_packing(patientNurse, acuityPatients[zone], workloadNurse, 0));
       
       binVariables = new IntVar[binBounds.length-1];
       for(int i = 0; i < binBounds.length-1; i++){
          binVariables[i] = VariableFactory.bounded("Bin "+i, 0, nursesNeeded, solver);
       }
       
       solver.post(IntConstraintFactorySt.bincounts(workloadNurse, binVariables, binBounds));
       
       this.chiSqDist = new ChiSquareDist(this.binVariables.length-1);
       
       chiSqStatistics = VariableFactory.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-pValue), precision, solver);
       
       RealVar[] realViews = VariableFactory.real(binVariables, precision);
       allRV = new RealVar[realViews.length+1];
       System.arraycopy(realViews, 0, allRV, 0, realViews.length);
       allRV[realViews.length] = chiSqStatistics;
       
       String chiSqExp = "";
       for(int i = 0; i < binVariables.length; i++)
          if(i == binVariables.length - 1)
             chiSqExp += "(({"+i+"}-"+targetFrequencies[i]+")^2)/"+targetFrequencies[i]+"={"+(binVariables.length)+"}";
          else
             chiSqExp += "(({"+i+"}-"+targetFrequencies[i]+")^2)/"+targetFrequencies[i]+"+";
       
       solver.post(new RealConstraint("chiSqTest",
             chiSqExp,
             Ibex.HC4_NEWTON, allRV
             ));
   }
   
   @Override
   public void configureSearch() {   
      solver.set(IntStrategyFactory.minDom_LB(patientNurse));
      SearchMonitorFactory.limitTime(solver,60000);
   }
   
   @Override
   public void solve() {
      solver.findSolution();
      //solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, chiSqStatistics, precision);
   }
   
   @Override
   public void prettyOut() {
      for(int i = 0; i < patientNurse.length; i++){
         System.out.println(patientNurse[i]+"\t");
      }
      for(int i = 0; i < workloadNurse.length; i++){
         System.out.println(workloadNurse[i]+"\t");
      }
      for(int i = 0; i < binVariables.length; i++){
         System.out.println(binVariables[i]+"\t");
      }
      System.out.println(chiSqStatistics);
   }
   
   public String getStats(){
      return   solver.getMeasures().getSolutionCount()+"\t"+
               solver.getMeasures().getTimeCount()+"\t"+
               solver.getMeasures().getNodeCount()+"\t"+
               solver.getMeasures().getBackTrackCount()+"\t"+
               solver.getMeasures().getFailCount()+"\t"+
               solver.getMeasures().getRestartCount()+"\t"+
               solver.getMeasures().getMaxDepth()+"\t"+
               solver.getMeasures().getPropagationsCount()+"\t"+
               chiSqStatistics;
   }
   
   public static void main(String[] args) {
      String[] str={"-log","SILENT"};
      
      int nbZones = 2;
      int nbInstances = 10;
      for(int instNo = 0; instNo < nbInstances; instNo++){
         
         String instance = "BNWP/SchausInstances/"+nbZones+"zones"+instNo+".txt";
      
         for(int i = 0; i < nbZones; i++){
            BNWP_Chi_Bincounts_GC chi = new BNWP_Chi_Bincounts_GC(instance, i);
            chi.execute(str);
            System.out.println(chi.getStats());
            chi = null;
            System.gc();
            try {
               Thread.sleep(1000);
            } catch (InterruptedException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }
   }
}
