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

package nl.npcf.eav.store;

import nl.npcf.eav.data.EAVEntity;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.meta.EAVAttribute;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TransientEAVStore extends AbstractEAVStore {
    private Map<String, EAVEntity> entities = new TreeMap<String,EAVEntity>();

    public void setMetadataXml(String xml, Principal principal) throws EAVException {
        throw new RuntimeException("Not implemented");
    }

    public boolean initialize() throws EAVException {
        if (initialized) {
            return true;
        }
        getEAVSchema();
        return initialized = true;
    }

    public EAVEntity createTransientEntity(String key) {
        checkInitialized();
        if (key == null) {
            key = createRandomKey();
        }
        return new EAVEntity(getEAVSchema(), key);
    }

    public void persist(Entity entity) throws EAVValidationException {
        checkInitialized();
        EAVEntity eavEntity = (EAVEntity)entity;
        eavEntity.validate();
        logger.info("pretend persist "+entity);
        entities.put(entity.getKey(), (EAVEntity)entity);
    }

    public void remove(Entity entity) {
        checkInitialized();
        logger.info("pretend remove"+entity);
        entities.remove(entity.getKey());
    }

    public Entity remove(Entity entity, Path path) throws EAVValidationException {
        throw new RuntimeException("not implemented");
    }

    public Entity setValue(String entity, Path path, String value, Principal createdBy) {
        throw new RuntimeException();
    }

    public Entity setValues(String entityKey, Map<Path, String> valueMap, Principal createdBy) throws EAVValidationException {
        throw new RuntimeException();
    }

    public Entity persist(Value value) {
        entities.put(value.getEntity().getKey(), (EAVEntity)value.getEntity());
        return value.getEntity();
    }

    public EAVEntity findByKey(String key) {
        return entities.get(key);
    }

    public List<Value> findByAttribute(Attribute attributeInterface) {
        throw new RuntimeException("Cannot execute");
    }

    public List<Value> findByValue(Attribute attributeInterface, Object attributeValue) {
        throw new RuntimeException("Cannot execute");
    }

    public List<EAVAttribute> getRequiredAttributes() {
        return getEAVSchema().getRequiredAttributes();
    }
}