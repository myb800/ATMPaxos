package atm;

import java.util.Scanner;

public class Node {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + port;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	String address;
	int port;
	long id;
	static Long toNum(String ip, int port){
		Scanner sc = new Scanner(ip).useDelimiter("\\.");
		return	(sc.nextLong() << 40) + 
				(sc.nextLong() << 32) +
				(sc.nextLong() << 24) +
				(sc.nextLong() << 16) + 
				port;
	}
	Node(String addr,int port){
		this.address = addr;
		this.port = port;
		this.id = toNum(addr, port);
	}
	
}
