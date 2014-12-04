package atm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
		recover();
		new Thread(new Server(this.port,client)).start();
	}
	public int getBalance(){
		updateBalance();
		return balance;
	}
	public boolean withdraw(int m){
		recover();
		PaxosLeader newPaxos = null;
		while(newPaxos == null || newPaxos.isMyProposedPermitted() == false){
			if(getBalance() < m){
				return false;
			}
			int slot = operation.size();
			newPaxos = new PaxosLeader(clients, processId + ":" + port + "-" + operation.size() + System.currentTimeMillis(), Integer.toString(slot));
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
	public void readLog(int idx) throws IOException{
		logLock.lock();
		FileInputStream fs = new FileInputStream("onRecvlog.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fs));
		for(int i = 0; i < idx - 1; i++)
			br.readLine();
		String line = br.readLine();
		writeLocalLog(line, idx);
		br.close();
		logLock.lock();
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
	
	
	public void recover(){
		for(Node node : clients){
			Client.send(node.recoveryPort, "", node.address, new ClientAction() {
				
				@Override
				public void onRecv(String data) {
					// TODO Auto-generated method stub
					String[] logs = data.split("\n");
					for(int i = 0;i < logs.length;i++){
						if(logs[i].equals("")){
							continue;
						}
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
			for(;i < operation.size();i++){
				log.append(operation.get(i))
				   .append(" ")
				   .append(values.get(i))
				   .append("\n");
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
