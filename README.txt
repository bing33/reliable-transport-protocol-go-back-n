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


******STATISTICS*****



