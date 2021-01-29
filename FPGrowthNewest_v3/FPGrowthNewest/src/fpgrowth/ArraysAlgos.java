/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fpgrowth;

import java.util.Arrays;
import java.util.Comparator;

public class ArraysAlgos {

    /**
     * Make a copy of this itemset but exclude a given item
     * @param itemToRemove the given item
     * @return the copy
     */
    public static int[] cloneItemSetMinusOneItem(int[] itemset, Integer itemToRemove) {
        // create the new itemset
        int[] newItemset = new int[itemset.length -1];
        int i=0;
        // for each item in this itemset
        for(int j =0; j < itemset.length; j++){
            // copy the item except if it is the item that should be excluded
            if(itemset[j] != itemToRemove){
                newItemset[i++] = itemset[j];
            }
        }
        return newItemset; // return the copy
    }

    /**
     * Make a copy of this itemset but exclude a set of items
     * @param itemsetToNotKeep the set of items to be excluded
     * @return the copy
     */
    public static int[] cloneItemSetMinusAnItemset(int[] itemset, int[] itemsetToNotKeep) {
        // create a new itemset
        int[] newItemset = new int[itemset.length - itemsetToNotKeep.length];
        int i=0;
        // for each item of this itemset
        for(int j = 0; j < itemset.length; j++){
            // copy the item except if it is not an item that should be excluded
            if(Arrays.binarySearch(itemsetToNotKeep, itemset[j]) < 0 ){
                    newItemset[i++] = itemset[j];
            }
        }
        return newItemset; // return the copy
    }

    /**
     * This method performs the intersection of two sorted arrays of integers and return a new sorted array.
     * @param a the first array
     * @param b the second array
     * @return the resulting sorted array
     */
    public static int[] intersectTwoSortedArrays(int[] array1, int[] array2){
            // create a new array having the smallest size between the two arrays
        final int newArraySize = (array1.length < array2.length) ? array1.length : array2.length;
        int[] newArray = new int[newArraySize];

        int pos1 = 0;
        int pos2 = 0;
        int posNewArray = 0;
        while(pos1 < array1.length && pos2 < array2.length) {
            if(array1[pos1] < array2[pos2]) {
                pos1++;
            }else if(array2[pos2] < array1[pos1]) {
                pos2++;
            }else { // if they are the same
                newArray[posNewArray] = array1[pos1];
                posNewArray++;
                pos1++;
                pos2++;
            }
        }
        // return the subrange of the new array that is full.
        return Arrays.copyOfRange(newArray, 0, posNewArray); 
    }

    /** A Comparator for comparing two itemsets having the same size using the lexical order. */
    public static Comparator<int[]> comparatorItemsetSameSize = new Comparator<int[]>() {
        @Override
        /**
         * Compare two itemsets and return -1,0 and 1 if the second itemset 
         * is larger, equal or smaller than the first itemset according to the lexical order.
         */
        public int compare(int[] itemset1, int[] itemset2) {
            // for each item in the first itemset
            for(int i=0; i < itemset1.length; i++) {
                    // if the current item is smaller in the first itemset
                    if(itemset1[i] < itemset2[i]) {
                            return -1; // than the first itemset is smaller
                    // if the current item is larger in the first itemset
                    }else if(itemset2[i] < itemset1[i]) {
                            return 1; // than the first itemset is larger
                    }
                    // otherwise they are equal so the next item in both itemsets will be compared next.
            }
            return 0; // both itemsets are equal
        }
    };
}
