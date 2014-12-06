package atm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

	public static void main(String[] args) throws IOException {
		Constants.config(args[0]);
		int idx = Integer.parseInt(args[1]);
		ATM atm = new ATM(Constants.CLIENTS[idx].port, idx, Constants.CLIENTS[idx].recoveryPort,Constants.CLIENTS);
		Socket[] echoSocket;
		echoSocket = new Socket[5];
		for(int j = 0; j < 5; j++){
			Socket temp = new Socket(Constants.CLIENTS[j].address, Constants.CLIENTS[j].port);
			echoSocket[j] = temp;
			PrintWriter out = new PrintWriter(echoSocket[j].getOutputStream(), true);
			out.println("Are you alive?");
		}
		PrintWriter out = new PrintWriter(echoSocket[idx].getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket[idx].getInputStream()));

		while(true){
			if(in.readLine() != null)
				out.println("Yes, I am alive!");
			
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
