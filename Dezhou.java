import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * huawei software competition
 * 
 * @language java
 * @team Twomonk
 * @author LD/LS
 */
public class Dezhou {
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
	/** current game player's number **/
	private int numOfPlayer = 0;
	/** current left in the game player's number **/
	private int numOfLeftPlayer = 0;

	/** global Acting **/
	private Acting mAction = new Acting();;

	/**
	 * init Socket only one
	 */
	void init(String[] args) {
		try {
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
	 * seat/+ button: pid jetton money+small blind: pid jetton money+ (big
	 * blind: pid jetton money+)0-1 (pid jetton money+)0-5 /seat+
	 */
	void getSeatInfo(String info) {
		hasAllIn = false;
		if (info.contains("button: " + mPID)) {
			myActor = 0;
		} else if (info.contains("small blind: " + mPID)) {
			myActor = 1;
			mBET = 40;
		} else if (info.contains("big blind: " + mPID)) {
			myActor = 2;
			mBET = 80;
		} else if (info.contains(mPID)) {
			myActor = -1;
		}

		numOfPlayer = info.split(eol).length;
		numOfLeftPlayer = numOfPlayer;
	}

	/**
	 * @key action
	 * @todo my own strategy
	 */
	void doCelve() {
		if (AllCards.size() == 5) {
			/** strategy 1 **/
			mAction.FPaiAction(AllCards, mBET, AllPot, hasAllIn);
		} else if (AllCards.size() == 2) {
			/** strategy 2 **/
			mAction.FristAction(AllCards, mBET, AllPot, hasAllIn);
		} else if (AllCards.size() == 6) {
			/** strategy 3 **/
			mAction.ZPaiAction(AllCards, mBET, AllPot, hasAllIn);
		} else if (AllCards.size() == 7) {
			/** strategy 4 **/
			mAction.HPaiAction(AllCards, mBET, AllPot, hasAllIn);
		}
	}

	/**
	 * game-over-msg
	 * 
	 */
	void gameOver() {
		closeSocket();
		running = false;
		/** exit application **/
		System.exit(0);
	}

	/**
	 * reset all data after each game
	 */
	void resetGame() {
		hasAllIn = false;
		myActor = -1;
		mBET = 0;
		AllPot = 0;
		numOfPlayer = 0;
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
	 * inquire/+ (pid jetton money bet blind | check | call | raise | all_in |
	 * fold+)1-8total pot: num+ /inquire+
	 * 
	 * @param info
	 */
	void getInquire(String info) {
		hasAllIn = info.contains("all_in") ? true : false;
		String infos[] = info.split(eol);
		int tempbet = 0;
		/** player number **/
		numOfPlayer = infos.length - 1;
		/** all pots in the game **/
		AllPot = Integer.valueOf(infos[numOfPlayer].split(": ")[1]);
		/** get all duishous **/
		for (int i = 0; i < numOfPlayer; i++) {
			Duishou ds = new Duishou(infos[i]);
			if (ds.getBet() > tempbet) {
				tempbet = ds.getBet();
			}
			if (ds.getAction().equals("fold")) {
				numOfLeftPlayer--;
			}
		}
		mBET = tempbet - mBET;
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
		AllCards.add(new Card(cards[0]));
		AllCards.add(new Card(cards[1]));
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
		for (int i = 0; i < 3; i++) {
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
		AllCards.add(new Card(cds[0]));
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
		AllCards.add(new Card(cds[0]));
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

	}

	/**
	 * @param args
	 *            args[0]Server IP args[1]Server Port args[2]Local IP
	 *            args[3]Local Port args[4]My ID
	 */
	public static void main(String[] args) throws UnknownHostException,
			IOException {
		/** local test **/
		/**
		 * // args = new String[] { "127.0.0.1", "1234", "127.0.0.1", "2222",
		 * "4444" };
		 */
		Dezhou game = new Dezhou();
		game.init(args);
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
				/**
				 * @param true for append output to a txt
				 **/
				// FileWriter fileWriter = new FileWriter("MYLOG.txt", true);
				// BufferedWriter bufferWritter = new
				// BufferedWriter(fileWriter);
				while (running) {
					byte[] buffer;
					buffer = new byte[in.available()];
					String info = null;
					if (buffer.length != 0) {
						in.read(buffer);
						info = new String(buffer);
						// bufferWritter.write(info
						// + "\r\n**********divider*********\r\n");
						// bufferWritter.flush();
					} else {
						continue;
					}
					/**
					 * analyze for info from server
					 */
					String[] infoStrings = info.split(eol);
					for (int line = 0; line < infoStrings.length; line++) {
						StringBuffer tempmsg = new StringBuffer();
						if (infoStrings[line].contains("seat")) {
							while (true) {
								line++;
								if (infoStrings[line].contains("seat")) {
									getSeatInfo(tempmsg.toString());
									break;
								} else {
									tempmsg.append(infoStrings[line]);
									tempmsg.append(eol);
								}
							}
							continue;
						}
						if (infoStrings[line].contains("blind")) {
							while (true) {
								line++;
								if (infoStrings[line].contains("blind")) {
									getBlind(tempmsg.toString());
									break;
								} else {
									tempmsg.append(infoStrings[line]);
									tempmsg.append(eol);
								}
							}
							continue;
						}
						if (infoStrings[line].contains("hold")) {
							while (true) {
								line++;
								if (infoStrings[line].contains("hold")) {
									getHandCards((tempmsg.toString()));
									break;
								} else {
									tempmsg.append(infoStrings[line]);
									tempmsg.append(eol);
								}
							}
							continue;
						}
						if (infoStrings[line].contains("inquire")) {
							while (true) {
								line++;
								if (infoStrings[line].contains("inquire")) {
									getInquire((tempmsg.toString()));
									doCelve();
									break;
								} else {
									tempmsg.append(infoStrings[line]);
									tempmsg.append(eol);
								}
							}
							continue;
						}
						if (infoStrings[line].contains("flop")) {
							while (true) {
								line++;
								if (infoStrings[line].contains("flop")) {
									getFlop((tempmsg.toString()));
									break;
								} else {
									tempmsg.append(infoStrings[line]);
									tempmsg.append(eol);
								}
							}
							continue;
						}
						if (infoStrings[line].contains("turn")) {
							while (true) {
								line++;
								if (infoStrings[line].contains("turn")) {
									getTurn((tempmsg.toString()));

									break;
								} else {
									tempmsg.append(infoStrings[line]);
									tempmsg.append(eol);
								}
							}
							continue;
						}
						if (infoStrings[line].contains("river")) {
							while (true) {
								line++;
								if (infoStrings[line].contains("river")) {
									getTurn((tempmsg.toString()));
									break;
								} else {
									tempmsg.append(infoStrings[line]);
									tempmsg.append(eol);
								}
							}
							continue;
						}
						if (infoStrings[line].contains("pot-win")) {
							while (true) {
								line++;
								if (infoStrings[line].contains("pot-win")) {
									getPotWin((tempmsg.toString()));
									resetGame();
									break;
								} else {
									tempmsg.append(infoStrings[line]);
									tempmsg.append(eol);
								}
							}
							continue;
						}
						if (infoStrings[line].contains("game-over"))
							gameOver();
					}
				}
				/** close our log bufferWritter **/
				// bufferWritter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Action defined
	 * 
	 * @todo action when be inquired
	 * 
	 * @author LS
	 *
	 */
	class Acting {
		private Judge j = new Judge();

		/**
		 * first cards
		 * 
		 * @param list2
		 * @param callnum
		 * @param pot
		 * @param is_all_in
		 */
		public void FristAction(List<Card> list2, double callnum, double pot,
				boolean is_all_in) {
			Card[] list = new Card[2];
			String state;
			list2.toArray(list);
			state = j.judgetwo(list);
			if (state.equals(Varaible.pair)) {
				if (Gailv.getBiggerpair(list[0].point, numOfPlayer) <= 20) {
					doAction("check");
				} else if (list[0].point > 10) {
					doAction("raise 100");
				} else if (is_all_in) {
					doAction("fold");
				} else {
					doAction("check");
				}

			} else if (state.equals(Varaible.zapai)) {
				if (numOfLeftPlayer <= 2 && !is_all_in) {
					doAction("check");
				} else if ((list[0].point > 11 || list[1].point > 11)
						&& !is_all_in) {
					doAction("check");
				} else if (myActor == 0 && numOfLeftPlayer == 3) {
					doAction("check");
				} else
					doAction("fold");
			} else if (!is_all_in && callnum <= 200) {
				doAction("check");
			} else {
				doAction("fold");
			}

		}

		/**
		 * turn cards
		 * 
		 * @param list2
		 * @param callnum
		 * @param pot
		 * @param is_all_in
		 */
		public void ZPaiAction(List<Card> list2, double callnum, double pot,
				boolean is_all_in) {
			double p = callnum / pot;
			Card[] list = new Card[6];
			int state;
			list2.toArray(list);
			state = j.judege(list);
			if (numOfLeftPlayer == 2 && !is_all_in
					&& state > Varaible.STRAIGHT_T) {
				doAction("check");// all_in
				return;
			}
			if (state >= Varaible.FULL_HOUSE) {
				doAction("all_in");// all_in
				return;
			}
			if (state >= Varaible.THREE_OF_A_KIND) {
				doAction("raise 200");// call
				return;
			}
			if (state == Varaible.TWO_PAIR) {
				if (p >= 0.5) {
					if (numOfLeftPlayer == 2 && !is_all_in
							&& state > Varaible.STRAIGHT_T) {
						doAction("check");// all_in
						return;
					}
					doAction("fold");// fold
					return;
				} else {
					doAction("check");// call
					return;
				}
			}
			if (state == Varaible.ONE_PAIR) {
				if (!is_all_in) {
					if (p > 0.1) {
						if (numOfLeftPlayer == 2 && !is_all_in
								&& state > Varaible.STRAIGHT_T) {
							doAction("check");// all_in
							return;
						}
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
			if (state == Varaible.FLUSH_T) {
				if (!is_all_in) {
					if (p > 0.196) {
						if (numOfLeftPlayer == 2 && !is_all_in
								&& state > Varaible.STRAIGHT_T) {
							doAction("check");// all_in
							return;
						}
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
			} else {
				if (numOfLeftPlayer == 2 && !is_all_in
						&& state > Varaible.STRAIGHT_T) {
					doAction("check");// all_in
					return;
				}
				doAction("fold");// fold
				return;
			}
		}

		/**
		 * flop cards
		 * 
		 * @param list2
		 * @param callnum
		 * @param pot
		 * @param is_all_in
		 */
		public void FPaiAction(List<Card> list2, double callnum, double pot,
				boolean is_all_in) {
			double p = callnum / pot;
			Card[] list = new Card[5];
			int state;
			list2.toArray(list);
			state = j.judege(list);

			if (state >= Varaible.FULL_HOUSE) {
				doAction("all_in");// all_in
				return;
			}
			if (state > Varaible.THREE_OF_A_KIND) {
				doAction("raise 200");
			}
			if (state == Varaible.THREE_OF_A_KIND) {
				doAction("check");// call
				return;
			}

			if (state >= Varaible.ONE_PAIR) {
				if (!is_all_in) {
					if (p > 0.2) {
						if (numOfLeftPlayer == 2 && !is_all_in
								&& state > Varaible.STRAIGHT_T) {
							doAction("check");// all_in
							return;
						}
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
			if (state >= Varaible.STRAIGHT_T) {
				if (!is_all_in) {
					if (p > 0.25) {
						if (numOfLeftPlayer == 2 && !is_all_in
								&& state > Varaible.STRAIGHT_T) {
							doAction("check");// all_in
							return;
						}
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
			if (!is_all_in) {
				doAction("check");
				return;
			} else {
				doAction("fold");
				return;
			}
		}

		/**
		 * river cards
		 * 
		 * @param list2
		 * @param callnum
		 * @param pot
		 * @param is_all_in
		 */
		public void HPaiAction(List<Card> list2, double callnum, double pot,
				boolean is_all_in) {
			Card[] list = new Card[7];
			int state;
			list2.toArray(list);
			state = j.judege(list);
			if (state >= Varaible.FULL_HOUSE) {
				doAction("all_in");
				return;
			}
			if (state >= Varaible.TWO_PAIR) {
				if ((is_all_in && callnum > 500) || callnum > 1000) {
					doAction("fold");
				} else {
					doAction("check");
				}
				return;
			}
			if (state == Varaible.ONE_PAIR) {
				if (!is_all_in) {
					doAction("check");
					return;
				} else {
					doAction("fold");
					return;
				}
			} else {
				if (numOfLeftPlayer == 2 && !is_all_in
						&& state > Varaible.STRAIGHT_T) {
					doAction("check");// all_in
					return;
				}
				doAction("fold");
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
		String ds[] = duishou.split(" ");
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

	/**
	 * when only has hand cards
	 * 
	 * @param list
	 * @return
	 */
	public String judgetwo(Card[] list) {
		if (list.length != 2) {
			// System.out.println("not 2");
			return null;
		}
		if (list[0].point == list[1].point)
			return Varaible.pair;
		if (list[0].color.equals(list[1].color))
			return Varaible.tonghua;
		if (list[0].point - list[1].point == 1
				|| list[0].point - list[1].point == -1)
			return Varaible.lianz;
		return "zapai";
	}

	/**
	 * judge cards in all
	 * 
	 * @param list
	 * @return
	 */
	public int judege(Card[] list) {
		Short(list);
		int numofpairs = 0;
		int maxsamepoint = 0, maxsamecolor = 0;
		int lessamepoint = 0;
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
				maxsamecolor = tmaxcolor;
			}
		}
		numOfStragiht = numOfStragiht(list);
		if (maxsamecolor >= 5 & numOfStragiht >= 5)
			return Varaible.STRAIGHT_FLUSH;
		if (maxsamepoint == 4)
			return Varaible.FOUR_OF_A_KIND;
		if (maxsamepoint == 3 & lessamepoint == 2)
			return Varaible.FULL_HOUSE;
		if (maxsamecolor >= 5)
			return Varaible.FLUSH;
		if (numOfStragiht >= 5)
			return Varaible.STRAIGHT;
		if (maxsamepoint == 3)
			return Varaible.THREE_OF_A_KIND;
		if (numofpairs >= 2)
			return Varaible.TWO_PAIR;
		if (numofpairs == 1)
			return Varaible.ONE_PAIR;
		if (numOfStragiht == 4)
			return Varaible.STRAIGHT_T;
		if (maxsamecolor == 4)
			return Varaible.FLUSH_T;
		return 0;

	}

	/**
	 * sort cards in all
	 * 
	 * @param list
	 */
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
	}

	/**
	 * shun zi
	 * 
	 * @param list
	 * @return
	 */
	public int numOfStragiht(Card[] list) {
		int s = 1, t = 1;
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
	 * @param cardString
	 */
	Card(String cardString) {
		String str[] = cardString.split(" ");
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

/**
 * @todo define varaibles
 * @author LD
 *
 */
class Varaible {
	public static final String pair = "pair";
	public static final String tonghua = "tonghua";
	public static final String lianz = "lianz";
	public static final String zapai = "zapai";
	/** tong hua shun **/
	public static final int STRAIGHT_FLUSH = 10;
	/** 4 tiao **/
	public static final int FOUR_OF_A_KIND = 9;
	/** hu lu **/
	public static final int FULL_HOUSE = 8;
	/** tong hua **/
	public static final int FLUSH = 7;
	/** shun zi **/
	public static final int STRAIGHT = 6;
	/** 3 tiao **/
	public static final int THREE_OF_A_KIND = 5;
	/** 2 dui **/
	public static final int TWO_PAIR = 4;
	/** 1 dui **/
	public static final int ONE_PAIR = 3;
	/** ting tong hua **/
	public static final int FLUSH_T = 2;
	/** ting shun zi **/
	public static final int STRAIGHT_T = 1;
}
