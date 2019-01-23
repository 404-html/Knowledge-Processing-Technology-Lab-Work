package com.rit.raheel;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class PositionalIndex {
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<DocId>> docLists;

	public PositionalIndex(HashMap<Integer, String> myDocsContentMap)
	{
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<DocId>>();
		ArrayList<DocId> docList;
		for(int i=1;i<myDocsContentMap.size()+1;i++){
			String[] tokens = myDocsContentMap.get(i).split(" ");
			String token;
			for(int j=0;j<tokens.length;j++){
				token = tokens[j];
				if(!termList.contains(token)){
					termList.add(token);
					docList = new ArrayList<DocId>();
					DocId doid = new DocId(i,j);
					docList.add(doid);
					docLists.add(docList);
				}
				else{ //existing term
					int index = termList.indexOf(token);
					docList = docLists.get(index);
					int k=0;
					boolean match = false;
					//search the postings for a document id, if match, insert a new position
					//number to the document id
					for(DocId doid:docList)
					{
						if(doid.docId==i)
						{
							doid.insertPosition(j);
							docList.set(k, doid);
							match = true;
							break;
						}
						k++;
					}
					//if no match, add a new document id along with the position number
					if(!match)
					{
						DocId doid = new DocId(i,j);
						docList.add(doid);
					}
				}
			}
		}
	}
	public String toString()
	{
		String matrixString = new String();
		ArrayList<DocId> docList;
		for(int i=0;i<termList.size();i++)
		{
			matrixString += String.format("%-15s", termList.get(i));
			docList = docLists.get(i);
			for(int j=0;j<docList.size();j++)
				matrixString += docList.get(j) + "\t";
			matrixString += "\n";
		}
		return matrixString;
	}

    public ArrayList<DocId> searchDocList(String query) {
        int index = termList.indexOf(query);
        if (index < 0)
            return null;
        return docLists.get(index);
    }

	public ArrayList<Integer> intersect(String q1, String q2)
	{
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
        if (termList.indexOf(q1) < 0)
            return null;
		ArrayList<DocId> l1 = searchDocList(q1);
		ArrayList<DocId> l2 = searchDocList(q2);
		int id1=0, id2=0;
		while(id1<l1.size()&&id2<l2.size()){
			//if both terms appear in the same document
			if(l1.get(id1).docId==l2.get(id2).docId){
				//get the position information for both terms
				ArrayList<Integer> pp1 = l1.get(id1).positionList;
				ArrayList<Integer> pp2 = l2.get(id2).positionList;
				int pid1 =0, pid2=0;
				while(pid1<pp1.size()){
					boolean match = false;
					while(pid2<pp2.size()){
						//if the two terms appear together, we find a match
						if(Math.abs(pp1.get(pid1)-pp2.get(pid2))<=1){
							match = true;
							mergedList.add(l1.get(id1).docId);
							break;
						}
						else if(pp2.get(pid2)>pp1.get(pid1))
							break;
						pid2++;
					}
					if(match) //if a match if found, the search for the current document can be stopped
						break;
					pid1++;
				}
				id1++;
				id2++;
			}
			else if(l1.get(id1).docId<l2.get(id2).docId)
				id1++;
			else
				id2++;
		}
		return mergedList;
	}

    public ArrayList<Integer> phraseQuery(String query){
        ArrayList<Integer> mergedListMultipleQueries = new ArrayList<Integer>();
	    String[] queryBreakDown = query.split(" ");
	    for(int i = 0;i<queryBreakDown.length;i++){
	        if((i+1)<queryBreakDown.length) {
                if(i==0) {
                    mergedListMultipleQueries = intersect(queryBreakDown[i], queryBreakDown[i + 1]);
                }else {
                    if(mergedListMultipleQueries != null){
                        mergedListMultipleQueries = merge(mergedListMultipleQueries, intersect(queryBreakDown[i], queryBreakDown[i + 1]), Constants.AND);
                    }

                }
            }
        }
	    return mergedListMultipleQueries;
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


	public static void main(String[] args)
	{
		File folder = new File("./Lab1_Data"); //args[0]

		Parser docParser = new Parser(folder, null);
		PositionalIndex pi = new PositionalIndex(docParser.myDocsContentMap);
		System.out.println(pi);
		ArrayList<Integer> result = pi.phraseQuery("crown 1997's best piece ");

		if(result!=null && !result.isEmpty())
		{
			for(Integer i:result)
				System.out.println("Phrase found in "+docParser.myDocsFileNameReferenceMap.get(i.intValue()));
		}
		else
			System.out.println("No match!");
	}

}

