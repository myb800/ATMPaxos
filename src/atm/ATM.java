package atm;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ATM {
	private int balance = 0;
	private ArrayList<String> operation;
	private ArrayList<Integer> values;
	private PaxosClientAll client;
	private int port;
	private int recoveryPort;
	private int processId;
	private Lock logLock = new ReentrantLock();
	private Node[] clients;
	public ATM(int port,int processId, int recoverPort,Node[] clients){
		this.port = port;
		this.processId = processId;
		this.recoveryPort = recoverPort;
		this.clients = clients;
		
		operation = new ArrayList<String>();
		values = new ArrayList<Integer>();
		client = new PaxosClientAll(clients);
		client.setOnDecide(new PaxosOnDecide() {
			
			@Override
			public void onDecide(String varName, String paxosId, String val) {
				// varName is the slot idx
				writeLocalLog(val,Integer.parseInt(varName));
			}
		});
		respondBackup();
		new Thread(new Server(this.port,client)).start();
	}
	public int getBalance(){
		updateBalance();
		System.out.println(operation.size());
		return balance;
	}
	public boolean withdraw(int m){
		try {
			recover();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PaxosLeader newPaxos = null;
		while(newPaxos == null || newPaxos.isMyProposedPermitted() == false){
			if(getBalance() < m){
				return false;
			}
			int slot = operation.size();
			newPaxos = new PaxosLeader(clients, processId + ":" + port + "-" + operation.size(), Integer.toString(slot));
			newPaxos.runPaxos("W " + m, new Ballot(0, processId));
			writeLocalLog(newPaxos.getDecidedVal(), slot);
		}
		return true;
		
	}
	public boolean deposit(int m){
		PaxosLeader newPaxos = null;
		while(newPaxos == null || newPaxos.isMyProposedPermitted() == false){
			Log.log("Beginning a new round for deposit");
			int slot = operation.size();
			newPaxos = new PaxosLeader(clients, processId + ":" + port + "-" + operation.size() + System.currentTimeMillis(), Integer.toString(slot));
			newPaxos.runPaxos("D " + m, new Ballot(0, processId));
			writeLocalLog(newPaxos.getDecidedVal(), slot);
		}
		return true;
	}
	private void writeLocalLog(String log,int idx){
		logLock.lock();
		String[] logTkens = log.split(" ");
		while(idx >= operation.size()){
			operation.add("");
			values.add(0);
		}
		operation.set(idx, logTkens[0].trim());
		values.set(idx, Integer.parseInt(logTkens[1].trim()));
		logLock.unlock();
	}
	private void updateBalance(){
		logLock.lock();
		balance = 0;
		for(int i = 0;i < operation.size();i++){
			if(operation.get(i).equals("W")){
				balance -= values.get(i);
			} else {
				balance += values.get(i);
			}
		}
		logLock.unlock();
	}
	public void updateBalance(String log){
		logLock.lock();
		String[] op = log.split(" ");
		operation.add(op[0]);
		values.add(Integer.parseInt(op[1].trim()));
		updateBalance();
		logLock.unlock();
	}
	public void writelog(String action, double value){
		logLock.lock();
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
		logLock.unlock();
	}
	
	
	public void recover() throws IOException{
		for(Node node : clients){
			Client.send(node.recoveryPort, "", node.address, new ClientAction() {
				
				@Override
				public void onRecv(String data) {
					// TODO Auto-generated method stub
					String[] logs = data.split("\n");
					for(int i = 0;i < logs.length;i++){
						writeLocalLog(logs[i],i);
					}
				}
				
				@Override
				public void onNotResponse() {
					// TODO Auto-generated method stub
					
				}
			});
		}
		
	}
	
	public void respondBackup(){
		new Thread(new Server(recoveryPort, new BackupServerAction())).start();
	}
	
	private class BackupServerAction implements ServerAction{

		@Override
		public void onRecv(String data, DataOutputStream replyStream) {
			logLock.lock();
			StringBuffer log = new StringBuffer();
			int i = 0;
			for(;i < operation.size() - 1;i++){
				log.append(operation.get(i))
				   .append(" ")
				   .append(values.get(i))
				   .append("\n");
			}
			if(operation.size() > 0){
				log.append(operation.get(i))
				   .append(" ")
				   .append(values.get(i));
			}
			try {
				replyStream.writeUTF(log.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logLock.unlock();
		}
		
	}
}
