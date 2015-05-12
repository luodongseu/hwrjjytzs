package com.twomonk.hw.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 华为软件精英挑战赛 language java team Twomonk
 * 
 * @author LD
 *
 */
public class Game {

	/**
	 * 牌 实体
	 * 
	 * @author LD
	 */
	class Card {
		String color;
		String point;

		/**
		 * 构造函数
		 * 
		 * @param reg
		 */
		Card(String reg) {// 将color point转化为Card类
			String str[] = reg.split(" ");
			if (str.equals("") || str == null || str.length < 2)
				return;
			this.color = str[0];
			this.point = str[1];
		}
	}

	static String eol = " eol ";

	/**
	 * reg-msg (1) 玩家注册
	 * 
	 * @param pid
	 * @param pname
	 */
	void register(String pid, String pname) {
		String msg = "reg: ";
		msg += pid;
		msg += " ";
		msg += pname;
		// 发送注册消息
		sendMessage(msg);
	}

	/**
	 * seat-info-msg (2)获得座次消息
	 * 
	 * seat/ eol button: pid jetton money eol small blind: pid jetton money eol
	 * (big blind: pid jetton money eol)0-1 (pid jetton money eol)0-5 /seat eol
	 */
	void getSeatInfo(String info) {
		// 获得消息
		info.replaceAll("/seat eol | eol /seat eol", "");
		String[] infos = info.split(eol);
		if (infos.length >= 2) {
			// 获取庄家
			if (infos[0].contains("button")) {
				String button = infos[1].split(": ")[1];
			}
			// 获取小盲注
			if (infos[1].contains("small blind")) {
				String sblind = infos[2].split(": ")[1];

			}
			// 如果有大盲注
			if (infos.length >= 3) {
				String bblind = infos[2].split(": ")[1];

			}
			// 如果有其他
			if (infos.length >= 4) {
				String other[][] = new String[infos.length - 3][3];
				for (int i = 3; i < infos.length; i++) {
					other[i] = infos[i].split(": ")[1].split(" ");
				}
			}
		}
	}

	/**
	 * game-over-msg (3)游戏结束
	 * 
	 */
	void gameOver() {
		// reset();
	}

	/**
	 * blind-msg (4)盲注信息
	 * 
	 * blind/ eol (pid: bet eol)1-2 /blind eol
	 * 
	 * @param info
	 */
	void getBlind(String info) {
		info = info.replaceAll("/blind eol | eol blind/ eol ", "");
		String infos[] = info.split(eol);
		if (infos.length == 1) {// 只有小盲注
			String sblind = infos[0].split(": ")[1];
		} else if (infos.length == 2) {// 大小盲注都有
			String b1 = infos[0].split(": ")[1];
			String b2 = infos[1].split(": ")[1];
		}
	}

	/**
	 * inquire-msg (5)询问消息
	 * 
	 * inquire/ eol (pid jetton money bet blind | check | call | raise | all_in
	 * | fold eol)1-8 total pot: num eol /inquire eol
	 * 
	 * @param info
	 */
	void getInquire(String info) {

	}

	/**
	 * action-msg (6)行动消息
	 * 
	 * check | call | raise num | all_in | fold eol
	 */
	void doAction() {
		String msg = "";
		msg = "check";
		// 发送行动消息
		this.sendMessage(msg);
	}

	/**
	 * flop-msg (7)公共牌消息
	 * 
	 * flop/ eol color point eol color point eol color point eol /flop eol
	 * 
	 * @param info
	 */
	void getFlop(String info) {
		info = info.replaceAll("flop/ eol | eol /flop eol	", "");
		String cds[] = info.split(eol);
		Card cards[] = new Card[3];
		for (int i = 0; i < 3; i++) {// 三张牌
			cards[i] = new Card(cds[i]);
		}
	}

	/**
	 * turn-msg (8)转牌消息
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
	 * river-msg (9)河牌消息
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
	 * showdown-msg (10)摊牌
	 * 
	 * showdown/ eol common/ eol color point eol /common eol rank: pid color
	 * point color point nut_hand eol /showdown eol
	 * 
	 * @param info
	 */
	void showCards(String info) {
		info = info.replaceAll("showdown/ eol | eol /showdown eol", "");
		info = info.replaceAll("common/ eol |/common eol ", "");
		String infos[] = info.split(eol);// 剩下的包括5张公共牌和所有玩家排名
		Card cCards[] = new Card[5];// 保存5张公牌
		for (int i = 0; i < 5; i++) {// 前面5张是公共牌
			cCards[i] = new Card(infos[i]);
		}
		for (int j = 5; j < infos.length; j++) {// 剩下的是排名
			/** 这里处理排名信息 **/
		}
	}

	/**
	 * pot-win-msg (11)彩池分配
	 * 
	 * pot-win/ eol (pid: num eol)0-8 /pot-win eol
	 * 
	 * @param info
	 */
	void getPotWin(String info) {
		info = info.replaceAll("pot-win/ eol | eol /pot-win eol", "");
		String infos[] = info.split(eol);// 彩池数组
		if (infos == null || infos.equals("")) {
			return;
		} else {
			for (String ifo : infos) {
				String ifs[] = ifo.split(":");// ifs[0]==pid,ifs[1]==num
			}
		}
	}

	/**
	 * hold-cards-msg (5)获得手牌
	 * 
	 * hold/ eol color point eol color point eol /hold eol
	 * 
	 * @param info
	 */
	void getHandCards(String info) {
		info = info.replaceAll("hold/ eol | eol /hold eol", "");
		String cards[] = info.split(eol);
		Card hCards[] = new Card[2];
		hCards[0] = new Card(cards[0]);// 手牌1
		hCards[1] = new Card(cards[1]);// 手牌2
	}

	/**
	 * 发送数据到服务器
	 * 
	 * @param msg
	 */
	void sendMessage(String msg) {

	}

	/**
	 * 从服务器获取数据
	 * 
	 * @return
	 */
	String getMessage() {
		return "";
	}

	/**
	 * 测试Socket获取数据&发送数据
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	void test() throws UnknownHostException, IOException {
		Socket socket = new Socket("127.0.0.1", 1234);
		OutputStream out = socket.getOutputStream();
		DataOutputStream dataOut = new DataOutputStream(out);
		dataOut.writeUTF("hello!");

		InputStream in = socket.getInputStream();
		DataInputStream dataIn = new DataInputStream(in);
		String s = dataIn.readUTF();
		System.out.println(s);

		in.close();
		out.close();
		socket.close();
	}

	/**
	 * 入口函数main
	 * 
	 * @param args
	 *            [5] args[0]牌桌程序IP args[1]牌桌程序端口好 args[2]牌手程序绑定的IP
	 *            args[3]牌手程序绑定的端口号 args[4]牌手的ID
	 */
	public static void main(String[] args) {
		// if (args.equals("") || args == null || args.length < 5) {// 参数错误则退出
		// System.out.println("args error!");
		// Game game = new Game();
		// try {
		// game.test();
		// } catch (UnknownHostException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// } else {// 参数正确则进入游戏
		// // //////////////////////////游戏代码区/////////////////////////////
		// Game game = new Game();
		// try {
		// game.test();
		// } catch (UnknownHostException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		String msg = "seat/ eol button: pid jetton money eol small blind: pid jetton money eol (big blind: pid jetton money eol)0-1 (pid jetton money eol)0-5 /seat eol";
		msg = msg.replaceAll("seat/ eol | /seat eol", "");
		System.out.println(msg);

	}
}
