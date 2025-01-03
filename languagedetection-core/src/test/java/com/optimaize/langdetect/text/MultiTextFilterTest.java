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

package com.optimaize.langdetect.text;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertEquals;

/**
 * @author Fabian Kessler
 */
public class MultiTextFilterTest {

    @Test
    public void empty() {
        assertEquals("foo", new MultiTextFilter(Collections.emptyList()).filter("foo"));
    }

    @Test
    public void doubleFilter() {
        assertEquals("nBnBnBB", new MultiTextFilter(ImmutableList.of(
            text -> text.toString().replace("a", "A"), text -> text.toString().replace("A", "B")
        )).filter("nananaa"));
    }
}
