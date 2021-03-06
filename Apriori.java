import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.util.*;
import java.io.*;


/**
 * Created by sreenath on 2/11/2016.
 */
public class Apriori {

    public ArrayList<Item> items;

    public ArrayList<String> words;

    public ArrayList<Item> frequentItemsList;

    //used for printing purposes
    public static BufferedWriter printer;

    public String nameOfTransactionFile;

    public int numDifItems;

    public int totalTransactions;

    public int min_sup;

    public Apriori(String file, double min) throws IOException{

        items = new ArrayList<Item>();
        frequentItemsList = new ArrayList<Item>();
        printer = new BufferedWriter(new FileWriter(generateFileName(file)));
        nameOfTransactionFile = file;
        numDifItems = 0;
        totalTransactions = 0;
        words = new ArrayList<String>();
        calculateNumItems();
        Double tmp;
        tmp = min * totalTransactions;
        min_sup = tmp.intValue();
        printValues();
        mine();

    }

    public String generateFileName(String f){
        if(f.contains("0")){
            return "pattern-0.txt";
        }
        else if(f.contains("1")){
            return "pattern-1.txt";
        }
        else if(f.contains("2")){
            return "pattern-2.txt";
        }
        else if(f.contains("3")){
            return "pattern-3.txt";
        }
        else if(f.contains("4")){
            return "pattern-4.txt";
        }

        return "pattern-def.txt";
    }

    public void calculateNumItems() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(nameOfTransactionFile));
        String data = "";
        String token = "";
        while(reader.ready()){
            totalTransactions++;
            data = reader.readLine();

            StringTokenizer word = new StringTokenizer(data," ");
            while (word.hasMoreTokens()) {
                numDifItems++;
                token = word.nextToken();
                words.add(token);
            }
        }
    }

    public void printValues(){

        for (int i = 0 ; i < words.size(); i++){
          //  System.out.println("ADDED WORDS: "+words.get(i));
        }

        for (int i = 0 ; i < items.size(); i++){
         //   System.out.println("ADDED WORDS: "+words.get(i));
        }

        System.out.println();
        System.out.println("Currently Processing File: " + nameOfTransactionFile);
        System.out.println("Number of items in File: " + numDifItems);
        System.out.println("Total Transactions: " + totalTransactions);
        System.out.println("min sup: " + min_sup);


    }

    public void printPatterns(){
        for (int i = 0; i < items.size(); i ++){

            System.out.print("FREQUENT PATTERN LIST: ");
            for (int j = 0; j < items.get(i).pattern.length; j++){
                System.out.print(" "+items.get(i).pattern[j]);
            }
            System.out.print(" COUNT: " + items.get(i).count);
            System.out.println();
        }

        System.out.println("NUMBER OF FREQUENT PATTERNS: " + items.size() + " TOTAL TRANSACTIONS: " + totalTransactions);
    }

    public void printToFile() throws IOException{

        //sort in the correct order before printing
        setupFormat();
        for (int i = 0; i < items.size(); i ++){

            printer.write(items.get(i).count + " ");
            System.out.print(items.get(i).count +" ");
            for (int j = 0; j < items.get(i).pattern.length; j++){
                if (j ==0){
                    System.out.print(items.get(i).pattern[j]);
                    printer.write(items.get(i).pattern[j]);
                }
                else{
                    System.out.print(" "+items.get(i).pattern[j]);
                    printer.write(" "+items.get(i).pattern[j]);
                }

            }
            printer.newLine();
            System.out.println();

        }
    }

    public void setupFormat(){

        Item[] sortpats =  new Item[frequentItemsList.size()];
        sortpats = frequentItemsList.toArray(sortpats);
        Arrays.sort(sortpats);
        ArrayList<Item> sortedListOfPatterns = new ArrayList<Item>(Arrays.asList(sortpats));
        frequentItemsList = sortedListOfPatterns;
        items = frequentItemsList;
    }

    public void addToFreqList(){
        removeDuplicatesInCandidateList();
        for(int i = 0; i < items.size(); i++){
            frequentItemsList.add(items.get(i));
        }
    }

    public void mine() throws IOException{

        int kVal = 1;

        //create the first itemset with all the items
        createFirstList();
        calculateFrequent();
        removeItemsBelowMinSupport();
        addToFreqList();
        //printToFile();

        while (items.size()>0){
            createOtherCandidateLists(kVal);
            calculateFrequent();
            removeItemsBelowMinSupport();
            addToFreqList();
            //printToFile();
            kVal++;
        }
        printToFile();
        printer.close();


    }

    public void createFirstList(){

        for(int i = 0;i< numDifItems; i++){
            String[] list = {words.get(i)};
            Item pattern = new Item(list, 0);
            items.add(pattern);
        }

        //remove duplicates added to list
        removeDuplicatesInCandidateList();
    }

    public void createOtherCandidateLists(int k){

        ArrayList<Item> newPat = new ArrayList<Item>();
        ArrayList<String> wordCombo = new ArrayList<String>();

        if (k == 1){
            //loop through the list of patterns
            for(int i = 0; i < items.size(); i ++){
                String word = items.get(i).pattern[0];
                wordCombo.add(word);

                //for every pattern loop thorugh all the other patterns to create combos
                for (int j = i +1; j < items.size(); j++){

                    if(!items.get(j).pattern[0].equals(items.get(i).pattern[0])){

                        String word2 = items.get(j).pattern[0];
                        wordCombo.add(word2);

                        //conver pattern into an array
                        String[] arr = new String[wordCombo.size()];
                        arr = wordCombo.toArray(arr);

                        //add this array to the new item and set count to 0
                        Item pattern = new Item(arr, 0);
                        newPat.add(pattern);

                        //clear wordscombo
                        wordCombo.remove(1);
                    }
                }
                wordCombo.clear();
            }

            items = newPat;
            removeDuplicatesInCandidateList();
        }

        else{

            for(int i = 0; i < items.size(); i ++){
                ArrayList<String> patternToMatch = new ArrayList<String>();
                ArrayList<String> newCandidate = new ArrayList<String>();
                //pick the k-1th pattern in the current patterns
                for(int l = 0; l < k-1; l ++){
                    patternToMatch.add(items.get(i).pattern[l]);
                }

                //check that the same patterns exist in all the other patterns in list
                for(int j = i+1; j < items.size(); j++){

                    boolean comp = false;
                    for(int m = 0; m < k-1; m ++){
                        if(patternToMatch.get(m).equals(items.get(j).pattern[m])){
                            comp = true;
                        }
                        else{
                            comp = false;
                        }
                    }
                    //found a matching pattern
                    if(comp){

                        //add all the values to the new candidate
                        for (int n = 0; n < k; n ++){
                                newCandidate.add(items.get(i).pattern[n]);
                        }
                        newCandidate.add(items.get(j).pattern[k-1]);
                        //convert pattern into an array
                        String[] arr = new String[newCandidate.size()];
                        arr = newCandidate.toArray(arr);

                        //add this array to the new item and set count to 0
                        Item pattern = new Item(arr, 0);
                        newPat.add(pattern);

                        //clear wordscombo
                        newCandidate.clear();
                    }
                }
            }
            items = newPat;
            removeDuplicatesInCandidateList();
        }


    }
    public void removeItemsBelowMinSupport(){

        ArrayList<Item> tmp = new ArrayList<Item>();

            for(int i = 0; i < items.size(); i++){
                if(items.get(i).count < min_sup){
                    //INSTEAD OF REMOVING WE JUST DONT ADD IT TO THE NEW LIST
                }
                else{
                    //All these are above the min support so add it to the new list
                    tmp.add(items.get(i));
                }
            }

            items = tmp;
    }

    public void calculateFrequent() throws IOException{

        boolean match = false;
        //frequent candidates list
        ArrayList<String[]> freqCand = new ArrayList<String[]>();

        String data = "";
        // read the file and compare every transcartion with the candidate list
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(nameOfTransactionFile)));

        while (reader.ready()){
            // read the transaction in and tokenize it for easy traversal.
            data = reader.readLine();

                //loop over the entier list of candidates and find out if there is a match
                for(int i = 0; i < items.size(); i++){

                    // this will store the patterns in the candidate list
                    String[] elementsToMatch;
                    elementsToMatch = items.get(i).pattern;

                    //if the pattern exists withing the read line then keep change match to true
                    for(int j = 0; j < elementsToMatch.length; j++){
                        //System.out.println(elementsToMatch[j]);
                        if(data.contains(elementsToMatch[j])){

                            match = true;
                        }
                        else{
                            //System.out.println("Data line: " + data + "  MISS matching: " + elementsToMatch[j]);
                            match = false;
                            break;
                        }
                    }

                    // if the pattern was found after the comparison of the entire patter then increment the count
                    if(match){
                        items.get(i).count++;
                    }
            }
        }

    }

    public void removeDuplicatesInCandidateList(){

        for (int i = 0; i < items.size(); i++){

            for (int j = i+1; j < items.size(); j++){
                if(Arrays.equals(items.get(i).pattern, items.get(j).pattern)){
                    items.remove(j);
                }
            }
        }

    }

}
