/*
 * Provided By Instructor For: CS 4480 Computer Networks 
 * 							   Programming Assignment: 2
 * 							   Reliable Transport Protocol
 * Modified By: xrawlinson
 * Last Modified Date: 4/3/2016
 * Class: StudentNetworkSimulator
 * Modifications: 
 * 		1. added needed variables;
 * 		2. added needed functions: computeChecksum, sameChecksum, and displayStats;
 * 		3. filled the give functions.
 * Note: 
 * 		1. this one is modified for Part B - The Go-Back-N Version;
 * 		2. also added two lines of code in the class "NetworkSimulator" 
 * 		   to display statistics as the following: 
 * 		   		Line 45: protected abstract void displayStats();
 * 				Line 170: displayStats();
 */

public class StudentNetworkSimulator extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B
     *
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(int entity, String dataSent)
     *       Passes "dataSent" up to layer 5 from "entity" [A or B]
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */

	
/**************************************START OF NEEDED VARIABLES*****************************************************/
    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)

	/*****START OF NEWLY ADDED FOR PART B*****/
	
	//FROM THE BOOK ABOUT GBN:Sequence numbers in the interval [0,base-1]
	//correspond to packets that have already been transmitted and acknowledged. The interval
	//[base,nextseqnum-1] corresponds to packets that have been sent but not yet
	//acknowledged. Sequence numbers in the interval [nextseqnum,base+N-1] can
	//be used for packets that can be sent immediately, should data arrive from the upper
	//layer. Finally, sequence numbers greater than or equal to base+N cannot be used until
	//an unacknowledged packet currently in the pipeline (specifically, the packet with
	//sequence number base) has been acknowledged.
	
	//window size
	int windowsize;

	//base, which is the sequence number of the oldest unacknowledged packet
	int base; 
	
	//nextseqnum, the smallest unused sequence number that is the sequence number of the next packet to be sent
	int nextseqnum;
	
	//buffer to store packets to be sent, I set it to 2000 so will not ever need to worry about it
	Packet[] sndpkt = new Packet[2000];
	
	//expected sequence number, for B-Side
	int expectedseqnum;	
	
	/*****END OF NEWLY ADDED FOR PART B*****/
		
	//a packet for A-Side
	Packet apacket;
	
	//a packet for for B-Side
	Packet bpacket;

	//keeps track of everything for displaying statistics
	int appSentPackCount = 0;//packet sent by application layer
	int appReceivedPackCount = 0;//packet received by application layer
	int droppedPackCount = 0;//dropped when the window is full
	
	int protocolSentPackCount = 0;//packet sent by the transport protocol
	int protocolReceivedPackCount = 0;//packet received by the transport protocol
	
	int corruptedAckCount = 0;//corrupted ACK
	int corruptedPackCount = 0;//corrupted packet
	int lostPackCount = 0;//lost packet
	int restranPackCount = 0;//retransmitted packet
	
	int aSentPackCount = 0;//A-Side sent packet
	int receivedAckCount = 0;//received ACK	
	
	int bReceivedPackCount = 0;//B-Side received packet
	int bReceivedCurruptFromA = 0;//received packets from A, but corrupted
	int sentAckCount = 0;//sent ACK	
	int bResentAckCount = 0;//resent ACK 	
	
	//will be used to calculate average RTT
	long startTime; 
	long endTime;
	long TotalRTT = 0;
/**************************************END OF NEEDED VARIABLES*************************************************/
	
	
/*************************************START OF ADDED FUNCTIONS*************************************************/
	//computes checksum
	protected int computeChecksum(Packet p)
	{
		//adds sequence and hash value of the payload to be the checksum
		int checksum = p.getSeqnum() + p.getPayload().hashCode();

		////System.out.println("*****COMPUTED CHECK SUM: "+checksum);//used for debugging
		
		return checksum;
	}
	
	//verifies the checksum, returns true if the same, returns false otherwise
	protected boolean sameChecksum(Packet p)
	{
		boolean sameChecksum = true;
		//returns false if not the same, and increment packCorruptedCount by one to count how many packets are corrupted
		if(computeChecksum(p)!= p.getChecksum())
		{
			//corruptedPackCount++;
			return false;
		}
    	
		return sameChecksum;
	}//end of sameChecksum
    
    //displays the statistics
    protected void displayStats()
    {
    	//since the simulator will only send number-1 messages, deducts one from the following
    	appSentPackCount = appSentPackCount - 1;
    	protocolReceivedPackCount = protocolReceivedPackCount - 1;
    	protocolSentPackCount = protocolSentPackCount -1;
    	aSentPackCount = aSentPackCount-1;
    	
    	//total corrupted packets including both corrupted packets sent by A and corrupted ACKs sent by B
    	corruptedPackCount = corruptedAckCount+bReceivedCurruptFromA;
    	
    	//total packets sent by both a and b (not include retransmission)
    	int totalSent = protocolSentPackCount + sentAckCount;
    	
    	//total packets resent by both a and b
    	int totalResent = restranPackCount+bResentAckCount;

    	//packet lost is calculated from all packets sent minus all packets received and all packets corrupted
    	lostPackCount = totalSent + totalResent - receivedAckCount - corruptedAckCount - bReceivedPackCount;	
    	
    	//calculates the average RTT, convert nanosecond to second
    	double avgRTT = TotalRTT/receivedAckCount/1000000000.0;
    	
    	//display the details
    	System.out.println("\n\n******STATISTICS*****");
    	
    	//for application layer
    	System.out.println("\n*****Application Layer Statistics*****");
    	System.out.println("Application Layer Sent Packets Amount: " + appSentPackCount);
    	System.out.println("Application Layer Received Packets Amount: " + appReceivedPackCount);
    	System.out.println("Dropped Packets (Packet Was Dropped When There Was Already A Message In Transit): " + droppedPackCount);
    	System.out.println("Note: Excluding The Last One That Will Never Send "
    			+ "(eg. If 10 is entered for the number of messages, 9 will be sent and received.)");
    	//for transport protocol
    	System.out.println("\n*****Transport Protocol Statistics*****");  	
    	System.out.println("Transport Protocol Received (From Application Layer) Packets Amount: " + protocolReceivedPackCount);
    	System.out.println("Transport Protocol Sent (To Application Layer) Packets Amount: " + protocolSentPackCount);
    	//for A-Side
    	System.out.println("\n*****A-Side Statistics*****");
    	System.out.println("Sent Packets Amount (Doesn't Include Retransmission): " + aSentPackCount);
    	System.out.println("Retransmitted Amount: " +(restranPackCount));
    	System.out.println("Received ACK Amount(Not Corrupted): " + receivedAckCount);
    	System.out.println("Received Corrupted ACK Amount: " + corruptedAckCount);
    	//for B-Side
    	System.out.println("\n*****B-Side Statistics*****");
    	System.out.println("Received Packets Amount(Possible Corrupted): " + bReceivedPackCount);
    	System.out.println("Corrupted Amount of The Received Packets: " + bReceivedCurruptFromA);
    	System.out.println("Sent ACK Amount (Doesn't Include Retransmission): " + sentAckCount);
    	System.out.println("Retransmitted ACK Amount (Because ACKs Lost/Corrupted From B-Side to A-Side): " + bResentAckCount); 
    	//total
    	System.out.println("\n*****Total Statistics*****");
    	System.out.println("Total Packets Sent By A-Side And ACKs Sent By B-Side (Doesn't Include Retransmission): "+ totalSent);
    	System.out.println("Total Retransmitted Packets Amount Caused By Loss/Corruption of Both A and B Sides: " + totalResent);
    	System.out.println("Total Corrupted Amount: " + corruptedPackCount);
    	System.out.println("Total Lost Amount: " + lostPackCount);   	
    	//average RTT & lost and corrupted percentage
    	System.out.println("\n*****Average RTT & Lost And Corrupted Percentage*****");
    	System.out.println("Average RTT In Seconds: " + avgRTT);   	
    	System.out.println("Corrupted Percentage: "+(corruptedPackCount*1.0/(totalSent+restranPackCount)*100)+"%");
    	System.out.println("Lost Percentage: "+(lostPackCount*1.0/(totalSent+totalResent)*100)+"%");
    }//end of displayStats
/**********************************END OF ADDED FUNCTIONS********************************************************/
    
    
/****************************START OF GIVEN FUNCTIONS THAT I FILLED UP*******************************************/
    
    // This is the constructor.  Don't touch!
    public StudentNetworkSimulator(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   long seed)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
    }

    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
    	//increments appSentPackCount by one every time A-Side receives a packet from layer 5
    	appSentPackCount++;
    	
		////System.out.println("NextSeqNumber: "+ nextseqnum);//used for debugging
		////System.out.println("base: "+ base);//used for debugging
		////System.out.println("windowsize: "+ windowsize);//used for debugging  		
    	////System.out.println("base+windowsize: "+(base+windowsize));//used for debugging

    	//if the window is not full, it accepts more messages
		if(nextseqnum<base+windowsize)
		{
			//increment protocolReceivedPackCount by one to count the amount received by the protocol
			protocolReceivedPackCount++;
			   		
			//fills up apacket (need three out of the four parameters in the constructor: public Packet(int seq, int ack, int check, String newPayload))
    		//sequence number:
			apacket.setSeqnum(nextseqnum);
    		System.out.println("*****APACKET SEQ NUMBER: " + apacket.getSeqnum());//used for debugging
    		//payload:
    		apacket.setPayload(message.getData());
    		System.out.println("*****Received Payload From The Application Layer: " + apacket.getPayload());//used for debugging
    		//checksum:
    		apacket.setChecksum(computeChecksum(apacket));   		
    		System.out.println("*****APACKET CHECK SUM: " + apacket.getChecksum());//used for debugging
    		
    		//adds the newly made packet to the send packet buffer
    		sndpkt[nextseqnum]=apacket;   		
    		
    		//reset apacket so it can be used again
    		apacket = new Packet(0,0,0,"");
    		
    		//used for debugging, checks if sndpkt saving the packets properly
        	/*for(int i=1; i<sndpkt.length;i++)
        	{
        		if(sndpkt[i]!= null)
        			System.out.println("sndpkt["+i+"]: "+sndpkt[i]);
        	}*/
        	
        	//gets the start time of sending a packet, will be used to calculate average RTT
        	startTime = System.nanoTime();
        	////System.out.println("&&&&& Start Time: " + startTime);//used for debugging
        	
        	System.out.println("*****Sending Packets To The Network*****");//used for debugging
        	//sends apacket to the network, and increments protocolSentPackCount and aSentPackCount by one
        	toLayer3(0,sndpkt[nextseqnum]);        		
        	aSentPackCount++;
        	protocolSentPackCount++;
            
        	//if the base equals to the nextseqnum, start the timer
			if(base==nextseqnum)
			{  		
        		System.out.println("*****Timer Started*****");//used for debugging
        		//starts the timer 
            	startTimer(0,20);               	      
			}

			//increments nextseqnum by one
			nextseqnum++;
		}
		//the window is full, refuse more data
		else
    	{
    		System.out.println("*****Refuse The Data Since The Window Is Full*****");
    		toLayer5(0, message.getData());
    		System.out.println("Returning the data: "+message.getData()+" back to the requested application");
    		
    		//increment droppedPackCount by one, this is to count how many packets were refused
    		droppedPackCount++;   
    	}
    }//end of aOutput
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet)
    {
    	System.out.println("Received ACK packet: "+packet);//used for debugging
		
    	//receives the non-corrupted ACK that is awaiting for, stops the timer,  
    	//increments receivedAckCount by one 	
    	if(sameChecksum(packet))
    	{
    		base = packet.getAcknum()+1;
    		////System.out.println("BASE: "+ base);//used for debugging
    		
    		System.out.println("*****Stop The Timer*****");//used for debugging
    		stopTimer(0);
    		
    		//gets the end time of receiving the ACK, will be used to calculate average RTT
    		endTime = System.nanoTime();    	
    		////System.out.println("&&&&& End Time: " + endTime);//used for debugging
    		
    		//gets the duration from sending a packet to receiving the ACK
    		long duration = (endTime - startTime);
    		////System.out.println("&&&&& Duration: " + duration);//used for debugging
    		
    		//gets to total RTT, will be used to calculate average RTT
    		TotalRTT += duration;
    		////System.out.println("&&&&& Total RTT: " + TotalRTT);//used for debugging
    		
    		//counts received ACKs
        	receivedAckCount++;
    		
            //if sent packets have not yet been all ACK'd, restart the timer
    		if(base!=nextseqnum)		
    		{
    			System.out.println("*****Timer Restarted Since Previously Sent Packets Have Not All Been Ack'd*****");//used for debugging
    			startTimer(0,20);
    		}
    	}
    	//increments corruptedAckCount by one to count corrupted ACKs
    	else
    		corruptedAckCount++;
    }//end of aInput
    
    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
    	System.out.println("*****Timer Expired*****");//used for debugging
    	System.out.println("*****Timer Retarted*****");//used for debugging
    	
		//starts the timer 
    	startTimer(0,20);
		
    	//resends everything that has not been ACK'd
    	for(int i = base; i<= nextseqnum-1; i++)
    	{
    		System.out.println("*****Resending Packets To The Network*****");//used for debugging
        	System.out.println("Resending: "+sndpkt[i]);//used for debugging
        	//resends the packet
        	toLayer3(0,sndpkt[i]);
        	//increments restranPackCount by one every time A-Side resends the packet
        	restranPackCount++; 
    	}	 	
    }//end of aTimerInterrupt
    
    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {
    	//initializes an empty packet for A-side, everything(seq, ack, checksum) starts at 0, newPayload starts at ""
    	apacket = new Packet(0,0,0,"");
  
    	//initializes the window size to 8
    	windowsize = 8;
    	
    	//initializes the base to 1
    	base = 1;
    	
    	//initializes the nextseqnum to 1
    	nextseqnum = 1;
    }//end of aInit
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
    	//increment bReceivedPackCount by one,
    	//this is for counting how many packets received from A-Side (possibly corrupted)
    	bReceivedPackCount++;
    	
    	System.out.println("*****AWAITING FOR SEQUENCE NUMBER: " + expectedseqnum);//used for debugging
    	System.out.println("*****GOT SEQUENCE NUMBER: " + packet.getSeqnum());//used for debugging
		System.out.println("*****AWAITING FOR CHECKSUM: " + packet.getChecksum());//used for debugging
		System.out.println("*****GOT CHECKSUM: " + computeChecksum(packet));//used for debugging
  	
    	//only takes actions when receives the non-corrupted and corrected seqnum packet;
    	//otherwise, sends the previous ACK packet back to A-Side
    	if(sameChecksum(packet) && packet.getSeqnum() == expectedseqnum)
    	{
    		//if it is the correct packet that is waiting for, sends the packet to the layer 5, 
    		//increments appReceivedPackCount by one, sets up the ACK packet that to be sent back to A-Side, 
    		//increments sentAckCount by one, and increment expectedseqnum by one
    		
			System.out.println("*****Sending The Payload To The Application Layer: " + packet.getPayload());//used for debugging
			toLayer5(1, packet.getPayload());
			appReceivedPackCount++;
		
			//make the ACK packet, sets acknum to the expectedseqnum, 
			//payload to "ACK", and checksum is the packet's checksum
			//seqnum is not needed here per the book so I leave it as how it came in
			packet.setAcknum(expectedseqnum);
			packet.setPayload("ACK");
			packet.setChecksum(computeChecksum(packet));
		  	
			//put the packet into bpacket, which is the new ACK packet that will be sent back to A-Side
			bpacket = packet;			
			System.out.println("*****Sending ACK Back To A-Side***** " + bpacket);
			toLayer3(1, bpacket);		    			
			sentAckCount++;
			
			//increment expectedseqnum by one for the next round
			expectedseqnum++;
    	}
    	//corrupted or wrong seqnum packet is received,  sends the previous ACK packet back to A-Side
    	else
    	{
    		System.out.println("*****Resending The Previous ACK Back to A-Side: "+bpacket);//used for debugging
			toLayer3(1, bpacket);
			
			//increments bReceivedCurruptFromA to count corrupted packets
			if(!sameChecksum(packet))
				bReceivedCurruptFromA++;

			//increments bResentAckCount by one, this counts re-sending of ACKs
			bResentAckCount++;
    	}
    }//end of bInput
    
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {
    	//initializes expectedseqnum to 1
    	expectedseqnum = 1;
    	
    	//initializes an empty packet for B-side, everything(seq, ack, checksum) starts at 0, and newPayload as ""
    	bpacket = new Packet(0,0,0,"GOT NOTHING YET"); 
    }//end of bInit
/****************************END OF GIVEN FUNCTIONS THAT I FILLED UP*******************************************/
}//end of class