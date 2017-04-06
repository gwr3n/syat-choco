package org.chocosolver.samples.real.bnwp.gc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
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
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public class BNWP_Ref_Chi_Bincounts_GC extends AbstractProblem {
   
   String instance;
   int zone;
   
   public BNWP_Ref_Chi_Bincounts_GC(String instance, int zone){
      super();
      this.instance = instance;
      this.zone = zone;
   }
   
   int nbZones = 2;
   int nbNurses = 11;
   int minPatientsPerNurse = 1;
   int nbSlotsPerNurse = 6;
   int maxWorkloadPerNurse = 105;
   int nbPatientsZone[] = {17, 11};
   int acuityPatients[][] = {
         {59,57,50,44,42,40,39,39,33,33,32,27,26,22,20,17,11},
         {49,47,39,39,38,30,30,28,27,15,14}
   };

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
         //nbSlotsPerNurse = Integer.parseInt(line1[1]);
         maxWorkloadPerNurse = Integer.parseInt(line1[2]);
         
         nbPatientsZone = new int[nbZones];
         acuityPatients = new int[nbZones][];
         for(int i = 0; i < nbZones; i++){
            String[] linei = lines[i+2].split(" ");
            nbPatientsZone[i] = Integer.parseInt(linei[0]);
            acuityPatients[i] = new int[nbPatientsZone[i] + nbSlotsPerNurse - nbPatientsZone[i] % nbSlotsPerNurse];
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
   //int nbNurses = 1;
   /*int acuityPatients[] = {2,27,20,44,
                           42,40,39,39,
                           33,33,32,7,
                           26,22,20,17,
                           11,12,30,0
                           };*/
   
   int maxAcuity = 100;
   
   int[] binBounds = {0,30,60,maxAcuity+1};
   int[] targetFrequencies = {2,2,2};
   
   IntVar[][] nurseSlots;
   IntVar[] nurseSlotsArray;
   IntVar[][] nursePatientAcuity;
   IntVar[] nursePatientAcuityArray;
   
   IntVar[][] binVariables;
   
   RealVar chiSqStatistics;
   RealVar[] allRV;
   
   double precision = 0.01;

   ChiSquareDist chiSqDist;
   double pValue = 0.05;
   
   @Override
   public void createSolver() {
       solver = new Solver("BNWP");
   }
   
   @Override
   public void buildModel() {  
       loadInstance();
       
       nbNurses = Math.min(nbNurses, (int) Math.ceil(1.0*acuityPatients[zone].length/nbSlotsPerNurse));
       
       nurseSlots = new IntVar[nbNurses][];
       for(int n = 0; n < nbNurses; n++){
          nurseSlots[n] = VariableFactory.enumeratedArray("n["+n+"]", nbSlotsPerNurse, 0, acuityPatients[zone].length, solver);
       }
       nursePatientAcuity = VariableFactory.enumeratedMatrix("a", nbNurses, nbSlotsPerNurse, acuityPatients[zone], solver);
       
       nurseSlotsArray = new IntVar[nbNurses*nbSlotsPerNurse];
       nursePatientAcuityArray = new IntVar[nbNurses*nbSlotsPerNurse];
       for(int i = 0; i < nbNurses; i++){
          System.arraycopy(nurseSlots[i], 0, nurseSlotsArray, i*nbSlotsPerNurse, nbSlotsPerNurse);
          System.arraycopy(nursePatientAcuity[i], 0, nursePatientAcuityArray, i*nbSlotsPerNurse, nbSlotsPerNurse);
       }
       
       solver.post(IntConstraintFactorySt.alldifferent(nurseSlotsArray, "AC"));
       
       for(int n = 0; n < nbNurses; n++){ 
          for(int i = 0; i < nbSlotsPerNurse; i++){ 
             solver.post(IntConstraintFactorySt.element(nursePatientAcuity[n][i], acuityPatients[zone], nurseSlots[n][i]));
             if(i > 0){
                for(int j = 0; j < i; j++)
                   solver.post(IntConstraintFactorySt.arithm(nurseSlots[n][i], ">", nurseSlots[n][j]));
             }
          }
          if(n > 0)
             solver.post(IntConstraintFactorySt.arithm(nurseSlots[n][0], ">", nurseSlots[n-1][0]));
       }
       
       binVariables = new IntVar[nbNurses][];
       
       this.chiSqDist = new ChiSquareDist(nbSlotsPerNurse-1);
       
       chiSqStatistics = VariableFactory.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-pValue), precision, solver);
       
       for(int n = 0; n < nbNurses; n++) {
       
          binVariables[n] = new IntVar[binBounds.length-1];
          for(int i = 0; i < binBounds.length-1; i++){
             binVariables[n][i] = VariableFactory.enumerated("Bin "+i, 0, nbSlotsPerNurse, solver);
          }
       
          solver.post(IntConstraintFactorySt.bincounts(nursePatientAcuity[n], binVariables[n], binBounds));
       
          RealVar[] realViews = VariableFactory.real(binVariables[n], precision);
          allRV = new RealVar[realViews.length+1];
          System.arraycopy(realViews, 0, allRV, 0, realViews.length);
          allRV[realViews.length] = chiSqStatistics;
       
          String chiSqExp = "";
          for(int i = 0; i < binVariables[n].length; i++)
             if(i == binVariables[n].length - 1)
                chiSqExp += "(({"+i+"}-"+targetFrequencies[i]+")^2)/"+targetFrequencies[i]+"<={"+(binVariables[n].length)+"}";
             else
                chiSqExp += "(({"+i+"}-"+targetFrequencies[i]+")^2)/"+targetFrequencies[i]+"+";
       
          solver.post(new RealConstraint("chiSqTest",
                chiSqExp,
                Ibex.HC4_NEWTON, allRV
                ));
       }
   }
   
   @Override
   public void configureSearch() {   
      solver.set(
            IntStrategyFactory.minDom_LB(nurseSlotsArray)
            //new RealStrategy(new RealVar[]{chiSqStatistics}, new Cyclic(), new RealDomainMiddle())
            );
      SearchMonitorFactory.limitTime(solver,60000);
   }
   
   @Override
   public void solve() {
      //solver.findSolution();
      solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, chiSqStatistics, precision);
   }
   
   @Override
   public void prettyOut() {
      for(int i = 0; i < nurseSlotsArray.length; i++){
         System.out.println(nurseSlotsArray[i]+"\t");
      }
      for(int i = 0; i < nursePatientAcuityArray.length; i++){
         System.out.println(nursePatientAcuityArray[i]+"\t");
      }
      for(int n = 0; n < nbNurses; n++){
         for(int i = 0; i < binVariables[n].length; i++){
            System.out.println(binVariables[n][i]+"\t");
         }
      }
      //System.out.println(chiSqStatistics);
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
      
      int nbInstances = 1;
      int nbZones = 2;
      for(int instNo = 0; instNo < nbInstances; instNo++){
         
         String instance = "BNWP/SchausInstances/"+nbZones+"zones"+instNo+".txt";
         
         for(int i = 0; i < nbZones; i++){
            BNWP_Ref_Chi_Bincounts_GC chi = new BNWP_Ref_Chi_Bincounts_GC(instance, i);
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
