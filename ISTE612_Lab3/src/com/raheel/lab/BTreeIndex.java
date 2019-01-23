package com.raheel.lab;

import java.util.*;
import java.util.stream.Stream;

public class BTreeIndex {
    String[] myDocs;
    BinaryTree termList;
    BTNode root;
    ArrayList<ArrayList<Integer>> docLists;

    /**
     * Construct binary search tree to store the term dictionary
     *
     * @param docs List of input strings
     */
    public BTreeIndex(String[] docs) {
        myDocs = docs;
        ArrayList<Integer> docList = new ArrayList<Integer>();
        ArrayList<String> termListArray = new ArrayList<String>();
        ;
        docLists = new ArrayList<ArrayList<Integer>>();
        termList = new BinaryTree();
        for (int i = 0; i < myDocs.length; i++) {
            String[] tokens = myDocs[i].split(" ");
            for (String token : tokens) {
                if (!termListArray.contains(token)) {//a new term
                    termListArray.add(token);
                }
            }
        }

        Collections.sort(termListArray);
        int start = 0;
        int end = termListArray.size() - 1;
        int mid = (start + end) / 2;
        BTNode r = new BTNode(termListArray.get(mid), docList);
        root = r;

        for (int i = 0; i < myDocs.length; i++) {
            String[] tokens = myDocs[i].split(" ");
            for (String token : tokens) {
                if (termList.search(r, token) == null) {//a new term
                    docList = new ArrayList<Integer>();
                    docList.add(new Integer(i));
                    docLists.add(docList);
                    termList.add(root, new BTNode(token, docList));
                } else {//an existing term
                    BTNode indexNode = termList.search(r, token);
                    docList = indexNode.docLists;
                    if (!docList.contains(new Integer(i))) {
                        docList.add(new Integer(i));
                    }
                    indexNode.docLists = docList;
                }
            }

        }

    }

    /**
     * Single keyword search
     *
     * @param query the query string
     * @return doclists that contain the term
     */
    public ArrayList<Integer> search(String query) {
        BTNode node = termList.search(root, query);
        if (node == null)
            return null;
        return node.docLists;
    }

    /**
     * conjunctive query search
     *
     * @param query the set of query terms
     * @return doclists that contain all the query terms
     */
    public ArrayList<Integer> search(String[] query) {
        ArrayList<Integer> result = search(query[0]);
        int termId = 1;
        while (termId < query.length) {
            ArrayList<Integer> result1 = search(query[termId]);
            if (result != null && result1 != null) {
                result = merge(result, result1);
            }
            termId++;
        }
        return result;
    }


    /**
     * @param wildcard the wildcard query, e.g., ho (so that home can be located)
     * @return a list of ids of documents that contain terms matching the wild card
     */
    public ArrayList<Integer> wildCardSearch(String wildcard) {
        ArrayList<BTNode> searchList = termList.wildCardSearch(root, wildcard);
        ArrayList<Integer> docIds = new ArrayList<>();
        searchList.stream().forEach(btNode -> {
            docIds.addAll(btNode.docLists);
        });
        return docIds;
    }


    private ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2) {
        ArrayList<Integer> mergedList = new ArrayList<Integer>();
        int id1 = 0, id2 = 0;
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
        return mergedList;
    }


    /**
     * Test cases
     *
     * @param args commandline input
     */
    public static void main(String[] args) {
        String[] docs = {"new home sales top forecasts",
                "home sales rise in july",
                "increase in home sales in july",
                "july new home sales rise"
        };

        BTreeIndex bTreeIndex = new BTreeIndex(docs);

        /**
         *   For one term
         */
        System.out.println("\nQuery 1 : Searching for Single Keyword :  home");
        ArrayList<Integer> result = bTreeIndex.search("home");
        if (!result.isEmpty()) {
            for (Integer i : result) {
                System.out.println("\nFound in : " + i);
            }
        } else
            System.out.println("No match!");


        /**
         *   For conjunctive terms
         */
        String[] query2 = {"home", "rise"};
        System.out.println("\nQuery 2 : Searching for query terms connected using AND : home and rise");
        ArrayList<Integer> result1 = bTreeIndex.search(query2);
        if (result1 != null && !result1.isEmpty()) {
            for (Integer i : result1) {
                System.out.println("\nFound in : " + i);
            }
        } else
            System.out.println("No match!");


        /**
         * For Wild card Query
         */

        String query3 = "ho";
        System.out.println("\nQuery 3 : Searching for wildcard "+query3);
        ArrayList<Integer> result3 = bTreeIndex.wildCardSearch(query3);
        if (result3 != null && !result3.isEmpty()) {
            for (Integer i : result3) {
                System.out.println("\nFound in : " + i);
            }
        } else
            System.out.println("No match!");
    }
}