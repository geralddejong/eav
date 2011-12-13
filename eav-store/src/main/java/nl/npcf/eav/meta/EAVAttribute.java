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
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
import nl.npcf.eav.EAVStore;
import nl.npcf.eav.data.EAVEntity;
import nl.npcf.eav.data.EAVValue;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVPathException;
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.xstream.EAVXStream;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * The Attribute is the main unit of metadata, describing the contents of the value data.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("attribute")
public class EAVAttribute implements EAVStore.Attribute {

    @XStreamAsAttribute
    String name;

    String prompt;

    String hint;

    String helpUrl;

    @XStreamAsAttribute
    boolean required;

    @XStreamAsAttribute
    boolean multiple;

    @XStreamAsAttribute
    @XStreamConverter(EnumConverter.class)
    EAVDataType dataType;

    @XStreamAsAttribute
    String templateName;

    @XStreamAlias("field")
    EAVFieldType fieldType;

    @XStreamImplicit
    List<EAVAttribute> attributes;

    @XStreamOmitField
    EAVPath path;

    @XStreamOmitField
    EAVAttribute parent;

    public EAVStore.Path getPath() {
        return path;
    }

    public EAVPath getEAVPath() {
        return path;
    }

    public EAVAttribute getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getHint() {
        return hint;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public Object stringToValue(String value) throws EAVValidationException {
        if (value == null) {
            throw new RuntimeException("Value is null!");
        }
        Object object = fieldType.stringToValue(this, value);
        dataType.typeCheck(this, object);
        return object;
    }

    public void toSearchFields(String baseUrl, EAVPath selectionPath, StringBuilder out) throws EAVException {
        if (path.contains(selectionPath, false)) {
            String searchBase = baseUrl + EAVStore.PATH_SEPARATOR;
            if (isAggregate()) {
                for (EAVAttribute child : attributes) {
                    out.append("<div class=\"ctrlHolder fieldOptional\">\n");
                    out.append("<a href=\"").append(searchBase).append(child.path).append("\">").append(child.path.getLastElement()).append("</a><br>\n");
                    out.append("</div>\n");
                }
            }
            else {
                out.append("<div class=\"ctrlHolder fieldOptional").append("\">\n");
                try {
                    out.append(fieldType.toFormField(prompt, hint, helpUrl, path, null));
                }
                catch (Exception e) {
                    throw new EAVException("Unable to render form field", e);
                }
                out.append("</div>\n");
            }
        }
        else if (isAggregate()) {
            for (EAVAttribute child : attributes) {
                child.toSearchFields(baseUrl, selectionPath, out);
            }
        }
    }

    public void toFormFields(EAVEntity entity, String baseUrl, EAVPath pagePath, EAVPath selectionPath, StringBuilder out, boolean onlyAggregates) throws EAVException {
        if (entity.isTransient()) {
            for (EAVAttribute child : attributes) {
                if (child.isRequired()) {
                    if (child.isAggregate()) {
                        if (child.prompt != null) {
                            out.append("<table cellpadding=\"5\" width=\"100%\">\n");
                            out.append("<tr><td><b>\n");
                            out.append(child.prompt);
                            out.append("</b></td></tr>\n");
                            out.append("<tr><td>\n");
                        }
                        child.toFormFields(entity, baseUrl, pagePath, selectionPath.extend(child.name), out, onlyAggregates);
                        if (child.prompt != null) {
                            out.append("</td></tr>\n");
                            out.append("</table>\n");
                        }
                    }
                    else if (!onlyAggregates) {
                        child.toTestedFormField(entity, selectionPath.extend(child.name), out);
                    }
                }
            }
        }
        else if (path.contains(selectionPath, false)) {
            String entityBase = baseUrl + EAVStore.PATH_SEPARATOR + entity.getKey() + EAVStore.PATH_SEPARATOR;
            for (EAVAttribute child : attributes) {
                EAVPath childPath = selectionPath.extend(child.name);
                SortedSet<EAVPath> pathSet = entity.getPathSet(childPath);
                if (child.isMultiple()) {
                    EAVPath newPath = entity.getNewPath(childPath, pathSet);
                    if (child.isAggregate()) {
                        for (EAVPath path : pathSet) {
                            out.append("<div class=\"ctrlHolder fieldExisting\">\n");
                            out.append("<a href=\"").append(entityBase).append(path).append("\">").append(path.after(pagePath)).append("</a><br>\n");
                            out.append("</div>\n");
                            child.toFormFields(entity, baseUrl, pagePath, path, out, true);
                        }
                        out.append("<div class=\"ctrlHolder fieldOptional\">\n");
                        out.append("(<a href=\"").append(entityBase).append(newPath).append("\">").append(newPath.after(pagePath)).append("</a>)\n");
                        out.append("</div>\n");
                    }
                    else if (!onlyAggregates) {
                        for (EAVPath path : pathSet) {
                            child.toExactFormField(entity, path, true, out);
                        }
                        child.toExactFormField(entity, newPath, false, out);
                    }
                }
                else {
                    if (child.isAggregate()) {
                        String cssClass = pathSet.isEmpty() ? "fieldOptional" : "fieldExisting";
                        out.append("<div class=\"ctrlHolder ").append(cssClass).append("\">\n");
                        out.append("<a href=\"").append(entityBase).append(childPath).append("\">").append(childPath.after(pagePath)).append("</a>\n");
                        out.append("</div>\n");
                        child.toFormFields(entity, baseUrl, pagePath, childPath, out, true);
                    }
                    else if (!onlyAggregates) {
                        child.toTestedFormField(entity, selectionPath.extend(child.name), out);
                    }
                }
            }
        }
        else {
            for (EAVAttribute child : attributes) {
                if (child.isAggregate()) {
                    child.toFormFields(entity, baseUrl, pagePath, selectionPath, out, onlyAggregates);
                }
            }
        }
    }

    private void toExactFormField(EAVEntity entity, EAVPath path, boolean existing, StringBuilder out) throws EAVException {
        EAVValue value = (EAVValue) (existing ? entity.getValue(path) : null);
        Object object = (value != null) ? value.getValue() : null;
        String cssClass = "fieldExisting";
        if (!existing) {
            cssClass = isRequired() ? "fieldRequired" : "fieldOptional";
        }
        if (isMultiple()) {
            cssClass += " fieldMultiple";
        }
        out.append("<div class=\"ctrlHolder ").append(cssClass).append("\">\n");
        try {
            out.append(fieldType.toFormField(prompt, hint, helpUrl, path, object));
        }
        catch (Exception e) {
            throw new EAVException("Unable to render form field", e);
        }
        out.append("</div>\n");
    }

    private void toTestedFormField(EAVEntity entity, EAVPath path, StringBuilder out) throws EAVException {
        List<EAVValue> values = entity.getValues(path, false);
        if (values.isEmpty()) {
            toExactFormField(entity, path, false, out);
        }
        else if (values.size() == 1) {
            toExactFormField(entity, values.get(0).getPath(), true, out);
        }
        else {
            throw new EAVException("Multiple values not expected");
        }
    }

    public boolean isAggregate() {
        return dataType == null;
    }

    public String getEntityName() {
        return dataType.getEntityName();
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public List<? extends EAVStore.Attribute> getSubAttributes() {
        return attributes;
    }

    public EAVDataType getDataType() {
        return dataType;
    }

    public List<EAVAttribute> getAttributes() {
        return attributes;
    }

    public EAVValue instantiate(EAVEntity eavEntity, EAVPath path, Object value, Principal createdBy) throws EAVValidationException {
        dataType.typeCheck(this, value);
        return dataType.createValue(eavEntity, path, value, createdBy);
    }

    public String toString() {
        return "Attribute(" + path + ")";
    }

    void initialize(EAVAttribute parent, Map<String, EAVAttribute> templateMap) throws EAVException {
        this.parent = parent;
        if (name == null) {
            throw new EAVException("Attribute name is always required");
        }
        path = new EAVPath((parent == null) ? name : parent.path.toString() + EAVStore.PATH_SEPARATOR + name);
        if (templateName != null) { // inherit sub-attributes from the template
            EAVAttribute template = templateMap.get(templateName);
            if (template == null) {
                throw new EAVException("Template name not found " + templateName);
            }
            EAVAttribute templateClone = (EAVAttribute) EAVXStream.clone(template);
            if (attributes == null) {
                attributes = templateClone.attributes;
            }
            else {
                attributes.addAll(0, templateClone.attributes);
            }
        }
        if (isAggregate()) {
            if (attributes == null) {
                throw new EAVException("Aggregate does not have sub-attributes " + this);
            }
            for (EAVAttribute attribute : attributes) {
                attribute.initialize(this, templateMap);
            }
        }
        else {
            if (fieldType == null) {
                throw new EAVException("Field type is required for " + this);
            }
            if (attributes != null) {
                throw new EAVException("Single field attribute cannot have sub-attributes " + this);
            }
            fieldType.initialize(this, dataType);
        }
    }

    void getRequired(List<EAVAttribute> requiredAttributes, List<EAVAttribute> requiredFieldAttributes) {
        if (required) {
            requiredAttributes.add(this);
            if (!isAggregate()) {
                requiredFieldAttributes.add(this);
            }
        }
        if (attributes != null) {
            for (EAVAttribute attribute : attributes) {
                attribute.getRequired(requiredAttributes, requiredFieldAttributes);
            }
        }
    }

    boolean indexingComplete(EAVPath path, int index) throws EAVPathException {
        EAVPath.Element element = path.getElements().get(index);
        for (EAVAttribute attribute : attributes) {
            if (attribute.name.equals(element.getNode())) {
                boolean isIndexed = path.getElements().get(index).isIndexed();
                if (isIndexed && !attribute.multiple) {
                    throw new EAVPathException(path.toString(), "Path node [" + attribute.name + "] should not be indexed");
                }
                if (attribute.multiple && !isIndexed) {
                    return false;
                }
                if (index < path.getElements().size() - 1) {
                    return attribute.indexingComplete(path, index + 1);
                }
            }
        }
        return true;
    }

    EAVAttribute getAttribute(EAVPath path, int index, boolean validate) throws EAVPathException {
        EAVPath.Element element = path.getElements().get(index);
        for (EAVAttribute attribute : attributes) {
            if (attribute.name.equals(element.getNode())) {
                if (validate) {
                    boolean isIndexed = path.getElements().get(index).isIndexed();
                    if (isIndexed && !attribute.multiple) {
                        throw new EAVPathException(path.toString(), "Path node [" + attribute.name + "] should not be indexed");
                    }
                    if (attribute.multiple && !isIndexed) {
                        throw new EAVPathException(path.toString(), "Path node [" + attribute.name + "] should be indexed");
                    }
                }
                if (index == path.getElements().size() - 1) {
                    return attribute;
                }
                else {
                    return attribute.getAttribute(path, index + 1, validate);
                }
            }
        }
        throw new EAVPathException(path.toString(), "Attribute not found");
    }

}
