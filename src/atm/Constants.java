package atm;

public class Constants {
	public static final Node[] clients = new Node[]{
		new Node("127.0.0.1", 2001),
		new Node("127.0.0.1", 2002),
		new Node("127.0.0.1", 2003)
	};
	public static final Node self = new Node("127.0.0.1", 2001);
}
