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

import nl.npcf.eav.data.EAVEntity;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.meta.EAVPath;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigInteger;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-context.xml"})
public class TestStore {
    private static final Principal PRINCIPAL = new Principal() {
        public String getName() {
            return "Principal";
        }
    };
    private Logger logger = Logger.getLogger(getClass());
    private List<String> keys = new ArrayList<String>();

    @Autowired
    private EAVStore eavStore;

    @Before
    public void start() throws EAVException {
        if (!eavStore.initialize()) {
            logger.info("Metadata not accepted");
        }
        else {
            logger.info("Metadata matches");
        }
        for (int walk=0; walk<16; walk++) {
            EAVStore.Entity entity = eavStore.createTransientEntity(null);
            entity.setTransientValue(new EAVPath("unique"), "unique"+walk, PRINCIPAL);
            if ((walk & 1) > 0) {
                entity.setTransientValue(new EAVPath("age"), "20", PRINCIPAL);
            }
            if ((walk & 2) > 0) {
                entity.setTransientValue(new EAVPath("time"), "11/11/2004", PRINCIPAL);
            }
            if ((walk & 4) > 0) {
                entity.setTransientValue(new EAVPath("name"), "Lacey-"+walk, PRINCIPAL);
            }
            if ((walk & 8) > 0) {
                entity.setTransientValue(new EAVPath("size"), "This is a long bit of text in a field that has more or less unlimited size", PRINCIPAL);
            }
            eavStore.persist(entity);
            keys.add(entity.getKey());
        }
    }

    @After
    public void finish() {
        for (String key : keys) {
            EAVStore.Entity entityByKey = eavStore.findByKey(key);
            eavStore.remove(entityByKey);
        }
    }

    @Test
    public void queries() throws EAVException {
        EAVStore.Entity entityByKey = eavStore.findByKey(keys.get(15));
        Assert.assertEquals(5, ((EAVEntity)entityByKey).getValues().size());
        Assert.assertEquals(BigInteger.valueOf(20L), entityByKey.getValues(new EAVPath("age"), false).get(0).getValue());

        EAVStore.Attribute ageAttribute = eavStore.getSchema().getAttribute(new EAVPath("age"),true);
        List<EAVStore.Value> valuesByAge = eavStore.findByAttribute(ageAttribute);
        Assert.assertEquals(8, valuesByAge.size());
        for (EAVStore.Value value : valuesByAge) {
            Assert.assertEquals("age", value.getAttribute().getPath().getNodeString());
        }

        EAVStore.Attribute nameAttribute = eavStore.getSchema().getAttribute(new EAVPath("name"),true);
        List<EAVStore.Value> valuesByName = eavStore.findByValue(nameAttribute, "Lacey-15");
        Assert.assertEquals(1, valuesByName.size());
        EAVStore.Value valueByName = valuesByName.get(0);
        Assert.assertEquals("name", valueByName.getAttribute().getPath().getNodeString());
        Assert.assertEquals("Lacey-15", valueByName.getValue());
        Assert.assertEquals(5, ((EAVEntity)valueByName.getEntity()).getValues().size());
    }

    @Test
    public void removeValue() throws EAVException {
        EAVStore.Attribute nameAttribute = eavStore.getSchema().getAttribute(new EAVPath("name"),true);
        List<EAVStore.Value> valuesByName = eavStore.findByValue(nameAttribute, "Lacey-15");
        Assert.assertEquals(1, valuesByName.size());
        Assert.assertEquals(keys.get(15), valuesByName.get(0).getEntity().getKey());
        eavStore.remove(valuesByName.get(0).getEntity(),valuesByName.get(0).getPath());

        EAVStore.Entity entityByKey = eavStore.findByKey(keys.get(15));
        Assert.assertEquals(4, ((EAVEntity)entityByKey).getValues().size());

        valuesByName = eavStore.findByAttribute(nameAttribute);
        Assert.assertEquals(7, valuesByName.size());
    }

    @Test
    public void setValue() throws EAVException {
        logger.info("settng a value");
        EAVStore.Entity entity = eavStore.findByKey(keys.get(15));
        Assert.assertEquals(5, ((EAVEntity)entity).getValues().size());
        Assert.assertEquals("Lacey-15", entity.getValues(new EAVPath("name"), false).get(0).getValue());

        eavStore.setValue(keys.get(15), new EAVPath("name"), "Gumby", PRINCIPAL);

        logger.info("retrieving again");
        EAVStore.Entity entity2 = eavStore.findByKey(keys.get(15));
        Assert.assertEquals(5, ((EAVEntity)entity2).getValues().size());
        Assert.assertEquals("Gumby", entity2.getValues(new EAVPath("name"), false).get(0).getValue());
    }

    @Test
    public void uniqueness() throws EAVException {
        logger.info("testing uniqueness");
        EAVStore.Entity entity = eavStore.createTransientEntity(null);
        entity.setTransientValue(new EAVPath("unique"), "unique7", PRINCIPAL);
        try {
            eavStore.persist(entity);
            Assert.fail("Should not have accepted duplicate identifier");
        }
        catch (EAVValidationException e) {
            logger.info("Good, validation exception: ",e);
        }
    }

}
