/*
 * Copyright 2011 Nakatani Shuyo
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

package com.optimaize.langdetect.cybozu.util;


import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Nakatani Shuyo
 */
public class LangProfileTest {

    /**
     * Test method for {@link LangProfile#LangProfile()}.
     */
    @Test
    public final void testLangProfile() {
        LangProfile profile = new LangProfile();
        assertNull(profile.getName());
    }

    /**
     * Test method for {@link LangProfile#LangProfile(java.lang.String)}.
     */
    @Test
    public final void testLangProfileStringInt() {
        LangProfile profile = new LangProfile("en");
        assertEquals(profile.getName(), "en");
    }

    /**
     * Test method for {@link LangProfile#add(java.lang.String)}.
     */
    @Test
    public final void testAdd() {
        LangProfile profile = new LangProfile("en");
        profile.add("a");
        assertEquals((int) profile.getFreq().get("a"), 1);
        profile.add("a");
        assertEquals((int) profile.getFreq().get("a"), 2);
        profile.omitLessFreq();
    }


    @Test(expectedExceptions = IllegalStateException.class)
    public final void testAddIllegally1() {
        LangProfile profile = new LangProfile(); // Illegal ( available for only JSONIC ) but ignore  
        profile.add("a");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public final void testAddIllegally2() {
        LangProfile profile = new LangProfile("en");
        profile.add("");  // Illegal (string's length of parameter must be between 1 and 3)
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public final void testAddIllegally3() {
        LangProfile profile = new LangProfile("en");
        profile.add("abcd");  // Illegal (string's length of parameter must be between 1 and 3)
    }

    /**
     * Test method for {@link LangProfile#omitLessFreq()}.
     */
    @Test
    public final void testOmitLessFreq() {
        LangProfile profile = new LangProfile("en");
        String[] grams = "a b c \u3042 \u3044 \u3046 \u3048 \u304a \u304b \u304c \u304d \u304e \u304f".split(" ");
        for (int i = 0; i < 5; ++i) {
            for (String g : grams) {
                profile.add(g);
            }
        }
        profile.add("\u3050");

        assertEquals((int) profile.getFreq().get("a"), 5);
        assertEquals((int) profile.getFreq().get("\u3042"), 5);
        assertEquals((int) profile.getFreq().get("\u3050"), 1);
        profile.omitLessFreq();
        assertNull(profile.getFreq().get("a")); // omitted
        assertEquals((int) profile.getFreq().get("\u3042"), 5);
        assertNull(profile.getFreq().get("\u3050")); // omitted
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public final void testOmitLessFreqIllegally() {
        LangProfile profile = new LangProfile();
        profile.omitLessFreq();
    }

}
