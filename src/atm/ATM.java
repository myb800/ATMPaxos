package atm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import org.apache.commons.io.FileUtils;
import java.util.Scanner;

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
	
	
	public void backup(File log_file) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(log_file));
		String line;
		while((line = br.readLine()) != null)
			updateBalance(line);
		br.close();
	}
	
	public void respondBackup(File log_file) throws IOException{
		ServerAction sact = null;
        DataOutputStream outputStream = new DataOutputStream(new FileOutputStream("log.txt"));
        outputStream.writeUTF(readFile(log_file));
		sact.onRecv("BackupRequest", outputStream);
	}
	
	public String readFile(File file) throws IOException{
		StringBuilder sb = new StringBuilder((int)file.length());
		Scanner sc = new Scanner(file);
		String lineSeperator = System.getProperty("line.seperator");
		try{
			while(sc.hasNextLine()){
				sb.append(sc.nextLine() + lineSeperator);
			}
			return sb.toString();
		}finally{
			sc.close();
		}
	}
	private class BackupServerAction implements ServerAction{

		@Override
		public void onRecv(String data, DataOutputStream replyStream) {
						
		}
		
	}
}
