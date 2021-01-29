/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fpgrowth;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AssociationRules_FPGrowth_saveToFile {
    
    public AssociationRules_FPGrowth_saveToFile(String input, double minconf, double minsupp) {
        try{
            
            String output = "Rules.txt";
            
            // STEP 1: Applying the FP-GROWTH algorithm to find frequent itemsets
            FPGrowth fpgrowth = new FPGrowth();
            Itemsets patterns = fpgrowth.runAlgorithm(input, null, minsupp);
            //patterns.printItemsets(database.size());
            fpgrowth.printStats();
            int databaseSize = fpgrowth.getDatabaseSize();
            
            // Get list string map to make this algorithm can run with input string
            Map<String,String> mapName = readMapName(input);

            // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
            //double  minconf = 0.60;
            AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94(mapName);
            algoAgrawal.runAlgorithm(patterns, output, databaseSize, minconf);
            algoAgrawal.printStats();
        }catch(IOException e)
        {
            
        }
    }
    public Map<String,String> readMapName(String input) throws IOException
    {
 
        Map<String,String> mapName = new HashMap<String, String>();
        
        BufferedReader reader = new BufferedReader(new FileReader(input));
	String line;
	// for each line (transaction) until the end of the file
	while( ((line = reader.readLine())!= null)){ 
            // if the line is  a comment, is  empty or is a
            // kind of metadata
            if (line.isEmpty() == true || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                 continue;
            }

            String[] lineSplited = line.split(" ");
            // for each item in the transaction
            for(String itemString : lineSplited){  
                Integer item = Integer.parseInt(itemString.replaceAll("[^\\d.]", ""));
                if(!mapName.containsKey(Integer.toString(item)))
                {
                    mapName.put(Integer.toString(item), itemString);
                }
            }
        }
        // close the input file
        reader.close();
        
        return mapName;
    }
}
