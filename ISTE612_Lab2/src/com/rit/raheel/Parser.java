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
         * stop words in the documents. Skip it in case of using a poisting list and using Phrase Queries
         */
        if(stopWordFilePath != null) {
            createStopWordList(stopWordFilePath);
        }
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


}
