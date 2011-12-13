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

package nl.npcf.eav.exception;

/**
 * This exception is specifically for problems while interpreting a path
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EAVPathException extends EAVException {
    private static final long serialVersionUID = 37780234034731699L;
    private String pathString;

    public EAVPathException(String pathString, String message) {
        super(message + " : [" + pathString+ "]");
        this.pathString = pathString;
    }

    public String getPathString() {
        return pathString;
    }
}