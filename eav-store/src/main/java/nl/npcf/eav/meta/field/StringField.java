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
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.meta.EAVAttribute;
import nl.npcf.eav.meta.EAVDataType;

import java.util.Map;

/**
 * This class handles general string fields, with max length and regex support
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("string")
public class StringField extends AbstractFreemarkerField {

    @XStreamAsAttribute
    Integer maximumLength;

    @XStreamAsAttribute
    Integer rows;

    @XStreamAsAttribute
    String regularExpression;

    public boolean supports(EAVDataType dataType) {
        return true;
    }

    public Object stringToValue(EAVAttribute attribute, String string) throws EAVValidationException {
        if (maximumLength != null && string.length() > maximumLength) {
            throw EAVValidationException.exceedsMaximumLength(attribute, maximumLength, string);
        }
        if (regularExpression != null) {
            if (!string.matches(regularExpression)) {
                throw EAVValidationException.mismatchRegularExpression(attribute, regularExpression, string);
            }
        }
        return string;
    }


    public String valueToString(Object value) {
        return (String) value;
    }

    protected void addToModel(Map<String, Object> model) {
        model.put("maximumLength", maximumLength);
        model.put("regularExpression", regularExpression);
        model.put("rows", rows);
    }
}
