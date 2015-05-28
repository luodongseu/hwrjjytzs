package com.twomonk.hw.game;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * huawei software competition
 * 
 * @language java
 * @team Twomonk
 * @author LD/LS
 */
public class game {
	/**
	 * astatement some static final vars
	 */
	/** player ->register state **/
	private static final int S_REGISTER = 0;
	/** server ->seat info **/
	private static final int S_SEAT = 1;
	/** server ->blind info **/
	private static final int S_BLIND = 2;
	/** server ->my 2 hold cards info **/
	private static final int S_HOLDCARDS = 3;
	/** player ->do action before flop **/
	private static final int S_INQUIRE_A_HOLD = 4;
	/** server ->3 flop cards **/
	private static final int S_FLOPCARDS = 5;
	/** player ->do action after flop **/
	private static final int S_INQUIRE_A_FLOP = 6;
	/** server ->1 turn card **/
	private static final int S_TRUNCARD = 7;
	/** player ->do action after turn **/
	private static final int S_INQUIRE_A_TRUN = 8;
	/** server ->1 river card **/
	private static final int S_RIVERCARD = 9;
	/** player ->do action after river **/
	private static final int S_INQUIRE_A_RIVER = 10;
	/** server ->show all players cards **/
	private static final int S_SHOWDOWN = 11;
	/** server ->show all player' won **/
	private static final int S_SHOWPOT = 12;
	/** server ->say game over **/
	private static final int S_GAMEOVER = 13;

	/** current state replaced by those static vars **/
	private int current = -1;
	/** gloabl Socket impl **/
	private Socket socket;
	/** gloabl PrintWriter impl **/
	private PrintWriter out;
	static final String eol = " \n";
	/** game run state **/
	private boolean running = false;

	/** All Cards in the game: dynamic **/
	private List<Card> AllCards = new ArrayList<Card>();
	/** all pot in the game **/
	private int AllPot = 0;
	/** return if someone has all_in action **/
	private boolean hasAllIn = false;

	/** my actor for blind or normal **/
	private int myActor = -1;// 0:button,1:small blind,2:big blind
	/** global my pid **/
	private String mPID;
	/** global my bet **/
	private int mBET = 0;

	/** global Acting **/
	private Acting mAction = new Acting();;

	/**
	 * init Socket only one
	 */
	void init(String[] args) {
		try {
			current = -1;
			running = true;
			mPID = args[4];
			if (socket == null || socket.isClosed()) {
				socket = new Socket(InetAddress.getByName(args[0]),
						Integer.parseInt(args[1]),
						InetAddress.getByName(args[2]),
						Integer.parseInt(args[3]));

				out = new PrintWriter(socket.getOutputStream(), true);
				/**
				 * 1.register
				 */
				doRegister(mPID, "twomonk");
				/**
				 * start to play thread
				 */
				new ClientThread().start();
			} else {
				return;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * close Socket and PrintWriter
	 */
	void closeSocket() {
		try {
			socket.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * reg-msg
	 * 
	 * @param pid
	 * @param pname
	 */
	void doRegister(String pid, String pname) {
		current = S_REGISTER;
		String msg = "reg: ";
		msg += pid;
		msg += " ";
		msg += pname;
		msg += eol;
		out.write(msg);
		out.flush();
	}

	/**
	 * seat-info-msg
	 * 
	 * seat/+button: pid jetton money+small blind: pid jetton money+ (big blind:
	 * pid jetton money+)0-1 (pid jetton money+)0-5/seat+
	 */
	void getSeatInfo(String info) {
		hasAllIn = false;
		if (info.contains("button: " + mPID)) {
			myActor = 0;
		} else if (info.contains("small blind: " + mPID)) {
			myActor = 1;
			mBET = 100;
		} else if (info.contains("big blind: " + mPID)) {
			myActor = 2;
			mBET = 200;
		} else if (info.contains(mPID)) {

		}

	}

	/**
	 * solve zhanbao
	 * 
	 * @param info
	 * @param flag
	 * @return
	 */
	public String getSingleInfo(String info, String flag) {
		if (info.contains(flag)) {
			return info.split(flag)[1];
		} else {
			return null;
		}
	}

	/**
	 * game-over-msg
	 * 
	 */
	void gameOver() {
		closeSocket();
		current = S_GAMEOVER;
		running = false;
	}

	/**
	 * reset all data after each game
	 */
	void resetGame() {
		hasAllIn = false;
		myActor = -1;
		mBET = 0;
		AllPot = 0;
		AllCards.clear();
	}

	/**
	 * blind-msg
	 * 
	 * blind/+(pid: bet+)1-2/blind+
	 * 
	 * @param info
	 */
	void getBlind(String info) {
		hasAllIn = false;
		String infos[] = info.split(eol);
		if (infos.length == 3) {
			/** only small blind **/

		} else {
			/** two blind **/

		}
	}

	/**
	 * inquire-msg
	 * 
	 * inquire/+(pid jetton money bet blind | check | call | raise | all_in |
	 * fold+)1-8total pot: num+/inquire+
	 * 
	 * @param info
	 */
	void getInquire(String info) {
		hasAllIn = info.contains("all_in") ? true : false;
		System.out.println("suanfa begin time: " + System.currentTimeMillis());
		String infos[] = info.split(eol);
		/** player number **/
		int numOfPlayer = infos.length - 3;
		/** all pots in the game **/
		AllPot = Integer.valueOf(infos[numOfPlayer + 1].split(": ")[1]);
		/** get all duishous **/
		for (int i = 1; i < numOfPlayer + 1; i++) {
			Duishou ds = new Duishou(infos[i]);
			if (!ds.getAction().equals("fold")) {
				mBET = ds.getBet() - mBET;
				break;
			}
		}

	}

	/**
	 * action-msg
	 * 
	 * check | call | raise num | all_in | fold eol
	 * 
	 */
	void doAction(String cho) {
		cho += eol;
		out.write(cho);
		out.flush();
		System.out.println("send success : " + System.currentTimeMillis());
	}

	/**
	 * hold-cards-msg
	 * 
	 * hold/+color point+color point+/hold+
	 * 
	 * @param info
	 */
	void getHandCards(String info) {
		hasAllIn = false;
		String cards[] = info.split(eol);
		AllCards.add(new Card(cards[1]));
		AllCards.add(new Card(cards[2]));
	}

	/**
	 * flop-msg
	 * 
	 * flop/+color point+color point+color point+/flop+
	 * 
	 * @param info
	 */
	void getFlop(String info) {
		hasAllIn = false;
		String cds[] = info.split(eol);
		for (int i = 1; i < 4; i++) {
			/** add three **/
			AllCards.add(new Card(cds[i]));
		}
	}

	/**
	 * turn-msg
	 * 
	 * turn/+color point+/turn+
	 * 
	 * @param info
	 */
	void getTurn(String info) {
		hasAllIn = false;
		String cds[] = info.split(eol);
		AllCards.add(new Card(cds[1]));
	}

	/**
	 * river-msg
	 * 
	 * river/+color point+/river+
	 * 
	 * @param info
	 */
	void getRiver(String info) {
		hasAllIn = false;
		String cds[] = info.split(eol);
		AllCards.add(new Card(cds[1]));
	}

	/**
	 * showdown-msg
	 * 
	 * @TODO showdown/+common/+(color point+)5/common+(rank: pid color point
	 *       color point nut_hand+)2-8/showdown+
	 * 
	 * @param info
	 * @return five common cards
	 */
	Card[] showCards(String info) {
		String infos[] = info.split(eol);
		Card cCards[] = new Card[5];
		for (int i = 0; i < 5; i++) {
			cCards[i] = new Card(infos[i + 2]);
		}
		return cCards;
	}

	/**
	 * pot-win-msg
	 * 
	 * pot-win/+(pid: num+)0-8 /pot-win+
	 * 
	 * @param info
	 */
	void getPotWin(String info) {
		String infos[] = info.split(eol);
		/** win pot player **/
		Map<String, Integer> pots = new HashMap<String, Integer>();

		int num = infos.length;
		/** Player number **/
		int numOfPlayer = num - 2;
		for (int i = 0; i < numOfPlayer; i++) {
			try {
				/***************************************************************************************************************************/
				// System.out.println("POT_WIN:" + infos[i + 1]);
				/** only use (: ) to// split **/
				String[] pot = infos[i + 1].split(": ");
				pots.put(pot[0], Integer.valueOf(pot[1]));
				/***************************************************************************************************************************/
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 *            args[0]Server IP args[1]Server Port args[2]Local IP
	 *            args[3]Local Port args[4]My ID
	 */
	public static void main(String[] args) throws UnknownHostException,
			IOException {
		args = new String[] { "127.0.0.1", "1234", "127.0.0.1", "2222", "4444" };
		game gam = new game();
		gam.init(args);

	}

	/**
	 * whole game play
	 * 
	 * @author LD
	 *
	 */
	class ClientThread extends Thread {
		public void run() {
			try {
				DataInputStream in = new DataInputStream(
						socket.getInputStream());
				/** true// for// append **/
				FileWriter fileWriter = new FileWriter("MYLOG.txt", true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWriter);
				while (true && running) {
					byte[] buffer;
					buffer = new byte[in.available()];
					String info = null;
					if (buffer.length != 0) {
						in.read(buffer);
						info = new String(buffer);
						bufferWritter.write(info + "\r\n****************"
								+ "************************\r\n");
						bufferWritter.flush();
					} else {
						continue;
					}
					try {
						System.out.println("info..............................................\n"+info);
						while (!info.equals("")) {
							if (info.startsWith("seat/")) {
								System.out.println("seat..............................................\n"+ info);
								/** Seat info **/
								current = S_SEAT;
								getSeatInfo(getSingleInfo(info, "seat"));
								info = info.replaceAll("seat/(?s).*?/seat \n","");

							}
							if (info.startsWith("blind/")) {
								System.out.println("blind.............................................\n"+ info);
								/** Blind info **/
								current = S_BLIND;
								getBlind(getSingleInfo(info, "blind"));
								info = info.replaceAll("blind/(?s).*?/blind \n", "");

							}
							if (info.startsWith("hold/")) {
								System.out.println("hold..............................................\n"+ info);
								/** My HandCards info **/
								current = S_HOLDCARDS;
								getHandCards(getSingleInfo(info, "hold"));
								info = info.replaceAll("hold/(?s).*?/hold \n","");

							}
							if (info.startsWith("inquire/")) {
								System.out.println("inquire..............................................\n"+ info);
								/** Server Inquire my action info **/
								if (AllCards.size() == 5) {
									/** strategy 1 **/
									getInquire(getSingleInfo(info, "inquire"));
									doAction("fold");
								//	mAction.FPaiAction(AllCards, mBET, AllPot,hasAllIn);
								} else if (AllCards.size() == 2) {
									/** strategy 2 **/
									getInquire(getSingleInfo(info, "inquire"));
									doAction("fold");
								//	mAction.FristAction(AllCards, mBET, AllPot,hasAllIn);
								} else if (AllCards.size() == 6) {
									/** strategy 3 **/
									getInquire(getSingleInfo(info, "inquire"));
									doAction("fold");
								//	mAction.ZPaiAction(AllCards, mBET, AllPot,hasAllIn);
								} else if (AllCards.size() == 7) {
									/** strategy 4 **/
									getInquire(getSingleInfo(info, "inquire"));
									doAction("fold");
								//	mAction.HPaiAction(AllCards, mBET, AllPot,	hasAllIn);
								}
								info = info.replaceAll("inquire/(?s).*?/inquire \n", "");

							}
							if (info.startsWith("flop/")) {
								System.out.println("flop..............................................\n"+ info);
								/** 3 Common cards info **/
								current = S_FLOPCARDS;
								getFlop(getSingleInfo(info, "flop"));
								info = info.replaceAll("flop/(?s).*?/flop \n","");

							}
							if (info.startsWith("turn/")) {
								System.out.println("turn..............................................\n"+ info);
								/** 1 Turn card info **/
								current = S_TRUNCARD;
								getTurn(getSingleInfo(info, "turn"));
								info = info.replaceAll("turn/(?s).*?/turn \n","");

							}
							if (info.startsWith("river/")) {
								System.out.println("river..............................................\n"+ info);
								/** 1 River card info **/
								current = S_RIVERCARD;
								getRiver(getSingleInfo(info, "river"));
								info = info.replaceAll("river/(?s).*?/river \n", "");

							}
							if (info.startsWith("pot-win/")) {
								System.out.println("pot-win..............................................\n"+ info);
								/** Show pot giving info **/
								current = S_SHOWPOT;
								getPotWin(getSingleInfo(info, "pot-win"));
								info = info.replaceAll("pot-win/(?s).*?/pot-win \n", "");

							}
							if (info.startsWith("showdown/")) {
								System.out.println("showdown..............................................\n"+ info);
								/** Show all players' cards info **/
								current = S_SHOWDOWN;
								showCards(getSingleInfo(info, "showdown"));
								info = info.replaceAll("showdown/(?s).*?/showdown \n", "");

							}
							if (info.startsWith("game-over")) {
								System.out.println("game-over..............................................\n"+ info);
								/** Game Over reset game **/
								current = S_GAMEOVER;
								gameOver();
								info = info.replaceAll("game-over \n", "");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				/** close our log bufferWritter **/
				bufferWritter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class Acting {
		private static final String pair = "pair";
		private static final String tonghua = "tonghua";
		private static final String lianz = "lianz";
		private static final String zapai = "zapai";
		private static final int STRAIGHT_FLUSH = 10;
		private static final int FOUR_OF_A_KIND = 9;
		private static final int FULL_HOUSE = 8;
		private static final int FLUSH = 7;
		private static final int STRAIGHT = 6;
		private static final int THREE_OF_A_KIND = 5;
		private static final int TWO_PAIR = 4;
		private static final int ONE_PAIR = 3;
		private static final int FLUSH_T = 2;
		private static final int STRAIGHT_T = 1;

		private Judge j = new Judge();

		public void FristAction(List<Card> list2, double callnum, double pot,
				boolean is_all_in) {
			Card[] list = new Card[2];
			String state;
			list2.toArray(list);
			state = j.judgetwo(list);
			if (state.equals(pair)) {
				if (Gailv.getBiggerpair(list[0].point, 6) <= 12.99) {
					doAction("check");// call
				} else if (is_all_in) {
					doAction("fold");// fold
				} else if ((1.0 - Gailv.getBiggerpair(list[0].point, 6) / 100.0) >= callnum
						/ pot) {
					doAction("check");// check/call
				}

			} else if (state.equals(zapai)) {
				doAction("fold");// fold
			} else if (callnum <= 100) {
				doAction("check");// check
			} else {
				doAction("fold");// fold
			}

		}

		public void ZPaiAction(List<Card> list2, double callnum, double pot,
				boolean is_all_in) {
			double p = callnum / pot;
			Card[] list = new Card[6];
			int state;
			list2.toArray(list);
			state = j.judege(list);
			if (state >= 8) {
				doAction("all_in");// all_in
				return;
			}
			if (state >= 5) {
				doAction("check");// call
				return;
			}
			if (state == 4) {

				if (p > 0.1) {
					doAction("fold");// fold
					return;
				} else {
					doAction("check");// call
					return;
				}
			}
			if (state == 3) {
				if (!is_all_in) {
					if (p > 0.1) {
						doAction("fold");// fold
						return;
					} else {
						doAction("check");// call
						return;
					}
				} else {
					doAction("fold");// fold
					return;
				}
			}
			if (state == 2) {
				if (!is_all_in) {
					if (p > 0.196) {
						doAction("fold");// fold
						return;
					} else {
						doAction("check");// call
						return;
					}
				} else {
					doAction("fold");// fold
					return;
				}
			}
			if (state == 1) {
				if (!is_all_in) {
					if (p > 0.174) {
						doAction("fold");// fold
						return;
					} else {
						doAction("check");// call
						return;
					}
				} else {
					doAction("fold");// fold
					return;
				}
			}
			if (state == 0) {
				doAction("fold");// fold
				return;
			}
		}

		public void FPaiAction(List<Card> list2, double callnum, double pot,
				boolean is_all_in) {
			double p = callnum / pot;
			Card[] list = new Card[5];
			int state;
			list2.toArray(list);
			state = j.judege(list);
			if (state >= 8) {
				doAction("all_in");// all_in
				return;
			}
			if (state >= 5) {
				doAction("check");// call
				return;
			}

			if (state == 2) {
				if (p > 0.191) {
					doAction("fold");// fold
					return;
				} else {
					doAction("check");// call
					return;
				}
			}
			if (state == 1) {
				if (p > 0.17) {
					doAction("fold");// fold
					return;
				} else {
					doAction("check");// call
					return;
				}
			}
			if (state == 0) {
				doAction("fold");// fold
				return;
			}
		}

		public void HPaiAction(List<Card> list2, double callnum, double pot,
				boolean is_all_in) {
			double p = callnum / pot;
			Card[] list = new Card[7];
			int state;
			list2.toArray(list);
			state = j.judege(list);
			if (state >= 8) {
				doAction("all_in");// all_in
				return;
			}
			if (state >= 5) {
				doAction("check");// call
				return;
			}
			if (!is_all_in) {
				doAction("check");// check
				return;
			} else {
				doAction("fold");// fold
				return;
			}
		}
	}
}

/**
 * against info entity
 * 
 * @author LD
 *
 */
class Duishou {
	private String pid;
	private String jetton;
	private int money;
	private int bet;
	private String action;

	public Duishou() {
	}

	public Duishou(String duishou) {
		String ds[] = duishou.split("\\s");
		if (ds.length < 5) {
			return;
		}
		this.pid = ds[0];
		this.jetton = ds[1];
		this.money = Integer.valueOf(ds[2]);
		this.bet = Integer.valueOf(ds[3]);
		this.action = ds[4];
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getJetton() {
		return jetton;
	}

	public void setJetton(String jetton) {
		this.jetton = jetton;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public int getBet() {
		return bet;
	}

	public void setBet(int bet) {
		this.bet = bet;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}

/**
 * gailv
 * 
 * @author LD
 *
 */
class Gailv {
	private static final double[][] biggerPair = {
			{ 0.49, 0.98, 1.47, 1.96, 2.44, 2.93, 3.42, 3.91, 4.39 },
			{ 0.98, 1.95, 2.92, 3.88, 4.84, 5.79, 6.73, 7.66, 8.59 },
			{ 1.47, 2.92, 4.36, 5.77, 7.17, 8.56, 9.92, 11.27, 12.59 },
			{ 1.96, 3.89, 5.78, 7.64, 9.46, 11.24, 12.99, 14.7, 16.37 },
			{ 2.45, 4.84, 7.18, 9.46, 11.68, 13.84, 15.93, 17.95, 19.9 },
			{ 2.94, 5.8, 8.57, 11.25, 13.84, 16.34, 18.73, 21.01, 23.18 },
			{ 3.43, 6.74, 9.94, 13.01, 15.95, 18.74, 21.38, 23.87, 26.19 },
			{ 3.92, 7.69, 11.3, 14.73, 17.99, 21.04, 23.89, 26.51, 28.9 },
			{ 4.41, 8.62, 12.63, 16.42, 19.96, 23.44, 26.23, 28.92, 31.29 },
			{ 4.90, 9.56, 13.95, 18.06, 21.86, 25.32, 28.41, 31.09, 33.34 },
			{ 5.39, 10.48, 15.26, 19.67, 23.7, 27.29, 30.4, 33, 35.03 },
			{ 5.88, 11.41, 16.54, 21.24, 25.46, 29.14, 32.22, 34.64, 36.33 } };
	public static final float pairToBetter = 12.7f;
	public static final float ontToPair = 32.4f;
	public static final float ontToDouble_Pair = 2f;
	public static final float stos = 0.842f;
	public static final float stobs = 10.9f;
	public static final float stobbs = 10.9f;
	public static float ltost = 9.6f;

	public static float tt_th_f = 19.1f;
	public static float ldszt_sz_f = 17f;
	public static float ntsz_sz_f = 8.5f;
	public static float st_st_f = 2.1f;
	public static float ld_hl_f = 8.5f;
	public static float yd_st_f = 4.3f;
	public static float ld_dz_f = 12.8f;
	public static float tt_th_h = 19.6f;
	public static float ldsz_sz_h = 17.4f;
	public static float ntsz_sz_h = 8.7f;
	public static float st_st_h = 2.2f;
	public static float ld_hl_h = 8.7f;
	public static float yd_st_h = 4.3f;
	public static float ld_dz_h = 13f;
	public static float st = 0.24f;
	public static float dz = 16.9f;
	public static float szth = 5.17f;
	public static float lzth = 5.17f;
	public static float zs = 39.8f;
	public static float sz = 3.45f;
	public static float lzxl = 40f;
	public static float zp = 55.6f;

	public static double getBiggerpair(int pair, int num) {
		if (num < 0 || num > 8)
			return 0;
		if (pair == 13)
			return biggerPair[0][num - 1];
		if (pair == 12)
			return biggerPair[1][num - 1];
		if (pair == 11)
			return biggerPair[2][num - 1];
		if (pair == 10)
			return biggerPair[3][num - 1];
		if (pair == 9)
			return biggerPair[4][num - 1];
		if (pair == 8)
			return biggerPair[5][num - 1];
		if (pair == 7)
			return biggerPair[6][num - 1];
		if (pair == 6)
			return biggerPair[7][num - 1];
		if (pair == 5)
			return biggerPair[8][num - 1];
		if (pair == 5)
			return biggerPair[9][num - 1];
		if (pair == 3)
			return biggerPair[10][num - 1];
		if (pair == 2)
			return biggerPair[11][num - 1];
		return 0;
	}

}

/**
 * judge cards type
 * 
 * @author LD
 *
 */
class Judge {
	private static String pair = "pair";
	private static String tonghua = "tonghua";
	private static String lianz = "lianz";
	private static String zapai = "zapai";
	private static int STRAIGHT_FLUSH = 10;
	private static int FOUR_OF_A_KIND = 9;
	private static int FULL_HOUSE = 8;
	private static int FLUSH = 7;
	private static int STRAIGHT = 6;
	private static int THREE_OF_A_KIND = 5;
	private static int TWO_PAIR = 4;
	private static int ONE_PAIR = 3;
	private static int FLUSH_T = 2;
	private static int STRAIGHT_T = 1;

	public String judgetwo(Card[] list) {
		if (list.length != 2) {
			// System.out.println("not 2");
			return null;
		}
		if (list[0].point == list[1].point)
			return pair;
		if (list[0].color.equals(list[1].color))
			return tonghua;
		if (list[0].point - list[1].point == 1
				|| list[0].point - list[1].point == -1)
			return lianz;
		return "zapai";
	}

	public int judege(Card[] list) {
		Short(list);
		int numofpairs = 0;
		int maxsamepoint = 0, maxsamecolor = 0;
		int lessamepoint = 0, lessamecolor = 0;
		int numOfStragiht;
		int tmaxpoint = 0, tmaxcolor = 0;
		for (int j = 0; j < list.length; j++) {
			Card tempCard = list[j];
			tmaxpoint = 0;
			for (int i = 0; i < list.length; i++) {
				if (tempCard.point == list[i].point) {
					tmaxpoint += 1;
					j++;
				}
			}
			if (tmaxpoint >= maxsamepoint) {
				if (tmaxpoint == 2)
					numofpairs++;
				lessamepoint = maxsamepoint;
				maxsamepoint = tmaxpoint;
			}
		}
		for (int j = 0; j < list.length; j++) {
			Card tempCard = list[j];
			tmaxcolor = 0;
			for (int i = 0; i < list.length; i++) {
				if (tempCard.color.equals(list[i].color)) {
					tmaxcolor += 1;
					j++;
				}
			}
			if (tmaxcolor >= maxsamecolor) {
				lessamecolor = maxsamecolor;
				maxsamecolor = tmaxcolor;
			}
		}
		numOfStragiht = numOfStragiht(list);
		if (maxsamecolor >= 5 & numOfStragiht >= 5)
			return STRAIGHT_FLUSH;
		if (maxsamepoint == 4)
			return FOUR_OF_A_KIND;
		if (maxsamepoint == 3 & lessamepoint == 2)
			return FULL_HOUSE;
		if (maxsamecolor >= 5)
			return FLUSH;
		if (numOfStragiht >= 5)
			return STRAIGHT;
		if (maxsamepoint == 3)
			return THREE_OF_A_KIND;
		if (numofpairs >= 2)
			return TWO_PAIR;
		if (numofpairs == 1)
			return ONE_PAIR;
		if (numOfStragiht == 4)
			return STRAIGHT_T;
		if (maxsamecolor == 4)
			return FLUSH_T;
		return 0;

	}

	public void Short(Card[] list) {
		int tag = 0;
		Card temp;
		for (int i = list.length - 1; i > 0; i--) {
			tag = 0;
			for (int j = 0; j < i; j++) {
				if (list[j].point > list[j + 1].point) {
					temp = list[j];
					list[j] = list[j + 1];
					list[j + 1] = temp;
					tag = 1;
				}
			}
			if (tag == 0)
				break;
		}

		for (Card c : list) {
			System.out.println("card: " + c.point);
		}
	}

	public int numOfStragiht(Card[] list) {
		int s = 1, l = 1, t = 1;
		for (int i = 0; i < list.length; i++) {
			for (int j = i; i < list.length - 1; j++) {
				if (list[j + 1].point - list[j].point == 1) {
					t++;
					i++;
				} else {
					break;
				}

			}
			if (s < t) {
				l = s;
				s = t;
			}

		}
		return s;
	}
}

/**
 * Card
 * 
 * @author LD
 *
 */
class Card {
	String color;
	int point;

	/**
	 * @param reg
	 */
	Card(String reg) {
		String str[] = reg.split(" ");
		if (str.equals("") || str == null || str.length < 2)
			return;
		this.color = str[0];
		if (str[1].endsWith("J"))
			this.point = 11;
		else if (str[1].endsWith("Q"))
			this.point = 12;
		else if (str[1].endsWith("K"))
			this.point = 13;
		else if (str[1].endsWith("A"))
			this.point = 14;
		else
			this.point = Integer.valueOf(str[1]);
	}
}
