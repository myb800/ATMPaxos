package atm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	public static void main(String[] args) throws IOException {
		Constants.config(args[0]);
		int idx = Integer.parseInt(args[1]);
		ATM atm = new ATM(Constants.CLIENTS[idx].port, idx, Constants.CLIENTS[idx].recoveryPort,Constants.CLIENTS);
		while(true){
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String[] op;
			try {
				op = br.readLine().split(" ");
				if(op[0].equals("deposit")){
					atm.deposit(Integer.parseInt(op[1]));
					System.out.println("deposit " + op[1] + "; current balance " + atm.getBalance());
				}	
				else if(op[0].equals("withdraw")){
					atm.withdraw(Integer.parseInt(op[1]));
					System.out.println("withdraw " + op[1] + "; current balance " + atm.getBalance());
				}
				else if(op[0].equals("balance")){
					System.out.println("current balance " + atm.getBalance());
				}
				else if(op[0].equals("fail")){
					atm.fail();
					System.out.println("the failed node: " + idx);
				}
				else
					System.out.println("error command!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
