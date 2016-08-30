package org.chocosolver.samples.real.bacp.preprocessing.shortestpath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import mascoptLib.algos.abstractalgos.KShortestPath;
import mascoptLib.algos.digraph.KShortestPaths;
import mascoptLib.graphs.Arc;
import mascoptLib.graphs.ArcSet;
import mascoptLib.graphs.DiGraph;
import mascoptLib.graphs.Vertex;
import mascoptLib.graphs.VertexSet;

public class ShortestPath {
   
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
              int n_courses = Integer.parseInt(parts[1]);
              prerequisiteMatrix = new int[n_courses][n_courses];
           }
           else if(lines[i].startsWith("constraint prerequisite")){
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
   
   public int computeLB(String instance, int task){
      int[][] connectionMatrix = loadPrerequisites(instance);
      
      /** 
       * Assuming transitivity: if 1 is prerequisite of 2 and 2 is prerequisite of 3, 
       * then constraint 1 is prerequisite of 3 is implied and not posted explicitly
       */
      //int[][] connectionMatrix ={{0,0,0},{1,0,0},{0,1,0}};
      
      Vertex[] vertex = new Vertex[connectionMatrix.length+1];
      VertexSet vertexSet = new VertexSet();
      for(int x = 0; x < vertex.length; x++){
          vertex[x] = new Vertex();
          vertex[x].setName(x+"");
          vertexSet.add(vertex[x]);
      }
      
      Arc[][] arc = new Arc[connectionMatrix.length+1][connectionMatrix.length+1];
      ArcSet arcSet = new ArcSet(vertexSet);
      DiGraph digraph = new DiGraph(vertexSet, arcSet);
      for(int i = 0; i < connectionMatrix.length; i++){
          if(Arrays.stream(connectionMatrix[i]).sum() == 0){
             arc[0][i+1] = new Arc(vertex[0], vertex[i+1]);
             arc[0][i+1].setValue(KShortestPath.WEIGHT, 0+"");
             arc[0][i+1].setDouValue("poids", digraph, 0);
             arcSet.add(arc[0][i+1]);
          }
          for(int j = 0; j < connectionMatrix.length; j++){
              if(connectionMatrix[i][j] == 1){
                  arc[j+1][i+1] = new Arc(vertex[j+1], vertex[i+1]);
                  arc[j+1][i+1].setValue(KShortestPath.WEIGHT, 1+"");
                  arc[j+1][i+1].setDouValue("poids", digraph, 1);
                  arcSet.add(arc[j+1][i+1]);
              }
          }
      }
      
      KShortestPaths path = new KShortestPaths(digraph, 1);
      KShortestPaths.NAME_OF_VALUE = "poids";
      path.run(vertex[0],vertex[task]);
      double lowerBound = path.getWeight(0);
      return (int) lowerBound;
   }
   
   public static void main(String args[]){
      ShortestPath path = new ShortestPath();
      for(int i = 1; i < 50; i++){
         System.out.print(path.computeLB("BACP/bacp-20.mzn", i) + " ");
      }
   }
}
