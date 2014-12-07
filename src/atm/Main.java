package atm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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
		for(Node node:Constants.CLIENTS)
			Client.send(node.port, "Are you alive?", node.address, null);
		while(true){
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String[] op;
			try {
				op = br.readLine().split(" ");
				if(op[0].equals("deposit")){
					for(Node node:Constants.CLIENTS)
						Client.send(node.port, "Are you alive?", node.address, null);
					initCheck serverActionDeposit = new initCheck();
					new Thread(new Server(Constants.CLIENTS[idx].port, serverActionDeposit)).start();
					if(serverActionDeposit.getreplyNum() > 2){
					atm.deposit(Integer.parseInt(op[1]));
					System.out.println("deposit " + op[1] + "; current balance " + atm.getBalance());
					}
					else
						System.out.println("deposit FAILURE: cannot get majority reply.");
				}	
				else if(op[0].equals("withdraw")){
					for(Node node:Constants.CLIENTS)
						Client.send(node.port, "Are you alive?", node.address, null);
					initCheck serverActionWithdraw = new initCheck();
					new Thread(new Server(Constants.CLIENTS[idx].port, serverActionWithdraw)).start();
					if(serverActionWithdraw.getreplyNum() > 2){
						atm.withdraw(Integer.parseInt(op[1]));
						System.out.println("withdraw " + op[1] + "; current balance " + atm.getBalance());
					}
					else
						System.out.println("withdraw FAILURE: cannot get majority reply.");
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
	
	private static class initCheck implements ServerAction{
		private int replyNum = 0;
		public int getreplyNum(){
			return replyNum;
		}
		@Override
		public void onRecv(String data, DataOutputStream replyStream) {
			// TODO Auto-generated method stub
			try {
				replyStream.writeUTF("Yes, I am alive!");
				replyNum++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
