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

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.npcf.eav.EAVStore;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVPathException;
import nl.npcf.eav.exception.EAVValidationException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class EAVController {
    private static final String METADATA_PARAMETER = "metadata";
    private static final String DELETE_PARAMETER = "DELETE_THIS";
    private Logger logger = Logger.getLogger(getClass());
    private EAVStore eavStore;

    @Autowired
    public void setEavStore(EAVStore eavStore) throws EAVException {
        this.eavStore = eavStore;
        eavStore.initialize();
    }

    @RequestMapping(value = "/meta", method = RequestMethod.GET)
    public ModelAndView metaGet() throws EAVException, IOException {
        String metadata = "";
        if (eavStore.initialize()) {
            metadata = eavStore.getMetadataXml();
        }
        ModelAndView modelAndView = new ModelAndView("meta");
        modelAndView.addObject("metadata", metadata);
        return modelAndView;
    }

    @RequestMapping(value = "/meta", method = RequestMethod.POST)
    public ModelAndView metaPost(@RequestParam(value = METADATA_PARAMETER) String metadata) throws EAVException, IOException {
        eavStore.setMetadataXml(metadata, getPrincipal());
        ModelAndView modelAndView = new ModelAndView("meta");
        modelAndView.addObject("metadata", metadata);
        return modelAndView;
    }

    @RequestMapping("/find/**")
    public ModelAndView find(HttpServletRequest request, HttpServletResponse response) throws EAVException, IOException {
        if (!eavStore.initialize()) {
            response.sendRedirect(request.getContextPath() + "/service/meta");
            return null;
        }
        EAVStore.Path path = null;
        String error = null;
        List<EAVStore.Value> valueList = null;
        EAVStore.Attribute attribute = null;
        try {
            path = getPath("/find", false, request);
            attribute = eavStore.getSchema().getAttribute(path, false);
            if (!attribute.isAggregate()) {
                Map<EAVStore.Path, String> values = getValues(request);
                switch (values.size()) {
                    case 0:
                        break;
                    case 1:
                        Map.Entry<EAVStore.Path, String> entry = values.entrySet().iterator().next();
                        if (entry.getValue().isEmpty()) {
                            valueList = eavStore.findByAttribute(attribute);
                        }
                        else {
                            Object value = attribute.stringToValue(entry.getValue());
                            valueList = eavStore.findByValue(attribute, value);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        catch (EAVValidationException e) {
            if (e.getValue() == null) {
                error = "Validation problem setting " + e.getAttribute().getPrompt() + ": \"" + e.getMessage() + "\".";
            }
            else {
                error = "Validation problem setting " + e.getAttribute().getPrompt() + "=[" + e.getValue() + "]: \"" + e.getMessage() + "\".";
            }
        }
        catch (EAVPathException e) {
            error = "Path problem using [" + e.getPathString() + "]: \"" + e.getMessage() + "\".";
        }
        catch (Throwable t) {
            logger.error("Unhandled exception", t);
            error = "Internal problem. It has been logged.";
        }
        ModelAndView modelAndView = new ModelAndView("find");
        modelAndView.addObject("schema", eavStore.getSchema());
        modelAndView.addObject("path", path);
        modelAndView.addObject("attribute", attribute);
        modelAndView.addObject("valueList", valueList);
        modelAndView.addObject("baseUrl", request.getContextPath() + "/service/find");
        modelAndView.addObject("baseEditUrl", request.getContextPath() + "/service/edit");
        modelAndView.addObject("error", error);
        return modelAndView;
    }

    @RequestMapping(value = "/edit/**")
    public ModelAndView edit(@RequestParam(value = DELETE_PARAMETER, required = false) String deleteParameter, HttpServletRequest request, HttpServletResponse response) throws IOException, EAVException {
        if (!eavStore.initialize()) {
            response.sendRedirect(request.getContextPath() + "/service/meta");
            return null;
        }
        String baseUrl = request.getContextPath() + "/service/edit";
        EAVStore.Entity entity = null;
        EAVStore.Path path = null;
        String error = null;
        try {
            String key = getFirstPathNode("/edit", request);
            path = getPath("/edit", true, request);
            if (key.isEmpty()) {
                entity = eavStore.createTransientEntity(null);
            }
            else {
                boolean delete = Boolean.parseBoolean(deleteParameter);
                Map<EAVStore.Path, String> values = getValues(request);
                try {
                    entity = eavStore.findByKey(key);
                    if (delete) {
                        if (path.isEmpty()) {
                            eavStore.remove(entity);
                            response.sendRedirect(baseUrl);
                        }
                        else {
                            entity = eavStore.remove(entity, path);
                            response.sendRedirect(baseUrl + EAVStore.PATH_SEPARATOR + key + EAVStore.PATH_SEPARATOR + path.getParent());
                        }
                    }
                    else {
                        entity = eavStore.setValues(key, values, getPrincipal());
                    }
                }
                catch (NoResultException e) {
                    if (delete) {
                        throw e;
                    }
                    entity = eavStore.createTransientEntity(key);
                    for (Map.Entry<EAVStore.Path, String> entry : values.entrySet()) {
                        entity.setTransientValue(entry.getKey(), entry.getValue(), getPrincipal());
                    }
                    eavStore.persist(entity);
                }
            }
        }
        catch (EAVValidationException e) {
            if (e.getValue() == null) {
                error = "Validation problem setting " + e.getAttribute().getPrompt() + ": \"" + e.getMessage() + "\".";
            }
            else {
                error = "Validation problem setting " + e.getAttribute().getPrompt() + "=[" + e.getValue() + "]: \"" + e.getMessage() + "\".";
            }
        }
        catch (EAVPathException e) {
            error = "Path problem using [" + e.getPathString() + "]: \"" + e.getMessage() + "\".";
        }
        catch (Throwable t) {
            logger.error("Unhandled exception", t);
            error = "Internal problem. It has been logged.";
        }
        ModelAndView modelAndView = new ModelAndView("edit");
        modelAndView.addObject("entity", entity);
        modelAndView.addObject("path", path);
        modelAndView.addObject("baseUrl", baseUrl);
        modelAndView.addObject("error", error);
        return modelAndView;
    }

    // ==========

    @SuppressWarnings("unchecked")
    private Map<EAVStore.Path, String> getValues(HttpServletRequest request) throws EAVPathException {
        Map<EAVStore.Path, String> valueMap = new TreeMap<EAVStore.Path, String>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            if (DELETE_PARAMETER.equals(entry.getKey())) {
                continue;
            }
            valueMap.put(eavStore.getSchema().createPath(entry.getKey(), true), entry.getValue()[0].trim());
        }
        return valueMap;
    }

    private String getFirstPathNode(String skip, HttpServletRequest request) {
        int length = skip.length();
        String pathInfo = request.getPathInfo();
        String path = pathInfo.substring(length);
        if (path.startsWith(EAVStore.PATH_SEPARATOR)) {
            path = path.substring(1);
        }
        int slash = path.indexOf(EAVStore.PATH_SEPARATOR);
        if (slash >= 0) {
            path = path.substring(0, slash);
        }
        return path;
    }

    private EAVStore.Path getPath(String skip, boolean skipFirstNode, HttpServletRequest request) throws EAVPathException {
        int length = skip.length();
        String pathInfo = request.getPathInfo();
        String path = pathInfo.substring(length);
        if (path.startsWith(EAVStore.PATH_SEPARATOR)) {
            path = path.substring(1);
        }
        if (skipFirstNode) {
            int slash = path.indexOf(EAVStore.PATH_SEPARATOR);
            if (slash >= 0) {
                path = path.substring(slash + 1);
            }
            else {
                path = "";
            }
        }
        return eavStore.getSchema().createPath(path, false);
    }

    private Principal getPrincipal() {
        return new UserPrincipal((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    private class UserPrincipal implements Principal {
        private User user;

        private UserPrincipal(User user) {
            this.user = user;
        }

        public String getName() {
            return user.getUsername();
        }
    }

//    @RequestMapping(value = "/entity/**", method = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.POST})
//    public void entity(HttpServletRequest request, HttpServletResponse response) throws IOException, EAVException {
//        String key = getFirstPathNode("/entity", request);
//        EAVStore.Path path = getPath("/entity", true, request);
//        EAVStore.Entity entity = null;
//        if (key.isEmpty()) {
//            entity = createTransientEntity(request);
//            eavStore.persist(entity);
//        }
//        else {
//            boolean delete = Boolean.FALSE; // todo: determine from the
//
//            Map<String, String> values = getValues(request);
//            if (values.isEmpty() && delete) {
//                entity = eavStore.findByKey(key);
//                if (!path.isEmpty()) {
//                    EAVStore.Value value = entity.getValue(path);
//                    entity = eavStore.remove(value);
//                    path = eavStore.getSchema().createPath(""); // show the whole entity
//                }
//                else {
//                    eavStore.remove(entity);
//                }
//            }
//            else {
//                for (Map.Entry<String, String> entry : values.entrySet()) {
//                    EAVStore.Path keyPath = eavStore.getSchema().createPath(entry.getKey());
//                    entity = eavStore.setValue(key, keyPath, entry.getValue(), FAKE_PRINCIPAL);
//                }
//                if (entity == null) {
//                    entity = eavStore.findByKey(key);
//                }
//            }
//        }
//        response.setContentType(xstream.getMimeType());
//        xstream.toXML(EAVResponse.create(entity, path), response.getOutputStream());
//    }
}
