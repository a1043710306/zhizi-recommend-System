package junit;

public class TestSys {
	
	private Object obj = new Object();
	
	private Object obj1 = new Object();
	
	
	public synchronized void A(){
		synchronized (obj) {
			
			obj1 = 1;
			System.out.println("A");
			try {
				Thread.sleep(1000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	public void B(){
		synchronized (obj1) {
			System.out.println("B");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) { 
		TestSys a =  new TestSys();
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				a.A();
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				a.B();
			}
		});
		
		t1.start();
		t2.start();
		
	}
}
