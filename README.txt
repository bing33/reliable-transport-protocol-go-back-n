/*
 * README file For: CS 4480 Computer Networks 
 * 	            Programming Assignment: 2
 *                  Reliable Transport Protocol
 * By: Xiaobing Rawlinson
 * Last Modified Date: 4/3/2016
 */


README:
1. I did Java version. To run the program, please import the source file into Eclipse via the import function.

2. To test it:
a. 1000 messages, 0.1 for both loss/corruption, and 1000 for average time between messages — this would show the correct statistics;
b. 50 messages, 0.05 for both loss/corruption, and 10 for average time between messages - this would show the correct behavior for recovering from loss/corruption so that 	   you can see it is working as how Go Back N should, however, the program more than likely would end before B-Side completes all its actions since A-Side would completing 	   sending all packets first (so the statistic is not accurate since it is reporting partial completed program). 

3. Please note that it may take a minute or two to show everything in the statistics since there is calculation involved to find the average and percentage. 

4. The details of each event will show up first. Once all packets have been transmitted and acknowledged, the statistics will show up. I display them in various sections as the following:


******STATISTICS**********Application Layer Statistics*****1.	Application Layer Sent Packets Amount;2.	Application Layer Received Packets Amount;3.	Dropped Packets (Packet Was Dropped When There Was Already A Message In Transit).Note: Excluding The Last One That Will Never Send (eg. If 10 is entered for the number of messages, 9 will be sent and received.)*****Transport Protocol Statistics*****1.	Transport Protocol Received (From Application Layer) Packets Amount;2.	Transport Protocol Sent (To Application Layer) Packets Amount.*****A-Side Statistics*****1.	Sent Packets Amount (Doesn't Include Retransmission);2.	Retransmitted Amount;3.	Received ACK Amount(Not Corrupted);4.	Received Corrupted ACK Amount.*****B-Side Statistics*****1.	Received Packets Amount(Possible Corrupted);2.	Corrupted Amount of The Received Packets;3.	Sent ACK Amount (Doesn't Include Retransmission);4.	Retransmitted ACK Amount (Because ACKs Lost/Corrupted From B-Side to A-Side).*****Total Statistics*****1.	Total Packets Sent By A-Side And ACKs Sent By B-Side (Doesn't Include Retransmission);2.	Total Retransmitted Packets Amount Caused By Loss/Corruption of Both A and B Sides;3.	Total Corrupted Amount;4.	Total Lost Amount.*****Average RTT & Lost And Corrupted Percentage*****1.	Average RTT In Seconds;2.	Corrupted Percentage;3.	Lost Percentage.




