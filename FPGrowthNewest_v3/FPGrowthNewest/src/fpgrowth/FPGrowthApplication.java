package fpgrowth;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class FPGrowthApplication {

    /**
     * @param args the command line arguments
     */
    	
    static int threshold = 2;   
    static String file = "F:\\LivePreson\\Java\\Anthem\\FP_Growth\\FP_Growth\\dataset";
	

    public static void main(String[] args) throws IOException { 
        long start = System.currentTimeMillis();
        FPGrowth app = new FPGrowth(new File(file), threshold);
        System.out.println((System.currentTimeMillis() - start));
      }
}

