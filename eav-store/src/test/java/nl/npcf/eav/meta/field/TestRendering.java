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

package nl.npcf.eav.meta.field;

import freemarker.template.TemplateException;
import junit.framework.Assert;
import nl.npcf.eav.exception.EAVPathException;
import nl.npcf.eav.meta.EAVFieldType;
import nl.npcf.eav.meta.EAVPath;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

/**
 * test the field rendering
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class TestRendering {

    private Logger logger = Logger.getLogger(getClass());

    @Test
    public void choice() throws EAVPathException, IOException, TemplateException {
        ChoiceField choiceField = new ChoiceField();
        choiceField.options = new ArrayList<String>();
        choiceField.options.add("One");
        choiceField.options.add("Two");
        System.out.println(render(choiceField, "Two"));
        choiceField.style = ChoiceField.Style.RADIO;
        System.out.println(render(choiceField, "Two"));
    }

    @Test
    public void integer() {
        IntegerField integerField = new IntegerField();
        System.out.println(render(integerField, 90L));
        integerField.minimumValue = BigInteger.valueOf(10L);
        integerField.maximumValue = BigInteger.valueOf(100L);
        System.out.println(render(integerField, 90L));
    }

    @Test
    public void string() {
        StringField stringField = new StringField();
        System.out.println(render(stringField, "Hello String Field"));
        stringField.maximumLength = 25;
        stringField.regularExpression = "[0-9]*";
        System.out.println(render(stringField, "Hello String Field"));
    }

    @Test
    public void url() {
        UrlField urlField = new UrlField();
        System.out.println(render(urlField, "Hello Url Field"));
        urlField.maximumLength = 25;
        System.out.println(render(urlField, "Hello Url Field"));
    }

    @Test
    public void timestamp() {
        TimestampField timestampField = new TimestampField();
        System.out.println(render(timestampField, new Date()));
    }

    private String render(EAVFieldType.Field field, Object value) {
        StringBuilder out = new StringBuilder();
        try {
            field.toFormField("prompt", "hint", "helpUrl", new EAVPath("eav/path"), null, out);
            field.toFormField("prompt", "hint", "helpUrl", new EAVPath("eav/path"), value, out);
        }
        catch (Exception e) {
            logger.error("Problem rendering!", e);
            Assert.fail();
        }
        return out.toString();
    }
}
