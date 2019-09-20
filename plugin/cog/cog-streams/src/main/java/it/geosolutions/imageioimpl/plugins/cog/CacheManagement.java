/*
 * GeoTools - The Open Source Java GIS Toolkit
 * http://geotools.org
 *
 * (C) 2017, Open Source Geospatial Foundation (OSGeo)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package it.geosolutions.imageioimpl.plugins.cog;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.ehcache.xml.XmlConfiguration;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/** Very basic EhCache handling */
public enum CacheManagement implements CogTileCacheProvider {

    DEFAULT;

    public static final String TILE_CACHE = "tile_cache";
    public static final String HEADER_CACHE = "header_cache";
    public static final String FILESIZE_CACHE = "filesize_cache";
    private CacheManager manager;
    private CacheConfig config;
    private static Logger LOGGER;

    CacheManagement() {
        config = CacheConfig.getDefaultConfig();
        manager = buildCache(false);
    }

    /**
     * Builds caches for tiles, headers, and filesizes.
     *
     * @param removeCacheIfExists
     * @return
     */
    private CacheManager buildCache(boolean removeCacheIfExists) {
        // if an xml config file has been declared, use it to construct the manager
        if (config.getXmlConfigPath() != null) {
            logger().fine("XML configuration declared for ehcache at " + config.getXmlConfigPath());
            final URL configUrl = this.getClass().getResource(config.getXmlConfigPath());
            Configuration xmlConfig = new XmlConfiguration(configUrl);
            manager = CacheManagerBuilder.newCacheManager(xmlConfig);
            manager.init();
            return manager;
        }

        CacheManagerBuilder managerBuilder = CacheManagerBuilder.newCacheManagerBuilder();
        if (config.isUseDiskCache()) {
            managerBuilder.with(CacheManagerBuilder.persistence(config.getCacheDirectory()));
        }
        manager = managerBuilder.build(true);

        if (removeCacheIfExists) {
            manager.removeCache(TILE_CACHE);
            manager.removeCache(HEADER_CACHE);
            manager.removeCache(FILESIZE_CACHE);
        }

        ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.heap(config.getHeapEntries());
        if (config.isUseDiskCache()) {
            resourcePoolsBuilder.disk(config.getDiskCacheSize(), MemoryUnit.B, true);
        }
        if (config.isUseOffHeapCache()) {
            resourcePoolsBuilder.offheap(config.getOffHeapSize(), MemoryUnit.B);
        }

        manager.createCache(TILE_CACHE,
                buildCacheConfiguration(TileCacheEntryKey.class, byte[].class, resourcePoolsBuilder));
        manager.createCache(HEADER_CACHE,
                buildCacheConfiguration(String.class, byte[].class, resourcePoolsBuilder));
        manager.createCache(FILESIZE_CACHE,
                buildCacheConfiguration(String.class, Integer.class, resourcePoolsBuilder));

        return manager;
    }

    public <K extends Class, V extends Class> CacheConfiguration<K, V> buildCacheConfiguration(K keyType, V valueType,
                                                      ResourcePoolsBuilder resourcePoolsBuilder) {
        CacheConfigurationBuilder cacheConfigurationBuilder = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(keyType, valueType, resourcePoolsBuilder);

        if (config.getTimeToLive() > 0) {
            cacheConfigurationBuilder.withExpiry(
                    Expirations.timeToLiveExpiration(Duration.of(config.getTimeToLive(), TimeUnit.MILLISECONDS)));
        }
        if (config.getTimeToIdle() > 0) {
            cacheConfigurationBuilder.withExpiry(
                    Expirations.timeToIdleExpiration(Duration.of(config.getTimeToIdle(), TimeUnit.MILLISECONDS)));
        }

        return cacheConfigurationBuilder.build();
    }


    /** Get the logger from this method because when needed the class hasn't been loaded yet */
    private static Logger logger() {
        if (LOGGER == null) {
            LOGGER = Logger.getLogger(CacheManagement.class.getCanonicalName());
        }
        return LOGGER;
    }

    private Cache<TileCacheEntryKey, byte[]> getTileCache() {
        return manager.getCache(TILE_CACHE, TileCacheEntryKey.class, byte[].class);
    }

    private Cache<String, byte[]> getHeaderCache() {
        return manager.getCache(HEADER_CACHE, String.class, byte[].class);
    }

    private Cache<String, Integer> getFilesizeCache() {
        return manager.getCache(FILESIZE_CACHE, String.class, Integer.class);
    }

    @Override
    public byte[] getTile(TileCacheEntryKey key) {
        return getTileCache().get(key);
    }

    @Override
    public void cacheTile(TileCacheEntryKey key, byte[] tileBytes) {
        getTileCache().put(key, tileBytes);
    }

    @Override
    public boolean keyExists(TileCacheEntryKey key) {
        return getTileCache().containsKey(key);
    }

    @Override
    public void cacheHeader(String key, byte[] headerBytes) {
        getHeaderCache().put(key, headerBytes);
    }

    @Override
    public byte[] getHeader(String key) {
        return getHeaderCache().get(key);
    }

    @Override
    public boolean headerExists(String key) {
        return getHeaderCache().containsKey(key);
    }

    @Override
    public void cacheFilesize(String key, int size) {
        getFilesizeCache().put(key, size);
    }

    @Override
    public int getFilesize(String key) {
        return getFilesizeCache().get(key);
    }

    @Override
    public boolean filesizeExists(String key) {
        return getFilesizeCache().containsKey(key);
    }

    public CacheConfig getCacheConfig() {
        return this.config;
    }
}
