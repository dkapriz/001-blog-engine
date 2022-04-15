package main.service;

import main.api.request.SettingsRequest;
import main.api.response.SettingsResponse;
import main.model.GlobalSetting;
import main.model.repositories.GlobalSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    @Autowired
    private final GlobalSettingRepository globalSettingRepository;

    public final static String MULTI_USER_MODE_FIELD_NAME = "MULTIUSER_MODE";
    public final static String POST_PRE_MODERATION_FIELD_NAME = "POST_PRE_MODERATION";
    public final static String STATISTIC_IS_PUBLIC_FIELD_NAME = "STATISTICS_IS_PUBLIC";
    private final static String TRUE_VALUE = "YES";
    private final static String FALSE_VALUE = "NO";

    public SettingsService(GlobalSettingRepository globalSettingRepository) {
        this.globalSettingRepository = globalSettingRepository;
    }

    public SettingsResponse getGlobalSettings() {
        SettingsResponse settingsResponse = new SettingsResponse();
        Iterable<GlobalSetting> globalSettings = globalSettingRepository.findAll();
        globalSettings.forEach(globalSetting -> {
            switch (globalSetting.getCode()) {
                case (MULTI_USER_MODE_FIELD_NAME):
                    settingsResponse.setMultiUserMode(stringToBoolean(globalSetting.getValue()));
                    break;
                case (POST_PRE_MODERATION_FIELD_NAME):
                    settingsResponse.setPostPreModeration(stringToBoolean(globalSetting.getValue()));
                    break;
                case (STATISTIC_IS_PUBLIC_FIELD_NAME):
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
        saveSetting(MULTI_USER_MODE_FIELD_NAME, settings.isMultiUserMode());
        saveSetting(POST_PRE_MODERATION_FIELD_NAME, settings.isPostPreModeration());
        saveSetting(STATISTIC_IS_PUBLIC_FIELD_NAME, settings.isStatisticsIsPublic());
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
        return value.equals(TRUE_VALUE);
    }

    private String booleanToString(Boolean value) {
        return value ? TRUE_VALUE : FALSE_VALUE;
    }
}
