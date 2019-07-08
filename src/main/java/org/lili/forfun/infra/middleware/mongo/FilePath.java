package org.lili.forfun.infra.middleware.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "localPath")
public class FilePath implements Comparable<FilePath> {
    private String localPath;
    private String ossPath;

    @Override
    public int compareTo(FilePath other) {
        if (other == null) {
            return -1;
        } else {
            return this.ossPath.compareTo(other.ossPath);
        }
    }
}
