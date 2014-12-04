package atm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Test2 {
	static Thread t;
	private static void test(){
		t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(!Thread.currentThread().isInterrupted()){
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					try {
						System.out.println(br.readLine());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}
	
	public static void main(String[] args) throws InterruptedException, UnknownHostException, IOException {
		// TODO Auto-generated method stub
		test();
		t.interrupt();
		for (int i = 0; i < 10; i++) {
		    Thread t = new Thread(new Runnable() {
		        @Override
		        public void run() {
		            try {
		                final ServerSocket ss = new ServerSocket();
		                ss.setReuseAddress(true);
		                ss.bind(new InetSocketAddress(12345));
		                Socket s = ss.accept();
		                System.out.println((char) s.getInputStream().read());
		                ss.close();
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		        }
		    });
		    t.start();
		    Thread.sleep(50);
		    Socket s = new Socket("localhost", 12345);
		    s.getOutputStream().write('c');
		    t.join();
		}
	}

}
