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

import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.store.TransientEAVStore;
import org.junit.Test;

import java.io.IOException;

public class TestExamples {

    @Test
    public void readDienst() throws EAVException, IOException {
        TransientEAVStore eavStore = new TransientEAVStore();
        eavStore.setSchemaResource("/dienst-schema.xml");
        eavStore.initialize();
    }

    @Test
    public void readZorgconsument() throws EAVException, IOException {
        TransientEAVStore eavStore = new TransientEAVStore();
        eavStore.setSchemaResource("/zorgconsument-schema.xml");
        eavStore.initialize();
    }

    @Test
    public void readZorgverlener() throws EAVException, IOException {
        TransientEAVStore eavStore = new TransientEAVStore();
        eavStore.setSchemaResource("/zorgverlener-schema.xml");
        eavStore.initialize();
    }
}