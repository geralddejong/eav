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

package nl.npcf.eav.xstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import nl.npcf.eav.EAVStore;
import nl.npcf.eav.data.EAVInteger;
import nl.npcf.eav.data.EAVString;
import nl.npcf.eav.data.EAVText;
import nl.npcf.eav.data.EAVTimestamp;
import nl.npcf.eav.data.EAVValue;

import java.util.Date;

/**
 * This class wraps a value for more careful rendering using XStream
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("value")
public class EAVXValue {

    public EAVXValue() {
    }

    public EAVXValue(EAVStore.Value value) {
        this((EAVValue)value);
    }

    public EAVXValue(EAVValue eavValue) {
        this.path = eavValue.getPath().toString();
        if (eavValue instanceof EAVInteger) {
            this.integer = (Long)eavValue.getValue();
        }
        else if (eavValue instanceof EAVString) {
            this.string = (String)eavValue.getValue();
        }
        else if (eavValue instanceof EAVText) {
            this.text = (String)eavValue.getValue();
        }
        else if (eavValue instanceof EAVTimestamp) {
            this.timestamp = (Date)eavValue.getValue();
        }
    }

    @XStreamAsAttribute
    private String path;

    private Long integer;

    private String string;

    private Date timestamp;

    private String text;

    public String getPath() {
        return path;
    }

    public Long getInteger() {
        return integer;
    }

    public String getString() {
        return string;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }
}