package org.qbynet.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberActivity {
    private String member; // the user
    private MemberActivityEnum activity;

    public enum MemberActivityEnum {
        TYPING,
    }
}
