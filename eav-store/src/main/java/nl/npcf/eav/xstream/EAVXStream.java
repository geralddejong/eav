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

package nl.npcf.eav.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import nl.npcf.eav.data.EAVInteger;
import nl.npcf.eav.data.EAVString;
import nl.npcf.eav.data.EAVText;
import nl.npcf.eav.data.EAVTimestamp;
import nl.npcf.eav.exception.EAVException;
import nl.npcf.eav.meta.EAVSchema;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * A convenient class that gives us an XStream that already knows about
 * all of the relevant types.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EAVXStream {

    private XStream xStream;
    private String mimeType;

    public EAVXStream(boolean json) {
        if (json) {
            xStream = new XStream(new JsonHierarchicalStreamDriver());
            mimeType = "text/plain";
        }
        else {
            xStream = new XStream();
            mimeType = "text/xml";
        }
        xStream.processAnnotations(new Class[]{
                EAVSchema.class,
                EAVXEntity.class,
                EAVResponse.class,
                EAVInteger.class,
                EAVString.class,
                EAVText.class,
                EAVTimestamp.class,
        });
        xStream.registerConverter(new DateConverter());
    }

    public EAVXStream() {
        this(false);
    }

    public void toXML(EAVResponse response, OutputStream outputStream) {
        xStream.toXML(response, outputStream);
    }

    public EAVSchema fromXML(InputStream inputStream) throws EAVException {
        EAVSchema schema = (EAVSchema) xStream.fromXML(inputStream);
        schema.initialize();
        return schema;
    }

    public EAVSchema fromXML(String string) throws EAVException {
        EAVSchema schema = (EAVSchema) xStream.fromXML(string);
        schema.initialize();
        return schema;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static Object clone(Object object) {
        EAVXStream stream = new EAVXStream();
        String xml = stream.xStream.toXML(object);
        return stream.xStream.fromXML(xml);
    }

    private static class DateConverter implements Converter {
        Logger logger = Logger.getLogger(getClass());
        DateTimeFormatter formatter = ISODateTimeFormat.basicDateTimeNoMillis();

        public void marshal(Object o, HierarchicalStreamWriter hierarchicalStreamWriter, MarshallingContext marshallingContext) {
            Date date = (Date)o;
            long time = date.getTime();
            hierarchicalStreamWriter.setValue(formatter.print(time));
        }

        public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
            String value = hierarchicalStreamReader.getValue();
            logger.info("Parse: "+value);
            return new Date(formatter.parseMillis(value));
        }

        public boolean canConvert(Class aClass) {
            return aClass == Date.class || aClass == java.sql.Timestamp.class;
        }
    }
}
