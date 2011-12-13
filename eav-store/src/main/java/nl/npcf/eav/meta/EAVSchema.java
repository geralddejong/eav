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

package nl.npcf.eav.meta;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import nl.npcf.eav.EAVStore;
import nl.npcf.eav.data.EAVEntity;
import nl.npcf.eav.data.EAVValue;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVPathException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Schema is the collection of metadata describing the contents of an EAV database.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("schema")
public class EAVSchema implements EAVStore.Schema {

    @XStreamAsAttribute
    String name;

    String description;

    List<EAVAttribute> templates;

    List<EAVAttribute> attributes;

    @XStreamOmitField
    List<EAVAttribute> requiredAttributes;

    @XStreamOmitField
    List<EAVAttribute> requiredFieldAttributes;

    @XStreamOmitField
    EAVAttribute root;

    public EAVAttribute getEAVAttribute(EAVPath path, boolean validate) throws EAVPathException {
        if (path.getElements().isEmpty()) {
            return getRootAttribute();
        }
        else {
            return getRootAttribute().getAttribute(path, 0, validate);
        }
    }

    public EAVStore.Path createPath(String pathString, boolean complete) throws EAVPathException {
        EAVPath path = new EAVPath(pathString);
        if (!path.isEmpty()) {
            path.setComplete(getRootAttribute().indexingComplete(path, 0));
        }
        if (complete && !path.isComplete()) {
            throw new EAVPathException(pathString, "Incomplete");
        }
        return path;
    }

    public EAVStore.Attribute getAttribute(EAVStore.Path path, boolean validate) {
        try {
            return getEAVAttribute((EAVPath) path, validate);
        }
        catch (EAVPathException e) {
            throw new RuntimeException("Expected path to be correct already", e);
        }
    }

    public List<EAVAttribute> getRequiredAttributes() {
        return requiredAttributes;
    }

    public void initialize() throws EAVException {
        Map<String, EAVAttribute> templateMap = new TreeMap<String, EAVAttribute>();
        if (templates != null) {
            for (EAVAttribute template : templates) {
                if (templateMap.containsKey(template.name)) {
                    throw new EAVException("Duplicate template name: " + template.name);
                }
                templateMap.put(template.name, template);
            }
        }
        for (EAVAttribute attribute : attributes) {
            attribute.initialize(null, templateMap);
        }
        requiredAttributes = new ArrayList<EAVAttribute>();
        requiredFieldAttributes = new ArrayList<EAVAttribute>();
        for (EAVAttribute attribute : attributes) {
            attribute.getRequired(requiredAttributes, requiredFieldAttributes);
        }
    }

    public synchronized EAVAttribute getRootAttribute() {
        if (root == null) {
            root = new EAVAttribute();
            root.attributes = attributes;
            root.prompt = description;
            root.required = true;
            root.multiple = false;
            root.path = new EAVPath();
        }
        return root;
    }

    public String toHtml(String baseUrl, EAVStore.Path pathInterface) throws EAVException {
        String action = baseUrl;
        if (!pathInterface.isEmpty()) {
            action += "/" + pathInterface;
        }
        EAVPath path = (EAVPath) pathInterface;
        StringBuilder out = new StringBuilder();
        out.append("<div class=\"eav123\">\n");
        out.append("<div class=\"eavLinks\">\n");
        out.append(path.toLinks(baseUrl, "find", null));
        out.append("\n</div>\n");
        out.append("<div class=\"eavForm\">\n");
        out.append("<form method=\"POST\" action=\"").append(action).append("\" class=\"uniForm\">\n"); //  enctype=\"multipart/form-data\">\n");
        out.append("<fieldset>\n");
        getRootAttribute().toSearchFields(baseUrl, path, out);
        out.append("<div class=\"buttonHolder\">\n");
        out.append("<button type=\"submit\" class=\"submitButton\">Submit</button>\n");
        out.append("</div>\n");
        out.append("</fieldset>\n");
        out.append("</form>\n");
        out.append("</div>\n");
        out.append("</div>\n");
        return out.toString();
    }

    public String toHtml(EAVEntity entity, String baseUrl, EAVPath selectionPath) throws EAVException {
        String action = baseUrl + EAVStore.PATH_SEPARATOR + entity.getKey();
        if (!selectionPath.isEmpty()) {
            action += EAVStore.PATH_SEPARATOR + selectionPath;
        }
        StringBuilder out = new StringBuilder();
        out.append("<div class=\"eav123\">\n");
        if (!entity.isTransient()) {
            out.append("<div class=\"eavLinks\">\n");
            out.append(selectionPath.toLinks(baseUrl, "edit", entity.getKey()));
            out.append("<div class=\"eavDelete\">\n");
            out.append("\n<a href=\"").append(action).append("?DELETE_THIS=true\">").append("(delete)").append("</a>\n");
            out.append("</div>\n");
            out.append("</div>\n");
        }
        out.append("<div class=\"eavForm\">\n");
        out.append("<form method=\"POST\" action=\"").append(action).append("\" class=\"uniForm\">\n"); //  enctype=\"multipart/form-data\">\n");
        out.append("<fieldset>\n");
        getRootAttribute().toFormFields(entity, baseUrl, selectionPath, selectionPath, out, false);
        out.append("<div class=\"buttonHolder\">\n");
        out.append("<button type=\"submit\" class=\"submitButton\">Submit</button>\n");
        out.append("</div>\n");
        out.append("</fieldset>\n");
        out.append("</form>\n");
        out.append("</div>\n");
        out.append("</div>\n");
        return out.toString();
    }

    public String toTableHeaders(EAVStore.Attribute attribute) throws EAVException {
        StringBuilder out = new StringBuilder();
        out.append("<tr>\n");
        out.append("<th>-</th>\n");
        out.append("<th>").append(attribute.getPrompt()).append("</th>\n");
        for (EAVAttribute required : requiredFieldAttributes) {
            if (required == attribute) continue;
            out.append("<th>").append(required.prompt).append("</th>\n");
        }
        out.append("</tr>\n");
        return out.toString();
    }

    public String toTableRow(String baseUrl, EAVValue value) throws EAVException {
        EAVAttribute attribute = (EAVAttribute)value.getAttribute();
        String valueString = attribute.fieldType.valueToString(value.getValue());
        EAVEntity entity = value.getEntity();
        String keyHyperlink = "<a href=\""+baseUrl+EAVStore.PATH_SEPARATOR+entity.getKey()+"\">"+entity.getKey()+"</a>";
        StringBuilder out = new StringBuilder();
        out.append("<tr>\n");
        out.append("<td>").append(keyHyperlink).append("</td>\n");
        out.append("<td>").append(valueString).append("</td>\n");
        for (EAVAttribute required : requiredFieldAttributes) {
            if (required == attribute) continue;
            out.append("<td>");
            List<EAVValue> requiredValues = entity.getValues(required.path, false);
            boolean lineBreak = false;
            for (EAVValue requiredValue : requiredValues) {
                if (lineBreak) {
                    out.append("<br/>");
                }
                String requiredValueString = attribute.fieldType.valueToString(requiredValue.getValue());
                out.append(requiredValueString);
                lineBreak = true;
            }
            out.append("</td>\n");
        }
        out.append("</tr>\n");
        return out.toString();
    }

}
