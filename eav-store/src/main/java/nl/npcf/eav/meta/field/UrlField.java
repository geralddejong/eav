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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * This class manages fields that hold a URL
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("url")
public class UrlField extends AbstractFreemarkerField {

    @XStreamAsAttribute
    Integer maximumLength;

    public boolean supports(EAVDataType dataType) {
        switch (dataType) {
            case STRING: return true;
        }
        return false;
    }

    public Object stringToValue(EAVAttribute attribute, String value) throws EAVValidationException {
        if (maximumLength != null && value.length() > maximumLength) {
            throw EAVValidationException.exceedsMaximumLength(attribute, maximumLength, value);
        }
        try {
            new URL(value);
            return value;
        }
        catch (MalformedURLException e) {
            throw EAVValidationException.badUrl(attribute, value);
        }
    }

    public String valueToString(Object value) {
        return (String)value;
    }

    protected void addToModel(Map<String, Object> model) {
        model.put("maximumLength", maximumLength);
    }
}
