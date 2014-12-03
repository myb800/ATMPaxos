package atm;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
public class ATM {
	private int balance = 0;
	private int currentTransId = 0;
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
	public int getBalance(int port,int processId){
		return balance;
	}
	private void addRecord(String op,int value,int index){
		if(index == operation.size()){
			operation.add(op);
			values.add(value);
		}
	}
	public boolean withdraw(int m){
		PaxosLeader newPaxos = null;
		while(newPaxos == null || newPaxos.isHasDecisionBefore()){
			if(balance < m){
				return false;
			}
			newPaxos = new PaxosLeader(Constants.clients, processId + ":" + port + "-" + System.currentTimeMillis(), "log-slot-" + operation.size());
			newPaxos.runPaxos("W " + m, new Ballot(0, processId));
			updateBalance(newPaxos.getDecidedVal());
		}
		return true;
		
	}
	public boolean deposit(int m){
		PaxosLeader newPaxos = null;
		while(newPaxos == null || newPaxos.isHasDecisionBefore()){
			newPaxos = new PaxosLeader(Constants.clients, processId + ":" + port + "-" + System.currentTimeMillis(), "log-slot-" + operation.size());
			newPaxos.runPaxos("W " + m, new Ballot(0, processId));
			updateBalance(newPaxos.getDecidedVal());
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