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
public class Logger {
	
	void log(String comment, byte[] bytes, int size) {
		
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < size; i++) {
		

			sb.append(Byte.toString(bytes[i])).append(" ");
		}

		System.out.println(comment + " = " + sb.toString());
		System.out.println("size = "+size);
	}

}
