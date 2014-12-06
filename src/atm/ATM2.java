package atm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ATM2 {
	private HashMap<String, Integer> depositRec;
	private ArrayList<Integer> withdrawRec;
	private PaxosClientAll paxosClientAll;
	private Node[] clients;
	private int processId;
	private int paxosPort;
	private Lock recdLock;
	private Server recoveryServer,paxosServer;
	public ATM2(int port,int processId,int recoverPort,Node[] clients){
		depositRec = new HashMap<String,Integer>();
		withdrawRec = new ArrayList<Integer>();
		paxosClientAll = new PaxosClientAll(clients);
		recdLock = new ReentrantLock();
		
		this.paxosPort = port;
		this.clients = clients;
		this.processId = processId;
		
		paxosClientAll.setOnDecide(new PaxosOnDecide() {
			
			@Override
			public void onDecide(String varName, String paxosId, String val) {
				// TODO Auto-generated method stub
				int idx = Integer.parseInt(varName);
				for(int i = withdrawRec.size();i <= idx;i++){
					withdrawRec.add(0);
				}
				withdrawRec.set(idx, Integer.parseInt(val));
			}
		});
		recoveryServer = new Server(recoverPort, new RecoverAction());
		paxosServer = new Server(port, paxosClientAll);
		new Thread(recoveryServer).start();
		new Thread(paxosServer).start();
		update();
	}
	public int getBalance(){
		int balance = 0;
		for(String i : depositRec.keySet()){
			balance += depositRec.get(i);
		}
		for(int i : withdrawRec){
			balance -= i;
		}
		return balance;
	}
	public boolean deposit(int m){
		depositRec.put(processId + "-" + depositRec.size(), m);
		update();
		return true;
	}
	public boolean withdraw(int m){
		update();
		PaxosLeader newPaxos = null;
		System.out.println(withdrawRec.size());
		while(newPaxos == null || newPaxos.isMyProposedPermitted() == false){
			if(getBalance() < m){
				return false;
			}
			int idx =withdrawRec.size();
			newPaxos = new PaxosLeader(clients, processId + ":" + paxosPort + "-" + withdrawRec.size(), Integer.toString(withdrawRec.size()));
			newPaxos.runPaxos(Integer.toString(m), new Ballot(0, processId));
			while(idx >= withdrawRec.size()){
				withdrawRec.add(0);
			}
			withdrawRec.set(idx, Integer.parseInt(newPaxos.getDecidedVal()));
		}
		
		
		return true;
	}
	public void fail(){
		paxosServer.stop();
		recoveryServer.stop();
	}
	private String serializemap(HashMap<String,Integer> map){
		StringBuffer sb = new StringBuffer();
		for(String i : map.keySet()){
			sb.append(i).append(" ").append(map.get(i)).append(";");
		}
		return sb.toString();
	}
	private HashMap<String, Integer> deserializemap(String str){
		String[] entries = str.split(";");
		HashMap<String, Integer> map = new HashMap<String,Integer>();
		for(String entry : entries){
			if(entry.length() == 0){
				continue;
			}
			String[] rec = entry.split(" ");
			map.put(rec[0], Integer.parseInt(rec[1]));
		}
		return map;
	}
	private void updateDeposit(String str){
		recdLock.lock();
		depositRec.putAll(deserializemap(str));
		recdLock.unlock();
	}
	private void updateWithdraw(String str){
		String[] recs = str.split(";");
		recdLock.lock();
		for(int i = 0;i < recs.length;i++){
			if(recs[i].length() == 0){
				continue;
			}
			if(withdrawRec.size() == i){
				withdrawRec.add(Integer.parseInt(recs[i]));
			} else {
				withdrawRec.set(i, Integer.parseInt(recs[i]));
			}
			paxosClientAll.addRecord(Integer.toString(i), recs[i], new Ballot(1, 1), new Ballot(1,1));
		}
		recdLock.unlock();
	}
	private String serializeRecord(){
		recdLock.lock();
		StringBuffer sB = new StringBuffer(serializemap(depositRec));
		sB.append("\n");
		for(int i = 0;i < withdrawRec.size();i++){
			sB.append(withdrawRec.get(i)).append(";");
		}
		recdLock.unlock();
		return sB.toString();
	}
	private void updateRecord(String str){
		String[] data = str.split("\n");
		if(data.length >= 1){
			updateDeposit(data[0]);
		}
		if(data.length >= 2){
			updateWithdraw(data[1]);
		}
	}
	private void update(){
		for(Node client : clients){
			Client.send(client.recoveryPort, serializeRecord(), client.address, new RecoverAction());
		}
	}
	private class RecoverAction implements ServerAction,ClientAction{

		@Override
		public void onRecv(String data, DataOutputStream replyStream) {
			System.out.println("old:" + serializeRecord());
			System.out.println("recv:" + data);
			updateRecord(data);
			System.out.println("new:" + serializeRecord());
			try {
				replyStream.writeUTF(serializeRecord());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onRecv(String data) {
			// TODO Auto-generated method stub
			updateRecord(data);
		}

		@Override
		public void onNotResponse() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
