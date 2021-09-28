
/*******************************************************************************
 * Copyright 2021 Mauricio Andrada
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private Proxy obcToQgc;
	private Proxy qgcToObc;
	private Socket qgcSocket;
	private Socket obcSocket;

	private String logSelection = "";
	private Object lock = new Object();

	public static void main(String[] args) {

		Server server = new Server();

		if (args.length > 0)
			server.logSelection = args[0];

		server.execute();
	}

	private void execute() {

		while (true) {

			obcToQgc = new Proxy();
			qgcToObc = new Proxy();

			(new ServerThread(0)).start();
			(new ServerThread(1)).start();

			synchronized (lock) {

				try {

					lock.wait();

				} catch (InterruptedException e) {

				}
			}

			obcToQgc.resetLock();
			qgcToObc.resetLock();

			if (qgcSocket != null) {

				try {
					qgcSocket.close();
				} catch (IOException e) {

				}
			}

			if (obcSocket != null) {

				try {
					obcSocket.close();
				} catch (IOException e) {

				}
			}
		}
	}

	private class ServerThread extends Thread {

		private int direction;

		public ServerThread(int direction) {
			super();
			this.direction = direction;
		}

		public void run() {

			if (direction == 0) {

				System.out.println("QGC started");

			} else {

				System.out.println("OBC started");

			}

			while (true) {

				try {

					ServerSocket ss = null;

					if (direction == 0) {

						ss = new ServerSocket(54321);
						qgcSocket = ss.accept();

					} else {

						ss = new ServerSocket(51001);
						obcSocket = ss.accept();

					}

					ss.close();

					if (direction == 0) {

						System.out.println(
								"QGC connected: IP = " + qgcSocket.getInetAddress() + " port = " + qgcSocket.getPort());

					} else {

						System.out.println("On-board computer connected: IP = " + obcSocket.getInetAddress()
								+ " port = " + obcSocket.getPort());
					}

					if (logSelection.equals("qgc"))
						qgcToObc.setLog("QGC to OBC: ", true);

					if (logSelection.equals("obc"))
						obcToQgc.setLog("OBC to QGC: ", true);

					qgcToObc.setLock(lock);
					obcToQgc.setLock(lock);

					if (direction == 0) {

						qgcToObc.setFrom(qgcSocket);
						obcToQgc.setTo(qgcSocket);

					} else {

						qgcToObc.setTo(obcSocket);
						obcToQgc.setFrom(obcSocket);
					}

				} catch (IOException e) {

				}
			}
		}
	}
}
