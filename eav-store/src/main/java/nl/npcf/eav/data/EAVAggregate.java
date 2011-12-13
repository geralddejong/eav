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

import nl.npcf.eav.exception.EAVPathException;
import nl.npcf.eav.meta.EAVPath;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a value used to hold other values, or a kind of virtual aggregate of
 * other values, because aggregates are not stored in the database.  That's why this is
 * not an @Entity, and not found in the hibernate spec.  It is a subclass so it allows
 * for a value hierarchy.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EAVAggregate extends EAVValue {

    private List<EAVValue> values = new ArrayList<EAVValue>();

    public EAVAggregate() {
    }

    public EAVAggregate(EAVEntity entity, EAVPath path) throws EAVPathException {
        super(entity, path, null);
    }

    public Object getValue() {
        return values;
    }

    public void setValue(Object value) {
        throw new RuntimeException("Not done");
    }

    public void add(EAVValue value) {
        values.add(value);
    }

    public void addAll(List<EAVValue> values) {
        values.addAll(values);
    }

    public String toString() {
        return getPath()+"=Aggregate";
    }

}