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
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import freemarker.template.TemplateException;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.meta.field.ChoiceField;
import nl.npcf.eav.meta.field.IntegerField;
import nl.npcf.eav.meta.field.StringField;
import nl.npcf.eav.meta.field.TimestampField;
import nl.npcf.eav.meta.field.UrlField;

import java.io.IOException;

/**
 * This XStream entity takes care of a conversion/validation, leaning heavily on the contained Type
 * enumeration which points out the class which must be instantiated to do the work.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("field")
public class EAVFieldType {

    UrlField url;
    StringField string;
    IntegerField integer;
    TimestampField timestamp;
    ChoiceField choice;

    @XStreamOmitField
    private Field field;

    public void initialize(EAVAttribute attribute, EAVDataType dataType) throws EAVException {
        setFieldOnce(url);
        setFieldOnce(string);
        setFieldOnce(integer);
        setFieldOnce(timestamp);
        setFieldOnce(choice);
        if (field == null) {
            throw new EAVException("Missing field definition");
        }
        if (!field.supports(dataType)) {
            throw new EAVException("Field "+field+" for "+attribute+" does not support data type "+dataType);
        }
    }

    private void setFieldOnce(Field field) throws EAVException {
        if (field != null) {
            if (this.field != null) {
                throw new EAVException("Multiple field definitions not allowed");
            }
            this.field = field;
        }
    }

    public Object stringToValue(EAVAttribute attribute, String value) throws EAVValidationException {
        return field.stringToValue(attribute, value);
    }

    public String toFormField(String prompt, String hint, String helpUrl, EAVPath path, Object value) throws IOException, TemplateException {
        StringBuilder out = new StringBuilder();
        field.toFormField(prompt, hint, helpUrl, path, value, out);
        return out.toString();
    }

    public String valueToString(Object value) {
        return field.valueToString(value);
    }

    public interface Field {
        boolean supports(EAVDataType dataType);
        Object stringToValue(EAVAttribute attribute, String value) throws EAVValidationException;
        String valueToString(Object value);
        void toFormField(String prompt, String hint, String helpUrl, EAVPath path, Object value, StringBuilder out) throws IOException, TemplateException;
    }
}