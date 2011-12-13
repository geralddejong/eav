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

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import nl.npcf.eav.meta.EAVFieldType;
import nl.npcf.eav.meta.EAVPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public abstract class AbstractFreemarkerField implements EAVFieldType.Field {

    public void toFormField(String prompt, String hint, String helpUrl, EAVPath path, Object value, StringBuilder out) throws IOException, TemplateException {
        Map<String,Object> model = new TreeMap<String,Object>();
        model.put("prompt", prompt);
        model.put("hint", hint);
        model.put("helpUrl", helpUrl);
        model.put("helpPrompt", "(?)");
        model.put("path",path);
        model.put("value",valueToString(value));
        addToModel(model);
        Template template = getResourceTemplate();
        StringWriter sw = new StringWriter();
        template.process(model,sw);
        out.append(sw.toString());
    }

    private Template getResourceTemplate() throws IOException {
        String className = getClassName();
        String resource = "/templates/"+className+".ftl";
        InputStream inputStream = AbstractFreemarkerField.class.getResourceAsStream(resource);
        Reader reader = new InputStreamReader(inputStream);
        return getTemplate(className, reader);
    }

    public String getClassName() {
        String cn = getClass().getName();
        return cn.substring(cn.lastIndexOf(".")+1);
    }

    private Template getTemplate(String name, Reader reader) throws IOException {
        Configuration configuration = new Configuration();
//        configuration.setLocale(new Locale("nl"));
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        return new Template(name, reader, configuration);
    }

    public abstract String valueToString(Object value);
    protected abstract void addToModel(Map<String,Object> model);

    public String toString() {
        return getClassName();
    }
}