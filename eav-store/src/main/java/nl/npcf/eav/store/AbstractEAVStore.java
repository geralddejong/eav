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

import nl.npcf.eav.EAVStore;
import nl.npcf.eav.data.EAVMetaVersion;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.meta.EAVSchema;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.Random;

/**
 * The things that all stores need to have are in this superclass.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public abstract class AbstractEAVStore implements EAVStore {

    private static final int KEY_SIZE = 16;
    protected Logger logger = Logger.getLogger(getClass());
    protected EAVMetaVersion metaVersion;
    protected boolean initialized;

    /**
     * Probably only for testing, creates a metaVersion instance from a classpath resource
     *
     * @param schemaResource what it's called, with path
     * @throws EAVException oops
     * @throws IOException oops
     */
    
    public void setSchemaResource(String schemaResource) throws EAVException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(schemaResource)));
        StringBuilder xml = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            xml.append(line).append('\n');
        }
        this.metaVersion = new EAVMetaVersion(xml.toString(), new Principal() {
            public String getName() {
                return "setSchemaResource()";
            }
        });
    }

    public Schema getSchema() {
        return getEAVSchema();
    }

    public String getMetadataXml() {
        return metaVersion.getXml();
    }

    public EAVSchema getEAVSchema() {
        try {
            return metaVersion.getSchema();
        }
        catch (EAVException e) {
            throw new RuntimeException("Unable to get schema",e);
        }
    }

    protected String createRandomKey() {
        Random random = new Random();
        StringBuilder key = new StringBuilder(KEY_SIZE);
        for (int walk=0; walk<KEY_SIZE; walk++) {
            key.append((char)('a'+random.nextInt(26)));
        }
        return key.toString();
    }

    protected void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Not initialized");
        }
    }

}
