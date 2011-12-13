/*
 * Copyright [2009] [Gerald de Jong]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.npcf.eav.web;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Bootstrap the jetty server
 * @author Gerald de Jong <geralddejong@gmail.com>
 *
 */
public class JettyStarter {
	public static void main(String... args) throws Exception {
		WebAppContext webAppContext = new WebAppContext("eav-123/src/main/webapp", "/eav-123");
		Server server = new Server(8080);
		server.setHandler(webAppContext);
		server.start();
	}
}
