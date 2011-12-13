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

package nl.npcf.eav.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import nl.npcf.eav.exception.EAVPathException;
import nl.npcf.eav.meta.EAVPath;
import org.hibernate.annotations.Index;

import java.security.Principal;

/**
 * The value subclass for handling unique identifier strings
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Entity
public class EAVIdentifier extends EAVValue {

    private static final int MAX_LENGTH = 16;

    @Column(length = MAX_LENGTH, unique = true)
    @Index(name = "eavidentifierindex")
    private String value;

    public EAVIdentifier() {
    }

    public EAVIdentifier(EAVEntity entity, EAVPath path, Principal createdBy, String value) throws EAVPathException {
        super(entity, path, createdBy);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = (String) value;
        if (this.value != null) {
            if (this.value.length() > MAX_LENGTH) {
                throw new RuntimeException("Identifier exceeds acceptable length ("+MAX_LENGTH+"): ["+this.value+"]");
            }
        }
    }

    public String toString() {
        return getPath()+"=Identity("+value+")";
    }
}