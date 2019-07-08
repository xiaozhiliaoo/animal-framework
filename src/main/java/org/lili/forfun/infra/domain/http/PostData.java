package org.lili.forfun.infra.domain.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostData {
    private String key;
    private String value;
}
