package fpgrowth;



import java.io.BufferedReader;
import java.io.BufferedWriter;
    import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author 
 */
public class FPGrowth {
    //new code
    // for statistics
	private long startTimestamp; // start time of the latest execution
	private long endTime; // end time of the latest execution
	private int transactionCount = 0; // transaction count in the database
	private int itemsetCount; // number of freq. itemsets found
	
	// parameter
	public int minSupportRelative;// the relative minimum support
	
	BufferedWriter writer = null; // object to write the output file
	
	// The  patterns that are found 
	// (if the user want to keep them into memory)
	protected Itemsets patterns = null;
		
	// This variable is used to determine the size of buffers to store itemsets.
	// A value of 50 is enough because it allows up to 2^50 patterns!
	final int BUFFERS_SIZE = 2000;
	
	// buffer for storing the current itemset that is mined when performing mining
	// the idea is to always reuse the same buffer to reduce memory usage.
	private int[] itemsetBuffer = null;
	// another buffer for storing fpnodes in a single path of the tree
	private FPNode[] fpNodeTempBuffer = null;
	
	// This buffer is used to store an itemset that will be written to file
	// so that the algorithm can sort the itemset before it is output to file
	// (when the user choose to output result to file).
	private int[] itemsetOutputBuffer = null;
	//end for fpgrowth    
            
            int threshold;
	    //fp-tree constructing fileds
	    Vector<FPtree> headerTable;
	    FPtree fptree;
		PrintWriter output = null;
		FileWriter fw;
	    //fp-growth
	    Map<String, Integer> frequentPatterns;
            
            public FPGrowth()
            {
                
            }
            
        public FPGrowth(File file, int threshold) throws IOException {
	        this.threshold = threshold;
	        //PrintWriter output = null;
            try{
            	File f = new File("outputFile.txt");
            	if(!f.exists())
            	    f.createNewFile();
                output = new PrintWriter(f);
            } catch(Exception e){
            System.exit(0);}
			output.close();
	        fptree(file);
	        fpgrowth(fptree, threshold, headerTable);
	        print();
	
        }
	
    private FPtree conditional_fptree_constructor(Map<String, Integer> conditionalPatternBase, Map<String, Integer> conditionalItemsMaptoFrequencies, int threshold, Vector<FPtree> conditional_headerTable) {
        //FPTree constructing
        //the null node!
        FPtree conditional_fptree = new FPtree("null");
        conditional_fptree.item = null;
        conditional_fptree.isroot = true;
        //remember our transactions here has oredering and non-frequent items for condition items
        for (String pattern : conditionalPatternBase.keySet()) {
            //adding to tree
            //removing non-frequents and making a vector instead of string
            Vector<String> pattern_vector = new Vector<String>();
            StringTokenizer tokenizer = new StringTokenizer(pattern);
            while (tokenizer.hasMoreTokens()) {
                String item = tokenizer.nextToken();
                if (conditionalItemsMaptoFrequencies.get(item) >= threshold) {
                    pattern_vector.addElement(item);
                }
            }
            //the insert method
            insert(pattern_vector, conditionalPatternBase.get(pattern), conditional_fptree, conditional_headerTable);
            //end of insert method
        }
        return conditional_fptree;
    }

    private void fptree(File file) throws IOException {
        //preprocessing fields
        Map<String, Integer> itemsMaptoFrequencies = new HashMap<String, Integer>();
        Scanner input = new Scanner(file);
        List <String> sortedItemsbyFrequencies = new LinkedList<String>();
        Vector<String> itemstoRemove = new Vector<String>();
        preProcessing(file, itemsMaptoFrequencies, input, sortedItemsbyFrequencies, itemstoRemove);
        construct_fpTree(file, itemsMaptoFrequencies, input, sortedItemsbyFrequencies, itemstoRemove);

    }
// 
    private void preProcessing(File file, Map<String, Integer> itemsMaptoFrequencies, Scanner input, List<String> sortedItemsbyFrequencies, Vector<String> itemstoRemove) throws FileNotFoundException {
        try{
        File f = new File("outputFile.txt");
        if(!f.exists())
            f.createNewFile();
        fw = new FileWriter(f.getAbsoluteFile(), true);
        output = new PrintWriter(fw);
    } catch(Exception e){
    System.exit(0);}
                //System.out.println("I am in preProcessing");
                //output.printf("I am in preProcessing");
    //output.printf("\n");
        while (input.hasNext()) {
            String temp = input.next();
            //System.out.println("temp = ");
            //System.out.println(temp);
            //System.exit(0);
            if (itemsMaptoFrequencies.containsKey(temp)) {
                int count = itemsMaptoFrequencies.get(temp);
                itemsMaptoFrequencies.put(temp, count + 1);
            } else {
                itemsMaptoFrequencies.put(temp, 1);
            }
        }
        input.close();
        // print all items with their frequencies
        for(String item: itemsMaptoFrequencies.keySet()){
        //System.out.println(item);
        System.out.println(itemsMaptoFrequencies.toString());
        output.printf(itemsMaptoFrequencies.toString());
        output.printf("\n");
        }
        //System.exit(0);
        //orderiiiiiiiiiiiiiiiiiiiiiiiiiiiing
        //also elimating non-frequents

        //for breakpoint for comparison
        sortedItemsbyFrequencies.add("null");
        itemsMaptoFrequencies.put("null", 0);
        for (String item : itemsMaptoFrequencies.keySet()) {
            int count = itemsMaptoFrequencies.get(item);
            // System.out.println( count );
            int i = 0;
            for (String listItem : sortedItemsbyFrequencies) {
                if (itemsMaptoFrequencies.get(listItem) < count) {
                    sortedItemsbyFrequencies.add(i, item);
                    break;
                }
                i++;
            }
        }
        //print the list of sorted items
        for(int i=0; i< sortedItemsbyFrequencies.size(); i++)
        {
            System.out.println(sortedItemsbyFrequencies.get(i));
            output.printf(sortedItemsbyFrequencies.get(i));
            output.printf("\n");
        }

        //remove item with frequencies less than the threshol

        for (String listItem : sortedItemsbyFrequencies) {
            if (itemsMaptoFrequencies.get(listItem) < threshold) {
                itemstoRemove.add(listItem);
            }
        }
        System.out.println("the items to be removed because they are not frequent");
        output.printf("the items to be removed because they are not frequent\n");
        for(String item: itemstoRemove)
        {
                System.out.println(item);
                output.printf(item);
            output.printf("\n");
        }
        for (String itemtoRemove : itemstoRemove) {
            sortedItemsbyFrequencies.remove(itemtoRemove);
        }
        System.out.println("the frequent items are");
        output.printf("the frequent items are");
        output.printf("\n");
        for(String item: sortedItemsbyFrequencies)
        {
                System.out.println(item);
                output.printf(item);
            output.printf("\n");
        }

        output.close();

        //System.exit(0);
    }

    private void construct_fpTree(File file, Map<String, Integer> itemsMaptoFrequencies, Scanner input, List<String> sortedItemsbyFrequencies, Vector<String> itemstoRemove) throws IOException {
        //HeaderTable Creation


// first elements use just as pointers
                try{
        File f = new File("outputFile.txt");
        if(!f.exists())
            f.createNewFile();
        fw = new FileWriter(f.getAbsoluteFile(), true);
        output = new PrintWriter(fw);
    } catch(Exception e){
    System.exit(0);}
        headerTable = new Vector<FPtree>();
        for (String itemsforTable : sortedItemsbyFrequencies) {
            headerTable.add(new FPtree(itemsforTable));
        }
        System.out.println("the elements of the header are");
        output.printf("the elements of the header are");
        output.printf("\n");
        for(FPtree element: headerTable){
        System.out.println(element.item);
        output.printf(element.item);
        output.printf("\n");
        }
        //System.exit(0);


        //FPTree constructing
        input = new Scanner(file);
        //the null node!
        fptree = new FPtree("null");
        fptree.next = null;
        fptree.isroot = true;
        //ordering frequent items transaction
        while (input.hasNextLine()) {
            String line = input.nextLine();
            StringTokenizer tokenizer = new StringTokenizer(line);
            Vector<String> transactionSortedbyFrequencies = new Vector<String>();
            while (tokenizer.hasMoreTokens()) {
                String item = tokenizer.nextToken();
                if (itemstoRemove.contains(item)) {
                    continue;
                }
                int index = 0;
                for (String vectorString : transactionSortedbyFrequencies) {
                    //some lines of condition is for alphabetically check in equals situatioans
                    if (itemsMaptoFrequencies.get(vectorString) < itemsMaptoFrequencies.get(item) || ((itemsMaptoFrequencies.get(vectorString) == itemsMaptoFrequencies.get(item)) && (item.compareToIgnoreCase(vectorString) < 0 ? true : false))) {
                        transactionSortedbyFrequencies.add(index, item);
                        break;
                    }
                    index++;
                }
                if (!transactionSortedbyFrequencies.contains(item)) {
                    transactionSortedbyFrequencies.add(item);
                }
            }
            System.out.println("the transactions after ordering based on most frequent items");
            output.printf("the transactions after ordering based on most frequent items");
            output.printf("\n");
            for(String item: transactionSortedbyFrequencies){
                System.out.print(item +"  ");
                output.printf(item +"  ");
            }
            System.out.println(); 
            output.printf("\n");
            //adding to tree
            insert(transactionSortedbyFrequencies, fptree, headerTable); //I am here


            transactionSortedbyFrequencies.clear();
        }
        //System.out.println(" the tree status");
        //output.printf(" the tree status\n");
        fw.close();
                        output.close();

        printTree(fptree);
        //System.exit(0);
        //headertable reverse ordering
        //first calculating item frequencies in tree
        for (FPtree item : headerTable) {
            int count = 0;
            FPtree itemtemp = item;
            while (itemtemp.next != null) {
                itemtemp = itemtemp.next;
                count += itemtemp.count;
            }
            item.count = count;
        }
        Comparator c = new HeaderTableComparator();
        Collections.sort(headerTable, c);
        input.close();
    }

    public void printTree(FPtree root) throws IOException {
        //System.out.println("I am in print tree");
        //writeFile("I am in print tree\n");
        String treePrint ="";
    if(root ==null) treePrint = "the tree is empty";
    else{
        treePrint = root.toString();
        for(FPtree pointer: root.children){
        printTree(pointer);}
        }
    System.out.println(treePrint);
    writeFile(treePrint);
    writeFile("\n");
    //System.out.println("I am exiting the print tree");
    //writeFile("I am exiting the print tree\n");

    }


public void writeFile(String result) throws IOException{
    try{
        File f = new File("outputFile.txt");
        if(!f.exists())
            f.createNewFile();
        fw = new FileWriter(f.getAbsoluteFile(), true);
        output = new PrintWriter(fw);
    } catch(Exception e)
    {
        System.exit(0);
    }
    output.printf(result);
    output.close();
    fw.close();
}


    void insert(Vector<String> transactionSortedbyFrequencies, FPtree fptree, Vector<FPtree> headerTable) {
        if (transactionSortedbyFrequencies.isEmpty()) {
            return;
        }
        String itemtoAddtotree = transactionSortedbyFrequencies.firstElement();
        FPtree newNode = null;
        boolean ifisdone = false;
        for (FPtree child : fptree.children) {
            if (child.item.equals(itemtoAddtotree)) {
                newNode = child;
                child.count++;
                ifisdone = true;
                break;
            }
        }
        if (!ifisdone) {
            newNode = new FPtree(itemtoAddtotree);
            newNode.count = 1;
            newNode.parent = fptree;
            fptree.children.add(newNode);
            for (FPtree headerPointer : headerTable) {
                if (headerPointer.item.equals(itemtoAddtotree)) {
                    while (headerPointer.next != null) {
                        headerPointer = headerPointer.next;
                    }
                    headerPointer.next = newNode;
                }
            }
        }
        transactionSortedbyFrequencies.remove(0);
        insert(transactionSortedbyFrequencies, newNode, headerTable);
    }

    private void fpgrowth(FPtree fptree, int threshold, Vector<FPtree> headerTable) {
        frequentPatterns = new HashMap<String, Integer>();
        FPgrowth(fptree, null, threshold, headerTable, frequentPatterns);
        int i = 0;
    }

    void FPgrowth(FPtree fptree, String base, int threshold, Vector<FPtree> headerTable, Map<String, Integer> frequentPatterns) {
        for (FPtree iteminTree : headerTable) {
            String currentPattern = (base != null ? base : "") + (base != null ? " " : "") + iteminTree.item;
            int supportofCurrentPattern = 0;
            Map<String, Integer> conditionalPatternBase = new HashMap<String, Integer>();
            while (iteminTree.next != null) {
                iteminTree = iteminTree.next;
                supportofCurrentPattern += iteminTree.count;
                String conditionalPattern = null;
                FPtree conditionalItem = iteminTree.parent;

                while (!conditionalItem.isRoot()) {
                    conditionalPattern = conditionalItem.item + " " + (conditionalPattern != null ? conditionalPattern : "");
                    conditionalItem = conditionalItem.parent;
                }
                if (conditionalPattern != null) {
                    conditionalPatternBase.put(conditionalPattern, iteminTree.count);
                }
            }
            frequentPatterns.put(currentPattern, supportofCurrentPattern);
            //counting frequencies of single items in conditional pattern-base
            Map<String, Integer> conditionalItemsMaptoFrequencies = new HashMap<String, Integer>();
            for (String conditionalPattern : conditionalPatternBase.keySet()) {
                StringTokenizer tokenizer = new StringTokenizer(conditionalPattern);
                while (tokenizer.hasMoreTokens()) {
                    String item = tokenizer.nextToken();
                    if (conditionalItemsMaptoFrequencies.containsKey(item)) {
                        int count = conditionalItemsMaptoFrequencies.get(item);
                        count += conditionalPatternBase.get(conditionalPattern);
                        conditionalItemsMaptoFrequencies.put(item, count);
                    } else {
                        conditionalItemsMaptoFrequencies.put(item, conditionalPatternBase.get(conditionalPattern));
                    }
                }
            }
            //conditional fptree
            //HeaderTable Creation
            // first elements are being used just as pointers
            // non conditional frequents also will be removed
            Vector<FPtree> conditional_headerTable = new Vector<FPtree>();
            for (String itemsforTable : conditionalItemsMaptoFrequencies.keySet()) {
                int count = conditionalItemsMaptoFrequencies.get(itemsforTable);
                if (count < threshold) {
                    continue;
                }
                FPtree f = new FPtree(itemsforTable);
                f.count = count;
                conditional_headerTable.add(f);
            }
            FPtree conditional_fptree = conditional_fptree_constructor(conditionalPatternBase, conditionalItemsMaptoFrequencies, threshold, conditional_headerTable);
            //headertable reverse ordering
            Collections.sort(conditional_headerTable, new HeaderTableComparator());
            //
            if (!conditional_fptree.children.isEmpty()) {
                FPgrowth(conditional_fptree, currentPattern, threshold, conditional_headerTable, frequentPatterns);
            }
        }
    }

    private void insert(Vector<String> pattern_vector, int count_of_pattern, FPtree conditional_fptree, Vector<FPtree> conditional_headerTable) {
        if (pattern_vector.isEmpty()) {
            return;
        }
        String itemtoAddtotree = pattern_vector.firstElement();
        FPtree newNode = null;
        boolean ifisdone = false;
        for (FPtree child : conditional_fptree.children) {
            if (child.item.equals(itemtoAddtotree)) {
                newNode = child;
                child.count += count_of_pattern;
                ifisdone = true;
                break;
            }
        }
        if (!ifisdone) {
            for (FPtree headerPointer : conditional_headerTable) {
                //this if also gurantees removing og non frequets
                if (headerPointer.item.equals(itemtoAddtotree)) {
                    newNode = new FPtree(itemtoAddtotree);
                    newNode.count = count_of_pattern;
                    newNode.parent = conditional_fptree;
                    conditional_fptree.children.add(newNode);
                    while (headerPointer.next != null) {
                        headerPointer = headerPointer.next;
                    }
                    headerPointer.next = newNode;
                }
            }
        }
        pattern_vector.remove(0);
        insert(pattern_vector, count_of_pattern, newNode, conditional_headerTable);
    }

    private void print() throws FileNotFoundException {
        /*
        Vector<String> sortedItems = new Vector<String>();
        sortedItems.add("null");
        frequentPatterns.put("null", 0);
        for (String item : frequentPatterns.keySet()) {
            int count = frequentPatterns.get(item);

            int i = 0;
            for (String listItem : sortedItems) {
                if (frequentPatterns.get(listItem) < count) {
                    sortedItems.add(i, item);
                    break;
                }
                i++;
            }
        }
         * 
         */
       // Formatter output = new Formatter("a.txt");
       PrintWriter output = null;
        try{
            output = new PrintWriter(new FileOutputStream("Frequent.txt"));
        } catch(FileNotFoundException e){
        System.exit(0);}
        for (String frequentPattern : frequentPatterns.keySet()) {
            output.printf("%s\t%d\n\n", frequentPattern,frequentPatterns.get(frequentPattern));

        }output.close();
    }
    
    //add new code for rules
    
    /**
	 * Method to run the FPGRowth algorithm.
	 * @param input the path to an input file containing a transaction database.
	 * @param output the output file path for saving the result (if null, the result 
	 *        will be returned by the method instead of being saved).
	 * @param minsupp the minimum support threshold.
	 * @return the result if no output file path is provided.
	 * @throws IOException exception if error reading or writing files
	 */
	public Itemsets runAlgorithm(String input, String output, double minsupp) throws FileNotFoundException, IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		// number of itemsets found
		itemsetCount = 0;
		
		//initialize tool to record memory usage
		
		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			patterns =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(output)); 
			itemsetOutputBuffer = new int[BUFFERS_SIZE];
		}
		
		// (1) PREPROCESSING: Initial database scan to determine the frequency of each item
		// The frequency is stored in a map:
		//    key: item   value: support
		final Map<Integer, Integer> mapSupport = scanDatabaseToDetermineFrequencyOfSingleItems(input); 

		// convert the minimum support as percentage to a
		// relative minimum support
		this.minSupportRelative = (int) Math.ceil(minsupp * transactionCount);
		
		// (2) Scan the database again to build the initial FP-Tree
		// Before inserting a transaction in the FPTree, we sort the items
		// by descending order of support.  We ignore items that
		// do not have the minimum support.
		FPtree tree = new FPtree();
		
		// read the file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) until the end of the file
		while( ((line = reader.readLine())!= null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||	line.charAt(0) == '#' || line.charAt(0) == '%'
				|| line.charAt(0) == '@') {
				continue;
			}
			
			String[] lineSplited = line.split(" ");
//			Set<Integer> alreadySeen = new HashSet<Integer>();
			List<Integer> transaction = new ArrayList<Integer>();
			
			// for each item in the transaction
			for(String itemString : lineSplited){  
				Integer item = Integer.parseInt(itemString.replaceAll("[^\\d.]", ""));
				// only add items that have the minimum support
				if(mapSupport.get(item) >= minSupportRelative){
					transaction.add(item);	
				}
			}
			// sort item in the transaction by descending order of support
			Collections.sort(transaction, new Comparator<Integer>(){
				public int compare(Integer item1, Integer item2){
					// compare the frequency
					int compare = mapSupport.get(item2) - mapSupport.get(item1);
					// if the same frequency, we check the lexical ordering!
					if(compare == 0){ 
						return (item1 - item2);
					}
					// otherwise, just use the frequency
					return compare;
				}
			});
			// add the sorted transaction to the fptree.
			tree.addTransaction(transaction);
		}
		// close the input file
		reader.close();
		
		// We create the header table for the tree using the calculated support of single items
		tree.createHeaderList(mapSupport);
		
		// (5) We start to mine the FP-Tree by calling the recursive method.
		// Initially, the prefix alpha is empty.
		// if at least an item is frequent
		if(tree.headerList.size() > 0) {
			// initialize the buffer for storing the current itemset
			itemsetBuffer = new int[BUFFERS_SIZE];
			// and another buffer
			fpNodeTempBuffer = new FPNode[BUFFERS_SIZE];
			// recursively generate frequent itemsets using the fp-tree
			// Note: we assume that the initial FP-Tree has more than one path
			// which should generally be the case.
			fpgrowth(tree, itemsetBuffer, 0, transactionCount, mapSupport);
		}
		
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		// record the execution end time
		endTime= System.currentTimeMillis();
		
		// check the memory usage
		
		// return the result (if saved to memory)
		return patterns;
	}


	
	/**
	 * Mine an FP-Tree having more than one path.
	 * @param tree  the FP-tree
	 * @param prefix  the current prefix, named "alpha"
	 * @param mapSupport the frequency of items in the FP-Tree
	 * @throws IOException  exception if error writing the output file
	 */
	private void fpgrowth(FPtree tree, int [] prefix, int prefixLength, int prefixSupport, Map<Integer, Integer> mapSupport) throws IOException {
////		======= DEBUG ========
//		System.out.print("###### Prefix: ");
//		for(int k=0; k< prefixLength; k++) {
//			System.out.print(prefix[k] + "  ");
//		}
//		System.out.println("\n");
////				========== END DEBUG =======
//		System.out.println(tree);
		
		// We will check if the FPtree contains a single path
		boolean singlePath = true;
		// We will use a variable to keep the support of the single path if there is one
		int singlePathSupport = 0;
		// This variable is used to count the number of items in the single path
		// if there is one
		int position = 0;
		// if the root has more than one child, than it is not a single path
		if(tree.root.childs.size() > 1) {
			singlePath = false;
		}else {
			
			// Otherwise,
			// if the root has exactly one child, we need to recursively check childs
			// of the child to see if they also have one child
			FPNode currentNode = tree.root.childs.get(0);
			while(true){
				// if the current child has more than one child, it is not a single path!
				if(currentNode.childs.size() > 1) {
					singlePath = false;
					break;
				}
				// otherwise, we copy the current item in the buffer and move to the child
				// the buffer will be used to store all items in the path
				fpNodeTempBuffer[position] = currentNode;
				
				position++;
				// if this node has no child, that means that this is the end of this path
				// and it is a single path, so we break
				if(currentNode.childs.size() == 0) {
					break;
				}
				currentNode = currentNode.childs.get(0);
			}
		}
		
		// Case 1: the FPtree contains a single path
		if(singlePath && singlePathSupport >= minSupportRelative){	
			// We save the path, because it is a maximal itemset
			saveAllCombinationsOfPrefixPath(fpNodeTempBuffer, position, prefix, prefixLength);
		}else {
			// For each frequent item in the header table list of the tree in reverse order.
			for(int i = tree.headerList.size()-1; i>=0; i--){
				// get the item
				Integer item = tree.headerList.get(i);
				
				// get the item support
				int support = mapSupport.get(item);
	
				// Create Beta by concatening prefix Alpha by adding the current item to alpha
				prefix[prefixLength] = item;
				
				// calculate the support of the new prefix beta
				int betaSupport = (prefixSupport < support) ? prefixSupport: support;
				
				// save beta to the output file
				saveItemset(prefix, prefixLength+1, betaSupport);
				
				// === (A) Construct beta's conditional pattern base ===
				// It is a subdatabase which consists of the set of prefix paths
				// in the FP-tree co-occuring with the prefix pattern.
				List<List<FPNode>> prefixPaths = new ArrayList<List<FPNode>>();
				FPNode path = tree.mapItemNodes.get(item);
				
				// Map to count the support of items in the conditional prefix tree
				// Key: item   Value: support
				Map<Integer, Integer> mapSupportBeta = new HashMap<Integer, Integer>();
				
				while(path != null){
					// if the path is not just the root node
					if(path.parent.itemID != -1){
						// create the prefixpath
						List<FPNode> prefixPath = new ArrayList<FPNode>();
						// add this node.
						prefixPath.add(path);   // NOTE: we add it just to keep its support,
						// actually it should not be part of the prefixPath
						
						// ####
						int pathCount = path.counter;
						
						//Recursively add all the parents of this node.
						FPNode parent = path.parent;
						while(parent.itemID != -1){
							prefixPath.add(parent);
							
							// FOR EACH PATTERN WE ALSO UPDATE THE ITEM SUPPORT AT THE SAME TIME
							// if the first time we see that node id
							if(mapSupportBeta.get(parent.itemID) == null){
								// just add the path count
								mapSupportBeta.put(parent.itemID, pathCount);
							}else{
								// otherwise, make the sum with the value already stored
								mapSupportBeta.put(parent.itemID, mapSupportBeta.get(parent.itemID) + pathCount);
							}
							parent = parent.parent;
						}
						// add the path to the list of prefixpaths
						prefixPaths.add(prefixPath);
					}
					// We will look for the next prefixpath
					path = path.nodeLink;
				}

				// (B) Construct beta's conditional FP-Tree
				// Create the tree.
				FPtree treeBeta = new FPtree();
				// Add each prefixpath in the FP-tree.
				for(List<FPNode> prefixPath : prefixPaths){
					treeBeta.addPrefixPath(prefixPath, mapSupportBeta, minSupportRelative); 
				}  
				
				// Mine recursively the Beta tree if the root has child(s)
				if(treeBeta.root.childs.size() > 0){

					// Create the header list.
					treeBeta.createHeaderList(mapSupportBeta); 
					// recursive call
					fpgrowth(treeBeta, prefix, prefixLength+1, betaSupport, mapSupportBeta);
				}
			}
		}
		
	}


	/**
	 * This method saves all combinations of a prefix path if it has enough support
	 * @param prefix the current prefix
	 * @param prefixLength the current prefix length
	 * @param prefixPath the prefix path
	 * @throws IOException if exception while writting to output file
	 */
	private void saveAllCombinationsOfPrefixPath(FPNode[] fpNodeTempBuffer, int position, 
			int[] prefix, int prefixLength) throws IOException {

		int support = 0;
		// Generate all subsets of the prefixPath except the empty set
		// and output them
		// We use bits to generate all subsets.
		for (long i = 1, max = 1 << position; i < max; i++) {
			
			// we create a new subset
			int newPrefixLength = prefixLength;
			
			// for each bit
			for (int j = 0; j < position; j++) {
				// check if the j bit is set to 1
				int isSet = (int) i & (1 << j);
				// if yes, add the bit position as an item to the new subset
				if (isSet > 0) {
					prefix[newPrefixLength++] = fpNodeTempBuffer[j].itemID;
					if(support == 0) {
						support = fpNodeTempBuffer[j].counter;
					}
				}
			}
			// save the itemset
			saveItemset(prefix, newPrefixLength, support);
		}
	}
	

	/**
	 * This method scans the input database to calculate the support of single items
	 * @param input the path of the input file
	 * @throws IOException  exception if error while writing the file
	 * @return a map for storing the support of each item (key: item, value: support)
	 */
	private  Map<Integer, Integer> scanDatabaseToDetermineFrequencyOfSingleItems(String input)
			throws FileNotFoundException, IOException {
		// a map for storing the support of each item (key: item, value: support)
		 Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();
		//Create object for reading the input file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) until the end of file
		while( ((line = reader.readLine())!= null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||  line.charAt(0) == '#' || line.charAt(0) == '%' 	|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the line into items
			String[] lineSplited = line.split(" ");
			// for each item
			for(String itemString : lineSplited){  
				// increase the support count of the item
				Integer item = Integer.parseInt(itemString.replaceAll("[^\\d.]", ""));
				// increase the support count of the item
				Integer count = mapSupport.get(item);
				if(count == null){
					mapSupport.put(item, 1);
				}else{
					mapSupport.put(item, ++count);
				}
			}
			// increase the transaction count
			transactionCount++;
		}
		// close the input file
		reader.close();
		
		return mapSupport;
	}


	/**
	 * Write a frequent itemset that is found to the output file or
	 * keep into memory if the user prefer that the result be saved into memory.
	 */
	private void saveItemset(int [] itemset, int itemsetLength, int support) throws IOException {
		
		// increase the number of itemsets found for statistics purpose
		itemsetCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			// copy the itemset in the output buffer and sort items
			System.arraycopy(itemset, 0, itemsetOutputBuffer, 0, itemsetLength);
			Arrays.sort(itemsetOutputBuffer, 0, itemsetLength);
			
			// Create a string buffer
			StringBuilder buffer = new StringBuilder();
			// write the items of the itemset
			for(int i=0; i< itemsetLength; i++){
				buffer.append(itemsetOutputBuffer[i]);
				if(i != itemsetLength-1){
					buffer.append(' ');
				}
			}
			// Then, write the support
			buffer.append(" #SUP: ");
			buffer.append(support);
			// write to file and create a new line
			writer.write(buffer.toString());
			writer.newLine();
			
		}// otherwise the result is kept into memory
		else{
			// create an object Itemset and add it to the set of patterns 
			// found.
			int[] itemsetArray = new int[itemsetLength];
			System.arraycopy(itemset, 0, itemsetArray, 0, itemsetLength);
			
			// sort the itemset so that it is sorted according to lexical ordering before we show it to the user
			Arrays.sort(itemsetArray);
			
			Itemset itemsetObj = new Itemset(itemsetArray);
			itemsetObj.setAbsoluteSupport(support);
			patterns.addItemset(itemsetObj, itemsetLength);
		}
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  FP-GROWTH 0.96r19 - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + transactionCount);
		System.out.print(" Max memory usage: " + " mb \n");
		System.out.println(" Frequent itemsets count : " + itemsetCount); 
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println("===================================================");
	}

	/**
	 * Get the number of transactions in the last transaction database read.
	 * @return the number of transactions.
	 */
	public int getDatabaseSize() {
		return transactionCount;
	}
    
    //end add new code for rules
}



