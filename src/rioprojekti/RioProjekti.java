package rioprojekti;

public class RioProjekti {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

	RioMergeSort rioMerge = new RioMergeSortImpl();
	
	rioMerge.start();
	
	System.out.println("Mergesort time: " + rioMerge.getTimeInMilliseconds() + "milliseconds");
    }
}
