import java.util.Random;
import java.util.Scanner;
//Kevin Reyes
//i) each producer has its own queue, but the consumers can consume from either queue;

public class PPC {	//Solution 1
	private static int bffrsize;
	private final int numPro = 2;
	private final int numCon = 2;
	private static int maxZPro;
	private static int maxZCon;
	private static int numMssgs;
	static long cycle1NS;
	static long startNS;
	static long endNS;
	static long cycle1MS;
	static long startMS;
	static long endMS;
	public PPC(int bffrsize, int maxZPro, int maxZCon, int numMssgs){
		this.bffrsize = bffrsize;
		this.maxZPro = maxZPro;
		this.maxZCon = maxZCon;
		this.numMssgs = numMssgs;
	}

   public static void main(String[] args) {     
		System.out.println("Solution 1");
	   System.out.println("IN () SEPARATED BY COMMAS(,) WRITE 4 INTEGERS");
		System.out.println("IN THIS FORMAT(BUFFER SIZE,");
	   	System.out.println("MAX SLEEP FOR PRODUCER THREADS, MAX SLEEP FOR CONSUMER THREADS");
	   	System.out.println("NUMBER OF MESSAGES PER PRODUCER THREAD)");
	   	Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		scanner.close();
		String s = input.substring(input.lastIndexOf("(")+1, input.lastIndexOf(")"));
		String a[] = s.split(",");
		bffrsize = Integer.parseInt(a[0]);
		maxZPro = Integer.parseInt(a[1]);
		maxZCon = Integer.parseInt(a[2]);
		numMssgs = Integer.parseInt(a[3]);
		PPC ppc = new PPC(bffrsize,maxZPro,maxZCon,numMssgs);
		   System.out.println("Cycle 1");
		  
		ppc.runSimulation1();
}



public void runSimulation1(){
	QManager q = new QManager(bffrsize);

	   for(int i = 0; i < numPro;i++){		//Create Producer Threads
		   Producer p = new Producer(q, "P"+(i+1), numMssgs);
		   p.start();
	   }
	   
	for(int indx = 0; indx < numCon;indx++){	//Create Consumer Threads
		Consumer c = new Consumer(q, "C"+(indx+1), numMssgs);
		c.start(); 
	   }
	
	  
}
  
   
   class QManager{			
      
	  private String Q1[];	//Queue for P1
	  private String Q2[];	//Queue for P2

	  private int bffrsize;	//Buffer size for all buffers
       
      private int front = 0;	//Q1 front
      private int front2 = 0;	//Q2 front

      private int currentSize = 0;	//Q1 current size
      private int currentSize2 = 0;	//Q2 current size
      String data = null;
      int cyclecounter;
      int startcounter; 
      public QManager(int bffrsize) {	//Takes on Buffer's buffersize
         this.bffrsize = bffrsize;
         Q1 = new String[bffrsize];
         Q2 = new String[bffrsize];

      }
       
      private  int rand_int(int max){					//sleep cycle time
		
		Random rand = new Random();
		int rand_int = rand.nextInt(max);

		return rand_int;
      }
	

      public synchronized void produceItem(String data, String producerName){		//Produce Item for P1
    	  if(startcounter == 0){	//get official start time when first producer thread first runs
    		  startNS = System.nanoTime();
    		 startMS = System.currentTimeMillis();
    		 startcounter++;    		  
    	  }
    	while(currentSize == bffrsize){
            
        	 try {
               wait();
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         	}
             Q1[(front+currentSize)%bffrsize] = data;
             System.out.println(producerName+" produced message "+data+".   The Q1 has "+(currentSize+1)+" elements.");
             currentSize++;
        
         notifyAll();
         try {
			Thread.sleep(rand_int(maxZPro));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      
      public synchronized void produceItem2(String data, String producerName){		//Produce Item for P2
    	  if(startcounter == 0){
    		  startNS = System.nanoTime();
    		 startMS = System.currentTimeMillis();
    		 startcounter++;    		  
    	  }
    	 
         while(currentSize2 == bffrsize){
            try {
               wait();
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         	}
       
             Q2[(front2+currentSize2)%bffrsize] = data;
             System.out.println(producerName+" produced message "+data+".   The Q2 has "+(currentSize2+1)+" elements.");
             currentSize2++;
             notifyAll();
         try {
			Thread.sleep(rand_int(maxZPro));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
       
     
      public synchronized String consumeItem(String consumerName){				//Consume Item
    	  while((currentSize == 0)&&(currentSize2 == 0)){
            try {
               wait();
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
         
    	  if(currentSize > currentSize2){	//choose longest buffer
    		  String data = Q1[front];
    	         front = (front+1)%bffrsize;
    	         currentSize--;
    	         System.out.println(consumerName+" got message "+data +".  The Q1 has "+currentSize+" elements.");
    	  }
          else{
        	  String data = Q2[front2];
              front2 = (front2+1)%bffrsize;
              currentSize2--;
              System.out.println(consumerName+" got message "+data +".  The Q2 has "+currentSize2+" elements.");

          }
    	  cyclecounter++;
    	  if(cyclecounter == (numMssgs*2)){
    		  	endNS = System.nanoTime();
    			endMS = System.currentTimeMillis();
    			cycle1NS = endNS- startNS;
    		    cycle1MS = endMS - startMS;
    		    System.out.println("This task took: "+cycle1MS+" milliseconds ("+cycle1NS+" nanoseconds).");
    		    System.out.println("End of Cycle 1");
    	  }
    	  notifyAll();
         try {
			Thread.sleep(rand_int(maxZCon));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
      }

   }
    
  
   class Producer extends Thread{	//producer thread class
      private QManager q;
	private int Thcount;
      public Producer(QManager q, String threadName, int numMssgs){
         this.q = q;
         setName(threadName);
              }
       
      @Override
      public void run() {
    	  while(true && Thcount<numMssgs){	//Limits each Producer Thread to its numMssgs
    		  Random r = new Random();
    		  int Low = 33;
    		  int High = 64;
    		  int result = r.nextInt(High-Low) + Low;//random character
    		 String str = String.valueOf((char)result);
    		 String data = str+str+str; 			//string of such character
    		 if(getName().equals("P1")){		//choose the correct producer method	
    			 q.produceItem(data, getName());	
                 Thcount++;
            }else{
            	q.produceItem2(data, getName());
                Thcount++;
            }
    	  }
      
      }
   }
    
   class Consumer extends Thread{	//Consumer thread class
       
      private QManager q;
      
      
      public Consumer(QManager q, String threadName, int numMssgs){
         this.q = q;
         setName(threadName);
      }
      
      @Override
      public void run() {
    	  while(true){
                q.consumeItem(getName());               

    	  }
      }
   }

}
