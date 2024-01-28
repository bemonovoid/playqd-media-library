package io.playqd.config.cache;

import io.playqd.service.metadata.Artwork;
import io.playqd.service.metadata.MetadataFile;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
class CacheContextConfiguration {

  @Bean
  JCacheManagerCustomizer jCacheManagerCustomizer() {
    return cacheManager -> {
      cacheManager.createCache(CacheNames.METADATA_FILES, metadataFileCacheConfiguration());
      cacheManager.createCache(CacheNames.ALBUM_ART_BY_ALBUM_ID, albumArtCacheConfiguration());
    };
  }

  private javax.cache.configuration.Configuration<String, MetadataFile> metadataFileCacheConfiguration() {
    var albumArtCacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            MetadataFile.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder()
                .offheap(1, MemoryUnit.MB)
                .build())
        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(1)))
        .build();
    return Eh107Configuration.fromEhcacheCacheConfiguration(albumArtCacheConfiguration);
  }

  private javax.cache.configuration.Configuration<String, Artwork> albumArtCacheConfiguration() {
    var albumArtCacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            Artwork.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder()
                .offheap(100, MemoryUnit.MB)
                .build())
        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(7)))
        .build();
    return Eh107Configuration.fromEhcacheCacheConfiguration(albumArtCacheConfiguration);
  }

}
