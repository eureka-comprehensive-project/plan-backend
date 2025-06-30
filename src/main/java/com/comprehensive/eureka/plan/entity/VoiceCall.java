package com.comprehensive.eureka.plan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "voiceCallId")
@Table(name = "voice_call")
public class VoiceCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voice_call_id")
    private Long voiceCallId;

    private Integer voiceAllowance;

    private Integer additionalCallAllowance;

    @OneToMany(mappedBy = "voiceCall")
    private List<Plan> plans = new ArrayList<>();
}
