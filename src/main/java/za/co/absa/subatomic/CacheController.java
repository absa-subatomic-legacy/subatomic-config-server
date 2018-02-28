package za.co.absa.subatomic;

import java.util.List;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Cache;

import org.springframework.cache.CacheManager;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/caches")
public class CacheController {

    private CacheManager cacheManager;

    public CacheController(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    List<Environment> caches() {
        Cache cache = (Cache) cacheManager.getCache("configServer").getNativeCache();
        return (List<Environment>) cache.asMap().keySet().stream()
                .map(name -> cache.asMap().get(name))
                .collect(Collectors.toList());
    }
}
