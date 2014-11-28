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
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					replyStream.writeUTF("reply message");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		})).start();
		Client client = new Client();
		client.send(1004, "send message", "127.0.0.1", new ClientAction() {

			@Override
			public void onTimeOut() {
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
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test_server_client();
	}

}
