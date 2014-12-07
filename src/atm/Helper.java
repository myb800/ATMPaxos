package atm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Helper {
	public static String readFile(File file) {
		StringBuilder sb = new StringBuilder((int) file.length());
		Scanner sc = null;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String lineSeperator = "\n";
		try {
			while (sc.hasNextLine()) {
				sb.append(sc.nextLine() + lineSeperator);
			}
			return sb.toString();
		} finally {
			sc.close();
		}
	}

	public static void checkLogFile() {
		File file = new File("ATMTrans.txt");
		try {
			file.createNewFile();
			
			file = new File("ATM2Trans.txt");
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
