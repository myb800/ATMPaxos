package atm;

import java.io.DataOutputStream;
import java.io.IOException;

public class Test {
	private static void test_server_client(){
		new Thread(new Server(1004, new ServerAction() {

			@Override
			public void onRecv(String data, DataOutputStream replyStream) {
				// TODO Auto-generated method stub
				System.out.println(data);
			}
		})).start();
		Client.send(1004, "send message", "127.0.0.1", new ClientAction() {

			@Override
			public void onNotResponse() {
				// TODO Auto-generated method stub
				System.out.println("Time out");
			}

			@Override
			public void onRecv(String data) {
				// TODO Auto-generated method stub
				System.out.println(data);
			}
		});

	}
	private static void test_paxos(){
		Node[] clients = new Node[2];
		clients[0] = new Node("127.0.0.1", 1006);
		clients[1] = new Node("127.0.0.1", 1007);
		new Thread(new Server(1006, new PaxosClientAll(clients))).start();
		new Thread(new Server(1007, new PaxosClientAll(clients))).start();
		
		PaxosLeader pl = new PaxosLeader(clients, "3","v");
		pl.runPaxos("13", new Ballot(1, 1));
	}
	private static void test_ATM(){
		ATM atm1 = new ATM(2001, 1);
		ATM atm2 = new ATM(2002, 2);
		
		atm1.deposit(100);
		System.out.println(atm2.getBalance());
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test_ATM();
	}

}
