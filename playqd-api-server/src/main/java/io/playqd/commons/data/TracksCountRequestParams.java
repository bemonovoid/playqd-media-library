package io.playqd.commons.data;

import lombok.Builder;

@Builder
public record TracksCountRequestParams(Boolean played, Boolean liked, Boolean lastRecentlyAdded) {
}
