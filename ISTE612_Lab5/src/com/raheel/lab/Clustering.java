package com.raheel.lab;

import java.util.*;

public class Clustering {
    // Declare attributes

    ArrayList<String[]> tokenizedDocs;
    HashMap<Integer, double[]> vectorSpace;
    ArrayList<String> termList;
    ArrayList<ArrayList<Doc>> docLists;
    int numberOfClusters;


    /**
     * Constructor for attribute initialization
     * @param numC number of clusters
     */
    public Clustering(int numC) {
        numberOfClusters = numC;
    }

    /**
     * Load the documents to build the vector representations
     * @param docs
     */
    public void preprocess(String[] docs){
        tokenizedDocs = new ArrayList<String[]>();
        termList = new ArrayList<String>();
        docLists = new ArrayList<ArrayList<Doc>>();
        ArrayList<Doc> docList;

        for (int i = 0; i < docs.length; i++) {
            String[] tokens = docs[i].split("\\p{Punct}|\\s");
            tokenizedDocs.add(i, tokens);

            for (String token : tokens) {
                if (!termList.contains(token)) {
                    termList.add(token);
                    docList = new ArrayList<Doc>();
                    Doc doc = new Doc(i, 1);
                    docList.add(doc);
                    docLists.add(docList);
                } else {
                    int index = termList.indexOf(token);
                    docList = docLists.get(index);
                    boolean match = false;
                    for (Doc d : docList) {
                        if (d.id == i) {
                            d.weights++;
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        Doc d = new Doc(i, 1);
                        docList.add(d);
                    }
                }
            }
        }
        vectorSpace = new HashMap<Integer, double[]>();
        double[] weights;
        for (int i = 0; i < docLists.size(); i++) {
            docList = docLists.get(i);
            for (int j = 0; j < docList.size(); j++) {
                Doc d = docList.get(j);
                if (!vectorSpace.containsKey(d.id)) {
                    weights = new double[termList.size()];
                    weights[i] = d.weights;
                    vectorSpace.put(d.id, weights);
                } else {
                    weights = vectorSpace.get(d.id);
                    weights[i] = d.weights;
                    vectorSpace.put(d.id, weights);
                }
            }
        }
    }


    /**
     * Cluster the documents using k-means
     */
    public void cluster(){
            double[][] centroids = new double[numberOfClusters][];
            centroids[0] = vectorSpace.get(8);
            centroids[1] = vectorSpace.get(0);
            HashMap<Integer, double[]>[] clusters = new HashMap[numberOfClusters];
            int iterations = 0;
            double[][] old = null;
            while (!Arrays.deepEquals(old, centroids)) {
                iterations++;
                // Store for convergence convergence test.
                old = centroids;
                clusters = getClusters(centroids);
                centroids = getCentroids(clusters);
            }
            print(clusters);
    }

    /**
     * Prints the clusters
     * @param clusters
     */
    public void print(HashMap<Integer, double[]>[] clusters) {
        //System.out.println("Printing clusters...");
        String clusterString;
        for (int i = 0; i < clusters.length; i++) {
            clusterString = "Cluster:  " +i+ "\n";
            HashMap<Integer, double[]> cluster = clusters[i];
            for (Integer id : cluster.keySet()) {
                clusterString += id + " ";
            }
            System.out.println(clusterString);
        }
    }



    /**
     * @param clusters
     * @return centroids
     */
    public double[][] getCentroids(HashMap<Integer, double[]>[] clusters) {
        double[][] centroids = new double[numberOfClusters][];
        for (int i = 0; i < clusters.length; i++) {
            HashMap<Integer, double[]> cluster = clusters[i];
            double[] mean = new double[termList.size()];
            for (Integer id : cluster.keySet()) {
                double[] currDocVector = cluster.get(id);
                for (int x = 0; x < currDocVector.length; x++) {
                    mean[x] += currDocVector[x];
                }
                for (int x = 0; x < mean.length; x++) {
                    mean[x] = mean[x] / cluster.size();
                }
            }
            centroids[i] = mean;
        }

        return centroids;
    }
    /**
     * @param centroids
     * @return clusters
     */
    public HashMap<Integer, double[]>[] getClusters(double[][] centroids) {
        HashMap<Integer, double[]>[] clusters = new HashMap[numberOfClusters];
        for (int i = 0; i < numberOfClusters; i++) {
            clusters[i] = new HashMap<Integer, double[]>();
        }
        for (int n = 0; n < vectorSpace.size(); n++) {
            double[] currDocVector = vectorSpace.get(n);
            int currDocId = n;
            double[] scores = new double[numberOfClusters];
            for (int i = 0; i < numberOfClusters; i++) {
                scores[i] = cosineSimilarity(centroids[i], currDocVector);
            }
            int clusterId = 0;
            double max = scores[clusterId];
            for (int i = 1; i < scores.length; i++) {
                if (scores[i] > max) {
                    max = scores[i];
                    clusterId = i;
                }
            }
            clusters[clusterId].put(currDocId, currDocVector);
        }

        return clusters;
    }
    /**
     * Calculate cosine similarity between two vectors
     * @param vector1
     * @param vector2
     * @return cosine similarity
     */
    private double cosineSimilarity(double[] vector1, double[] vector2) {
        double dotProduct = 0.0,magnitude1 = 0.0,magnitude2 = 0.0;
        double cosineSimilarity = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            magnitude1 += Math.pow(vector1[i], 2);
            magnitude2 += Math.pow(vector2[i], 2);
        }
        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);
        if (magnitude1 != 0.0 | magnitude2 != 0.0) {
            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
        }
        return cosineSimilarity;
    }
    /**
     * Main entry point of program.
     * @param args
     */
    public static void main(String[] args){
        String[] docs = {"hot chocolate cocoa beans",
                "cocoa ghana africa",
                "beans harvest ghana",
                "cocoa butter",
                "butter truffles",
                "sweet chocolate can",
                "brazil sweet sugar can",
                "suger can brazil",
                "sweet cake icing",
                "cake black forest"
        };

        Clustering c = new Clustering(2);
        c.preprocess(docs);
        c.cluster();
		
		/*
		 * Expected result:
		 * Cluster: 0
			0	1	2	3	4	
		   Cluster: 1
			5	6	7	8	9	
		 */
    }
}
