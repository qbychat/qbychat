package org.qbynet.chat.service;

import org.qbynet.chat.entity.Contact;
import org.qbynet.chat.entity.User;

public interface ContactService {
    Contact findContact(User owner, User partner);

    boolean hasContact(User owner, User target);
}
