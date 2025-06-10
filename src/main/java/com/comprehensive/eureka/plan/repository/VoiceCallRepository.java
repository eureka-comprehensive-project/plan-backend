package com.comprehensive.eureka.plan.repository;

import com.comprehensive.eureka.plan.entity.VoiceCall;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoiceCallRepository extends JpaRepository<VoiceCall, Integer> {
    Optional<VoiceCall> findByVoiceAllowanceAndAdditionalCallAllowance(
            Integer voiceAllowance, Integer additionalCallAllowance);
}
