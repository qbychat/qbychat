package org.qbynet.chat.entity;


import lombok.Data;

@Data
public class Privacy {
    private PrivacyPreferment status = PrivacyPreferment.EVERYONE;
    private PrivacyPreferment onlineStatus = PrivacyPreferment.EVERYONE; // who can know the user's online status
    private PrivacyPreferment calls = PrivacyPreferment.CONTACTS; // who can call this user
}
