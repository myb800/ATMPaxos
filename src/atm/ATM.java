package atm;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
public class ATM {
	private int balance = 0;
	private ArrayList<String> operation;
	private ArrayList<Integer> values;
	private PaxosClientAll client;
	private int port;
	private int processId;
	public ATM(int port,int processId){
		this.port = port;
		this.processId = processId;
		operation = new ArrayList<String>();
		values = new ArrayList<Integer>();
		client = new PaxosClientAll(Constants.clients);
		client.setOnDecide(new PaxosOnDecide() {
			
			@Override
			public void onDecide(String varName, String paxosId, String val) {
				// varName is the slot idx
				writeLocalLog(val,Integer.parseInt(varName));
			}
		});
		new Thread(new Server(this.port,client)).start();
	}
	public int getBalance(){
		updateBalance();
		return balance;
	}
	public boolean withdraw(int m){
		backup();
		PaxosLeader newPaxos = null;
		while(newPaxos == null || newPaxos.isHasDecisionBefore()){
			if(getBalance() < m){
				return false;
			}
			int slot = operation.size();
			newPaxos = new PaxosLeader(Constants.clients, processId + ":" + port + "-" + operation.size(), Integer.toString(slot));
			newPaxos.runPaxos("W " + m, new Ballot(0, processId));
			writeLocalLog(newPaxos.getDecidedVal(), slot);
		}
		return true;
		
	}
	public boolean deposit(int m){
		PaxosLeader newPaxos = null;
		while(newPaxos == null || newPaxos.isHasDecisionBefore()){
			Log.log("Beginning a new round for deposit");
			int slot = operation.size();
			newPaxos = new PaxosLeader(Constants.clients, processId + ":" + port + "-" + operation.size(), Integer.toString(slot));
			newPaxos.runPaxos("D " + m, new Ballot(0, processId));
			writeLocalLog(newPaxos.getDecidedVal(), slot);
		}
		return true;
	}
	private synchronized void writeLocalLog(String log,int idx){
		String[] logTkens = log.split(" ");
		while(idx >= operation.size()){
			operation.add("");
			values.add(0);
		}
		operation.set(idx, logTkens[0].trim());
		values.set(idx, Integer.parseInt(logTkens[1].trim()));
	}
	private void updateBalance(){
		balance = 0;
		for(int i = 0;i < operation.size();i++){
			if(operation.get(i).equals("W")){
				balance -= values.get(i);
			} else {
				balance += values.get(i);
			}
		}
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
