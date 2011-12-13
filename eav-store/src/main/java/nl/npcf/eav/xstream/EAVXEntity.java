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
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import nl.npcf.eav.EAVStore;
import nl.npcf.eav.data.EAVEntity;
import nl.npcf.eav.data.EAVValue;
import nl.npcf.eav.exception.EAVException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class wraps an entity and its values for more careful presentation
 * using XStream.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("entity")
public class EAVXEntity {

    @XStreamAsAttribute
    private String key;

    @XStreamAsAttribute
    private String path;

    @XStreamImplicit
    private List<EAVXValue> values;

    public EAVXEntity() {
    }

    public EAVXEntity(EAVStore.Entity entity) {
        this((EAVEntity)entity);
    }

    public EAVXEntity(EAVEntity eavEntity) {
        this.key = eavEntity.getKey();
        values = new ArrayList<EAVXValue>();
        for (EAVValue value : eavEntity.getValues()) {
            values.add(new EAVXValue(value));
        }
    }

    public EAVXEntity(EAVStore.Entity entity, EAVStore.Path path) throws EAVException {
        this((EAVEntity)entity, path);
    }

    public EAVXEntity(EAVEntity eavEntity, EAVStore.Path path) throws EAVException {
        this.key = eavEntity.getKey();
        this.path = path.isEmpty() ? null : path.toString();
        values = new ArrayList<EAVXValue>();
        for (EAVValue value : eavEntity.getValues(path, false)) {
            values.add(new EAVXValue(value));
        }
    }

    public String getKey() {
        return key;
    }

    public String getPath() {
        return path;
    }

    public List<EAVXValue> getValues() {
        return values;
    }
}
