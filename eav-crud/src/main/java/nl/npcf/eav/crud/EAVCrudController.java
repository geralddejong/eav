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

/**
 *
 */
package nl.npcf.eav.crud;

import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller handling all message portlet actions.
 */

@Controller
@RequestMapping("VIEW")
@SessionAttributes("command")
public class EAVCrudController {
    private static final String COMMAND = "command";
    private Logger log = Logger.getLogger(getClass());

    @RequestMapping
    public String render(final Model model) {
        if (!model.containsAttribute(COMMAND)) {
            Command command = new Command();
            command.setBsn("initial");
            model.addAttribute(COMMAND, command);
            log.info("Render: new command object");
        }
        else {
            log.info("Render: existing command object");
        }
        return "eav-crud";
    }

    @RequestMapping(params = "action=fetch")
    public void recordBSN(@ModelAttribute(COMMAND) final Command command) {
        System.out.println("Command="+command);
        log.info("Command="+command);
    }
}
