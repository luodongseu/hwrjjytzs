package com.twomonk.hw.game;


import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 测试的服务器
 * 
 * @author LD
 *
 */
public class Server2 {
	Socket socket;

	long time1;
	long time2;

	class mThread extends Thread {

		@Override
		public void run() {
			InputStream in;
			try {
				while (true) {
					in = socket.getInputStream();
					DataInputStream dataIn = new DataInputStream(in);
					String name = dataIn.readLine();
					if (name.startsWith("reg")) {
						continue;
					}
					time2 = System.currentTimeMillis();
					System.out.println("get:" + name+time2);
					long d = (time2 - time1);
					System.out.println("rec:" + d);
					if(d>500){
						System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
					}
					
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 开启服务
	 * 
	 * @throws IOException
	 */
	void start() throws IOException {
		@SuppressWarnings("resource")
		ServerSocket ss = new ServerSocket(1234);
		socket = ss.accept();

		new mThread().start();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		 while (true)
		{

			System.out.println("1111111111111111111111111111111111111");
			bw.write( "seat/ \nbutton: 4444 2000 8000"
			+" \nsmall blind: 7777 2000 8000" 
			+" \nbig blind: 3333 2000 8000" 
			+" \n1111 2000 8000" 
			+" \n8888 2000 8000" 
			+" \n6666 2000 8000" 
			+" \n2222 2000 8000" 
			+" \n5555 2000 8000 \n/seat \n");
			bw.flush();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("22222222222222222222222222222222222222");
			bw.write("blind/ \n7777: 50 \n8888: 100 \n/blind \n");
			bw.flush();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//
			
			System.out.println("33333333333333333333333333333333333333333");
			bw.write("hold/ \nDIAMONDS 7 \nHEARTS 4 \n/hold \n");
			bw.flush();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("4444444444444444444444444444444444444444444");
			bw.write("inquire/ \n4444 1950 8000 50 fold \n7777 2000 8000 0 fold \n6666 0 8000 2000 all_in \n1111 0 8000 2000 all_in \n3333 1800 8000 200 call \n5555 1800 8000 200 raise \n2222 1900 8000 100 call \n8888 1900 8000 100 blind \ntotal pot: 4650 \n/inquire");
			bw.flush();
			time1 = System.currentTimeMillis();
			System.out.println("time0:" + time1);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out
					.println("5555555555555555555555555555555555555555555555");
			bw.write("flop/ \nHEARTS 9 \nHEARTS 3 \nHEARTS Q \n/flop \n");
			bw.flush();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out
					.println("666666666666666666666666666666666666666666666666666");
			bw.write("turn/ \nCLUBS 9 \n/turn \nriver/ \nHEARTS 8 \n/river \nshowdown/ \ncommon/ \nHEARTS 9 \nHEARTS 3 \nHEARTS Q \nCLUBS 9 \nHEARTS 8 \n/common \n2: 2222 CLUBS 7 HEARTS 5 FLUSH \n3: 5555 SPADES 9 SPADES K THREE_OF_A_KIND \n5: 3333 SPADES J CLUBS 3 TWO_PAIR \n4: 1111 DIAMONDS 9 CLUBS 10 THREE_OF_A_KIND \n1: 6666 CLUBS 8 HEARTS A FLUSH \n/showdown \npot-win/ \n6666: 10150 \n/pot-win \nseat/ \nbutton: 4444 1950 8000 \nsmall blind: 8888 1900 8000 \nbig blind: 2222 2000 6000 \n5555 2000 6000 \n3333 2000 6000 \n1111 2000 6000 \n6666 10150 8000 \n7777 2000 8000 \n/seat \nblind/ \n8888: 50 \n2222: 100 \n/blind \nhold/ \nCLUBS K \nHEARTS 10 \n/hold \n");
			bw.flush();
			time1 = System.currentTimeMillis();
			System.out.println("time1:" + time1);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out
					.println("7777777777777777777777777777777777777777777777777");
			bw.write("inquire/ \n4444 1950 8000 0 fold \n7777 0 8000 2000 all_in \n6666 6350 8000 3800 raise \n1111 0 6000 2000 all_in \n3333 1800 6000 200 call \n5555 1800 6000 200 raise \n2222 1900 6000 100 blind \n8888 1850 8000 50 blind \ntotal pot: 8350 \n/inquire \n");
			bw.flush();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//			System.out
//					.println("88888888888888888888888888888888888888888888888888");
//			bw.write("inquire/ \n1111 0 8000 2000 all_in \n2222 0 8000 2000 all_in \n6666 0 8000 2000 all_in \n5555 0 8000 2000 all_in \n3333 0 8000 2000 all_in "
//					+ "\n7777 0 8000 2000 all_in \n4444 2000 8000 0 fold \n8888 1900 8000 100 call \ntotal pot: 12100 \n/inquire \n");
//			bw.flush();
//			time1 = System.currentTimeMillis();
//			System.out.println("time2:" + time1);
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			System.out
//					.println("99999999999999999999999999999999999999999999999");
//			bw.write("river/ \nR 3 \n/river \n");
//			bw.flush();
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			System.out
//					.println("0000000000000000000000000000000000000000000000000");
//			bw.write("inquire/ \n1111 0 8000 2000 all_in \n2222 0 8000 2000 all_in \n6666 0 8000 2000 all_in \n5555 0 8000 2000 all_in \n3333 0 8000 2000 all_in "
//					+ "\n7777 0 8000 2000 all_in \n4444 2000 8000 0 fold \n8888 1900 8000 100 call \ntotal pot: 12100 \n/inquire \n");
//			bw.flush();
//			time1 = System.currentTimeMillis();
//			System.out.println("time3:" + time1);
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			System.out.println("showdownshowdownshowdownshowdownshowdown");
//			bw.write("showdown/" + " \ncommon/" + " \nDIAMONDS 6"
//					+ " \nDIAMONDS 3" + " \nSPADES 6" + " \nCLUBS 7"
//					+ " \nDIAMONDS 5" + " \n/common"
//					+ " \n3: 2222 SPADES 7 CLUBS Q TWO_PAIR"
//					+ " \n2: 1111 SPADES 4 SPADES K STRAIGHT"
//					+ " \n1: 8888 DIAMONDS 8 HEARTS 4 STRAIGHT"
//					+ " \n6: 7777 SPADES Q HEARTS 2 ONE_PAIR"
//					+ " \n5: 3333 CLUBS 9 CLUBS A ONE_PAIR"
//					+ " \n7: 5555 SPADES 9 DIAMONDS J ONE_PAIR"
//					+ " \n4: 6666 HEARTS 10 DIAMONDS A ONE_PAIR"
//					+ " \n/showdown \n");
//			bw.flush();
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
////			seat/ 
////			button: 4444 2000 8000 
////			small blind: 7777 2000 8000 
////			big blind: 3333 2000 8000 
////			1111 2000 8000 
////			8888 2000 8000 
////			6666 2000 8000 
////			2222 2000 8000 
////			5555 2000 8000 
////			at
////			blind/ 
////			7777: 50 
////			3333: 100 
////			/blind 
////			/hold 
////			player 8888:hold/ 
////			CLUBS 7 
////			CLUBS Q 
////			/hold 
////			inquire/ 
////			1111 0 8000 2000 all_in 
////			3333 1900 8000 100 blind 
////			7777 1950 8000 50 blind 
////			total pot: 2150 
////			/inquire 
//			
//			
//			System.out.println("1234456867978609805673465235236");
//			bw.write("pot-win/" + " \n8888: 14000" + " \n/pot-win \n");
//			bw.flush();
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
//		// bw.close();
//		// ss.close();
	}

	public static void main(String[] args) {
		Server server = new Server();
		try {
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
