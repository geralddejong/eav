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

import nl.npcf.eav.EAVStore;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVPathException;
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.store.TransientEAVStore;
import org.apache.log4j.Logger;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.Principal;

public class TestSchema {
    private static final Principal PRINCIPAL = new Principal() {
        public String getName() {
            return "EAVSchemaTest";
        }
    };
    private Logger logger = Logger.getLogger(getClass());
    private EAVSchema schema;
    private TransientEAVStore eavStore = new TransientEAVStore();

    private EAVAttribute getAttribute(String path) throws EAVException {
        return (EAVAttribute) schema.getAttribute(new EAVPath(path), true);
    }

    @Before
    public void readSchema() throws EAVException, IOException {
        eavStore.setSchemaResource("/schema-example.xml");
        eavStore.initialize();
        schema = (EAVSchema) eavStore.getSchema();
    }

    @Test
    public void integerValidation() throws EAVException {
        EAVAttribute attribute = getAttribute("aggregate/rangecheck");
        logger.info("successful validations");
        attribute.stringToValue("15");
        attribute.stringToValue("10");
        attribute.stringToValue("20");
        try {
            attribute.stringToValue("21");
            fail("should not have convertd");
        }
        catch (EAVValidationException e) {
            logger.info("successful non-validation");
        }
        try {
            attribute.stringToValue("9");
            fail("should not have convertd");
        }
        catch (EAVValidationException e) {
            logger.info("successful non-validation");
        }
    }

    @Test
    public void lengthValidation() throws EAVException {
        EAVAttribute attribute = getAttribute("aggregate/lengthcheck");
        logger.info("successful validations");
        attribute.stringToValue("123456789");
        attribute.stringToValue("");
        attribute.stringToValue("1234567890");
        try {
            attribute.stringToValue("12345678901");
            fail("should not have convertd");
        }
        catch (EAVValidationException e) {
            logger.info("successful non-validation");
        }
    }

    @Test
    public void regexValidation() throws EAVException {
        EAVAttribute attribute = getAttribute("regex");
        logger.info("successful validations");
        attribute.stringToValue("1111-11");
        attribute.stringToValue("1234-56");
        try {
            attribute.stringToValue("0000-0");
            fail("should not have convertd");
        }
        catch (EAVValidationException e) {
            logger.info("successful non-validation");
        }
        try {
            attribute.stringToValue("1");
            fail("should not have convertd");
        }
        catch (EAVValidationException e) {
            logger.info("successful non-validation");
        }
    }

    @Test
    public void enumerationValidation() throws EAVException {
        EAVAttribute attribute = getAttribute("enumeration");
        logger.info("successful validations");
        attribute.stringToValue("ONE");
        attribute.stringToValue("FOUR");
        try {
            attribute.stringToValue("THIRTY");
            fail("should not have convertd");
        }
        catch (EAVValidationException e) {
            logger.info("successful non-validation");
        }
        try {
            attribute.stringToValue("");
            fail("should not have convertd");
        }
        catch (EAVValidationException e) {
            logger.info("successful non-validation");
        }
    }

    @Test
    public void createEntity() throws EAVException {
        EAVStore.Entity entity = eavStore.createTransientEntity(null);
        entity.setTransientValue(new EAVPath("enumeration"), "ONE", PRINCIPAL);
        try {
            entity.validate();
            fail("should not have validated");
        }
        catch (EAVValidationException e) {
            logger.info("good, not valid: " + e);
        }
        entity.setTransientValue(new EAVPath("regex"), "2324-11", PRINCIPAL);
        try {
            entity.validate();
            fail("should not have validated");
        }
        catch (EAVValidationException e) {
            logger.info("good, not valid: " + e);
        }
        entity.setTransientValue(new EAVPath("aggregate/rangecheck"), "15", PRINCIPAL);
        entity.validate();
        try {
            EAVStore.Path path = eavStore.getSchema().createPath("multiaggregate/age", true);
            entity.setTransientValue(path, "26", PRINCIPAL);
            fail("should not have allowed nonindexed");
        }
        catch (EAVPathException e) {
            logger.info("good, not valid: " + e);
        }
        try {
            EAVStore.Path path = eavStore.getSchema().createPath("aggregate00/rangecheck", true);
            entity.setTransientValue(path, "26", PRINCIPAL);
            fail("should not have allowed indexed");
        }
        catch (EAVPathException e) {
            logger.info("good, not valid: " + e);
        }
        entity.setTransientValue(new EAVPath("multiaggregate00/age"), "26", PRINCIPAL);
        try {
            entity.validate();
            fail("should not have validated");
        }
        catch (EAVValidationException e) {
            logger.info("good, not valid: " + e);
        }
        entity.setTransientValue(new EAVPath("multiaggregate00/name"), "booger", PRINCIPAL);
        entity.validate();
        entity.setTransientValue(new EAVPath("multiaggregate01/name"), "bogsmoke", PRINCIPAL);
        try {
            entity.validate();
            fail("should not have validated");
        }
        catch (EAVValidationException e) {
            logger.info("good, not valid: " + e);
        }
        entity.setTransientValue(new EAVPath("multiaggregate01/age"), "29", PRINCIPAL);
        entity.validate();
    }
}