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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import nl.npcf.eav.EAVStore;
import nl.npcf.eav.exception.EAVException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class wraps all of the responses that are rendered using XStream.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("eav")
public class EAVResponse {

    public static EAVResponse create(Throwable throwable) {
        EAVResponse response = new EAVResponse();
        response.setThrowable(throwable);
        return response;
    }

    public static EAVResponse create(EAVStore.Entity entity) {
        EAVResponse response = new EAVResponse();
        response.add(new EAVXEntity(entity));
        return response;
    }

    public static EAVResponse create(EAVStore.Entity entity, EAVStore.Path path) throws EAVException {
        EAVResponse response = new EAVResponse();
        response.add(new EAVXEntity(entity, path));
        return response;
    }

    public static EAVResponse create(List<? extends EAVStore.Value> valueList) throws EAVException {
        EAVResponse response = new EAVResponse();
        for (EAVStore.Value value : valueList) {
            response.add(new EAVXEntity(value.getEntity()));
        }
        return response;
    }

    public static EAVResponse create(EAVStore.Attribute attribute) {
        EAVResponse response = new EAVResponse();
        response.add(attribute);
        return response;
    }

    private EAVResponse() {
    }

    private void setThrowable(Throwable throwable) {
        exceptionReport = new ExceptionReport(throwable);
    }

    private void add(Object element) {
        if (content == null) {
            content = new ArrayList<Object>();
        }
        this.content.add(element);
    }

    List<Object> content;

    @XStreamAlias("exception")
    ExceptionReport exceptionReport;

    public static class ExceptionReport {

        public ExceptionReport(Throwable throwable) {
            this.message = throwable.getMessage();
            this.type = throwable.getClass().getName();
            while (throwable != null) {
                stackTrace.add(getStackTrace(throwable));
                throwable = throwable.getCause();
            }
        }

        public ExceptionReport() {
        }

        @XStreamAsAttribute
        String message;

        @XStreamAsAttribute
        String type;

        @XStreamImplicit(itemFieldName = "cause")
        List<String> stackTrace = new ArrayList<String>();
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
