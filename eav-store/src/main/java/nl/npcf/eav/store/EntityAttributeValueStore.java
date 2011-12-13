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

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import nl.npcf.eav.EAVStore;
import nl.npcf.eav.data.EAVEntity;
import nl.npcf.eav.data.EAVIdentifier;
import nl.npcf.eav.data.EAVMetaVersion;
import nl.npcf.eav.data.EAVValue;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVPathException;
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.meta.EAVAttribute;
import nl.npcf.eav.meta.EAVPath;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is the implementation of the EAVStore interface that uses an entity manager
 * to do all the interaction with the database.  It's actually a specialized DAO.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EntityAttributeValueStore extends AbstractEAVStore {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void setMetadataXml(String xml, Principal principal) throws EAVException {
        metaVersion = new EAVMetaVersion(xml, principal);
        metaVersion.getSchema();
        entityManager.persist(metaVersion);
        initialized = true;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public boolean initialize() throws EAVException {
        if (initialized) {
            return true;
        }
        Query query = entityManager.createQuery("select mv from EAVMetaVersion mv order by mv.createdOn");
        List<EAVMetaVersion> metaVersionList = query.getResultList();
        if (this.metaVersion == null) {
            if (metaVersionList.isEmpty()) {
                return false;
            }
            this.metaVersion = metaVersionList.get(metaVersionList.size()-1);
        }
        return initialized = true;
    }

    @Transactional
    public EAVEntity createTransientEntity(String key) {
        checkInitialized();
        do {
            Query query = entityManager.createQuery("select entity.key from EAVEntity entity where entity.key = :key");
            if (key == null) {
                key = createRandomKey();
            }
            query.setParameter("key", key);
            if (!query.getResultList().isEmpty()) {
                key = null;
            }
        }
        while (key == null);
        return new EAVEntity(getEAVSchema(), key);
    }

    @Transactional(rollbackFor = EAVValidationException.class)
    public void persist(EAVStore.Entity entity) throws EAVValidationException {
        checkInitialized();
        EAVEntity eavEntity = (EAVEntity)entity;
        eavEntity.validate();
        try {
            entityManager.persist(entity);
            entityManager.flush();
        }
        catch (EntityExistsException e) {
            EAVIdentifier identifier = null;
            for (EAVValue value: eavEntity.getValues()) {
                if (value instanceof EAVIdentifier) {
                    identifier = (EAVIdentifier)value;
                }
            }
            if (identifier == null) {
                throw EAVValidationException.entityExists();
            }
            else {
                try {
                    EAVAttribute attribute = getEAVSchema().getEAVAttribute(identifier.getPath(), false);
                    throw EAVValidationException.identifierExists(attribute, (String)identifier.getValue());
                }
                catch (EAVPathException e1) {
                    throw new RuntimeException("Expected path to be okay", e1);
                }
            }
        }
    }

    @Transactional
    public void remove(EAVStore.Entity entity) {
        checkInitialized();
        entity = entityManager.merge(entity);
        entityManager.remove(entity);
    }

    @Transactional(rollbackFor = EAVValidationException.class)
    public Entity remove(EAVStore.Entity entityInterface, EAVStore.Path path) throws EAVValidationException {
        checkInitialized();
        EAVEntity entity = (EAVEntity)entityManager.merge(entityInterface);
        entity.setSchema(getSchema());
        for (Value value : entity.getValues(path, true)) {
            EAVValue eavValue = (EAVValue)value;
            entity.getValues().remove(eavValue);
            entityManager.remove(value);
        }
        entity.validate();
        entity.sortValues();
        return entity;
    }

    @Transactional(rollbackFor = EAVValidationException.class)
    public Entity setValue(String entityKey, Path path, String valueString, Principal createdBy) throws EAVValidationException {
        EAVAttribute attribute = (EAVAttribute)getEAVSchema().getAttribute(path, true);
        if (attribute.isAggregate()) {
            throw EAVValidationException.cannotBeSet(attribute);
        }
        Query query = entityManager.createQuery("select entity from EAVEntity entity where entity.key = :key");
        query.setParameter("key", entityKey);
        EAVEntity entity = (EAVEntity)query.getSingleResult();
        entity.setSchema(getSchema());
        Object value = attribute.stringToValue(valueString); //validate first
        Iterator<EAVValue> valueIterator = entity.getValues().iterator();
        while (valueIterator.hasNext()) { // clear any existing value
            EAVValue existing = valueIterator.next();
            if (existing.getPath().equals(path)) {
                valueIterator.remove();
                entityManager.remove(existing);
            }
        }
        EAVValue eavValue = attribute.instantiate(entity, (EAVPath)path, value, createdBy);
        entity.getValues().add(eavValue);
        eavValue.getPath();
        entity.validate();
        entity.sortValues();
        return entity;
    }

    @Transactional(rollbackFor = EAVValidationException.class)
    public Entity setValues(String entityKey, Map<Path, String> valueMap, Principal createdBy) throws EAVValidationException {
        Query query = entityManager.createQuery("select entity from EAVEntity entity where entity.key = :key");
        query.setParameter("key", entityKey);
        EAVEntity entity = (EAVEntity)query.getSingleResult();
        entity.setSchema(getSchema());
        for (Map.Entry<Path, String> entry : valueMap.entrySet()) {
            EAVAttribute attribute = (EAVAttribute)getEAVSchema().getAttribute(entry.getKey(), true);
            if (attribute.isAggregate()) {
                throw EAVValidationException.cannotBeSet(attribute);
            }
            Iterator<EAVValue> valueIterator = entity.getValues().iterator();
            while (valueIterator.hasNext()) { // clear any existing value
                EAVValue existing = valueIterator.next();
                if (existing.getPath().equals(entry.getKey())) {
                    valueIterator.remove();
                    entityManager.remove(existing);
                }
            }
            if (!entry.getValue().isEmpty()) {
                Object value = attribute.stringToValue(entry.getValue()); //validate first
                EAVValue eavValue = attribute.instantiate(entity, (EAVPath)entry.getKey(), value, createdBy);
                entity.getValues().add(eavValue);
                eavValue.getPath();
            }
        }
        entity.validate();
        entity.sortValues();
        return entity;
    }

    @Transactional
    public EAVEntity findByKey(String key) {
        checkInitialized();
        Query query = entityManager.createQuery("select entity from EAVEntity entity where entity.key = :key");
        query.setParameter("key", key);
        EAVEntity entity = (EAVEntity)query.getSingleResult();
        entity.setSchema(getSchema());
        entity.sortValues();
        return entity;
    }

    @Transactional
    public List<Value> findByAttribute(Attribute attributeInterface) {
        checkInitialized();
        EAVAttribute attribute = (EAVAttribute) attributeInterface;
        Query query = entityManager.createQuery("from "+attribute.getEntityName()+" v where v.pathNodes = :path");
        query.setParameter("path", attribute.getPath().getNodeString());
        return getContextualizedValueList(query.getResultList());
    }

    @Transactional
    public List<Value> findByValue(Attribute attributeInterface, Object attributeValue) {
        checkInitialized();
        EAVAttribute attribute = (EAVAttribute) attributeInterface;
        Query query = entityManager.createQuery("from "+attribute.getEntityName()+" v where v.pathNodes = :path and v.value = :value");
        query.setParameter("path", attribute.getPath().getNodeString());
        query.setParameter("value", attributeValue);
        return getContextualizedValueList(query.getResultList());
    }

    @SuppressWarnings("unchecked")
    private List<Value> getContextualizedValueList(List resultList) {
        for (EAVStore.Value value : (List<EAVStore.Value>)resultList) {
            ((EAVValue)value).getEntity().setSchema(getSchema());
        }
        return resultList;
    }

}
