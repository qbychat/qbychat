package org.qbynet.chat.entity.dto;

import lombok.Data;
import org.qbynet.chat.entity.MemberActivity;

@Data
public class UpdateMemberActivityDTO {
    private String conversation; // conversation id
    private MemberActivity.MemberActivityEnum activity;
}
