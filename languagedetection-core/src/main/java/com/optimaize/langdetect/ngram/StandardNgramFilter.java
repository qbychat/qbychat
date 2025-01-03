/*
 * Copyright 2011 Fabian Kessler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.optimaize.langdetect.ngram;

/**
 * Filters what is generally not desired.
 * <p>
 * Impl is immutable.
 *
 * @author Fabian Kessler
 */
public class StandardNgramFilter implements NgramFilter {

    private static final StandardNgramFilter INSTANCE = new StandardNgramFilter();

    private StandardNgramFilter() {
    }

    public static NgramFilter getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean use(String ngram) {
        switch (ngram.length()) {
            case 1:
                return ngram.charAt(0) != ' ';
            case 2:
                return true;
            case 3:
                //middle char is a space
                return ngram.charAt(1) != ' ';
            case 4:
                //one of the middle chars is a space
                return ngram.charAt(1) != ' ' && ngram.charAt(2) != ' ';
            default:
                //would need the same check: no space in the middle, border is fine.
                throw new UnsupportedOperationException("Unsupported n-gram length: " + ngram.length());
        }
    }

}
