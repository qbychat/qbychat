package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Contact;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.repository.ContactRepository;
import org.qbynet.chat.service.ContactService;
import org.springframework.stereotype.Service;

@Service
public class ContactServiceImpl implements ContactService {
    @Resource
    ContactRepository contactRepository;

    @Override
    public Contact findContact(User owner, User partner) {
        return contactRepository.findByOwnerAndTarget(owner, partner).orElse(null);
    }

    @Override
    public boolean hasContact(User owner, User target) {
        return contactRepository.existsByOwnerAndTarget(owner, target);
    }
}
