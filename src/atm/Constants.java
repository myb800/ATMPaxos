package atm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Constants {
	public static Node[] CLIENTS;
	public static void config(String path){
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			ArrayList<Node> clients = new ArrayList<Node>();
			String line;
			while((line = br.readLine()) != null){
				String[] tokens = line.split(",");
				clients.add(new Node(tokens[0], Integer.parseInt(tokens[1].trim()), Integer.parseInt(tokens[2].trim())));
			}
			br.close();
			CLIENTS = clients.toArray(new Node[clients.size()]);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			CLIENTS = new Node[]{
					new Node("127.0.0.1", 5011, 6011),
					new Node("127.0.0.1", 5012, 6012),
					new Node("127.0.0.1", 5013, 6013),
			};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
