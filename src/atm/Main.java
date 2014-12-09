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
		Helper.checkLogFile();
		Constants.config(args[0]);
		int idx = Integer.parseInt(args[1]);
		new Thread(new Server(5555/*Constants.CLIENTS[idx].port*/, new ServerAction(){

			@Override
			public void onRecv(String data, DataOutputStream replyStream) {
				// TODO Auto-generated method stub
				try {
					replyStream.writeUTF("ok!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		})).start();
		int algorithm = Integer.parseInt(args[2]);
		AbstractATM atm;
		if(algorithm == 1){
			atm = new ATM2(Constants.CLIENTS[idx].port, idx, Constants.CLIENTS[idx].recoveryPort,Constants.CLIENTS);
		}
		else{
			atm = new ATM(Constants.CLIENTS[idx].port, idx, Constants.CLIENTS[idx].recoveryPort,Constants.CLIENTS);
		}
		while(true){
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String[] op;
			try {
				op = br.readLine().split(" ");
				if(op[0].equals("deposit")){
					initCheck serverActionDeposit = new initCheck();
					for(Node node:Constants.CLIENTS)
						Client.send(5555/*node.port*/, "Are you alive?", node.address, serverActionDeposit);
					System.out.println("current checkNum: " + serverActionDeposit.getreplyNum());
					if(serverActionDeposit.getreplyNum() > Constants.CLIENTS.length / 2){
						atm.deposit(Integer.parseInt(op[1]));
						System.out.println("deposit " + op[1] + "; current balance " + atm.getBalance());
					}
					else
						System.out.println("deposit FAILURE: cannot get majority reply.");
				}	
				else if(op[0].equals("withdraw")){
					initCheck serverActionWithdraw = new initCheck();
					for(Node node:Constants.CLIENTS)
						Client.send(5555/*node.port*/, "Are you alive?", node.address, serverActionWithdraw);
					System.out.println("current checkNum: " + serverActionWithdraw.getreplyNum());
					if(serverActionWithdraw.getreplyNum() > Constants.CLIENTS.length / 2){
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
					break;
				}
				else if(op[0].equals("recover")){
					atm = new ATM(Constants.CLIENTS[idx].port, idx, Constants.CLIENTS[idx].recoveryPort,Constants.CLIENTS);
				}
				else if(op[0].equals("print")){
					atm.print();
				}
				else
					System.out.println("error command!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*private static class initCheck implements ClientAction{
		private int replyNum = 0;
		public int getreplyNum(){
			return replyNum;
		}
		@Override
		public synchronized void onRecv(String data) {
			replyNum++;
		}
		@Override
		public void onNotResponse() {
			
		}
	}*/

}
