package com.twomonk.hw.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ��Ϊ�����Ӣ��ս??
 * 
 * @language java
 * @team Twomonk
 * @author LD/LS
 */
public class game {
	/**
	 * statement some static final vars
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

	/**
	 * init Socket only one
	 */
	void init(String[] args) {
		try {
			current = -1;
			if (socket == null || socket.isClosed()) {
				socket = new Socket(InetAddress.getByName(args[0]),
						Integer.parseInt(args[1]),
						InetAddress.getByName(args[2]),
						Integer.parseInt(args[3]));

				out = new PrintWriter(socket.getOutputStream(), true);
				/**
				 * 1.register
				 */
				doRegister(args[4], "twomonk");
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

	class Card {
		String color;
		String point;

		/**
		 * @param reg
		 */
		Card(String reg) {
			String str[] = reg.split(" ");
			if (str.equals("") || str == null || str.length < 2)
				return;
			this.color = str[0];
			this.point = str[1];
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
		out.println(msg);
	}

	/**
	 * seat-info-msg
	 * 
	 * seat/ eol button: pid jetton money eol small blind: pid jetton money eol
	 * (big blind: pid jetton money eol)0-1 (pid jetton money eol)0-5 /seat eol
	 */
	void getSeatInfo(String info) {
		info.replaceAll("/seat eol | eol /seat eol", "");
		String[] infos = info.split(eol);
		if (infos.length >= 2) {

			if (infos[0].contains("button")) {
				String button = infos[1].split(": ")[1];
			}

			if (infos[1].contains("small blind")) {
				String sblind = infos[2].split(": ")[1];

			}

			if (infos.length >= 3) {
				String bblind = infos[2].split(": ")[1];

			}

			if (infos.length >= 4) {
				String other[][] = new String[infos.length - 3][3];
				for (int i = 3; i < infos.length; i++) {
					other[i] = infos[i].split(": ")[1].split(" ");
				}
			}
		}
	}

	/**
	 * game-over-msg
	 * 
	 */
	void gameOver() {
		closeSocket();
		current = S_GAMEOVER;
	}

	/**
	 * blind-msg
	 * 
	 * blind/ eol (pid: bet eol)1-2 /blind eol
	 * 
	 * @param info
	 */
	void getBlind(String info) {
		info = info.replaceAll("/blind eol | eol blind/ eol ", "");
		String infos[] = info.split(eol);
		if (infos.length == 1) {
			String sblind = infos[0].split(": ")[1];
		} else if (infos.length == 2) {
			String b1 = infos[0].split(": ")[1];
			String b2 = infos[1].split(": ")[1];
		}
	}

	/**
	 * inquire-msg
	 * 
	 * inquire/ eol (pid jetton money bet blind | check | call | raise | all_in
	 * | fold eol)1-8 total pot: num eol /inquire eol
	 * 
	 * @param info
	 */
	void getInquire(String info) {

	}

	/**
	 * action-msg
	 * 
	 * check | call | raise num | all_in | fold eol
	 * 
	 */
	void doAction() {
		String msg = "";
		msg = "check";
		msg += eol;
		out.println(msg);
	}

	/**
	 * flop-msg
	 * 
	 * flop/ eol color point eol color point eol color point eol /flop eol
	 * 
	 * @param info
	 */
	void getFlop(String info) {
		info = info.replaceAll("flop/ eol | eol /flop eol	", "");
		String cds[] = info.split(eol);
		Card cards[] = new Card[3];
		for (int i = 0; i < 3; i++) {
			cards[i] = new Card(cds[i]);
		}
	}

	/**
	 * turn-msg
	 * 
	 * turn/ eol color point eol /turn eol
	 * 
	 * @param info
	 */
	void getTurn(String info) {
		info = info.replaceAll("turn/ eol | eol /turn eol", "");
		Card card = new Card(info);
	}

	/**
	 * river-msg
	 * 
	 * river/ eol color point eol /river eol
	 * 
	 * @param info
	 */
	void getRiver(String info) {
		info = info.replaceAll("river/ eol | eol /river eol", "");
		Card card = new Card(info);
	}

	/**
	 * showdown-msg
	 * 
	 * showdown/ eol common/ eol color point eol /common eol rank: pid color
	 * point color point nut_hand eol /showdown eol
	 * 
	 * @param info
	 */
	void showCards(String info) {
		info = info.replaceAll("showdown/ eol | eol /showdown eol", "");
		info = info.replaceAll("common/ eol |/common eol ", "");
		String infos[] = info.split(eol);
		Card cCards[] = new Card[5];
		for (int i = 0; i < 5; i++) {
			cCards[i] = new Card(infos[i]);
		}
		for (int j = 5; j < infos.length; j++) {

		}
	}

	/**
	 * pot-win-msg
	 * 
	 * pot-win/ eol (pid: num eol)0-8 /pot-win eol
	 * 
	 * @param info
	 */
	void getPotWin(String info) {
		info = info.replaceAll("pot-win/ eol | eol /pot-win eol", "");
		String infos[] = info.split(eol);
		if (infos == null || infos.equals("")) {
			return;
		} else {
			for (String ifo : infos) {
				String ifs[] = ifo.split(":");// ifs[0]==pid,ifs[1]==num
			}
		}
	}

	/**
	 * hold-cards-msg
	 * 
	 * hold/ eol color point eol color point eol /hold eol
	 * 
	 * @param info
	 */
	void getHandCards(String info) {
		info = info.replaceAll("hold/ eol | eol /hold eol", "");
		String cards[] = info.split(eol);
		Card hCards[] = new Card[2];
		hCards[0] = new Card(cards[0]);
		hCards[1] = new Card(cards[1]);
	}

	/**
	 * ��ں���main 1. player向server注册自己的id和name（reg-msg）
	 * 2.while（还有2个及以上玩家并且未超过最大局数） ｛ a) 发布座次信息：seat-info-msg（轮流坐庄）
	 * b)强制押盲注：blind-msg c) 为每位牌手发两张底牌：hold-cards-msg d)
	 * 翻牌前喊注：inquire-msg/action-msg（多次） e) 发出三张公共牌：flop-msg
	 * f)翻牌圈喊注：inquire-msg/action-msg（多次） g) 发出一张公共牌（转牌）：turn-msg h)
	 * 转牌圈喊注：inquire-msg/action-msg（多次） i) 发出一张公共牌（河牌）：river-msg
	 * j)河牌圈喊注：inquire-msg/action-msg（多次） k) 若有两家以上未盖牌则摊牌比大小：showdown-msg
	 * l)公布彩池分配结果：pot-win-msg ｝ 3. 本场比赛结束（game-over-msg）
	 * 
	 * @param args
	 *            [5] args[0]��������IP args[1]��������˿�??args[2]���ֳ���󶨵�IP
	 *            args[3]���ֳ���󶨵Ķ˿ں� args[4]���ֵ�ID
	 */
	public static void main(String[] args) throws UnknownHostException,
			IOException {
		if (args == null || args.equals("")) {
			return;
		}

		/**
		 * init
		 */
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
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				while (true) {
					String info = in.readLine().trim();
					System.out.println(info);
					if (info.startsWith("seat/")) {
						/** Seat info **/
						current = S_SEAT;
						getSeatInfo(info);
					} else if (info.startsWith("blind/")) {
						/** Blind info **/
						current = S_BLIND;
						getBlind(info);
					} else if (info.startsWith("hold/")) {
						/** My HandCards info **/
						current = S_HOLDCARDS;
						getHandCards(info);
					} else if (info.startsWith("inquire/")) {
						/** Server Inquire my action info **/
						switch (current) {
						case S_FLOPCARDS:
							current = S_INQUIRE_A_FLOP;
							/**strategy 1**/
							doAction();
							break;
						case S_HOLDCARDS:
							current = S_INQUIRE_A_HOLD;
							/**strategy 2**/
							doAction();
							break;
						case S_TRUNCARD:
							current = S_INQUIRE_A_TRUN;
							/**strategy 3**/
							doAction();
							break;
						case S_RIVERCARD:
							current = S_INQUIRE_A_RIVER;
							/**strategy 4**/
							doAction();
							break;
						default:
							break;
						}
					} else if (info.startsWith("flop/")) {
						/** 3 Common cards info **/
						current = S_FLOPCARDS;
						getFlop(info);
					} else if (info.startsWith("turn/")) {
						/** 1 Turn card info **/
						current = S_TRUNCARD;
						getTurn(info);
					} else if (info.startsWith("river/")) {
						/** 1 River card info **/
						current = S_RIVERCARD;
						getRiver(info);
					} else if (info.startsWith("showdown/")) {
						/** Show all players' cards info **/
						current = S_SHOWDOWN;
						showCards(info);
					} else if (info.startsWith("pot-win/")) {
						/** Show pot giving info **/
						current = S_SHOWPOT;
						getPotWin(info);
					} else if (info.startsWith("game-over")) {
						/** Game Over reset game **/
						current = S_GAMEOVER;
						gameOver();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
