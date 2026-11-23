package com.zynolo_nexus.setting_service.repository;

import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.model.UserCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCompanyRepository extends JpaRepository<UserCompany, Long> {

    List<UserCompany> findByUser(User user);

    Optional<UserCompany> findFirstByUserAndIsDefaultTrue(User user);

    Optional<UserCompany> findByUserAndCompany_Id(User user, Long companyId);
}
