package com.eventhive.backend.service;

import com.eventhive.backend.entity.SiteSettings;
import com.eventhive.backend.repository.SiteSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingsService {

    @Autowired
    private SiteSettingsRepository repository;

    public SiteSettings getSettings() {
        return repository.findAll().stream().findFirst().orElseGet(() -> {
            SiteSettings defaultSettings = new SiteSettings();
            defaultSettings.setContactPhone("076 280 7271");
            defaultSettings.setContactEmail("hello@eventhive.lk");
            defaultSettings.setContactAddress("123 Event Avenue\nColombo 03\nSri Lanka");
            defaultSettings.setPrivacyPolicyContent("Default Privacy Policy Content...\nPlease update in Admin Settings.");
            defaultSettings.setTermsOfServiceContent("Default Terms of Service Content...\nPlease update in Admin Settings.");
            return repository.save(defaultSettings);
        });
    }

    public SiteSettings updateSettings(SiteSettings updated) {
        SiteSettings current = getSettings();
        current.setContactPhone(updated.getContactPhone());
        current.setContactEmail(updated.getContactEmail());
        current.setContactAddress(updated.getContactAddress());
        current.setPrivacyPolicyContent(updated.getPrivacyPolicyContent());
        current.setTermsOfServiceContent(updated.getTermsOfServiceContent());
        return repository.save(current);
    }
}
