package main.model.repositories;

import main.model.GlobalSetting;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalSettingRepository extends CrudRepository<GlobalSetting, Integer> {
    GlobalSetting findByCode(String code);

    boolean existsByCodeAndValueIgnoreCase(String code, String value);
}
