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

package nl.npcf.eav;

import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVPathException;
import nl.npcf.eav.exception.EAVValidationException;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This interface is the only thing that the rest of the code sees of the
 * Entity Attribute Value store.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface EAVStore {

    /**
     * When building a path string, this is the separator between the nodes
     */

    String PATH_SEPARATOR = "/";

    /**
     * Push in a new version of the metadata, stuffing it in the database.
     *
     * @param xml the new metadata
     * @param principal who is doing this?
     * @throws EAVException somethign went wrong interpreting the XML
     */
    
    void setMetadataXml(String xml, Principal principal) throws EAVException;

    /**
     * Take a look at the metadata active here
     *
     * @return the metadata in XML form
     */
    
    String getMetadataXml();

    /**
     * Get this baby in the air
     *
     * @return false if it didn't work because there's no metadata in the database
     * @throws EAVException something went wrong reading the metadata
     */

    boolean initialize() throws EAVException;

    /**
     * Access the currently active schema
     *
     * @return the schema
     */

    Schema getSchema();

    /**
     * This creates a new entity that can be filled in, and then later persisted.  This is the only way
     * to create new entities.
     *
     * @param key the key of the new entity, if null one is invented and checked
     * @return an implementation of the Entity interface
     */

    Entity createTransientEntity(String key);

    /**
     * Persist a transient entity, created by createEntityTransient and then filled using methods of
     * the Entity interface.
     *
     * @param entity the new entity that must be persisted
     * @throws EAVValidationException something went wrong while validating the entity
     */

    void persist(Entity entity) throws EAVValidationException;

    /**
     * Make the entity transient, in other words remove it from the database.
     *
     * @param entity the thing to remove.
     */

    void remove(Entity entity);

    /**
     * Make the entity's values matching the path transient
     *
     * @param entity the thing we're removing things from
     * @param path specifies the value
     * @return the entity after the removal
     * @throws nl.npcf.eav.exception.EAVValidationException without the values it wasn't valid
     */

    Entity remove(Entity entity, Path path) throws EAVValidationException;

    /**
     * Set a value, effectively creating a new one, and removing the old one if necessary.
     *
     * @param entityKey to which entity will this value belong
     * @param path identifying the value
     * @param valueString string representation of the value
     * @param createdBy the person responsible for setting the value (recorded)
     * @return the entity after the value has been set.
     * @throws nl.npcf.eav.exception.EAVValidationException the value is not valid
     */

    Entity setValue(String entityKey, Path path, String valueString, Principal createdBy) throws EAVValidationException;

    /**
     * Set a number of values at once, effectively creating new ones.
     *
     * @param entityKey to which entity will this value belong
     * @param valueMap map containing path to string mappings
     * @param createdBy the person responsible for setting the value (recorded)
     * @return the entity after the value has been set.
     * @throws nl.npcf.eav.exception.EAVValidationException the value is not valid
     */

    Entity setValues(String entityKey, Map<Path,String> valueMap, Principal createdBy) throws EAVValidationException;

    /**
     * Fetch an entity based on its unique key
     * @param key identifies the entity uniquely
     * @return an entity
     */

    Entity findByKey(String key);

    /**
     * Fetch all the values (with their entities attached!) which are in possession of the given
     * attribute. This one is a little dangerous because it could return lots of results.
     *
     * @param attribute what the entities must have
     * @return a list of values
     */

    List<Value> findByAttribute(Attribute attribute);

    /**
     * Fetch all the values of the given attribute that are equal to the given value object.
     *
     * @param attribute which attribute we're talking about
     * @param value the value we are lookin for
     * @return a list of values (with their entities attached!) equal to the given value
     */

    List<Value> findByValue(Attribute attribute, Object value);


    public interface Path {
        String getNodeString();
        boolean contains(Path path, boolean inclusive);
        boolean isComplete();
        boolean isEmpty();
        Path extend(String name);
        Path getParent();
    }

    /**
     * This is all the outside world sees of the metadata schema.  Different ways of getting to attributes.
     */

    public interface Schema {
        Path createPath(String path, boolean complete) throws EAVPathException;
        Attribute getAttribute(Path path, boolean validate) throws EAVPathException;
        List<? extends Attribute> getRequiredAttributes();
        String toHtml(String baseUrl, Path path) throws EAVException;
        String toTableHeaders(Attribute attribute) throws EAVException;
    }

    /**
     * This is what the world sees of an entity.  It is a key with a collection of attribute-value pairs, and
     * the attributes are specified by path strings.
     */

    public interface Entity {
        String getKey();
        Value getValue(Path path) throws EAVPathException;
        List<? extends Value> getValues(Path path, boolean inclusive);
        void sortValues();
        void setTransientValue(Path path, String valueString, Principal createdBy) throws EAVValidationException;
        void validate() throws EAVValidationException;
        String toHtml(String baseUrl, Path path) throws EAVException;
    }

    /**
     * This is the element of metadata.
     */

    public interface Attribute {
        String getName();
        String getPrompt();
        String getHint();
        String getHelpUrl();
        Path getPath();
        Object stringToValue(String value) throws EAVValidationException;
        boolean isAggregate();
        boolean isRequired();
        boolean isMultiple();
        List<? extends Attribute> getSubAttributes();
    }

    /**
     * A value has an associated entity, and it identifies which attribute its stored value belongs to.
     * It also holds the name of its creator as well as the timestamp when it was created.
     */

    public interface Value {
        Entity getEntity();
        Path getPath();
        Attribute getAttribute() throws EAVException;
        String getCreatedBy();
        Date getCreatedOn();
        Object getValue();
        String toTableRow(String baseEditUrl) throws EAVException;
    }
}
