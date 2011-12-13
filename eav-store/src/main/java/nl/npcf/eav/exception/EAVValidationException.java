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

import nl.npcf.eav.EAVStore;
import nl.npcf.eav.meta.EAVAttribute;

import java.math.BigInteger;

/**
 * This exception is specifically for validation failures
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EAVValidationException extends EAVException {
    private static final long serialVersionUID = -8412060961273508462L;
    private EAVStore.Attribute attribute;
    private Object value;

    private EAVValidationException(EAVStore.Attribute attribute, Object value, String message) {
        super(message);
        this.attribute = attribute;
        this.value = value;
    }

    public EAVStore.Attribute getAttribute() {
        return attribute;
    }

    public Object getValue() {
        return value;
    }

    public static EAVValidationException missingAttribute(EAVStore.Attribute attribute) {
        return new EAVValidationException(attribute, null, "missing");
    }

    public static EAVValidationException illegalStringValue(EAVAttribute attribute, String value, String message) {
        return new EAVValidationException(attribute, value, message);
    }

    public static EAVValidationException exceedsMinimumValue(EAVAttribute attribute, BigInteger minimumValue, BigInteger value) {
        return new EAVValidationException(attribute, value, "lower than "+minimumValue);
    }

    public static EAVValidationException exceedsMaximumValue(EAVAttribute attribute, BigInteger maximumValue, BigInteger value) {
        return new EAVValidationException(attribute, value, "higher than "+maximumValue);
    }

    public static EAVValidationException badNumberFormat(EAVAttribute attribute, String value) {
        return new EAVValidationException(attribute, value, "does not resemble a number");
    }

    public static EAVValidationException badType(EAVAttribute attribute, Class<?> expected, Class<?> received) {
        return new EAVValidationException(attribute, null, "type check: expected " + expected + ", but got " + received);
    }

    public static EAVValidationException cannotBeSet(EAVAttribute attribute) {
        return new EAVValidationException(attribute, null, "not settable");
    }

    public static EAVValidationException badDateFormat(EAVAttribute attribute, String value, String acceptableValue) {
        return new EAVValidationException(attribute, value, "bad date, should resemble: "+acceptableValue);
    }

    public static EAVValidationException badUrl(EAVAttribute attribute, String value) {
        return new EAVValidationException(attribute, value, "bad url");
    }

    public static EAVValidationException exceedsMaximumLength(EAVAttribute attribute, int maximumLength, String value) {
        return new EAVValidationException(attribute, value, "too long (> "+maximumLength+")");
    }

    public static EAVValidationException mismatchRegularExpression(EAVAttribute attribute, String regularExpression, String value) {
        return new EAVValidationException(attribute, value, "mismatches regex [" + regularExpression + "]");
    }

    public static EAVValidationException identifierExists(EAVAttribute attribute, String value) {
        return new EAVValidationException(attribute, value, "identifier exists");
    }

    public static EAVValidationException entityExists() {
        return new EAVValidationException(null, null, "entity exists");
    }
}