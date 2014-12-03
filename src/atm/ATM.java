package atm;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class ATM {
	private int balance = 0;
	private List<String> operation;
	private List<Integer> values;
	private PaxosClientAll client;
	private int port;
	private int processId;
	public ATM(int port,int processId){
		this.port = port;
		this.processId = processId;
		operation = new ArrayList<String>();
		values = new ArrayList<Integer>();
		client = new PaxosClientAll(Constants.clients);
		new Thread(new Server(this.port,client)).start();
	}
	public int getBalance(){
		return balance;
	}
	public boolean withdraw(int m){
		backup();
		PaxosLeader newPaxos = null;
		while(newPaxos == null || newPaxos.isHasDecisionBefore()){
			if(balance < m){
				return false;
			}
			newPaxos = new PaxosLeader(Constants.clients, processId + ":" + port + "-" + operation.size(), "log-slot-" + operation.size());
			newPaxos.runPaxos("W " + m, new Ballot(0, processId));
			updateBalance(newPaxos.getDecidedVal());
		}
		return true;
		
	}
	public boolean deposit(int m){
		PaxosLeader newPaxos = null;
		while(newPaxos == null || newPaxos.isHasDecisionBefore()){
			Log.log("Beginning a new round for deposit");
			newPaxos = new PaxosLeader(Constants.clients, processId + ":" + port + "-" + operation.size(), "log-slot-" + operation.size());
			newPaxos.runPaxos("D " + m, new Ballot(0, processId));
			updateBalance(newPaxos.getDecidedVal());
			System.out.println(newPaxos.isHasDecisionBefore());
			break;
		}
		return true;
	}
	
	public void updateBalance(String log){
		String[] op = log.split(" ");
		operation.add(op[0]);
		values.add(Integer.parseInt(op[1].trim()));
		if(op[0].equals("W")){
			balance -= Integer.parseInt(op[1].trim());
		} else {
			balance += Integer.parseInt(op[1].trim());
		}
	}
	public void writelog(String action, double value){
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter("log.txt"));
			if(action.equals("deposit")){
				out.write("deposit " + value);
				out.newLine();
				out.close();
			}
			else if(action.equals("withdraw")){
				out.write("withdraw " + value);
				out.newLine();
				out.close();
			}
			else
				System.out.println("error: write log fail");
		}
		catch (IOException e){
			System.out.println("Exception");
		}
	}
	
	
	public void backup(){
		
	}
	public void respondBackup(){
		
	}
	private class BackupServerAction implements ServerAction{

		@Override
		public void onRecv(String data, DataOutputStream replyStream) {
						
		}
		
	}
}
