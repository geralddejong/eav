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
import nl.npcf.eav.exception.EAVPathException;
import org.junit.Assert;
import org.junit.Test;

public class TestPath {

    @Test
    public void reproducePaths() throws EAVException {
        String[] paths = {
                "dude",
                "dude01",
                "dude/babe",
                "dude02/babe",
                "dude/babe05",
                "dude03/babe05"
        };
        for (String pathString : paths) {
            EAVPath path = new EAVPath(pathString);
            Assert.assertEquals(pathString, path.toString());
        }
    }

    @Test
    public void stripIndexes() throws EAVException {
        String[][] paths = {
                {"dude01", "dude"},
                {"dude03/babe05", "dude/babe"}
        };
        for (String[] pathStrings : paths) {
            EAVPath path = new EAVPath(pathStrings[0]);
            Assert.assertEquals(pathStrings[1], path.getNodeString());
        }
    }

    @Test
    public void combinedCreation() throws EAVException {
        String[][] paths = {
                {"dude", "05", "dude05"},
                {"dude/babe", "03/05", "dude03/babe05"},
                {"dude/babe/rock/roll", "///", "dude/babe/rock/roll"}
        };
        for (String[] pathStrings : paths) {
            EAVPath path = new EAVPath(pathStrings[0], pathStrings[1]);
            Assert.assertEquals(pathStrings[2], path.toString());
        }
    }

    @Test
    public void testContains() throws EAVPathException {
        Assert.assertTrue(new EAVPath("").contains(new EAVPath(""), false));
        Assert.assertTrue(new EAVPath("gumby").contains(new EAVPath("gumby"), false));
        Assert.assertTrue(new EAVPath("gumby").contains(new EAVPath("gumby03"), false));
        Assert.assertTrue(new EAVPath("one/two/three").contains(new EAVPath("one/two04/three04"), false));
        Assert.assertFalse(new EAVPath("one/two/three").contains(new EAVPath("one/two04"), false));
        Assert.assertFalse(new EAVPath("one/two").contains(new EAVPath("one/two04/three"), false));
        Assert.assertTrue(new EAVPath("one/two").contains(new EAVPath("one/two04/three"), true));
    }

    @Test
    public void testSize() throws EAVPathException {
        Assert.assertEquals(0, new EAVPath("").size());
        Assert.assertEquals(1, new EAVPath("gumby").size());
        Assert.assertEquals(1, new EAVPath("gumby11").size());
        Assert.assertEquals(2, new EAVPath("gumby/pokey").size());
        Assert.assertEquals(2, new EAVPath("gumby00/pokey").size());
        Assert.assertEquals(2, new EAVPath("gumby/pokey88").size());
        Assert.assertEquals(3, new EAVPath("gumby/pokey/you11").size());
    }

    @Test
    public void testLinks() throws EAVPathException {
        EAVPath path = new EAVPath("one/two/three");
        System.out.println(path.toLinks("http://host/app", "app", "entity"));
    }

    @Test
    public void testAfter() throws EAVPathException {
        Assert.assertEquals(
                new EAVPath("one/two/three").after(new EAVPath("one")).toString(),
                new EAVPath("two/three").toString()
        );
        Assert.assertEquals(
                new EAVPath("one/two").after(new EAVPath("one")).toString(),
                new EAVPath("two").toString()
        );
        Assert.assertEquals(
                new EAVPath("one/two").after(new EAVPath("")).toString(),
                new EAVPath("one/two").toString()
        );
    }
}