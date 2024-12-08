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
public class TagExtractorTest {

    /**
     * Test method for {@link TagExtractor#TagExtractor(java.lang.String, int)}.
     */
    @Test
    public final void testTagExtractor() {
        TagExtractor extractor = new TagExtractor(null, 0);
        assertNull(extractor.target_);
        assertEquals(0, extractor.threshold_);

        TagExtractor extractor2 = new TagExtractor("abstract", 10);
        assertEquals("abstract", extractor2.target_);
        assertEquals(10, extractor2.threshold_);
    }

    /**
     * Test method for {@link TagExtractor#setTag(java.lang.String)}.
     */
    @Test
    public final void testSetTag() {
        TagExtractor extractor = new TagExtractor(null, 0);
        extractor.setTag("");
        assertEquals("", extractor.tag_);
        extractor.setTag(null);
        assertNull(extractor.tag_);
    }

    /**
     * Test method for {@link TagExtractor#add(java.lang.String)}.
     */
    @Test
    public final void testAdd() {
        TagExtractor extractor = new TagExtractor(null, 0);
        extractor.add("");
        extractor.add(null);    // ignore
    }

    /**
     * Test method for {@link TagExtractor#closeTag(LangProfile)}.
     */
    @Test
    public final void testCloseTag() {
        TagExtractor extractor = new TagExtractor(null, 0);
        LangProfile profile = null;
        extractor.closeTag(profile);    // ignore
    }


    /**
     * Scenario Test of extracting &lt;abstract&gt; tag from Wikipedia database.
     */
    @Test
    public final void testNormalScenario() {
        TagExtractor extractor = new TagExtractor("abstract", 10);
        assertEquals(0, extractor.count());

        LangProfile profile = new LangProfile("en");

        // normal
        extractor.setTag("abstract");
        extractor.add("This is a sample text.");
        extractor.closeTag(profile);
        assertEquals(1, extractor.count());
        assertEquals(17, profile.getNWords()[0]);  // Thisisasampletext
        assertEquals(22, profile.getNWords()[1]);  // _T, Th, hi, ...
        assertEquals(17, profile.getNWords()[2]);  // _Th, Thi, his, ...

        // too short
        extractor.setTag("abstract");
        extractor.add("sample");
        extractor.closeTag(profile);
        assertEquals(1, extractor.count());

        // other tags
        extractor.setTag("div");
        extractor.add("This is a sample text which is enough long.");
        extractor.closeTag(profile);
        assertEquals(1, extractor.count());
    }

    /**
     * Test method for {@link TagExtractor#clear()}.
     */
    @Test
    public final void testClear() {
        TagExtractor extractor = new TagExtractor("abstract", 10);
        extractor.setTag("abstract");
        extractor.add("This is a sample text.");
        assertEquals("This is a sample text.", extractor.buf_.toString().trim());
        assertEquals("abstract", extractor.tag_);
        extractor.clear();
        assertEquals("", extractor.buf_.toString().trim());
        assertNull(extractor.tag_);
    }


}
