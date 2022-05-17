package main.service;

import lombok.AllArgsConstructor;
import main.api.request.SettingsRequest;
import main.api.response.SettingsResponse;
import main.configuratoin.BlogConfig;
import main.model.GlobalSetting;
import main.model.repositories.GlobalSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SettingsService {

    @Autowired
    private final GlobalSettingRepository globalSettingRepository;

    public SettingsResponse getGlobalSettings() {
        SettingsResponse settingsResponse = new SettingsResponse();
        Iterable<GlobalSetting> globalSettings = globalSettingRepository.findAll();
        globalSettings.forEach(globalSetting -> {
            switch (globalSetting.getCode()) {
                case (BlogConfig.MULTI_USER_MODE_FIELD_NAME):
                    settingsResponse.setMultiUserMode(stringToBoolean(globalSetting.getValue()));
                    break;
                case (BlogConfig.POST_PRE_MODERATION_FIELD_NAME):
                    settingsResponse.setPostPreModeration(stringToBoolean(globalSetting.getValue()));
                    break;
                case (BlogConfig.STATISTIC_IS_PUBLIC_FIELD_NAME):
                    settingsResponse.setStatisticsIsPublic(stringToBoolean(globalSetting.getValue()));
                    break;
            }
        });
        return settingsResponse;
    }

    public boolean getGlobalSettingByCode(String code) {
        Iterable<GlobalSetting> globalSettings = globalSettingRepository.findAll();
        for (GlobalSetting globalSetting : globalSettings) {
            if (globalSetting.getCode().equals(code)) {
                return stringToBoolean(globalSetting.getValue());
            }
        }
        return false;
    }

    public void setGlobalSettings(SettingsRequest settings) {
        saveSetting(BlogConfig.MULTI_USER_MODE_FIELD_NAME, settings.isMultiUserMode());
        saveSetting(BlogConfig.POST_PRE_MODERATION_FIELD_NAME, settings.isPostPreModeration());
        saveSetting(BlogConfig.STATISTIC_IS_PUBLIC_FIELD_NAME, settings.isStatisticsIsPublic());
    }

    private void saveSetting(String code, Boolean value) {
        GlobalSetting setting = globalSettingRepository.findByCode(code);
        if (setting == null) {
            setting = new GlobalSetting();
            setting.setCode(code);
            setting.setName("");
        }
        setting.setValue(booleanToString(value));
        globalSettingRepository.save(setting);
    }

    private boolean stringToBoolean(String value) {
        return value.equals(BlogConfig.TRUE_VALUE);
    }

    private String booleanToString(Boolean value) {
        return value ? BlogConfig.TRUE_VALUE : BlogConfig.FALSE_VALUE;
    }
}
