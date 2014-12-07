package atm;

public abstract class AbstractATM {
	public abstract boolean withdraw(int m);
	public abstract boolean deposit(int m);
	public abstract void print();
	public abstract int getBalance();
	public abstract void fail();
}
