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
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.meta.EAVAttribute;
import nl.npcf.eav.meta.EAVDataType;
import nl.npcf.eav.meta.EAVPath;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.Map;

/**
 * This class uses jodatime to handle timestamp fields
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("timestamp")
public class TimestampField extends AbstractFreemarkerField {

    @XStreamOmitField
    private DateTimeFormatter dateTimeFormatter;

    String pattern;

    private DateTimeFormatter getFormatter() {
        if (dateTimeFormatter == null) {
            if (pattern == null) {
                pattern = "dd/MM/yyyy";
            }
            dateTimeFormatter = DateTimeFormat.forPattern(pattern);
        }
        return dateTimeFormatter;
    }

    public boolean supports(EAVDataType dataType) {
        switch (dataType) {
            case DATETIME: return true;
        }
        return false;
    }

    public Object stringToValue(EAVAttribute attribute, String value) throws EAVValidationException {
        try {
            return new Date(getFormatter().parseMillis(value));
        }
        catch (RuntimeException e) {
            throw EAVValidationException.badDateFormat(attribute, value, getFormatter().print(System.currentTimeMillis()));
        }
    }

    public void toFormField(EAVPath path, Object value, StringBuilder out) {
        Date date = (Date)value;
        out.append("<input");
        out.append(" name=\"").append(path).append('"');
        out.append(" id=\"").append(path).append('"');
        if (value != null) {
            out.append(" value=\"").append(getFormatter().print(date.getTime())).append('"');
        }
        out.append(" size=\"20\" type=\"text\" class=\"textInput eavTimestampField\"/>");
    }

    public String valueToString(Object value) {
        if (value == null) {
            return null;
        }
        Date date = (Date)value;
        return getFormatter().print(date.getTime());
    }

    protected void addToModel(Map<String, Object> model) {
    }
}
