package com.raheel.lab;

import java.util.*;

public class NBClassifier {
    HashMap<Integer, String> trainingDocs;
    int[] trainingLabels;
    int[] classCounts; //number of docs per class
    String[] classStrings; //concatenated string for a given class
    int[] classTokenCounts; //total number of tokens per class
    HashMap<String, Double>[] condProb;
    HashSet<String> vocabulary; //entire vocabuary
    int numClasses = 2;
    Parser docParser;

    /**
     * Build a Naive Bayes classifier using a training document set
     * @param trainDataFolder
     * @param testDataFolder
     */
    public NBClassifier(String trainDataFolder,String testDataFolder){
        preprocess(trainDataFolder,testDataFolder);
        trainingDocs = docParser.myDocsContentMap;
        trainingLabels = docParser.trainLabels;
        classCounts = new int[numClasses];
        classStrings = new String[numClasses];
        classTokenCounts = new int[numClasses];
        condProb = new HashMap[numClasses];
        vocabulary = new HashSet<String>();

        for (int i = 0; i < numClasses; i++) {
            classStrings[i] = "";
            condProb[i] = new HashMap<String, Double>();
        }
        for (int i = 0; i < trainingLabels.length; i++) {
            classCounts[trainingLabels[i]]++;
            classStrings[trainingLabels[i]] += (trainingDocs.get(i) + " ");
        }
        for (int i = 0; i < numClasses; i++) {
            String[] tokens = classStrings[i].split("\\p{Punct}|\\s");
            classTokenCounts[i] = tokens.length;
            //collecting the counts
            for (String token : tokens) {
                vocabulary.add(token);
                if (condProb[i].containsKey(token)) {
                    double count = condProb[i].get(token);
                    condProb[i].put(token, count + 1);
                } else
                    condProb[i].put(token, 1.0);
            }

        }
        //computing the class conditional probability
        for (int i = 0; i < numClasses; i++) {
            Iterator<Map.Entry<String, Double>> iterator = condProb[i].entrySet().iterator();
            int vSize = vocabulary.size();
            while (iterator.hasNext()) {
                Map.Entry<String, Double> entry = iterator.next();
                String token = entry.getKey();
                Double count = entry.getValue();
                count = (count + 1) / (classTokenCounts[i] + vSize);
                condProb[i].put(token, count);
            }
        }
    }

    public int classfiy(String doc) {
        int label = 0;
        int vSize = vocabulary.size();
        double[] score = new double[numClasses];
        for (int i = 0; i < score.length; i++) {
            score[i] = Math.log(classCounts[i] * 1.0 / trainingDocs.size());
        }
        String[] tokens = doc.split("\\p{Punct}|\\s");
        for (int i = 0; i < numClasses; i++) {
            for (String token : tokens) {
                if (condProb[i].containsKey(token))
                    score[i] += Math.log(condProb[i].get(token));
                else
                    score[i] += Math.log(1.0 / (classTokenCounts[i] + vSize));
            }
        }
        double maxScore = score[0];
        for (int i = 0; i < score.length; i++) {
            if (score[i] > maxScore)
                label = i;
        }

        return label;
    }

    public void classifyAll(HashMap<Integer, String> testDocs, int[] trainingLabels) {
        int tp = 0;
        int tn = 0;
        int fp = 0;
        int fn = 0;
        int correctlyClassified = 0;
        float precision;
        float recall;
        float fmeasure;
        float accuracy;

        for (Map.Entry<Integer, String> testDoc : testDocs.entrySet()) {
            int result = classfiy(testDoc.getValue());
            if (result == trainingLabels[testDoc.getKey()] && trainingLabels[testDoc.getKey()] == 1) { //pos
                tp++;
            } else if (result == trainingLabels[testDoc.getKey()] && trainingLabels[testDoc.getKey()] == 0) { // neg
                tn++;
            } else if (result != trainingLabels[testDoc.getKey()] && trainingLabels[testDoc.getKey()] == 0) {
                fn++;
            } else if (result != trainingLabels[testDoc.getKey()] && trainingLabels[testDoc.getKey()] == 1) {
                fp++;
            }
        }
        correctlyClassified=tp+tn;
        precision = (float) tp / (float) (tp + fp);
        recall = (float) tp / (float) (tp + fn);
        fmeasure = 2 * ((precision * recall) / (precision + recall));
        accuracy = (float) (tp + tn) / (float) (tp + tn + fp + fn);


        System.out.println("Correctly classified " +correctlyClassified+" of "+testDocs.size());
        System.out.println("Accuracy is : " + accuracy);
        System.out.println("");
        System.out.println("Precision is : " + precision);
        System.out.println("Recall is : " + recall);
        System.out.println("Fmeasure is : " + fmeasure);

    }

    /**
     * Pre process both train and test data for future processing
     * @param trainDataFolder
     * @param testDataFolder
     */
    public void preprocess(String trainDataFolder,String testDataFolder)
    {
        docParser =  new Parser(trainDataFolder, testDataFolder);
    }



    public static void main(String[] args) {
        NBClassifier nb = new NBClassifier("./data/train", "./data/test");
        nb.classifyAll(nb.docParser.myDocsTestContentMap, nb.docParser.trainLabels);

     }
}
