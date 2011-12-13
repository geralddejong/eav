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
import nl.npcf.eav.data.EAVEntity;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.exception.EAVValidationException;
import nl.npcf.eav.store.TransientEAVStore;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.Principal;

public class TestServiceSchema {
    private static final Principal PRINCIPAL = new Principal() {
        public String getName() {
            return "EAVServiceTest";
        }
    };
    private Logger logger = Logger.getLogger(getClass());
    private TransientEAVStore eavStore = new TransientEAVStore();

    @Before
    public void readSchema() throws EAVException, IOException {
        eavStore.setSchemaResource("/service-eav.xml");
        eavStore.initialize();
    }

    @Test
    public void validateFAQ() throws EAVException {
        EAVStore.Attribute faqAttribute = eavStore.getSchema().getAttribute(new EAVPath("faq00"),true);
        String faqUrl = "http://www.kiesbeter.nl/algemeen/Zoeken/default.aspx?zoekterm=bloed";
        faqAttribute.stringToValue(faqUrl);
        logger.info("validated faq=" + faqUrl);
        faqUrl = "https://www.kiesbeter.nl/algemeen/Zoeken/default.aspx?zoekterm=bloed";
        faqAttribute.stringToValue(faqUrl);
        logger.info("validated faq=" + faqUrl);
        faqUrl = "www.kiesbeter.nl/algemeen/Zoeken/default.aspx?zoekterm=bloed";
        try {
            faqAttribute.stringToValue(faqUrl);
            Assert.fail();
        }
        catch (EAVValidationException e) {
            logger.info("invalid faq=" + faqUrl);
        }
    }

    @Test
    public void validateLink() throws EAVException {
        EAVEntity entity = eavStore.createTransientEntity(null);
        entity.setTransientValue(new EAVPath("faq66"), "http://www.kiesbeter.nl/algemeen/Zoeken/default.aspx?zoekterm=zweet", PRINCIPAL);
        entity.setTransientValue(new EAVPath("faq00"), "http://www.kiesbeter.nl/algemeen/Zoeken/default.aspx?zoekterm=bloed", PRINCIPAL);
        entity.setTransientValue(new EAVPath("link/url"), "http://gumby.com/path/to?something=true", PRINCIPAL);
        entity.setTransientValue(new EAVPath("link/method"), "WSRP", PRINCIPAL);
        try {
            eavStore.persist(entity);
            Assert.fail();
        }
        catch (EAVException e) {
            logger.info("good, couldn't persist without purpose: "+e);
        }
        entity.setTransientValue(new EAVPath("purpose"), "To go where noone has ever gone before", PRINCIPAL);
        eavStore.persist(entity);
        entity.setTransientValue(new EAVPath("link/parameter00/field"), "SUBSCRIPTION_VARIABLE", PRINCIPAL);
        try {
            eavStore.persist(entity);
            Assert.fail();
        }
        catch (EAVException e) {
            logger.info("good, couldn't persist without format as well: "+e);
        }
        entity.setTransientValue(new EAVPath("link/parameter00/format"), "This is format for a ${substitution machine}", PRINCIPAL);
        eavStore.persist(entity);
    }

}