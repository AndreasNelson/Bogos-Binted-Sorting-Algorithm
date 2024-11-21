import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

// To run on a single core, compile and then run as:
// taskset -c 0 java GroupN
// To avoid file reading/writing connections to the server, run in /tmp 
// of your lab machine.

// to run for testing: java Group1 input.txt output.txt

public class Group1 {

	public static void main(String[] args) throws InterruptedException, FileNotFoundException {

		if (args.length < 2) {
			System.out.println(
					"Running tests since input and output file names not specified");
			SortingCompetitionComparator.runComparatorTests();
			System.exit(0);
		}

		String inputFileName = args[0];
		String outFileName = args[1];
		
		// Uncomment to test comparator methods

		int [][] data = readData(inputFileName); // read data as strings
		
		int [][] toSort = data.clone(); // clone the data

		sort(toSort); // call the sorting method once for JVM warmup
		
		toSort = data.clone(); // clone again

		Thread.sleep(10); // to let other things finish before timing; adds stability of runs

		long start = System.currentTimeMillis();

		sort(toSort); // sort again

		long end = System.currentTimeMillis();

		System.out.println(end - start + " ms");

		writeOutResult(toSort, outFileName); // write out the results

	}


	private static int [][] readData(String inputFileName) throws FileNotFoundException {
		ArrayList<int[]> input = new ArrayList<>();
		Scanner in = new Scanner(new File(inputFileName));

		while (in.hasNext()) {
			String str = in.next();
			input.add(Arrays.stream(str.split(",")).mapToInt(Integer::parseInt).toArray());
		}

		in.close();

		// the string array is passed just so that the correct type can be created
		return input.toArray(new int[0][]);
	}

	// YOUR SORTING METHOD GOES HERE.
	// You may call other methods and use other classes.
	// Note: you may change the return type of the method.
	// You would need to provide your own function that prints your sorted array to
	// a file in the exact same format that my program outputs
	private static void sort(int[][] toSort) {
		quicksort(toSort, 0, toSort.length - 1, new SortingCompetitionComparator());
	}
	
	private static void quicksort(int[][] array, int low, int high, Comparator<int[]> comparator) {
		while (low < high) {
			// Use insertion sort for small subarrays
			if (high - low <= 10) {  // Reduced threshold for single-core
				insertionSort(array, low, high, comparator);
				return;
			}
			
			// Simple pivot selection - middle element to avoid stack overflow on sorted arrays
			int mid = low + ((high - low) >> 1);
			swap(array, mid, high);
			int[] pivot = array[high];
			
			// Optimized single-scan partition
			int i = low - 1;
			for (int j = low; j < high; j++) {
				if (comparator.compare(array[j], pivot) <= 0) {
					i++;
					if (i != j) {
						int[] temp = array[i];
						array[i] = array[j];
						array[j] = temp;
					}
				}
			}
			int pivotPos = i + 1;
			array[high] = array[pivotPos];
			array[pivotPos] = pivot;
	
			// Handle smaller partition first to minimize stack depth
			if (pivotPos - low < high - pivotPos) {
				quicksort(array, low, pivotPos - 1, comparator);
				low = pivotPos + 1;
			} else {
				quicksort(array, pivotPos + 1, high, comparator);				high = pivotPos - 1;
			}
		}
	}
	
	private static void insertionSort(int[][] array, int low, int high, Comparator<int[]> comparator) {
		for (int i = low + 1; i <= high; i++) {
			int[] key = array[i];
			int j = i - 1;
			
			// Direct scanning is faster than binary search for small arrays on single core
			while (j >= low && comparator.compare(array[j], key) > 0) {
				array[j + 1] = array[j];
				j--;
			}
			array[j + 1] = key;
		}
	}
	
	private static void swap(int[][] array, int i, int j) {
		if (i != j) {
			int[] temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
	}
	

	private static class SortingCompetitionComparator implements Comparator<int []> {
		
		@Override
		public int compare(int [] seq1, int [] seq2) {
			// looking for different elements in the same positions
			for (int i = 0; i < seq1.length && i < seq2.length ; ++i) {
				int diff = seq1[i] - seq2[i];
				if (diff != 0) return diff;
			}
			
			// two sequences are identical:
			if (seq1.length == seq2.length) return 0;
			
			// one sequence is a prefix of the other:
			
			// comparing even values:
			int seq1_evens = 0;
			for (int i = 0; i < seq1.length; ++i) {
				if (seq1[i] % 2 == 0) seq1_evens++;
			}
			
			int seq2_evens = 0;
			for (int i = 0; i < seq2.length; ++i) {
				if (seq2[i] % 2 == 0) seq2_evens++;
			}
			
			int diff = seq1_evens - seq2_evens;
			if (diff != 0) return diff; 
			
			// return the negated difference of odds 
			return (seq2.length - seq2_evens) - (seq1.length - seq1_evens);
		}



		public static void runComparatorTests() {
			int [] arr1 = {1, 3, 2};
			int [] arr2 = {1, 2, 3};
			System.out.println("Comparing arr1 and arr2");
			System.out.println((new SortingCompetitionComparator()).compare(arr1, arr2));	
			System.out.println((new SortingCompetitionComparator()).compare(arr2, arr1));
			System.out.println((new SortingCompetitionComparator()).compare(arr1, arr1));
			
			int [] arr3 = {1, 3, 2, 5, 4};
			
			System.out.println("Comparing arr1 and arr3");
			// arr3 should be larger:
			System.out.println((new SortingCompetitionComparator()).compare(arr1, arr3));
			System.out.println((new SortingCompetitionComparator()).compare(arr3, arr1));
			
			int [] arr4 = {1, 3, 2, 7, 6, 5, 4};
			
			System.out.println("Comparing arr1 and arr4");
			// arr1 should be larger since they have the same number of evens, but the number
			// of odds is higher in arr4, and the comparison goes the opposite way:
			System.out.println((new SortingCompetitionComparator()).compare(arr1, arr4));
			System.out.println((new SortingCompetitionComparator()).compare(arr4, arr1));
			
			System.out.println("Comparing arr4 and arr3");
			System.out.println((new SortingCompetitionComparator()).compare(arr4, arr3));
			
			int [] arr5 = {1, 3, 2, 5, 6, 7, 4};
			
			System.out.println("Comparing arr1 and arr5");

			System.out.println((new SortingCompetitionComparator()).compare(arr1, arr5));
			System.out.println((new SortingCompetitionComparator()).compare(arr5, arr1));			
			
			System.out.println("Comparing arr5 and arr3");
			System.out.println((new SortingCompetitionComparator()).compare(arr5, arr3));
			
			System.out.println("Comparing arr5 and arr4");
			System.out.println((new SortingCompetitionComparator()).compare(arr5, arr4));
			
			int [] arr6 = {1, 3, 2, 6, 5, 7, 4};
			System.out.println("Comparing arr1 and arr6");
			System.out.println((new SortingCompetitionComparator()).compare(arr1, arr6));
			
			System.out.println("Comparing arr3 and arr6");
			System.out.println((new SortingCompetitionComparator()).compare(arr3, arr6));
			
			System.out.println("Comparing arr5 and arr6");
			System.out.println((new SortingCompetitionComparator()).compare(arr5, arr6));
			
			
			System.out.println("Comparing arr4 and arr6");
			System.out.println((new SortingCompetitionComparator()).compare(arr4, arr6));
			 
		}
		

	}
	
	private static void writeOutResult(int [][] sorted, String outputFilename) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(outputFilename);
		for (int [] s : sorted) {
			for (int i = 0; i < s.length; ++i) {
				out.print(s[i]+(i<s.length-1?",":""));
			}
			out.println();
		}
		out.close();
	}
	
	// java Group1 input.txt output.txt
}