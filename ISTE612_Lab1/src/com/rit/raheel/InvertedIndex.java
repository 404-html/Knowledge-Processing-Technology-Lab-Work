package com.rit.raheel;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class InvertedIndex {
    ArrayList<String> termList;
    ArrayList<ArrayList<Integer>> docLists;

    public InvertedIndex(HashMap<Integer, String> myDocsContentMap, Set<String> stopWords) {
        termList = new ArrayList<>();
        docLists = new ArrayList<>();
        ArrayList<Integer> docList;
        for (int i = 1; i < myDocsContentMap.size() + 1; i++) {
            /**
             * 	Read in the text in each document and perform tokenization. Treat punctuation, symbols, and
             * 	spaces as delimiters.
             */
            String[] tokens = myDocsContentMap.get(i).split("\\p{Punct}|\\s");  //Any Punct char ->  ! ' # S % & ' ( ) * + , - . / : ; < = > ? @ [ / ] ^ _ { | } ~
            /**
             * Create the postings list and add each term here.
             */
            for (String token : tokens) {
                if (!stopWords.contains(token)) {
                    /**
                     * Perform Stemming on Tokens and then store the resulting term.
                     */
                    token = performStemming(token);
                    if (!termList.contains(token)) {//a new term
                        termList.add(token);
                        docList = new ArrayList<Integer>();
                        docList.add(new Integer(i));
                        docLists.add(docList);
                    } else {//an existing term
                        int index = termList.indexOf(token);
                        docList = docLists.get(index);
                        if (!docList.contains(new Integer(i))) {
                            docList.add(new Integer(i));
                            docLists.set(index, docList);
                        }
                    }
                }
            }

        }
    }

    /**
     * @return Formatted Inverted Index
     */
    public String toString() {
        String matrixString = new String();
        ArrayList<Integer> docList;
        System.out.println("Size of TermList  " + termList.size());
        for (int i = 0; i < termList.size(); i++) {
            matrixString += String.format("%-25s", termList.get(i));
            docList = docLists.get(i);
            for (int j = 0; j < docList.size(); j++)
                matrixString += docList.get(j) + "\t";
            matrixString += "\n";
        }
        return matrixString;
    }

    /**
     * @param query
     * @return
     */
    public ArrayList<Integer> searchDocList(String query) {
        int index = termList.indexOf(performStemming(query));
        if (index < 0)
            return null;
        return docLists.get(index);
    }

    /**
     * @param query
     * @return
     */
    public ArrayList<Integer> search(String query, String searchType) {
        String[] queryArr = query.split(" ");
        queryArr = optimizeSearch(queryArr);
        ArrayList<Integer> result = searchDocList(queryArr[0]);
        int termId = 1;
        while (termId < queryArr.length) {
            ArrayList<Integer> result1 = searchDocList((queryArr[termId]));
            if (result != null && result1 != null) {
                if (searchType == Constants.AND) {
                    //result = (ArrayList<Integer>) result.stream().filter(result1::contains).collect(Collectors.toList());  //merge AND
                    result = merge(result,result1,Constants.AND);
                } else if (searchType == Constants.OR) {
                   // result = (ArrayList<Integer>) Stream.concat(result.stream(), result1.stream()).distinct().collect(Collectors.toList()); //concat OR
                    result = merge(result,result1,Constants.OR);
                }

            }
            termId++;
        }
        return result;
    }


    /**
     * Sort the query terms based on increasing order of their frequency  and return the result of terms in the
     * ascending order of their frequency
     * @param query
     * @return
     */
    public String[] optimizeSearch(String[] query) {
        Map<String, Integer> queryDocFreqMap = new HashMap<>();

        for (String s : query) {
            int index = termList.indexOf(performStemming(s));
            if (index < 0) {
                queryDocFreqMap.put(s, 0);
            } else {
                queryDocFreqMap.put(s, docLists.get(index).size());
            }
        }
        Map<String, Integer> result = queryDocFreqMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
         System.out.println("Query terms sorted based on their frequency :\n");
        result.keySet().stream().forEach(key -> System.out.println(key));
        return result.keySet().toArray(new String[0]);
    }

    /**
     *
     * Start the stemming by instantiating the stemmer
     * @param token
     * @return
     */
    private String performStemming(String token) {
        Stemmer stemmer = new Stemmer();
        stemmer.add(token.toLowerCase().toCharArray(), token.length());
        stemmer.stem();
        return stemmer.toString();
    }

    /**
     *
     * Merge the two list based on And/Or operation type
     * @param l1
     * @param l2
     * @return
     */
    private ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2,String type) {
        ArrayList<Integer> mergedList = new ArrayList<Integer>();
        int id1 = 0, id2 = 0;
        if(type == Constants.AND) {
            while (id1 < l1.size() && id2 < l2.size()) {
                if (l1.get(id1).intValue() == l2.get(id2).intValue()) {
                    mergedList.add(l1.get(id1));
                    id1++;
                    id2++;
                } else if (l1.get(id1) < l2.get(id2))
                    id1++;
                else
                    id2++;
            }
        }else if(type == Constants.OR){
            if(l1.size() > l2.size()) {
                mergedList = l1;
                for (Integer doc : l2) {
                    if(!mergedList.contains(doc)){
                        mergedList.add(doc);
                    }
                }
            }else{
                mergedList = l2;
                for(Integer doc : l1){
                    if(!mergedList.contains(doc)){
                        mergedList.add(doc);
                    }
                }
            }
        }

        return mergedList;
    }



}
