package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TicTacToeServer {

	private static List<PrintWriter> clientOutputStreams = new ArrayList<PrintWriter>();
	private static Game game = new Game();

	public static void main(String[] args) {		
		new TicTacToeServer().startServer(8000);
	}

	private void startServer(int port) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Http Server started on port " + port);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				new RequestHandler(clientSocket).start();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			closeResource(serverSocket);
		}
	}

	private void closeResource(ServerSocket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static class RequestHandler extends Thread {

		Socket socket;
		BufferedReader reader;
		PrintWriter response;
		private char id;

		public RequestHandler(Socket clientSocket) {
			// client = user;
			try {
				socket = clientSocket;
				InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
				reader = new BufferedReader(isReader);
				response = new PrintWriter(socket.getOutputStream());
				clientOutputStreams.add(response);
				setClientId();
				sendIntro();
			} catch (Exception ex) {
			}
		}

		private void setClientId() {
			if (clientOutputStreams.size() == 1) {
				this.id = Game.CIRCLE;
			} else {
				this.id = Game.CROSS;
			}
		}

		private void sendIntro() {
			response.println("**** Bem vindo ao jogo da velha! **** ");
			response.println(game.getBoard());
			response.println("Voce joga como: " + id);
			response.println("Esperando jogada do jogador " + Game.CIRCLE);
			response.flush();
		}

		private void writeToClient(PrintWriter response, String data) {
			response.println(data);
			response.flush();
		}

		private void writeToClient(String data) {
			response.println(data);
			response.flush();
		}

		@Override
		public void run() {
			String message = null;
			try {
				while ((message = reader.readLine()) != null) {
					int markedPosition = -1;
					try {
						markedPosition = Integer.parseInt(message);
						String result = game.makeMove(id, markedPosition);
						if (result.startsWith("~~")) {
							writeToClient(result);
						} else {
							showGame(result);
						}
					} catch (Exception ex) {
						writeToClient("Entrada invalida! ");
					}
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		private void showGame(String data) {
			for (PrintWriter client : clientOutputStreams) {
				writeToClient(client, data);
				writeToClient(client, "Voce joga como: " + id);
				writeToClient(client, "Esperando jogada do jogador " + getNextMove());
			}
		}

		private char getNextMove() {
			if (id == Game.CIRCLE) {
				return Game.CROSS;
			}
			return Game.CIRCLE;
		}

	}

}
