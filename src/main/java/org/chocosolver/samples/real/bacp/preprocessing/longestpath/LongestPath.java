package org.chocosolver.samples.real.bacp.preprocessing.longestpath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class LongestPath {
   
   int[] distances;
   int n_courses;
   int n_periods;
   
   Graph g;
   
   public int[][] loadPrerequisites(String instance){
      int[][] prerequisiteMatrix = null;
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
        for(int i = 1; i < lines.length; i++){
           if(lines[i].startsWith("n_courses")){
              String[] parts = lines[i].substring(0, lines[i].length()-1).split(" = ");
              n_courses = Integer.parseInt(parts[1]);
              prerequisiteMatrix = new int[n_courses][n_courses];
           } else if(lines[i].startsWith("n_periods")){
              String[] parts = lines[i].substring(0, lines[i].length()-1).split(" = ");
              n_periods = Integer.parseInt(parts[1]);
           } else if(lines[i].startsWith("constraint prerequisite")){
              String[] parts = lines[i].substring(0, lines[i].length()-1).split("prerequisite");
              String prerequisite[] = parts[1].substring(1, parts[1].length()-1).split(", ");
              prerequisiteMatrix[Integer.parseInt(prerequisite[0])-1][Integer.parseInt(prerequisite[1])-1] = 1;
           }
        }
     } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
     } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
     }
      
     return prerequisiteMatrix;
   }
   
   private void initialiseGraph(String instance){
      /** 
       * Assuming transitivity: if 1 is prerequisite of 2 and 2 is prerequisite of 3, 
       * then constraint 1 is prerequisite of 3 is implied and not posted explicitly
       */
      //int[][] connectionMatrix ={{0,0,0},{1,0,0},{0,1,0}};
      
      int[][] connectionMatrix = loadPrerequisites(instance);
      
      g = new Graph(connectionMatrix.length + 1);
      
      for(int i = 0; i < connectionMatrix.length; i++){
         if(Arrays.stream(connectionMatrix[i]).sum() == 0){
            g.addEdge(0, i+1, 1);
            
         }
         for(int j = 0; j < connectionMatrix.length; j++){
            if(connectionMatrix[i][j] == 1){
               g.addEdge(j+1, i+1, 1);
            }
         }
      }
   }
   
   /**
    * To propagate compute displacement w.r.t. to initial distances and update 
    * longest path
    * 
    * @param instance
    * @return
    */
   public int[] computeLBs(String instance){
      initialiseGraph(instance);
      int source = 0;
      return distances = g.findLongestPath(source);
   }
   
   public int[] computeUBs(String instance){
      int[] dist = new int[n_courses+1];
      for(int i = 1; i < n_courses+1; i++){
         initialiseGraph(instance);
         dist[i] = n_periods - Arrays.stream(g.findLongestPath(i)).max().getAsInt();
      }
      return dist;
   }
   
   public static void main(String[] args) {
      
      String instance = "BACP/bacp-10.mzn";
      
      /*Graph g = new Graph(6);
      g.addEdge(0, 1, 5);
      g.addEdge(0, 2, 3);
      g.addEdge(1, 3, 6);
      g.addEdge(1, 2, 2);
      g.addEdge(2, 4, 4);
      g.addEdge(2, 5, 2);
      g.addEdge(2, 3, 7);
      g.addEdge(3, 5, 1);
      g.addEdge(3, 4, -1);
      g.addEdge(4, 5, -2);
      int s = 1;
      g.findLongestPath(s);*/
      
      LongestPath lp = new LongestPath();
      int[] distancesLB = lp.computeLBs(instance);
      for(int i : distancesLB){
         System.out.print((i-1)+" ");
      }
      System.out.println();
      int[] distancesUB = lp.computeUBs(instance);
      for(int i : distancesUB){
         System.out.print((i-1)+" ");
      }
   }
}