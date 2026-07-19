package io.serendia.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostPlatformTargetId implements Serializable {
    private UUID postId;
    private SocialPlatform platform;
}
