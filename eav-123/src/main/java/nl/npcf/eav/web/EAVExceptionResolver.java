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

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.npcf.eav.xstream.EAVResponse;
import nl.npcf.eav.xstream.EAVXStream;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EAVExceptionResolver implements HandlerExceptionResolver {
    private Logger logger = Logger.getLogger(getClass());

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/edit")) {
            try {
                PrintWriter out = response.getWriter();
                out.println("<html><head><title>Exception</title></head><body>");
                out.println("<pre>");
                out.println(EAVResponse.getStackTrace(e));
                out.println("</pre>");
                out.println("</body>");
            }
            catch (IOException e1) {
                logger.fatal("Can't report exception");
            }
        }
        else {
            response.setContentType("text/xml");
            try {
                new EAVXStream().toXML(EAVResponse.create(e), response.getOutputStream());
            }
            catch (IOException e1) {
                logger.fatal("Cannot respond with exception",e);
            }
        }
        return null;
    }
    
}
