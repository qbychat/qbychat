/*
 * Copyright 2011 Francois ROLAND
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

package com.optimaize.langdetect.frma;

import com.optimaize.langdetect.cybozu.util.LangProfile;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GenProfileTest extends GenProfile {

    @Test
    public void generateProfile() throws IOException {
        File inputFile = File.createTempFile("profileInput", ".txt");
        try {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(inputFile), StandardCharsets.UTF_8))) {
                writer.println("Salut tout le monde.");
                writer.println("Bonjour toi tout seul.");
                writer.println("Ca va ?");
                writer.println("Oui ça va. Et toi ?");
            }

            LangProfile trucProfile = generate("truc", inputFile);
            Map<String, Integer> freqs = trucProfile.getFreq();
            assertThat(freqs, is(notNullValue()));
            assertThat(freqs.get("t"), is(equalTo(8)));
            assertThat(freqs.get("to"), is(equalTo(4)));
            assertThat(freqs.get("out"), is(equalTo(2)));
            assertThat(freqs.get("o"), is(equalTo(7)));
            assertThat(freqs.get("ou"), is(equalTo(3)));
            assertThat(freqs.get("toi"), is(equalTo(2)));
            assertThat(freqs.get("u"), is(equalTo(6)));
            assertThat(freqs.get("ut"), is(equalTo(3)));
            assertThat(freqs.get("tou"), is(equalTo(2)));
            assertThat(freqs.get("a"), is(equalTo(5)));
            assertThat(freqs.get("oi"), is(equalTo(2)));
            assertThat(freqs.get("alu"), is(equalTo(1)));
            assertThat(freqs.get("on"), is(equalTo(2)));
            assertThat(freqs.get("Bon"), is(equalTo(1)));
            assertThat(freqs.get("e"), is(equalTo(3)));
            assertThat(freqs.get("va"), is(equalTo(2)));
            assertThat(freqs.get("i"), is(equalTo(3)));
            assertThat(freqs.get("jou"), is(equalTo(1)));
        } finally {
            //noinspection ResultOfMethodCallIgnored
            inputFile.delete();
        }
    }

}
