package com.rit.raheel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Parser {
    HashMap<Integer, String> myDocsContentMap;
    protected static HashMap<Integer, String> myDocsFileNameReferenceMap;
    Set<String> stopWords;

    public Parser(File processFilesFolder, String stopWordFilePath) {
        /**
         * Adopt a proper data structure to store the given stop word list and use it to efficiently remove all the
         * stop words in the documents.
         */
        createStopWordList(stopWordFilePath);

        /**
         * Create the list of documents after removing all the stopwords.
         */
        parseDocuments(processFilesFolder);
    }

    /**
     * @param stopWordFilePath path to the stopWordFile.
     */
    private void createStopWordList(String stopWordFilePath) {
        Path path = Paths.get(stopWordFilePath);
        /**
         * TreeSet guarantees log(n) time cost for the basic operations (add, remove and contains).
         */
        stopWords = new TreeSet<>();
        try (Scanner document = new Scanner(path, Constants.ENCODING.name())) {
            while (document.hasNextLine()) {
                stopWords.add(document.nextLine());
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     * @param folderPath path to the files that need to be read.
     */
    private void parseDocuments(File folderPath) {

        /**
         * Using two maps as the listFiles() does not return a sorted file list hence storing references to each file.
         * and their respective positions
         */
        myDocsContentMap = new HashMap<Integer, String>();
        myDocsFileNameReferenceMap = new HashMap<Integer, String>();
        int index = 1;
        try {
            for (File file : folderPath.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
                    String content = new String(encoded, Constants.ENCODING);
                    myDocsContentMap.put(index, content.toLowerCase()); //case Folding
                    myDocsFileNameReferenceMap.put(index, file.getName());
                    index++;
                }
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Instant start = Instant.now();

        File folder = new File("./Lab1_Data"); //args[0]
        String stopWordFilePath = "./stopwords.txt"; //args[1]

        InvertedIndex inverted = null;
        Parser docParser = new Parser(folder, stopWordFilePath);
        inverted = new InvertedIndex(docParser.myDocsContentMap, docParser.stopWords);
        System.out.println(inverted);
        InvertedIndex finalInverted = inverted;

        /**
         *   For one term
         */
        String[] query1 =  {"Plot","QUEST"};
        Arrays.stream(query1).forEach(query ->{
            System.out.println("\nQuery 1 : Searching for Single Keyword : " + query);
            ArrayList<Integer> result = finalInverted.searchDocList(query);
            if (!result.isEmpty()) {
                for (Integer i : result) {
                    System.out.println("\nFound in : " + myDocsFileNameReferenceMap.get(i));
                }
            } else
                System.out.println("No match!");
        });


        /**
         *   For two terms AND
         */
        String[] query2 = {"QUEST camelot","donald sutherland"};
        Arrays.stream(query2).forEach(query -> {
            System.out.println("\nQuery 2 : Searching for query terms connected using AND : " + query);
            ArrayList<Integer> result1 = finalInverted.search(query, Constants.AND);
            if (result1 != null && !result1.isEmpty()) {
                for (Integer i : result1) {
                    System.out.println("\nFound in : " + myDocsFileNameReferenceMap.get(i));
                }
            } else
                System.out.println("No match!");
        });


        /**
         * For two term OR
         */
        String[] query3 = {"late cool","donald sutherland"};
        Arrays.stream(query3).forEach(query -> {
            System.out.println("\nQuery 3 : Searching for query terms connected using OR : " + query);
            ArrayList<Integer> result1 = finalInverted.search(query, Constants.OR);
            if (result1 != null && !result1.isEmpty()) {
                for (Integer i : result1) {
                    System.out.println("\nFound in : " + myDocsFileNameReferenceMap.get(i));
                }
            } else
                System.out.println("No match!");
        });

        /**
         * For three term AND
         */
        String[] query4 = {"kayley garrett disney","donald sutherland cool"};
        Arrays.stream(query4).forEach(query -> {
            System.out.println("\nQuery 4 : Searching for query terms connected using AND : " + query);
            ArrayList<Integer> result1 = finalInverted.search(query, Constants.AND);
            if (result1 != null && !result1.isEmpty()) {
                for (Integer i : result1) {
                    System.out.println("\nFound in : " + myDocsFileNameReferenceMap.get(i));
                }
            } else
                System.out.println("No match!");
        });
        Instant end = Instant.now();
        System.out.println(Duration.between(start, end)); // prints PT1M3.553S
    }
}
