package atm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ATM atm = new ATM(2005, 1);
		while(true){
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String[] op;
			try {
				op = br.readLine().split(" ");
				if(op[0].equals("deposit"))
					atm.deposit(Integer.parseInt(op[1]));
				else if(op[0].equals("withdraw"))
					atm.withdraw(Integer.parseInt(op[1]));
				else
					System.out.println("error command!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
