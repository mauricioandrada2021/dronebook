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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;

public class ProxyOBC extends Thread {

	private SerialPort serialPort;
	private Socket sock;
	private String hostname;
	private int verbose;

	private Object lock = new Object();

	public ProxyOBC(String hostname, int verbose) {
		super();
		this.hostname = hostname;
		this.verbose = verbose;
	}

	public void run() {

		if (hostname == null)
			hostname = "localhost";

		while (true) {
			
			if (verbose > 0)
				System.out.println("Connecting to server..." + hostname);

			while (true) {

				try {

					sock = new Socket(hostname, 51001);
					break;

				} catch (Exception e) {
					if (verbose > 0)
						System.out.println("Failed to connect to server " + hostname
								+ "... Retrying in 1 second...Check if server is running...");
				}

				synchronized (this) {

					try {
						wait(10);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (verbose > 0) {
				System.out.println("Success connecting to proxy server...");
				System.out.println("Connecting to serial port...");

			}

			try {

				if (serialPort == null)
					serialPort = initSerialPort();

			} catch (SerialPortInvalidPortException e) {
				e.printStackTrace();
			}

			if (serialPort != null && serialPort.openPort()) {

				if (verbose > 0) {
					System.out.println("Success connecting to serial port...");
				}

				(new SerialToServer()).start();
				(new ServerToSerial()).start();

				synchronized (lock) {

					try {
						lock.wait();
					} catch (InterruptedException e) {

					}
				}
			} else {

				System.out.println("Failed to connect to the serial port.");

				if (sock != null) {
					try {
						sock.close();
					} catch (IOException e) {

					}
				}
				break;
			}
		}
	}

	private class SerialToServer extends Thread {

		public void run() {

			try {

				BufferedOutputStream bos = new BufferedOutputStream(sock.getOutputStream());
				byte[] buffer = new byte[200];

				while (true) {

					int size = serialPort.readBytes(buffer, buffer.length);

					if (size > 0) {

						if (verbose == 2)
							System.out.println(
									"data from serial: " + Arrays.toString(Arrays.copyOfRange(buffer, 0, size - 1)));

						bos.write(buffer, 0, size);
						bos.flush();

					}

					synchronized (this) {

						try {
							wait(10);
						} catch (InterruptedException e) {

							e.printStackTrace();
						}
					}
				}

			} catch (IOException e) {

				System.out.println("Lost connection to server...");
				if (sock != null) {
					try {
						sock.close();
					} catch (IOException ex) {

					}
				}
				
				synchronized (lock) {
					
					lock.notifyAll();

				}
			}
		}
	}

	private class ServerToSerial extends Thread {

		public void run() {

			try {

				BufferedInputStream bis = new BufferedInputStream(sock.getInputStream());

				byte[] buffer = new byte[200];

				while (true) {

					int size = bis.read(buffer);

					if (size > 0) {

						if (verbose == 3)
							System.out.println(
									"data from server: " + Arrays.toString(Arrays.copyOfRange(buffer, 0, size - 1)));

						serialPort.writeBytes(buffer, size);

					}

					synchronized (this) {

						try {
							wait(10);
						} catch (InterruptedException e) {

							e.printStackTrace();
						}
					}
				}

			} catch (IOException e) {
				
				synchronized (lock) {
					
					lock.notifyAll();

				}
			}
		}
	}

	private SerialPort initSerialPort() throws SerialPortInvalidPortException {

		SerialPort serialPort = null;

		serialPort = SerialPort.getCommPort("/dev/ttyS0");

		if (serialPort != null) {

			serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
			serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, SerialPort.TIMEOUT_NONBLOCKING,
					SerialPort.TIMEOUT_NONBLOCKING);
		}

		return serialPort;
	}
}
