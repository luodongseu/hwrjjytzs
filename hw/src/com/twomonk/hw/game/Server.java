package com.twomonk.hw.game;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 测试的服务器
 * 
 * @author LD
 *
 */
public class Server {
	/**
	 * 开启服务
	 * 
	 * @throws IOException
	 */
	void start() throws IOException {
		@SuppressWarnings("resource")
		ServerSocket ss = new ServerSocket(1234);
		Socket socket = ss.accept();
		InputStream in = socket.getInputStream();
		DataInputStream dataIn = new DataInputStream(in);
		String name = dataIn.readUTF();
		System.out.println("get:" + name);
		
		in.close();
		socket.close();
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
