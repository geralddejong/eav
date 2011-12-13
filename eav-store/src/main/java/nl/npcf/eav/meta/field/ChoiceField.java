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

package nl.npcf.eav.meta.field;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.meta.EAVAttribute;
import nl.npcf.eav.meta.EAVDataType;

import java.util.List;
import java.util.Map;

/**
 * This class handles selections from a number of options
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("choice")
public class ChoiceField extends AbstractFreemarkerField {

    @XStreamAsAttribute
    @XStreamConverter(EnumConverter.class)
    Style style;

    @XStreamImplicit(itemFieldName = "option")
    List<String> options;

    public boolean supports(EAVDataType dataType) {
        switch (dataType) {
            case STRING: return true;
        }
        return false;
    }

    public Object stringToValue(EAVAttribute attribute, String value) throws EAVValidationException {
        if (!options.contains(value)) {
            throw EAVValidationException.illegalStringValue(attribute, value, "Must be one of "+optionsString());
        }
        return value;
    }

    private String optionsString() {
        StringBuilder out = new StringBuilder("[");
        int count = options.size();
        for (String option : options) {
            out.append(option);
            if (--count > 0) {
                out.append(", ");
            }
        }
        out.append("]");
        return out.toString();
    }

    public String valueToString(Object value) {
        return (String)value;
    }

    protected void addToModel(Map<String, Object> model) {
        if (style == null) {
            style = Style.SELECT; // default
        }
        model.put("style", style.toString());
        model.put("options", options);
    }

    public enum Style {
        SELECT,
        RADIO
    }
}
