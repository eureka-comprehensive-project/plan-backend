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
    @Column(name = "voice_call_id")
    private Integer voiceCallId;

    private Integer voiceAllowance;

    private Integer additionalCallAllowance;

    @OneToMany(mappedBy = "voiceCall")
    private List<Plan> plans = new ArrayList<>();
}
