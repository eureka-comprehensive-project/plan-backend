package com.comprehensive.eureka.plan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "voice_call")
public class VoiceCall {

    @Id
    @Column(name = "음성통화 ID")
    private Integer voiceCallId;

    @Column(name = "음성 제공량")
    private Integer voiceAllowance;

    @Column(name = "부가통화 제공량")
    private Integer additionalCallAllowance;

    @OneToMany(mappedBy = "voiceCall")
    private List<Plan> plans = new ArrayList<>();
}
