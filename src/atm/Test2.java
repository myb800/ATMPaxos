package atm;

public class Test2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Node[] clients = new Node[1];
		clients[0] = new Node("127.0.0.1", 1005);
		new Server(1005, new PaxosClientAll(clients)).run();
	}

}
